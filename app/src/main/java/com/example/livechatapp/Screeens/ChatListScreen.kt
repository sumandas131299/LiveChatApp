package com.example.livechatapp.Screeens

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.livechatapp.CommonProgressBar
import com.example.livechatapp.CommonRow
import com.example.livechatapp.DestinationScreen

import com.example.livechatapp.LCViewModel
import com.example.livechatapp.TitleText
import com.example.livechatapp.navigateTo

@Composable
fun ChatListScreen(navController: NavController, vm: LCViewModel) {

    val inProgress = vm.inProcessChat

    if (inProgress.value) {
        CommonProgressBar()
    } else {
        val chats = vm.chats.value
        val userData = vm.userData.value
        val showDialog = remember {
            mutableStateOf(false)
        }
        val onFabuClick: () -> Unit = { showDialog.value = true }
        val onDissmiss: () -> Unit = { showDialog.value = false }
        val onAddChat: (String) -> Unit = {
            vm.onAddChat(it)
            showDialog.value = false
        }
        Scaffold(
            floatingActionButton = {FAB(
                showDialog = showDialog.value,
                onFabClick = onFabuClick,
                onDissmiss = onDissmiss,
                onAddChat = onAddChat
            ) }, content =
            {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ){
                TitleText(txt = "Chats")

                    if(chats.isEmpty()){
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "No chat Available")
                        }
                    }else{
                        LazyColumn(modifier = Modifier.weight(1f) ) {

                            items(chats){
                                chat->
                                val chatUser= if(chat.user1.userId==userData?.userId){
                                    chat.user2
                                }else{
                                    chat.user1
                                }
                                CommonRow(imageUrl = chatUser.imageUrl, name =chatUser.name ) {

                                    chat.chatId?.let {
                                        navigateTo(navController,DestinationScreen.SingleChat.createRoute(id = it))
                                    }


                                }
                            }
                        }
                    }

                    BottomNavigationMenu(
                        selectedItem = BottomNavigationItem.CHATLIST,
                        navController = navController
                    )
                }
            }
        )
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAB(
    showDialog: Boolean,
    onFabClick: () -> Unit,
    onDissmiss: () -> Unit,
    onAddChat: ((String) -> Unit)
) {
    val addChatNumber = remember {
        mutableStateOf("")
    }
    if (showDialog) {
        AlertDialog(onDismissRequest = {
            onDissmiss.invoke()
            addChatNumber.value = ""
        },
            confirmButton = {
                Button(onClick = {
                    onAddChat(addChatNumber.value)
                }) {
                    Text(text = "Add Chat")
                }
            }, title = { Text(text = "Add Chat") },
            text = {
                OutlinedTextField(
                    value = addChatNumber.value, onValueChange = { addChatNumber.value = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
        )




    }
    FloatingActionButton(
        onClick =  onFabClick ,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 39.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.AddCircle,
            contentDescription = null,
            tint = Color.White
        )
    }

}

