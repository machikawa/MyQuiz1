package machikawa.hidemasa.techacademy.myquiz1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            buttonLogin.text = "ログアウト"
        }

        // クイズジャンルを洗濯して遷移させる。これは共通処理に近しいのでOnclickでやらせる
        buttonGenreSports.setOnClickListener(this)
        buttonGenre2019Neta.setOnClickListener(this)

        // ログイン時にはログアウトボタンに早変わり。
        buttonLogin.setOnClickListener{v ->
            if (user != null) {
                FirebaseAuth.getInstance().signOut()
                Snackbar.make(v, "ログアウトしますた", Snackbar.LENGTH_LONG).show()
                finish()
                startActivity(getIntent())
            } else {
                intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
        }
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

        }
        startActivity(intent)
    }

    // ログイン画面から戻ってきたときにログアウトボタンに切り替える。
    override fun onResume() {
        super.onResume()

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            buttonLogin.text = "ログアウト"
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val dispName = sp.getString(SP_STR_DISPLAY_NAME, "")
            STRDisplayName.text = "こんにちは " + dispName + "さん"
        }

    }
}
