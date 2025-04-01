import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

class IperfLogger(private val context: Context) {
    private val logFile by lazy {
        File(context.filesDir, "iperf_tests.json").apply {
            if (!exists()) createNewFile()
        }
    }
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    var tests: List<IperfTest> by mutableStateOf(emptyList())
        private set

    init {
        loadTests()
    }

    data class IperfTest(
        val id: String = UUID.randomUUID().toString(),
        val command: String,
        val result: String,
        val datetime: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
    )

    private fun loadTests() {
        try {
            val json = logFile.readText()
            if (json.isNotEmpty()) {
                val type = object : TypeToken<List<IperfTest>>() {}.type
                tests = gson.fromJson(json, type) ?: emptyList()
            }
        } catch (e: Exception) {
            logFile.delete()
            logFile.createNewFile()
            tests = emptyList()
        }
    }

    fun addTest(command: String, result: String) {
        if (command.contains("-h") || command.contains("--help")) return

        val newTest = IperfTest(
            command = command,
            result = result
        )

        val updatedTests = listOf(newTest) + tests
        saveTests(updatedTests)
    }

    private fun saveTests(tests: List<IperfTest>) {
        try {
            logFile.writeText(gson.toJson(tests))
            this.tests = tests
        } catch (e: Exception) {
            Log.e("IperfLogger", "Failed to save tests", e)
        }
    }

    fun getTestById(id: String): IperfTest? {
        return tests.find { it.id == id }
    }

    fun clearTests() {
        saveTests(emptyList())
    }
}