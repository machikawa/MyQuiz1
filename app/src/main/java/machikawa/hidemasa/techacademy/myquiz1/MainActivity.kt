package machikawa.hidemasa.techacademy.myquiz1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // クイズジャンルを洗濯して遷移させる。これは共通処理に近しいのでOnclickでやらせる
        buttonGenreSports.setOnClickListener(this)
        buttonGenre2019Neta.setOnClickListener(this)
        buttonLogin.setOnClickListener(this)
    }

    // クイズスタート：ジャンルの番号をインテントに乗せて次へ
    override fun onClick(v: View) {
        // インテント
        when (v.id) {
            // スポート押下時
            R.id.buttonGenreSports -> {
                intent = Intent(applicationContext, PlayQuiz::class.java)
                intent.putExtra("genre", GENRE_SPORTS)
            }
            //　2019ネタ
            R.id.buttonGenre2019Neta -> {
                intent = Intent(applicationContext, PlayQuiz::class.java)
                intent.putExtra("genre", GENRE_2019NETA)
            }
            // MySettings押下時
            R.id.buttonLogin -> intent = Intent(applicationContext, LoginActivity::class.java)
        }
        startActivity(intent)
    }
}
