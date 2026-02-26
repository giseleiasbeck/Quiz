package com.example.quiz.ui.feature.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tela de RESULTADO do Quiz.
 *
 * Exibida quando o quiz termina. Mostra:
 * - Emoji e mensagem baseados no desempenho
 * - Pontuação em porcentagem (grande e destacada)
 * - Cards com estatísticas (acertos, tempo)
 * - Botões para jogar novamente ou voltar ao início
 *
 * Os dados chegam via parâmetros (não via ViewModel) porque
 * são passados pela Navigation como argumentos da rota.
 * Isso garante que o resultado persiste mesmo se o ViewModel for destruído.
 */
@Composable
fun QuizResultScreen(
    totalQuestions: Int,
    correctAnswers: Int,
    scorePercentage: Double,
    totalTimeSeconds: Long,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    // 1. Determina a mensagem e emoji baseados na nota
    val (emoji, message) = when {
        scorePercentage >= 90 -> "🏆" to "Excelente!"
        scorePercentage >= 70 -> "🎉" to "Muito Bom!"
        scorePercentage >= 50 -> "👍" to "Bom Trabalho!"
        scorePercentage >= 30 -> "📚" to "Continue Estudando!"
        else -> "💪" to "Não Desista!"
    }

    // 2. Cor da pontuação baseada no desempenho
    val scoreColor = when {
        scorePercentage >= 70 -> Color(0xFF4CAF50) // Verde
        scorePercentage >= 50 -> Color(0xFFFFC107) // Amarelo
        else -> Color(0xFFF44336) // Vermelho
    }

    // 3. Formata o tempo para "Xmin Ys" ou "Xs"
    val timeFormatted = if (totalTimeSeconds >= 60) {
        "${totalTimeSeconds / 60}min ${totalTimeSeconds % 60}s"
    } else {
        "${totalTimeSeconds}s"
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ===== EMOJI =====
            Text(
                text = emoji,
                fontSize = 72.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ===== MENSAGEM =====
            Text(
                text = message,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ===== PONTUAÇÃO GRANDE =====
            Text(
                text = "${scorePercentage.toInt()}%",
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )

            Text(
                text = "de acerto",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ===== CARDS DE ESTATÍSTICAS =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card de Acertos
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CheckCircle,
                    iconTint = Color(0xFF4CAF50),
                    title = "Acertos",
                    value = "$correctAnswers/$totalQuestions"
                )

                // Card de Tempo
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Timer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Tempo",
                    value = timeFormatted
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ===== BOTÕES =====
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Jogar Novamente", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voltar ao Início", fontSize = 16.sp)
            }
        }
    }
}

/**
 * Card de estatística individual (ex: "Acertos: 4/5").
 * Reutilizável — basta mudar ícone, título e valor.
 */
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
