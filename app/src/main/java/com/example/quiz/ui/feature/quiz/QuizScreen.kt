package com.example.quiz.ui.feature.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onQuizFinished: (Int, Int, Double, Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startQuiz()
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is QuizUiState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Carregando perguntas...")
                    }
                }

                is QuizUiState.Playing -> {
                    QuizPlayingContent(
                        state = state,
                        onOptionSelected = { viewModel.selectAnswer(it) },
                        onNextClicked = { viewModel.nextQuestion() }
                    )
                }

                is QuizUiState.Finished -> {
                    LaunchedEffect(state) {
                        onQuizFinished(
                            state.totalQuestions,
                            state.correctAnswers,
                            state.scorePercentage,
                            state.totalTimeSeconds
                        )
                    }
                    CircularProgressIndicator()
                }

                is QuizUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "😕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.startQuiz() }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizPlayingContent(
    state: QuizUiState.Playing,
    onOptionSelected: (Int) -> Unit,
    onNextClicked: () -> Unit
) {
    val timerColor = when {
        state.remainingSeconds <= 10 -> Color(0xFFF44336)
        state.remainingSeconds <= 20 -> Color(0xFFFFC107)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pergunta ${state.currentIndex + 1} de ${state.totalQuestions}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = "Tempo",
                        tint = timerColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = formatTime(state.remainingSeconds),
                        fontWeight = FontWeight.Bold,
                        color = timerColor,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = state.currentQuestion.category,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (state.currentIndex + 1).toFloat() / state.totalQuestions },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { state.remainingSeconds.toFloat() / QuizViewModel.QUIZ_TIME_SECONDS },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = timerColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "✓ ${state.correctAnswers} acerto(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = state.currentQuestion.questionText,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val options = state.currentQuestion.getOptions()
            val labels = listOf("A", "B", "C", "D")

            options.forEachIndexed { index, optionText ->
                OptionButton(
                    label = labels[index],
                    text = optionText,
                    index = index,
                    isSelected = state.selectedOptionIndex == index,
                    isAnswered = state.isAnswered,
                    isCorrect = index == state.currentQuestion.correctOptionIndex,
                    onClick = { onOptionSelected(index) }
                )
                if (index < options.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        if (state.isAnswered) {
            Button(
                onClick = onNextClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (state.currentIndex < state.totalQuestions - 1)
                        "Próxima Pergunta"
                    else
                        "Ver Resultado",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "$min:${sec.toString().padStart(2, '0')}"
}

@Composable
private fun OptionButton(
    label: String,
    text: String,
    index: Int,
    isSelected: Boolean,
    isAnswered: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !isAnswered -> Color.Transparent
            isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f)
            isSelected -> Color(0xFFF44336).copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "optionBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            !isAnswered && isSelected -> MaterialTheme.colorScheme.primary
            !isAnswered -> MaterialTheme.colorScheme.outline
            isCorrect -> Color(0xFF4CAF50)
            isSelected -> Color(0xFFF44336)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        animationSpec = tween(300),
        label = "optionBorder"
    )

    OutlinedButton(
        onClick = { if (!isAnswered) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected || (isAnswered && isCorrect)) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label.",
                fontWeight = FontWeight.Bold,
                color = borderColor
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isAnswered) {
                if (isCorrect) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Correto",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Incorreto",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
