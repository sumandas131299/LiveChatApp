package com.example.livechatapp.Screeens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.livechatapp.CommonDivider
import com.example.livechatapp.CommonProgressBar
import com.example.livechatapp.CommonRow
import com.example.livechatapp.DestinationScreen
import com.example.livechatapp.LCViewModel
import com.example.livechatapp.TitleText
import com.example.livechatapp.navigateTo

@Composable
fun StatusScreen(navController: NavController, vm: LCViewModel) {
    val inProcess = vm.inProgressStatus.value
    if (inProcess) {
        CommonProgressBar()
    } else {
        val statuses = vm.status.value
        val userData = vm.userData.value

        val myStatuses = statuses.filter {
            it.user.userId == userData?.userId
        }
        val otherStatuses = statuses.filter {
            it.user.userId != userData?.userId
        }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        uri ->
        uri?.let {
            vm.uploadStatus(uri)
        }
    }
        Scaffold(
            floatingActionButton = {
                FAB {
                    launcher.launch("image/s")
                }
            }, content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(it)
                ) {
                    TitleText(txt = "Status")
                    if (statuses.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Statuses available")
                        }
                    }
                    else{
                        if(myStatuses.isNotEmpty()){
                            CommonRow(imageUrl = myStatuses[0].user.imageUrl, name =myStatuses[0].user.name ) {
                                navigateTo(navController,DestinationScreen.SingleStatus.createRoute(myStatuses[0].user.userId!!))
                            }
                            CommonDivider()
                            val uniqueUser = otherStatuses.map { it.user }.toSet().toList()
                            LazyColumn (modifier = Modifier.weight(1f)){

                                items(uniqueUser){
                                    user ->
                                    CommonRow(imageUrl = user.imageUrl, name = user.name) {
                                        navigateTo(navController , DestinationScreen.SingleStatus.createRoute(user.userId!!))
                                    }
                                }
                            }

                        }

                    }
                    BottomNavigationMenu(
                        selectedItem = BottomNavigationItem.STATUSLIST,
                        navController = navController
                    )
                }

            }
        )



    }

}

@Composable
fun FAB(
    onFabClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "ADD STATUS",
            tint = Color.White
        )

    }
}