package com.example.quiz.ui.feature.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.quiz.data.model.QuizModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    quiz: QuizModel,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val questions = quiz.questionList

    var currentIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf("") }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }

    if (questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Este quiz não tem questões disponíveis.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack) { Text("Voltar") }
            }
        }
        return
    }

    if (showResult) {
        ResultDialog(
            score = score,
            total = questions.size,
            onFinish = onFinish
        )
    }

    val currentQuestion = questions[currentIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quiz.title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Progresso
            Text(
                text = "Questão ${currentIndex + 1} de ${questions.size}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )

            Spacer(Modifier.height(24.dp))

            // Pergunta
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Text(
                    text = currentQuestion.question,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(20.dp),
                    lineHeight = 26.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // Opções
            currentQuestion.options.forEach { option ->
                val isSelected = selectedAnswer == option
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    label = "border"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    label = "bg"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .border(2.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable { selectedAnswer = option }
                        .padding(16.dp)
                ) {
                    Text(
                        text = option,
                        fontSize = 15.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Botão Próxima
            Button(
                onClick = {
                    if (selectedAnswer.isEmpty()) return@Button
                    if (selectedAnswer == currentQuestion.correct) {
                        score++
                    }
                    if (currentIndex + 1 >= questions.size) {
                        showResult = true
                    } else {
                        currentIndex++
                        selectedAnswer = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = selectedAnswer.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentIndex + 1 >= questions.size) "Ver Resultado" else "Próxima",
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ResultDialog(
    score: Int,
    total: Int,
    onFinish: () -> Unit
) {
    val percentage = if (total > 0) (score.toFloat() / total.toFloat() * 100).toInt() else 0
    val passed = percentage >= 60

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (passed) "🎉 Parabéns!" else "😢 Não foi dessa vez",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Spacer(Modifier.height(16.dp))

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier.size(100.dp),
                        strokeWidth = 10.dp,
                        color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "$percentage%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "$score de $total questões corretas",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (passed) "Você passou! Bom trabalho." else "Continue praticando!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Finalizar")
                }
            }
        }
    }
}
