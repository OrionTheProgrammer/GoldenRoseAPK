package com.example.golden_rose_apk.Screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.util.Calendar
import com.example.golden_rose_apk.repository.LocalUserRepository
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {

    // Estados
    var user by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(false) }

    // Estados de errores
    var userError by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var termsError by remember { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF5649A5),
        unfocusedBorderColor = Color(0xFF5649A5).copy(alpha = 0.5f),
        focusedLabelColor = Color(0xFF5649A5),
        unfocusedLabelColor = Color.Gray,
        cursorColor = Color(0xFF5649A5),

        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledTextColor = Color.Black.copy(alpha = 0.6f),

        errorBorderColor = Color.Red,
        errorCursorColor = Color.Red,

        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        errorContainerColor = Color.White
    )

    // Validaciones de contraseña
    val hasMinLength = password.length >= 8
    val hasMaxLength = password.length <= 100
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    val hasNumber = password.any { it.isDigit() }

    // Convalidacion de Registro
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var registrationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val userRepository = remember { LocalUserRepository(context) }
    val scope = rememberCoroutineScope()


    // Función para validar email
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return emailRegex.matches(email)
    }

    // Función para validar teléfono chileno (opcional)
    fun isValidChileanPhone(phone: String): Boolean {
        // Si está vacío, es válido (porque es opcional)
        if (phone.isBlank()) return true

        // Eliminar todos los espacios
        val cleanPhone = phone.replace("\\s".toRegex(), "")

        // Validar que sean solo números
        if (!cleanPhone.matches("\\d+".toRegex())) return false

        // Validar longitud y formato chileno
        return when {
            // Número móvil: 9XXXXXXXX (9 dígitos, empieza con 9)
            cleanPhone.startsWith("9") && cleanPhone.length == 9 -> true

            // Número fijo: 2XXXXXXXX (8 dígitos, empieza con 2)
            cleanPhone.startsWith("2") && cleanPhone.length == 8 -> true

            // Otros casos no válidos
            else -> false
        }
    }

    // Función para formatear teléfono mientras se escribe
    fun formatChileanPhone(input: String): String {
        val digits = input.filter { it.isDigit() }

        return when {
            digits.isEmpty() -> ""
            digits.startsWith("9") && digits.length <= 9 -> {
                when (digits.length) {
                    1 -> digits
                    2 -> "${digits[0]} ${digits[1]}"
                    in 3..5 -> "${digits[0]} ${digits.substring(1, digits.length)}"
                    else -> "${digits[0]} ${digits.substring(1, 5)} ${digits.substring(5)}"
                }
            }
            digits.startsWith("2") && digits.length <= 8 -> {
                when (digits.length) {
                    1 -> digits
                    2 -> "${digits[0]} ${digits[1]}"
                    in 3..5 -> "${digits[0]} ${digits.substring(1, digits.length)}"
                    else -> "${digits[0]} ${digits.substring(1, 5)} ${digits.substring(5)}"
                }
            }
            else -> digits.take(if (digits.startsWith("9")) 9 else 8)
        }
    }

    // Función para validar nombre de usuario
    fun isValidUsername(username: String): Boolean {
        // Solo letras, números y guiones bajos, entre 3 y 20 caracteres
        return username.matches("^[a-zA-Z0-9_]{3,20}\$".toRegex())
    }

    // Función para validar fecha (formato DD/MM/YYYY)
    fun isValidDate(date: String): Boolean {
        return date.matches("^\\d{2}/\\d{2}/\\d{4}\$".toRegex())
    }

    // Función para validar formulario completo
    fun validateForm(): Boolean {
        var isValid = true

        // Limpiar errores previos
        userError = ""
        dateError = ""
        phoneError = ""
        emailError = ""
        termsError = ""

        // Validar usuario
        if (user.isBlank()) {
            userError = "El usuario es requerido"
            isValid = false
        } else if (!isValidUsername(user)) {
            userError = "3-20 caracteres, solo letras, números y _"
            isValid = false
        }

        // Validar fecha
        if (dateText.isBlank()) {
            dateError = "La fecha de nacimiento es requerida"
            isValid = false
        } else if (!isValidDate(dateText)) {
            dateError = "Formato: DD/MM/AAAA"
            isValid = false
        }

        // Validar teléfono (opcional, pero si se ingresa debe ser válido)
        if (phone.isNotBlank() && !isValidChileanPhone(phone)) {
            phoneError = "Formato chileno: 9 1234 5678 (móvil) o 2 1234 567 (fijo)"
            isValid = false
        }

        // Validar email
        if (email.isBlank()) {
            emailError = "El correo es requerido"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailError = "Ingresa un correo válido"
            isValid = false
        }

        // Validar contraseña
        if (password.isBlank()) {
            // El error se mostrará en los requisitos
            isValid = false
        } else {
            if (!hasMinLength || !hasMaxLength || !hasUpperCase ||
                !hasLowerCase || !hasNumber || !hasSpecialChar) {
                isValid = false
            }
        }

        // Validar términos
        if (!accepted) {
            termsError = "Debes aceptar los términos"
            isValid = false
        }

        return isValid
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Crea tu Cuenta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF5649A5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFE7F1F1))
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Información Personal
            Text(
                text = "Información Personal",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF5649A5),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Campo: Usuario CON VALIDACIÓN
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Usuario",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = user,
                    onValueChange = {
                        user = it
                        if (userError.isNotEmpty()) userError = ""
                    },
                    label = { Text("Ingresa tu nombre de usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    isError = userError.isNotEmpty(),
                    supportingText = {
                        if (userError.isNotEmpty()) {
                            Text(text = userError, color = Color.Red)
                        } else {
                            Text(text = "3-20 caracteres, letras, números y _")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Fecha de Nacimiento CON VALIDACIÓN
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Fecha de Nacimiento",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                SelectorFechas(
                    dateText = dateText,
                    onDateSelected = { newDate ->
                        dateText = newDate
                        if (dateError.isNotEmpty()) dateError = ""
                    },
                    isError = dateError.isNotEmpty(),
                    errorMessage = dateError
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Teléfono OPCIONAL CON VALIDACIÓN
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "Teléfono",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "(opcional)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        // Aplicar formato mientras se escribe
                        phone = formatChileanPhone(it)
                        if (phoneError.isNotEmpty()) phoneError = ""
                    },
                    label = { Text("Teléfono (opcional)") },
                    placeholder = { Text("9 1234 5678") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5649A5),
                        focusedLabelColor = Color(0xFF5649A5),
                        errorBorderColor = Color.Red
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = phoneError.isNotEmpty(),
                    supportingText = {
                        if (phoneError.isNotEmpty()) {
                            Text(text = phoneError, color = Color.Red)
                        } else {
                            Text(
                                text = "Formato: 9 1234 5678",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Línea divisoria
            Divider(
                color = Color(0xFF5649A5).copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sección: Información Cuenta
            Text(
                text = "Información Cuenta",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF5649A5),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Campo: Correo CON VALIDACIÓN
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Correo Electrónico",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError.isNotEmpty()) emailError = ""
                    },
                    label = { Text("ejemplo@correo.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5649A5),
                        focusedLabelColor = Color(0xFF5649A5),
                        errorBorderColor = Color.Red
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError.isNotEmpty(),
                    supportingText = {
                        if (emailError.isNotEmpty()) {
                            Text(text = emailError, color = Color.Red)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Contraseña con validaciones
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Contraseña",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Crea una contraseña segura") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5649A5),
                        focusedLabelColor = Color(0xFF5649A5)
                    )
                )

                // REQUISITOS DE CONTRASEÑA
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF5649A5).copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Requisitos de contraseña:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5649A5)
                    )

                    // Cada requisito con icono de validación
                    RequerimientoContraseñaItem(
                        text = "Entre 8 y 100 caracteres",
                        isValid = hasMinLength && hasMaxLength
                    )

                    RequerimientoContraseñaItem(
                        text = "Al menos una mayúscula (A-Z)",
                        isValid = hasUpperCase
                    )

                    RequerimientoContraseñaItem(
                        text = "Al menos una minúscula (a-z)",
                        isValid = hasLowerCase
                    )

                    RequerimientoContraseñaItem(
                        text = "Al menos un número (0-9)",
                        isValid = hasNumber
                    )

                    RequerimientoContraseñaItem(
                        text = "Al menos un carácter especial (@, #, $, etc.)",
                        isValid = hasSpecialChar
                    )

                    // Indicador de fortaleza
                    if (password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        IndicadorContraseñaFuerte(
                            requirements = listOf(
                                hasMinLength && hasMaxLength,
                                hasUpperCase,
                                hasLowerCase,
                                hasNumber,
                                hasSpecialChar
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Checkbox Términos y Condiciones CON VALIDACIÓN
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
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
                            if (termsError.isNotEmpty()) termsError = ""
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF5649A5),
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        text = "He leído y acepto los ",
                        fontSize = 14.sp,
                        color = if (termsError.isNotEmpty()) Color.Red else Color.Black
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

                // Mensaje de error de términos
                if (termsError.isNotEmpty()) {
                    Text(
                        text = termsError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (registrationError != null) {
                Text(
                    text = registrationError!!,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Botón Crear Cuenta
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && validateForm(),
                onClick = {
                    isLoading = true
                    registrationError = null

                    scope.launch {
                        userRepository.registerUser(
                            username = user,
                            email = email,
                            password = password
                        ).onSuccess {
                            isLoading = false
                            showSuccessDialog = true
                        }.onFailure { error ->
                            isLoading = false
                            registrationError = error.localizedMessage
                        }
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Crear Cuenta")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            },
            title = { Text("Cuenta creada") },
            text = { Text("Tu cuenta se creó con éxito. Ahora inicia sesión.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                ) {
                    Text("Ir al Login")
                }
            }
        )
    }
}


@Composable
fun SelectorFechas(
    dateText: String,
    onDateSelected: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            label = { Text("DD/MM/AAAA") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5649A5),
                focusedLabelColor = Color(0xFF5649A5),
                errorBorderColor = Color.Red
            ),
            isError = isError,
            supportingText = {
                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = Color.Red)
                }
            }
        )

        Button(
            onClick = { datePicker.show() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5649A5),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Seleccionar fecha")
        }
    }
}

@Composable
fun RequerimientoContraseñaItem(text: String, isValid: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isValid) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (isValid) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isValid) Color(0xFF4CAF50) else Color.Gray
        )
    }
}

@Composable
fun IndicadorContraseñaFuerte(requirements: List<Boolean>) {
    val requirementsMet = requirements.count { it }
    val totalRequirements = requirements.size

    val strengthText = when {
        requirementsMet == totalRequirements -> "Muy fuerte ✓"
        requirementsMet >= totalRequirements * 0.7 -> "Fuerte"
        requirementsMet >= totalRequirements * 0.4 -> "Media"
        else -> "Débil"
    }

    val strengthColor = when {
        requirementsMet == totalRequirements -> Color(0xFF4CAF50)  // Verde
        requirementsMet >= totalRequirements * 0.7 -> Color(0xFF8BC34A)  // Verde claro
        requirementsMet >= totalRequirements * 0.4 -> Color(0xFFFF9800)  // Naranja
        else -> Color(0xFFF44336)  // Rojo
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Barra de progreso
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(totalRequirements) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (index < requirementsMet) strengthColor
                            else Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        // Texto de fortaleza
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fortaleza:",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = strengthText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = strengthColor
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(navController = rememberNavController())
}
