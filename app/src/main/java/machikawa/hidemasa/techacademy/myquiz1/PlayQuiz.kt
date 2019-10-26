package machikawa.hidemasa.techacademy.myquiz1

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
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
import kotlin.collections.HashMap

class PlayQuiz : AppCompatActivity(), View.OnClickListener {
    // クイズ全問が入っている
    private lateinit var mQuizArray: ArrayList<Quiz>
    private lateinit var choiseArray: ArrayList<String>
    private lateinit var resultArray: ArrayList<Result>
    private lateinit var userId:String
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
    // クイズの取得リスナー
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
    // お気に入り
    var isFavorite = false
    private lateinit var mfavArray:ArrayList<String>
    private lateinit var mfavRef:DatabaseReference
    val favoritedBtnColor:String = "#FFD700"
    val notFavoritedBtnColor:String = "#DCDCDC"

    // おきに追加のイベントリスナー
    private val mFavoriteEventListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError) {
        }
        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
        }
        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
        }
        // お気に入りの追加が押さたときの処理として
        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            // 画面ロード時に当該ユーザーのお気に入りQuesitonID一覧が読み込まれるため、現在のQuestionUIDのものがあるか判断する
            mfavArray.add(p0.key.toString())
        }
        // Remove 時の処理は EventListern にて実施する。
        override fun onChildRemoved(p0: DataSnapshot) {
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_quiz)

        val user = FirebaseAuth.getInstance().currentUser

        // お気に入り関係の処理
        if (user != null) {
            val userFavsDBRef= FirebaseDatabase.getInstance().reference
            mfavRef = userFavsDBRef.child(FBPATH_FAVORITES).child(user!!.uid.toString())
            mfavRef.addChildEventListener(mFavoriteEventListener)
        }
        // 各種アレイの初期化
        mQuizArray = ArrayList<Quiz>()
        resultArray= ArrayList<Result>()
        mfavArray = ArrayList<String>()
        userId = ""

        // ページタイトルの設定
        title = "クイズに正解して高みを目指せ"

        // ジャンルをSPから取得
        val extras = intent.extras
        mGenre = extras.getInt("genre")

        // intentに含まれるGenreからクイズを取得
        getQuizFromFB(mGenre)

        // 全てのボタンにリスナー登録を
        btnChoiseA.setOnClickListener(this)
        btnChoiseB.setOnClickListener(this)
        btnChoiseC.setOnClickListener(this)
        btnChoiseD.setOnClickListener(this)

        // 解説ボタンのアクション
        btnDetail.setOnClickListener {view ->
            val builder = AlertDialog.Builder(this@PlayQuiz)
            builder.setTitle("正解は " + mQuizArray[currentQuizIndex].correctAnswer)
            builder.setMessage(mQuizArray[currentQuizIndex].descriptions)

            builder.setNeutralButton("お気に入りに追加"){_, _ ->
                /// Firebase へのお気に入り追加処理, ログインしていないと警告に終わる。
                if (user == null) {
                    Snackbar.make(view, "お気に入りはログイン時のみ有効です", Snackbar.LENGTH_LONG).show()
                } else if (user != null) {
                    saveFavoriteToFB(user.uid)
                    // Snackbarでの表示。成功前提…
                    Snackbar.make(view, "お気に入りに追加しました", Snackbar.LENGTH_LONG).show()
                }
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
                judgeFavorite()
                showQuiz()
            }
        }
        // お気に入りボタンのアクション
        if (user != null) {
            favoriteBtn.visibility = View.VISIBLE
            //ボタンタップでオキニ削除or登録.
            favoriteBtn.setOnClickListener {
                if (isFavorite) {
                    removeFavoriteToFB(user.uid)
                    undoFavoriteAction()
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "お気に入りから削除されました",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else if (!isFavorite) {
                    saveFavoriteToFB(user.uid)
                    favoriteAction()
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "お気に入りに追加されました",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            favoriteBtn.visibility = View.INVISIBLE
        }
    }

    // 選択肢ボタンの処理成否判断して次へ
    override fun onClick(v: View) {
        // どのボタンを押したかリッスン
        when (v.id) {
            R.id.btnChoiseA -> {userSelectedAnswerIndex = 0; btnChoiseA.setBackgroundResource(R.drawable.border)}
            R.id.btnChoiseB -> {userSelectedAnswerIndex = 1; btnChoiseB.setBackgroundResource(R.drawable.border)}
            R.id.btnChoiseC -> {userSelectedAnswerIndex = 2; btnChoiseC.setBackgroundResource(R.drawable.border)}
            R.id.btnChoiseD -> {userSelectedAnswerIndex = 3; btnChoiseD.setBackgroundResource(R.drawable.border)}
        }
        // クイズの成否判断選んだ文字列と、クイズの正解の文字列が等しい場合正解
        // テキスト切り替えと、正解不正解数のカウントアップを行う
        if (mQuizArray[currentQuizIndex].quizChoises[userSelectedAnswerIndex]
            == mQuizArray[currentQuizIndex].correctAnswer) {
            resultString.text = "⭕️正解🙆‍♀️"
            resultArray.add(Result(mQuizArray[currentQuizIndex].quizId,currentQuizIndex,true))
            numCorrectAnswers += 1
            // ボタン変更処理
        } else {
            resultString.text = "❌不正解🙅‍♂️"
            resultArray.add(Result(mQuizArray[currentQuizIndex].quizId, currentQuizIndex,false))
            numIncorrectAnswers += 1
            // ボタン変更処理
        }
        // 結果表示：インビジブル解放
        resultString.isInvisible = false
        // 既存選択肢ボタンをNOT活性化して押せないようにする
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
        // お気に入りかどうか判断させる
        judgeFavorite()

        // 選択肢数に応じてボタンをインビジブルに切り替える処理
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

    // 次へボタンの可視化AND有効化
    private fun showAfterCareButton(){
        // 次へボタンは最後の問題なら終えるようにしなきゃいけない
        if (currentQuizIndex >= mQuizArray.size -1) {
            btnNextQuiz.text = "クイズを終える"
        }
        btnDetail.isEnabled = true; btnNextQuiz.isEnabled = true
        btnDetail.isVisible = true; btnNextQuiz.isVisible = true
    }

    // 次へボタン押下時のボタン状態切り戻し
    private fun resetBtnState(){
        resultString.text = ""
        resultString.isVisible = false
        disappearAfterCareButton()
        enableAllChoiseBtn()
    }

    // FBから情報とります
    private fun getQuizFromFB(genre:Int){
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mQuizRef = dataBaseReference.child(FBPATH_GENRE).child(genre.toString())
        mQuizRef.addChildEventListener(mQuizListener)
    }

    // クイズ結果をFBに保存する。[Result] - [Genre] - [userId] - [日付]　の順。
    // UserIdはアノニマスの場合、GUID。結果はResultというアレイに入っている。
    private fun saveResultToFB(){
        // 現在の日付を取得
        val df = SimpleDateFormat("yyyyMMddHHmmss")
        val date = Date()
        val quizPerformDate = df.format(date)
        val user = FirebaseAuth.getInstance().currentUser
        // ユーザーIDを指定する
        if (user != null) {
            userId = user.uid
        } else if (user == null) {
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            userId = sp.getString(SP_STR_USER_ID, "")
            if (userId.length == 0) {
                val editor = sp.edit()
                userId = UUID.randomUUID().toString()
                editor.putString(SP_STR_USER_ID, userId)
                editor.commit()
            }
        }
        val databaseReference = FirebaseDatabase.getInstance().reference
        val resultRef = databaseReference.child(FBPATH_RESULTS).child(mGenre.toString())
           .child(userId).child(quizPerformDate.toString())
        resultRef.setValue(resultArray)
    }

    // お気に入りをFBに保存する
    private fun saveFavoriteToFB(userid:String){
        val databaseReference = FirebaseDatabase.getInstance().reference
        val addFavRef = databaseReference
            .child(FBPATH_FAVORITES)
            .child(userid)
            .child(mQuizArray[currentQuizIndex].quizId)
        addFavRef.setValue(mGenre)
        favoriteAction()
    }
    // お気に入りをFBからさくじょする
    private fun removeFavoriteToFB(userid:String){
        val databaseReference = FirebaseDatabase.getInstance().reference
        val removeFavRef = databaseReference
            .child(FBPATH_FAVORITES)
            .child(userid)
            .child(mQuizArray[currentQuizIndex].quizId)
        removeFavRef.removeValue()
        undoFavoriteAction()
    }

    // 最終画面に遷移する。正解数と不正回数を渡してアップないで計算する
    private fun goToFinishScreen(){
        intent = Intent(applicationContext, FinishQuizActivity::class.java)
        intent.putExtra("numCorrect", numCorrectAnswers)
        intent.putExtra("numIncorrect", numIncorrectAnswers)
        startActivity(intent)
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
    private fun disappearAfterCareButton(){
        btnDetail.isEnabled = false; btnNextQuiz.isEnabled = false
        btnDetail.isVisible = false; btnNextQuiz.isVisible = false
    }

    ////// お気に入り機能
    // オキニフラグを反転させて、ボタンの色を変える
    fun favoriteAction (){
        isFavorite = true
        favoriteBtn.setTextColor(Color.parseColor(favoritedBtnColor))
    }
    fun undoFavoriteAction (){
        isFavorite = false
        favoriteBtn.setTextColor(Color.parseColor(notFavoritedBtnColor))
    }

    // お気に入りかどうか判断させている
    fun judgeFavorite(){
        if (mfavArray.contains(mQuizArray[currentQuizIndex].quizId)) {
            favoriteAction()
        } else {
            undoFavoriteAction()
        }
    }

}