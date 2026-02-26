package com.example.quiz.data.remote

import com.example.quiz.data.local.entity.Question
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Fonte de dados que se comunica com o Firebase Realtime Database.
 *
 * Responsabilidade ÚNICA: buscar as perguntas do Firebase e convertê-las
 * para o modelo local (Question do Room).
 *
 * Estrutura esperada no Firebase Realtime Database:
 *
 *   quizzes/
 *     pergunta_1/
 *       questionText: "Qual é a capital do Brasil?"
 *       optionA: "São Paulo"
 *       optionB: "Rio de Janeiro"
 *       optionC: "Brasília"
 *       optionD: "Salvador"
 *       correctOptionIndex: 2
 *       category: "Geografia"
 *     pergunta_2/
 *       ...
 *
 * Por que "quizzes" como nome do nó raiz?
 * → Nome simples e descritivo. O Firebase Realtime Database organiza
 *   dados como uma árvore JSON. "quizzes" é o nó pai de todas as perguntas.
 *
 * Por que @Inject constructor()?
 * → Permite que o Hilt injete essa classe automaticamente onde for necessário.
 *   O FirebaseDatabase.getInstance() é chamado internamente (não precisa do Hilt).
 */
class QuestionFirebaseSource @Inject constructor() {

    // Referência ao nó "quizzes" no Firebase Realtime Database
    // A URL precisa ser explícita porque o google-services.json pode não conter
    // o campo "firebase_url" se o RTDB foi ativado depois do setup inicial.
    // ⚠️ IMPORTANTE: Substitua pela URL REAL do seu Realtime Database
    //    (que aparece no topo do Firebase Console → Realtime Database).
    //    O formato padrão é: https://{project-id}-default-rtdb.firebaseio.com
    private val database = FirebaseDatabase.getInstance("https://quiz-app-d7112-default-rtdb.firebaseio.com")
    private val questionsRef = database.getReference("quizzes")

    /**
     * Busca TODAS as perguntas do Firebase Realtime Database.
     *
     * Fluxo:
     * 1. questionsRef.get() → faz a requisição HTTP ao Firebase
     * 2. .await() → converte o callback do Firebase em suspend (coroutine)
     *    (por isso adicionamos kotlinx-coroutines-play-services)
     * 3. snapshot.children → itera cada filho do nó "quizzes"
     * 4. getValue(QuestionFirebase::class.java) → converte JSON → objeto Kotlin
     * 5. toLocalQuestion() → converte QuestionFirebase → Question (Room)
     *
     * Retorna:
     * - Result.success(lista) → se deu certo
     * - Result.failure(exception) → se falhou (sem internet, etc.)
     */
    suspend fun fetchAllQuestions(): Result<List<Question>> {
        return try {
            val snapshot = questionsRef.get().await()

            val questions = snapshot.children.mapNotNull { child ->
                // Cada child é um nó filho de "quizzes" (uma pergunta)
                val firebaseQuestion = child.getValue(QuestionFirebase::class.java)
                firebaseQuestion?.toLocalQuestion()
            }

            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Converte QuestionFirebase (modelo do Firebase) → Question (modelo do Room).
     *
     * O id = 0 faz o Room gerar um ID automático (autoGenerate = true).
     * Assim não há conflito de IDs entre Firebase e Room.
     */
    private fun QuestionFirebase.toLocalQuestion(): Question {
        return Question(
            id = 0, // Room vai gerar o ID automaticamente
            questionText = this.questionText,
            optionA = this.optionA,
            optionB = this.optionB,
            optionC = this.optionC,
            optionD = this.optionD,
            correctOptionIndex = this.correctOptionIndex,
            category = this.category
        )
    }
}
