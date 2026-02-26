package com.example.quiz.ui.feature.singup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quiz.ui.feature.auth.AuthState
import com.example.quiz.ui.feature.auth.AuthViewModel

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel,
    navigateToHome: () -> Unit,
    navigateToLogin: () -> Unit
) {
    val authState by viewModel.authState.observeAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 1. ESTADO PARA CONTROLAR A VISIBILIDADE
    var passwordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    /* GEMINI PRO - START
     Prompt:
     I'm finishing the signup screen for my app. I already have a ViewModel that gives me an authState (whether it's loading, if there was an error, or if it authenticated).
     My problem is: How do I make the app automatically navigate to the 'Home' screen as soon as the state changes to Authenticated?
     I tried putting an if statement inside the screen, but it tries to navigate multiple times or gives a 'recomposition' error. Also, if there's an error in Firebase, I want to show a Snackbar (that pop-up notification) with the error message from the ViewModel.
     How do I use LaunchedEffect to 'keep an eye' on the state and only trigger the navigation or the Snackbar once when the state changes? Can you show me how to configure the Scaffold with SnackbarHost to make this work?*/
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> navigateToHome()
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
            }
            //Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    )


    /* GEMINI PRO - END*/

    { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Cadastre-se", fontSize = 32.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    errorContainerColor = MaterialTheme.colorScheme.surface
                ),
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                // Configura teclado de email (adiciona o @)
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- CAMPO DE SENHA ---
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    errorContainerColor = MaterialTheme.colorScheme.surface
                ),
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                singleLine = true,
                // 2. LÓGICA DE VISIBILIDADE
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                // 3. ÍCONE DE CLICAR
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Esconder senha" else "Mostrar senha"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(0.8f),
                onClick = { viewModel.signup(email, password) },
                enabled = authState != AuthState.Loading
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = "Criar Conta")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                modifier = Modifier.fillMaxWidth(0.8f),
                onClick = navigateToLogin
            ) {
                Text(text = "Já tem conta? Faça Login")
            }
        }
    }


}