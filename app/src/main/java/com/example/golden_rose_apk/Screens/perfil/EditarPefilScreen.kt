package com.example.golden_rose_apk.Screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.golden_rose_apk.ViewModel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPefilScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    isDarkTheme: Boolean,                 // viene desde Main
    onThemeChange: (Boolean) -> Unit      // callback al Main
) {
    val uiState by userViewModel.userUiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Nombre mostrado
            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = { userViewModel.onDisplayNameChange(it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            // Nick in-game
            OutlinedTextField(
                value = uiState.nickname,
                onValueChange = { userViewModel.onNicknameChange(it) },
                label = { Text("Nick in-game") },
                modifier = Modifier.fillMaxWidth()
            )

            // Email (solo lectura)
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { },
                label = { Text("Correo") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            // País
            OutlinedTextField(
                value = uiState.country,
                onValueChange = { userViewModel.onCountryChange(it) },
                label = { Text("País / Región") },
                modifier = Modifier.fillMaxWidth()
            )

            // Preferencia de tema (control global desde Main)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tema oscuro", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Usar tema oscuro en la app",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { checked ->
                        // 1) Actualizar estado global en Main
                        onThemeChange(checked)

                        // 2) (Opcional) Guardar preferencia en el perfil/BD
                        userViewModel.onDarkModeChange(checked)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Guardar
            Button(
                onClick = {
                    scope.launch {
                        val ok = userViewModel.saveProfile()
                        snackbarHostState.showSnackbar(
                            if (ok) "Perfil actualizado" else "Error al guardar cambios"
                        )
                        if (ok) navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }

            // Botón Historial de compras
            OutlinedButton(
                onClick = { navController.navigate("orderHistory") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver historial de compras")
            }
        }
    }
}