package machikawa.hidemasa.techacademy.myquiz1

import java.io.Serializable
import java.util.ArrayList

class Result(
    val quizId: String,
    val quizIndex: Int,
    val isCorrect: Boolean) // True = 正解、 False = 不正解
    : Serializable {
}
