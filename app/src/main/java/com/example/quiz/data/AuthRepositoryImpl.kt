package com.example.quiz.data

import com.example.quiz.data.local.UserDao
import com.example.quiz.data.local.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun login(email: String, pass: String): Result<Boolean> {
        return try {
            // 1. Autentica no Firebase
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user

            if (user != null) {
                // 2. Tenta recuperar os dados da nuvem para atualizar o telemóvel localmente
                val document = firestore.collection("users").document(user.uid).get().await()
                val pontuacao = document.getLong("pontuacaoTotal")?.toInt() ?: 0

                // 3. Guarda localmente para uso offline
                userDao.insertUser(UserEntity(uid = user.uid, email = email, pontuacaoTotal = pontuacao))

                Result.success(true)
            } else {
                Result.failure(Exception("Utilizador não encontrado após login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signup(email: String, pass: String): Result<Boolean> {
        return try {
            // 1. Cria a conta no Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user

            if (user != null) {
                // 2. Prepara o documento para o Firestore
                val userProfile = hashMapOf(
                    "uid" to user.uid,
                    "email" to email,
                    "pontuacaoTotal" to 0
                )

                // 3. Salva no Firestore (Nuvem)
                firestore.collection("users").document(user.uid).set(userProfile).await()

                // 4. Salva no Room (Local)
                userDao.insertUser(UserEntity(uid = user.uid, email = email, pontuacaoTotal = 0))

                Result.success(true)
            } else {
                Result.failure(Exception("Falha ao criar utilizador: Registo nulo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        auth.signOut()
        // Opcional: userDao.clearUsers() se quiser limpar os dados locais ao sair
    }
}