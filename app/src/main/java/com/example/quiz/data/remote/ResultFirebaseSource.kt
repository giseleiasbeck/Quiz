package com.example.quiz.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Fonte de dados para salvar/ler os RESULTADOS do quiz no Firebase.
 *
 * Estrutura no Firebase Realtime Database:
 *
 *   results/
 *     {userId}/                          ← ID do usuário logado
 *       {resultId}/                      ← ID único gerado pelo Firebase (.push())
 *         totalQuestions: 5
 *         correctAnswers: 4
 *         scorePercentage: 80.0
 *         totalTimeSeconds: 45
 *         dateTimestamp: 1708900000000
 *
 * Por que salvar por userId?
 * → O trabalho pede: "desempenho salvo na nuvem junto ao perfil do usuário".
 *   Cada usuário tem seu próprio nó. Isso permite:
 *   - Ranking entre usuários (comparar scores)
 *   - Histórico individual na nuvem (acessível de qualquer dispositivo)
 *   - Privacidade (regras do Firebase podem restringir acesso ao próprio nó)
 *
 * Por que ".push()"?
 * → Gera uma chave única automática (ex: "-NxBc3f..."). Assim cada resultado
 *   tem seu próprio ID sem risco de conflito, mesmo que o usuário faça
 *   dois quizzes no mesmo milissegundo.
 */
class ResultFirebaseSource @Inject constructor() {

    private val database = FirebaseDatabase.getInstance("https://quiz-app-d7112-default-rtdb.firebaseio.com")
    private val resultsRef = database.getReference("results")
    private val auth = FirebaseAuth.getInstance()

    /**
     * Modelo simples para enviar ao Firebase.
     * Não usa a Entity do Room diretamente porque:
     * 1. O Firebase não precisa do campo "id" (ele gera seu próprio)
     * 2. Evita acoplamento entre camadas (Room ≠ Firebase)
     */
    data class ResultData(
        val totalQuestions: Int = 0,
        val correctAnswers: Int = 0,
        val scorePercentage: Double = 0.0,
        val totalTimeSeconds: Long = 0,
        val dateTimestamp: Long = System.currentTimeMillis()
    )

    /**
     * Salva um resultado no Firebase, vinculado ao usuário logado.
     *
     * Fluxo:
     * 1. Pega o uid do usuário logado (FirebaseAuth)
     * 2. Se não estiver logado → retorna failure (não salva)
     * 3. .push() → cria um nó novo com chave única
     * 4. .setValue() → escreve os dados
     * 5. .await() → espera a operação completar
     *
     * Retorna Result para indicar sucesso/falha.
     * Se falhar (sem internet), o resultado ainda estará no Room local.
     */
    suspend fun saveResult(
        totalQuestions: Int,
        correctAnswers: Int,
        scorePercentage: Double,
        totalTimeSeconds: Long
    ): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuário não logado"))

            val resultData = ResultData(
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                scorePercentage = scorePercentage,
                totalTimeSeconds = totalTimeSeconds
            )

            // Cria um novo nó em results/{userId}/{autoId}
            resultsRef
                .child(userId)
                .push()
                .setValue(resultData)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca todos os resultados do usuário logado no Firebase.
     * Usado para sincronizar histórico entre dispositivos.
     * Ordenado por dateTimestamp (mais recente primeiro).
     */
    suspend fun getUserResults(): Result<List<ResultData>> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuário não logado"))

            val snapshot = resultsRef
                .child(userId)
                .orderByChild("dateTimestamp")
                .get()
                .await()

            val results = snapshot.children.mapNotNull { child ->
                child.getValue(ResultData::class.java)
            }.reversed() // Reversed para mais recente primeiro

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
