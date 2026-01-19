package com.example.golden_rose_apk.Screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.golden_rose_apk.R
import com.example.golden_rose_apk.ViewModel.AuthViewModel
import com.example.golden_rose_apk.repository.LocalUserRepository
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    // Estados
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Estados de errores
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var termsError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val userRepository = remember { LocalUserRepository(context) }
    val scope = rememberCoroutineScope()

    // Función para validar email
    fun isValidEmail(email: String): Boolean {
        val emailRegex = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        )
        return emailRegex.matcher(email).matches()
    }

    // Función para validar formulario completo
    fun validateForm(): Boolean {
        var isValid = true

        // Validar email
        emailError = when {
            email.isBlank() -> "El correo es requerido"
            !isValidEmail(email) -> "Ingresa un correo válido"
            else -> ""
        }
        if (emailError.isNotEmpty()) isValid = false

        // Validar contraseña
        passwordError = when {
            password.isBlank() -> "La contraseña es requerida"
            password.length < 6 -> "Mínimo 6 caracteres"
            else -> ""
        }
        if (passwordError.isNotEmpty()) isValid = false

        // Validar términos
        termsError = if (!accepted) "Debes aceptar los términos" else ""
        if (termsError.isNotEmpty()) isValid = false

        return isValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        modifier = Modifier.background(Color(0xFFE7F1F1))
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 30.dp, vertical = 20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo de Golden Rose",
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(Modifier.height(10.dp))


            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Correo",
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        // Limpiar error mientras el usuario escribe
                        if (emailError.isNotEmpty()) {
                            emailError = ""
                        }
                    },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = emailError.isNotEmpty(),
                    supportingText = {
                        if (emailError.isNotEmpty()) {
                            Text(text = emailError, color = Color.Red)
                        }
                    }
                )
            }

            Spacer(Modifier.height(30.dp))

            // Campo de contraseña con validación y toggle de visibilidad
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Contraseña", fontSize = 15.sp, modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        // Limpiar error mientras el usuario escribe
                        if (passwordError.isNotEmpty()) {
                            passwordError = ""
                        }
                    },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña"
                                else "Mostrar contraseña"
                            )
                        }
                    },
                    isError = passwordError.isNotEmpty(),
                    supportingText = {
                        if (passwordError.isNotEmpty()) {
                            Text(text = passwordError, color = Color.Red)
                        }
                    }
                )
            }


            Spacer(Modifier.height(10.dp))

            Text(
                "Recuperar contraseña",
                color = Color(0xFF5649A5),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        navController.navigate("recover_password")
                    }
            )

            Spacer(Modifier.height(20.dp))

            // Términos y condiciones con validación
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (termsError.isNotEmpty()) 4.dp else 0.dp)
                ) {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = {
                            accepted = it
                            if (termsError.isNotEmpty()) {
                                termsError = ""
                            }
                        }
                    )
                    Text(
                        text = "He leído y acepto los ",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "términos y condiciones",
                        color = Color(0xFF5649A5),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            // Navegar a términos y condiciones
                            navController.navigate("terms")
                        }
                    )
                }

                // Error de términos
                if (termsError.isNotEmpty()) {
                    Text(
                        text = termsError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                }
            }

            Spacer(Modifier.height(25.dp))

            Button(
                onClick = {
                    if (validateForm() && !loading) {
                        loading = true

                        scope.launch {
                            userRepository.login(email, password)
                                .onSuccess { user ->
                                    authViewModel.login(user)
                                    Toast.makeText(context, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                                .onFailure { error ->
                                    Toast.makeText(
                                        context,
                                        error.localizedMessage ?: "Error al iniciar sesión",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            loading = false
                        }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5649A5),
                    disabledContainerColor = Color(0xFF9E9E9E)
                ),
                shape = RoundedCornerShape(50),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar Sesión", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¿No tienes una cuenta? ",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Crear cuenta",
                    fontSize = 14.sp,
                    color = Color(0xFF5649A5),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        navController.navigate("register")
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}
