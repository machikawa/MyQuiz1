package machikawa.hidemasa.techacademy.myquiz1

import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_finish_quiz.*

class FinishQuizActivity : AppCompatActivity() {
    private val mHandler = Handler()

    private lateinit var soundPool: SoundPool
    private var soundDrum = 0
    private var numCorrectAnswers:Int = 0
    private var numIncorrectAnswers:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_quiz)
        title = "クイズ結果発表！"
        // Intent からの正解、不正回数の確認
        val extras = intent.extras
        numCorrectAnswers = extras.getInt("numCorrect")
        numIncorrectAnswers = extras.getInt("numIncorrect")

        //////// 結果のフィードバック
        val result: Double = (numCorrectAnswers / (numCorrectAnswers + numIncorrectAnswers)).toDouble()

        /// 正答率の表示
        STRPersentage.text = (result * 100).toInt().toString() + "%"

        // 合否判定
        if (result < 0.8) {
            Genjitsu.text = "不合格です..."
            Genjitsu.textSize = 40.toFloat()
            Genjitsu.setTextColor(Color.parseColor("#4e32a8"))
        } else if (result >= 0.8) {
            Genjitsu.text = "合格！！"
            Genjitsu.textSize = 50.toFloat()
            Genjitsu.setTextColor(Color.parseColor("#ff6347"))
        }
        // 遅れて結果表示
        mHandler.postDelayed(Runnable {
            Genjitsu.isVisible = true
        }, 5000)

        // 終わるボタンのクリックリスナー
        btnFinish.setOnClickListener{view ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        // リストビューでテストを復習、行をクリックしたら復習 or お気に入り登録
        //////////////////////
        // BGM再生
        playBGM()
    }

    // BGM再生するぞ
    private fun playBGM(){
        // BGM 周り
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(2)
            .build()
        // mp3 import と再生
        soundDrum = soundPool.load(this, R.raw.drum, 1)
        soundPool.setOnLoadCompleteListener{ soundPool, sampleId, status ->
            Log.d("machid","status=$status")
            /// まだうまくいっておらず。。
        }
        soundPool.play(soundDrum, 1.0f, 1.0f, 0, 0, 1.0f)
    }
}
