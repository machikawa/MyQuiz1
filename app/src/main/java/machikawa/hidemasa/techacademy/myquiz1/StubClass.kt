package machikawa.hidemasa.techacademy.myquiz1

import androidx.appcompat.app.AppCompatActivity
import java.util.ArrayList

//class StubClass :AppCompatActivity() {

//    private lateinit var  mQuiz: Quiz
    // SPORTS QUIZ 1 //
    private val quizBody: String = "マラソンの走行距離は"
    val quizChoises: ArrayList<String> = arrayListOf("40.115 km", "41.155 km", "42.195 km", "43.225 km")
    val correctAnswer: String = "42.195 km"
    val descriptions: String = ""
    val quizId: String = "0001"
    val genre: Int = GENRE_SPORTS

    val stubSportsQuiz1 = Quiz(quizBody,quizChoises,correctAnswer,descriptions,quizId,genre)

    // SPORTS QUIZ 2 //
    private val quizBody2: String = "ババンギダ選手の出身国は"
    val quizChoises2: ArrayList<String> = arrayListOf("チュニジア","南アフリカ","ナイジェリア")
    val correctAnswer2: String = "ナイジェリア"
    val descriptions2: String = "みんな大好きババンギダ"
    val quizId2: String = "0002"
    val genre2: Int = GENRE_SPORTS

    val stubSportsQuiz2 = Quiz(quizBody2,quizChoises2,correctAnswer2,descriptions2,quizId2,genre2)
    var stubSportsQuizArrayList = arrayListOf<Quiz>(stubSportsQuiz1,stubSportsQuiz2)

    //
    private val quizBody11: String = "2019年1月にあった出来事といえば？"
    val quizChoises11: ArrayList<String> = arrayListOf("日産会長逮捕","レオパレス不正施工","高輪ゲートウェイ駅に決定")
    val correctAnswer11: String = "日産会長逮捕"
    val descriptions11: String = "いやぁ、やっちゃいましたね"
    val quizId11: String = "1001"
    val genre11: Int = GENRE_2019NETA
    val stubSportsQuiz11 = Quiz(quizBody11,quizChoises11,correctAnswer11,descriptions11,quizId11,genre11)
    var stub2019QuizArrayList = arrayListOf<Quiz>(stubSportsQuiz11)
// }
