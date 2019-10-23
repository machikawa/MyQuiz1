package machikawa.hidemasa.techacademy.myquiz1

import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_finish_quiz.*

class FinishQuizActivity : AppCompatActivity() {
    private val mHandler = Handler()

    private lateinit var soundPool: SoundPool
    private var soundDrum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish_quiz)

        //////// 結果のフィードバック
        val result: Double = 0.8

        if (result < 0.8) {
            Uzaiko.text = "不合格"
        } else if (result >= 0.8) {
            Uzaiko.text = "合格！！"
            Uzaiko.textSize = 50.toFloat()
            Uzaiko.setTextColor(Color.parseColor("#ff6347"))
        }

        // 1秒遅れて結果表示
        mHandler.postDelayed(Runnable {
            Uzaiko.isVisible = true
        }, 5000)

        // 終わるボタンのクリックリスナー
        btnFinish.setOnClickListener{view ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // リストビューでテストを復習、行をクリックしたら復習 or お気に入り登録

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
        soundPool.play(soundDrum, 1.0f, 1.0f, 0, 0, 1.0f)
    }
}
