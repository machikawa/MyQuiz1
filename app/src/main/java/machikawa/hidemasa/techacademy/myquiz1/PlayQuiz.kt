package machikawa.hidemasa.techacademy.myquiz1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_play_quiz.*
import kotlinx.android.synthetic.main.activity_play_quiz.view.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlayQuiz : AppCompatActivity(), View.OnClickListener {
    // クイズ全問が入っている
    private lateinit var mQuizArray: ArrayList<Quiz>
//    private lateinit var mFBQuizArray: ArrayList<Quiz>
    private lateinit var choiseArray: ArrayList<String>
    private lateinit var resultArray: ArrayList<Result>

    // ただいま何問目かを示す
    var currentQuizIndex: Int = 0
    // ユーザーが洗濯したAnswer
    private var userSelectedAnswerIndex: Int = GENRE_DEFAULTVALUE
    // 間違えた問題と、正解した問題の数
    var numCorrectAnswers: Int = 0
    var numIncorrectAnswers: Int = 0
    // ジャンルの初期値
    var mGenre = GENRE_DEFAULTVALUE
    // ShowQuizの読み込みを一回だけにするためのもの
    var isAlreadyCalledShowQuiz: Boolean = false
    // Quiz読み出し時のDBRef
    private lateinit var mQuizRef: DatabaseReference

    private val mQuizListener = object : ChildEventListener {
        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val quizMap = p0.value as Map<String, String>
            // 各項目の設定
            val quizText: String = quizMap["quizText"] ?: ""
            choiseArray = quizMap["choises"] as ArrayList<String>
            choiseArray.removeAt(0) // ゼロ番目になぜか null が入るのでこちらでカバー
            val correctAnswer: String = quizMap["correctAnswer"] ?: ""
            val descriptions: String = quizMap["descriptions"] ?: ""
            val quizId: String = p0.key ?: ""
            val genre: Int = genre
            val sportsQuiz = Quiz(quizText, choiseArray, correctAnswer, descriptions, quizId, genre)
            mQuizArray.add(sportsQuiz)
            // クイズの表示は初回だけ
            if (isAlreadyCalledShowQuiz) {
            } else {
                showQuiz()
            }
        }

        override fun onCancelled(p0: DatabaseError) {
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        }

        override fun onChildRemoved(p0: DataSnapshot) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_quiz)
        // アレイの初期化
        mQuizArray = ArrayList<Quiz>()
        resultArray=ArrayList<Result>()
        // ジャンルをSPから取得
        val extras = intent.extras
        mGenre = extras.getInt("genre")
        // intentに含まれるGenreからクイズを取得
        getQuizFromFB(mGenre)
        /////// 当該ジャンルのクイズ読み込み処理 - From テストデータ
        //   testGetQuiz()

        // 全てのボタンにリスナー登録を
        btnChoiseA.setOnClickListener(this)
        btnChoiseB.setOnClickListener(this)
        btnChoiseC.setOnClickListener(this)
        btnChoiseD.setOnClickListener(this)

        // 解説ボタンのアクション
        btnDetail.setOnClickListener {view ->
            val builder = AlertDialog.Builder(this@PlayQuiz)
            builder.setTitle(STR_DETAIL_EXPLAINATION)
            if (mQuizArray[currentQuizIndex].descriptions.length == 0){
                builder.setMessage("この問題の解説はありませんorz")
            }
            builder.setMessage(mQuizArray[currentQuizIndex].descriptions)

            builder.setNeutralButton("お気に入りに追加"){_, _ ->
                /// Firebase へのお気に入り追加処理
                /////////////////////////////////
                // Snackbarでの表示
                Snackbar.make(view, "お気に入りに追加しました", Snackbar.LENGTH_LONG).show()
            }
            // ダイアログを閉じるボタン
            builder.setNegativeButton("とじる", null)

            val dialog = builder.create()
            dialog.show()
        }

        // 次へボタンのアクション
        btnNextQuiz.setOnClickListener {view ->
            // 最終問題の時の処理
            if (currentQuizIndex >= mQuizArray.size -1) {
                // FB へ結果のアレイリストを格納
                saveResultToFB()
                goToFinishScreen()
                // 次Quizにいく時の処理
            } else {
                currentQuizIndex += 1
                resetBtnState()
                showQuiz()
            }
        }
    }

    // 成否判断して次へ
    override fun onClick(v: View) {
        // どのボタンを押したかリッスン
        when (v.id) {
            R.id.btnChoiseA -> userSelectedAnswerIndex = 0
            R.id.btnChoiseB -> userSelectedAnswerIndex = 1
            R.id.btnChoiseC -> userSelectedAnswerIndex = 2
            R.id.btnChoiseD -> userSelectedAnswerIndex = 3
        }

        // 成否判断 + 文字列セット
        if (mQuizArray[currentQuizIndex].quizChoises[userSelectedAnswerIndex]
            == mQuizArray[currentQuizIndex].correctAnswer) {
            resultString.text = "正解🙌"
            // 結果アレイに格納
            resultArray.add(Result(mQuizArray[currentQuizIndex].quizId,currentQuizIndex,true))
            numCorrectAnswers += 1
        } else {
            resultString.text = "不正解😡"
            resultArray.add(Result(mQuizArray[currentQuizIndex].quizId, currentQuizIndex,false))
            numIncorrectAnswers += 1
        }
        // 結果表示
        resultString.isInvisible = false
        // 既存選択肢ボタンをNOT活性化して押せないように
        disableAllChoiseBtn()
        // 解説ボタンと、次へボタンの表示を行う
        showAfterCareButton()
    }

    // UI 常にクイズを出す処理、問題切り替え時には毎回実行させる。
    private fun showQuiz(){
        // クイズ本文の表示
        quizBodyText.text = mQuizArray[currentQuizIndex].quizBody
        // 現在何問目にいるかを示す
        currentQuiz.text = "第" + (currentQuizIndex + 1).toString() + "問"
        // 選択肢をボタンに描写
        btnChoiseA.text = mQuizArray[currentQuizIndex].quizChoises[0]
        btnChoiseB.text = mQuizArray[currentQuizIndex].quizChoises[1]
        // 一旦全ての選択肢をVisibleに
        changeVisibleState()
        // 選択肢数に応じてボタンをインビジブル
        if (mQuizArray[currentQuizIndex].quizChoises.size == 2) {
            btnChoiseC.isInvisible = true
            btnChoiseD.isInvisible = true
        } else if (mQuizArray[currentQuizIndex].quizChoises.size == 3) {
            btnChoiseC.text = mQuizArray[currentQuizIndex].quizChoises[2]
            btnChoiseD.isInvisible = true
        } else if (mQuizArray[currentQuizIndex].quizChoises.size == 4) {
            btnChoiseC.text = mQuizArray[currentQuizIndex].quizChoises[2]
            btnChoiseD.text = mQuizArray[currentQuizIndex].quizChoises[3]
        }
    }

    // 選択肢BTNの無効化
    private fun disableAllChoiseBtn(){
        btnChoiseA.isEnabled = false; btnChoiseB.isEnabled = false; btnChoiseC.isEnabled = false; btnChoiseD.isEnabled = false
    }
    // 選択肢BTNの有効化
    private fun enableAllChoiseBtn(){
        btnChoiseA.isEnabled = true; btnChoiseB.isEnabled = true; btnChoiseC.isEnabled = true; btnChoiseD.isEnabled = true
    }
    // 選択肢BTNと問題文のVISIBLE化
    private fun changeVisibleState(){
        btnChoiseA.isVisible = true; btnChoiseB.isVisible = true ; btnChoiseC.isVisible = true ; btnChoiseD.isVisible = true ; quizBodyText.isVisible = true
    }

    // 次へボタンの可視化AND有効化
    private fun showAfterCareButton(){
        // 次へボタンは最後の問題なら終えるようにしなきゃいけない
        if (currentQuizIndex >= mQuizArray.size -1) {
            btnNextQuiz.text = "クイズを終える"
        }
        btnDetail.isEnabled = true; btnNextQuiz.isEnabled = true
        btnDetail.isVisible = true; btnNextQuiz.isVisible = true
    }

    // 次へボタンの可視化AND有効化
    private fun disappearAfterCareButton(){
        btnDetail.isEnabled = false; btnNextQuiz.isEnabled = false
        btnDetail.isVisible = false; btnNextQuiz.isVisible = false
    }

    // 次へボタン押下時のボタン状態切り戻し
    private fun resetBtnState(){
        resultString.text = ""
        resultString.isVisible = false
        disappearAfterCareButton()
        enableAllChoiseBtn()
    }

    override fun onResume() {
        super.onResume()
        Log.d("machid", "ONRESUMEだよ〜")
    }

    // FBから情報とります
    private fun getQuizFromFB(genre:Int){
        /*
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d("machid","ログインOK")
        } else {
            Log.d("machid","ログインできてない")
        }
         */
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mQuizRef = dataBaseReference.child(FBPATH_GENRE).child(genre.toString())
        mQuizRef.addChildEventListener(mQuizListener)
    }

    private fun saveResultToFB(){
        // 現在の日付を取得
        val df = SimpleDateFormat("yyyyMMddHHmmss")
        val date = Date()
        val quizPerformDate = df.format(date)
        val databaseReference = FirebaseDatabase.getInstance().reference
        val resultRef = databaseReference.child(FBPATH_RESULTS).child(mGenre.toString())
           .child(quizPerformDate.toString())
        resultRef.push().setValue(resultArray)
    }

    private fun goToFinishScreen(){
        intent = Intent(applicationContext, FinishQuizActivity::class.java)
        intent.putExtra("numCorrect", numCorrectAnswers)
        intent.putExtra("numIncorrect", numIncorrectAnswers)
        startActivity(intent)
    }

    /////// ゴミ箱
    /*
// テスト用のメソッド：クイズ取得処理
private fun testGetQuiz(){
    lateinit var stubQuiz: ArrayList<Quiz>

    if (mGenre == GENRE_SPORTS) {
        stubQuiz = stubSportsQuizArrayList
    } else if (mGenre == GENRE_2019NETA) {
        stubQuiz = stub2019QuizArrayList
    }

    for (quiz in stubQuiz) {
        val quizBody: String = quiz.quizBody
        val quizChoises: java.util.ArrayList<String> = quiz.quizChoises
        val correctAnswer: String = quiz.correctAnswer
        val descriptions: String = quiz.descriptions
        val quizId: String = quiz.quizId
        val genre: Int = quiz.genre
        val stubSportsQuiz = Quiz(quizBody, quizChoises, correctAnswer, descriptions, quizId, genre)
        mQuizArray.add(stubSportsQuiz)
    }
}

 */

}

