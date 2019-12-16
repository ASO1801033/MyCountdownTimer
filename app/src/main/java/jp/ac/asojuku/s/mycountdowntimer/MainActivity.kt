package jp.ac.asojuku.s.mycountdowntimer

import android.content.IntentSender
import android.icu.util.DateInterval
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var soundPool: SoundPool
    private var soundResId = 0

    //内部クラスとして、カウントダウンタイマー継承クラスを定義する
    //inner class キーワードで内部クラスを宣言
    inner class MyCountDownTimer(
        millisInFuture: Long, //親クラスのコンストラクタに引き渡す(残り時間ミリ秒)
        countDownInterval: Long //親クラスのコンストラクタに引き渡す(チック刻み間隔)
        ) : CountDownTimer(millisInFuture, countDownInterval) //親クラスの指定
    {
        //タイマーが動いているかのフラグ
        var isRunning = false

        //カヌントダウン完了後のイベントで呼ばれるコールバックメソッド
        override fun onFinish() {
            //タイマー表示をゼロにする
            timerText.text = "0:00"

            //音源を鳴らす
            soundPool.play(
                soundResId, //サウンドリソースID(音を指定)
                1.0f, //左ボリューム(0.0f〜1.0f)
                100f, //右ボリューム(0.0f〜1.0f)
                0, //優先度
                0, //ループする(1)、ループしない(0)
                1.0f //再生スピード(1.0=正常、0.5〜2.0までの範囲)
            )
        }

        //チックイベントごとに呼ばれるコールバックメソッド
        override fun onTick(millisUntilFinished: Long) {
            //残り時間表示を書き換える(残り時間：millisUntilFinished=ミリ秒)
            //millisUntilFinishedの分の部分を計算
            val minute = millisUntilFinished / 1000L / 60L //1000で割って60で割ると分単位

            //millisUntilFinishedの秒の部分を計算
            val second = millisUntilFinished / 1000L % 60L //1000で割って60で割ったあまりが秒単位

            timerText.text = "%1d:%2$02d".format(minute, second) //残り時間表示を書き換える
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //タイマーの時間表示を初期設定する　
        //timerText.text = "3:00"
        timerText.text = "0:30"

        //カウントダウンタイマーの継承クラスインスタンスを生成する(3分カウントダウン、100ミリ秒ごとにチック)
        //val timer = MyCountDownTimer(3 * 60 * 1000, 100)
        val timer = MyCountDownTimer(30 * 1000, 100)


        //FAVがクリックされた時のコールバックメソッド
        playStop.setOnClickListener {
            timer.isRunning = when(timer.isRunning) {
                true -> {
                    //タイマーをストップ
                    timer.cancel() //タイマーインスタンスを止める
                    playStop.setImageResource(
                        //FAVボタンの画像を「▶︎」にする
                        R.drawable.ic_play_arrow_black_24dp
                    )
                    false
                }
                false -> {
                    //タイマーをスタート
                    timer.start() //タイマーインスタンスを動かす
                    playStop.setImageResource(
                        //FAVボタンの画像を「■」にする
                        R.drawable.ic_stop_black_24dp
                    )
                    true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        //画面が表示されている間だけインスタンスをメモリに保持したい
        //SoundPoolクラスのインスタンスを生成して変数に代入
        soundPool =
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //OSのLOLLIPOPより古い端末のとき
                SoundPool(
                    2, //
                     AudioManager.STREAM_ALARM, //オーディオの種類
                    0) //音源品質、現在未使用。初期値の0を指定
            }else {
                //新しい端末のとき
                val audioAttributes = AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()

                //オーディオ設定を使ってSoundPoolのインスタンスを作る
                SoundPool.Builder()
                    .setMaxStreams(1) //同時音源数(1)
                    .setAudioAttributes(audioAttributes)
                    .build() //ビルド
            }

        //鳴らすサウンドファイルのリソースIDを設定
        soundResId = soundPool.load(
            this, //アクティビティのインスタンス(自分自身)
             R.raw.bellsound, //鳴らす音源のリソースID
            1) //音の優先順位、現在は使用されていない。互換性のために設定
    }

    //画面が隠れるときに呼ばれるイベントコールバックメソッド
    override fun onPause() {
        super.onPause()
        soundPool.release() //サウンドプールインスタンスをメモリから解放
    }
}
