package machikawa.hidemasa.techacademy.myquiz1

import java.io.Serializable
import java.util.ArrayList

class Quiz(
    val quizBody: String,
    val quizChoises: ArrayList<String>,
    val correctAnswer: String,
    val descriptions: String,
    val quizId: String,
    val genre: Int)
    : Serializable {
}