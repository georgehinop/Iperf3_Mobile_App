package com.example.iperf3

import AppNavigation
import IperfLogger
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TopAppBarDefaults
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.example.iperf3.ui.theme.Iperf3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Iperf3Theme { // Make sure you have your theme defined
                AppNavigation(
                    context = LocalContext.current,
                    iperfLogger = IperfLogger(LocalContext.current),
                    preferencesManager = PreferencesManager(LocalContext.current)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Iperf3App(
    context: Context,
    navController: NavController,
    iperfLogger: IperfLogger,
    preferencesManager: PreferencesManager
) {
    var command by remember { mutableStateOf(TextFieldValue("")) }
    var output by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Load saved command on startup
    LaunchedEffect(Unit) {
        val savedCommand = preferencesManager.getLastCommand()
        command = if (!savedCommand.isNullOrEmpty()) {
            TextFieldValue(savedCommand)
        } else {
            // Default command if none saved
            TextFieldValue("-c iperf3.moji.fr -p 5200 -t 10")
        }
    }
    // Show loading state while command is null
    if (command == null) {
        CircularProgressIndicator()
        return
    }

    fun runIperfCommand(cmd: String, isHelpCommand: Boolean = false) {
        if (isRunning) return

        coroutineScope.launch {
            isRunning = true
            output = "Running command: $cmd\n\n"
            try {
                executeIperf3Command(context, cmd)
                    .collect { newLine ->
                        output += newLine
                    }
                // Only log if it's NOT a help command
                if (!isHelpCommand && output.contains("iperf Done.")) {
                    preferencesManager.saveLastCommand(cmd)
                    iperfLogger.addTest(cmd, removeFirstLine(output))
                }
            } catch (e: Exception) {
                output += "Error: ${e.message}\n"
            } finally {
                isRunning = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("iPerf3 Network Test") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Help") },
                            onClick = {
                                showMenu = false
                                runIperfCommand("-h", isHelpCommand = true)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = {
                                showMenu = false
                                navController.navigate("history") // Navigate to history
                            },
                            leadingIcon = {
                                Icon(Icons.Default.List, contentDescription = null)
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                label = { Text("Enter iperf3 Command") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { runIperfCommand(command.text) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning
            ) {
                Text(if (isRunning) "Running..." else "Start Test")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Output display
            val scrollState = rememberScrollState()
            LaunchedEffect(output) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            Text(
                text = output,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}


fun executeIperf3Command(
    context: Context,
    command: String
): Flow<String> = flow {
    val nativeLibraryDir = context.applicationInfo.nativeLibraryDir
    val iperfPath = "$nativeLibraryDir/libiperf3.so"

    val process = try {
        Runtime.getRuntime()
            .exec(arrayOf(iperfPath) + command.split(" ").filter { it.isNotEmpty() })
    } catch (e: Exception) {
        throw Exception("Failed to start iperf3: ${e.message}")
    }

    val inputStream = process.inputStream.bufferedReader()
    val errorStream = process.errorStream.bufferedReader()

    try {
        // Read all output at once (simpler than streaming)
        val output = inputStream.readText()
        val error = errorStream.readText()

        if (output.isNotEmpty()) emit(output)
        if (error.isNotEmpty()) emit(error)

        if (process.waitFor() != 0) {
            emit("\n[Process exited with code ${process.exitValue()}]\n")
        }
    } finally {
        inputStream.close()
        errorStream.close()
        process.destroy()
    }
}
    .flowOn(Dispatchers.IO)
    .catch { e ->
        emit("\n[ERROR: ${e.message}]\n")
    }

fun removeFirstLine(result: String): String {
    return result.lines().let { lines ->
        if (lines.isNotEmpty() && lines.first().contains("Running command")) {
            // Remove first line if it contains "Running command"
            lines.drop(1).joinToString("\n")
        } else {
            // Keep original output if first line doesn't match
            result
        }
    }
}