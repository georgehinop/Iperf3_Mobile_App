import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.iperf3.HistoryScreen
import com.example.iperf3.Iperf3App
import com.example.iperf3.PreferencesManager
import com.example.iperf3.TestDetailScreen

@Composable
fun AppNavigation(context: Context,iperfLogger: IperfLogger,preferencesManager: PreferencesManager) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            Iperf3App(
                context = context,
                navController = navController,
                iperfLogger = iperfLogger,
                preferencesManager = preferencesManager
            )
        }
        composable("history") {
            HistoryScreen(
                navController = navController,
                iperfLogger = iperfLogger
            )
        }
        composable(
            "test_details/{testId}",
            arguments = listOf(navArgument("testId") { type = NavType.StringType })
        ) { backStackEntry ->
            TestDetailScreen(
                navController = navController,
                iperfLogger = iperfLogger,
                testId = backStackEntry.arguments?.getString("testId")
            )
        }
    }
}