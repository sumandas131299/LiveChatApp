package com.example.livechatapp.Screeens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.livechatapp.CheckSignedIn
import com.example.livechatapp.CommonProgressBar
import com.example.livechatapp.DestinationScreen
import com.example.livechatapp.LCViewModel
import com.example.livechatapp.R
import com.example.livechatapp.navigateTo

@Composable
fun SignUpScreen(navController: NavHostController, vm: LCViewModel) {
    CheckSignedIn(vm,navController)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight()
                .verticalScroll(
                    rememberScrollState()
                ),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val nameState = remember {
                mutableStateOf(TextFieldValue())
            }
            val numberState = remember {
                mutableStateOf(TextFieldValue())
            }
            val emailState = remember {
                mutableStateOf(TextFieldValue())
            }
            val passwordState = remember {
                mutableStateOf(TextFieldValue())
            }


            val focus = LocalFocusManager.current
            Image(
                painter = painterResource(id = R.drawable.chat), contentDescription = null,
                modifier = Modifier
                    .width(200.dp)
                    .padding(top = 16.dp)
                    .padding(8.dp)
            )


            Text(
                text = "Sign Up",
                fontSize = 40.sp,
                fontFamily = FontFamily.SansSerif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )


            OutlinedTextField(value = nameState.value,
                onValueChange = {
                    nameState.value = it
                }, label = { Text(text = "NAME") }, modifier = Modifier.padding(9.dp)
            )


            OutlinedTextField(value = numberState.value,
                onValueChange = {
                    numberState.value = it
                }, label = { Text(text = "NUMBER") }, modifier = Modifier.padding(9.dp)
            )
            OutlinedTextField(value = emailState.value, onValueChange = {
                emailState.value = it
            }, label = { Text(text = "EMAIL") }, modifier = Modifier.padding(9.dp))
            OutlinedTextField(value = passwordState.value, onValueChange = {
                passwordState.value = it
            }, label = { Text(text = "PASSWORD") }, modifier = Modifier.padding(9.dp))



            Button(
                onClick = {
                    vm.signUp(
                        name = nameState.value.text,
                        number = numberState.value.text,
                        email = emailState.value.text,
                        password = passwordState.value.text
                    )
                },
                modifier = Modifier.padding(9.dp)
            ) {
                Text(text = "SIGN UP")
            }

            Text(text = "Already a user ? Go to login->",
                color = Color.Blue,
                modifier = Modifier
                    .padding(9.dp)
                    .clickable {
                        navigateTo(navController, DestinationScreen.Login.route)

                    }
            )
        }

    }
    if (vm.inProcess.value) {
        CommonProgressBar()
    }
}

