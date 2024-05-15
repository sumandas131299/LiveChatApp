package com.example.livechatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.livechatapp.Screeens.ChatListScreen
import com.example.livechatapp.Screeens.LoginScreen
import com.example.livechatapp.Screeens.ProfileScreen
import com.example.livechatapp.Screeens.SignUpScreen
import com.example.livechatapp.Screeens.SingleChatScreen
import com.example.livechatapp.Screeens.SingleStatusScreen
import com.example.livechatapp.Screeens.StatusScreen
import com.example.livechatapp.ui.theme.LiveChatAppTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class DestinationScreen(var route: String){
    object SignUp : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object ChatList : DestinationScreen("chatList")
    object SingleChat : DestinationScreen("singleChat/{chatId}"){
        fun createRoute(id: String)="singlechat/$id"
    }
    object StatusList : DestinationScreen("StatusList")
    object SingleStatus : DestinationScreen("singleStatus/{userId}"){
        fun createRoute(userId: String)="singlestatus/$userId"
    }

}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveChatAppTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatAppNavigation()

                }

            }
        }
    }

    @Composable
    fun ChatAppNavigation(){
        val navController = rememberNavController()
        var vm = hiltViewModel<LCViewModel>()
        NavHost(navController = navController, startDestination =DestinationScreen.SignUp.route ){
            composable(DestinationScreen.SignUp.route){
                SignUpScreen(navController, vm)
            }
            composable(DestinationScreen.Login.route){
                LoginScreen(navController, vm)
            }
            composable(DestinationScreen.ChatList.route){
                ChatListScreen(navController, vm)
            }
            composable(DestinationScreen.SingleChat.route){
                val chatId= it.arguments?.getString("chatId")
                chatId?.let {
                    SingleChatScreen(navController,vm,chatId)
                }
            }
            composable(DestinationScreen.StatusList.route){
                StatusScreen(navController, vm)
            }
            composable(DestinationScreen.SingleStatus.route, arguments = listOf(navArgument("userId") {
                type = NavType.StringType
            })) {
                val userId = it.arguments?.getString("userId")
                userId?.let {
                    SingleStatusScreen(navController = navController, vm = vm, userId = it)
                }
            }
            composable(DestinationScreen.Profile.route){
                ProfileScreen(navController, vm)
            }


        }

    }
}


