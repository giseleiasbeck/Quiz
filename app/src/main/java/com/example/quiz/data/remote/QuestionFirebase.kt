package com.example.quiz.data.remote

/**
 * Modelo que representa uma pergunta NO FIREBASE Realtime Database.
 *
 * Por que um modelo SEPARADO do Question.kt (Room)?
 * → O Firebase e o Room têm necessidades diferentes:
 *   - O Firebase serializa/deserializa usando nomes de campo (JSON)
 *   - O Room usa @Entity com @PrimaryKey autoGenerate
 *   - Misturar os dois criaria acoplamento desnecessário
 *
 * Esse modelo é um "espelho" simples do que está no Firebase.
 * Quando baixamos do Firebase, convertemos para Question (Room) e salvamos.
 *
 * IMPORTANTE: O Firebase Realtime Database precisa de:
 * 1. Um construtor SEM parâmetros (construtor vazio) → por isso os defaults ""
 * 2. Propriedades var (mutáveis) OU um construtor vazio
 * Sem isso, o Firebase não consegue deserializar o JSON em objeto Kotlin.
 */
data class QuestionFirebase(
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctOptionIndex: Int = 0,
    val category: String = "Geral"
)
