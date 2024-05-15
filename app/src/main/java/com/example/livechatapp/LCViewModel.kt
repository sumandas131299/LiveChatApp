package com.example.livechatapp

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.livechatapp.Data.CHATS
import com.example.livechatapp.Data.ChatData
import com.example.livechatapp.Data.ChatUser
import com.example.livechatapp.Data.Event
import com.example.livechatapp.Data.MESSAGE
import com.example.livechatapp.Data.Message
import com.example.livechatapp.Data.STATUS
import com.example.livechatapp.Data.Status
import com.example.livechatapp.Data.USER_NODE
import com.example.livechatapp.Data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage

import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.FileFilter
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LCViewModel @Inject constructor(
    val auth: FirebaseAuth, var db: FirebaseFirestore, val storage: FirebaseStorage
) : ViewModel() {


    var inProcess = mutableStateOf(false)
    var inProcessChat = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn = mutableStateOf(false)
    var userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessage = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration? = null

    val status = mutableStateOf<List<Status>>(listOf())
    var inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun signUp(name: String, number: String, email: String, password: String) {
        inProcess.value = true
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please Fill All fields")
            return
        }
        inProcess.value = true
        db.collection(USER_NODE).whereEqualTo("number", number).get().addOnSuccessListener {
            if (it.isEmpty) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        signIn.value = true
                        createOrUpdateProfile(name, number)
                        Log.d("TAG", "signup: User Logged IN")
                    } else {
                        handleException(it.exception, customMessage = "SignUp failed")
                    }
                }
            } else {
                handleException(customMessage = "Number Already Exits")
                inProcess.value = false
            }
        }


    }

    fun populateChats() {
        inProcessChat.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProcessChat.value = false
            }
        }


    }

    fun populateMessage(chatId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener =
            db.collection(CHATS).document(chatId).collection(MESSAGE).addSnapshotListener {
                    value, error ->
                if (error != null)
                    handleException(error)
                if (value != null) {
                    chatMessage.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timestamp }
                    inProgressChatMessage.value = false
                }


            }
    }

    fun depopulatedMessage(){
        chatMessage.value= listOf()
        currentChatMessageListener=null
    }

    fun onSendReply(chatId: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.userId, message, time)
        db.collection(CHATS).document(chatId).collection(MESSAGE).document().set(msg)
    }

    fun loginIn(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please Fill the all Fields")
            return
        } else {
            inProcess.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    signIn.value = true
                    inProcess.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                } else {
                    handleException(exception = it.exception, customMessage = " Login Failed")
                }
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageurl = it.toString())

        }
    }

    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProcess.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri).addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
            inProcess.value = false
        }.addOnFailureListener {
            handleException(it)
        }
    }

    fun createOrUpdateProfile(
        name: String? = null, number: String? = null, imageurl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val currentUserData = userData.value
        val updatedUserData = UserData(
            userId = uid,
            name = name ?: currentUserData?.name,
            number = number ?: currentUserData?.number,
            imageUrl = imageurl ?: currentUserData?.imageUrl
        )
        uid?.let { uid ->
            inProcess.value = true
            db.collection(USER_NODE).document(uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Document exists, update it
                    db.collection(USER_NODE).document(uid).set(updatedUserData)
                        .addOnSuccessListener {
                            // Update the local state after successful update
                            userData.value = updatedUserData
                            inProcess.value = false
                        }.addOnFailureListener { exception ->
                            handleException(exception, "Failed to update user data")
                        }
                } else {
                    // Document does not exist, create a new one
                    db.collection(USER_NODE).document(uid).set(updatedUserData)
                        .addOnSuccessListener {
                            userData.value = updatedUserData
                            inProcess.value = false
                        }.addOnFailureListener { exception ->
                            handleException(exception, "Failed to create user data")
                        }
                }
            }.addOnFailureListener { exception ->
                handleException(exception, "Cannot retrieve user")
            }
        }
    }

    private fun getUserData(uid: String) {
        inProcess.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, " Cannot retrieve User")
            }
            if (value != null) {
                var user = value.toObject<UserData>()
                userData.value = user
                inProcess.value = false
                populateChats()
                populateStatuses()
            }
        }
    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("ChatApplication", "Live chat exception", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isNullOrEmpty()) errorMsg else customMessage
        eventMutableState.value = Event(message)
        inProcess.value = false

    }

    fun logOut() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        depopulatedMessage()
        currentChatMessageListener=null
        eventMutableState.value = Event("Logged Out")
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() or !number.isDigitsOnly()) handleException(customMessage = "The Number must contain all Digits")
        else {
            db.collection(CHATS).where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.number", number),
                        Filter.equalTo("user2.number", userData.value?.number)
                    ), Filter.and(
                        Filter.equalTo("user2.number", number),
                        Filter.equalTo("user1.number", userData.value?.number)
                    )
                )
            ).get().addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection(USER_NODE).whereEqualTo("number", number).get()
                        .addOnSuccessListener {
                            if (it.isEmpty) handleException(customMessage = "number not found")
                            else {
                                val chatPartner = it.toObjects<UserData>()[0]
                                val id = db.collection(CHATS).document().id
                                val chat = ChatData(
                                    chatId = id, ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.imageUrl,
                                        userData.value?.number
                                    ),

                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.imageUrl,
                                        chatPartner.number
                                    )
                                )

                                db.collection(CHATS).document(id).set(chat)

                            }
                        }.addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(customMessage = "chat already exist")
                }
            }
        }

    }

    fun uploadStatus(uri: Uri) {
        uploadImage(uri) { imageUrl ->
            if (imageUrl != null) {
                createStatus(imageUrl)
            } else {
                handleException(customMessage = "Failed to upload image for status")
            }
        }
    }
    fun createStatus(imageUrl: Uri) {
        val currentUserData = userData.value
        if (currentUserData != null) {
            val newStatus = Status(
                user = ChatUser(
                    currentUserData.userId,
                    currentUserData.name,
                    currentUserData.imageUrl,
                    currentUserData.number
                ), imageUrl = imageUrl.toString(), timestamp = System.currentTimeMillis()
            )
            db.collection(STATUS).add(newStatus).addOnFailureListener {
                handleException(it)
            }
        } else {
            handleException(customMessage = "User data is null")
        }
    }

    fun populateStatuses(){
        val timeDelta = 24L*60*60*1000
        val cutOff = System.currentTimeMillis()-timeDelta
        inProgressStatus.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId" , userData.value?.userId),
                Filter.equalTo("user2.userId" , userData.value?.userId)

            )
        ).addSnapshotListener{
            value,error->
            if(error!=null) handleException(error)
            if(value!=null){
               val currentConnections = arrayListOf(userData.value?.userId)

                val chats = value.toObjects<ChatData>()

                chats.forEach{
                    chat->
                    if(chat.user1.userId == userData.value?.userId){
                        currentConnections.add(chat.user2.userId)
                    }
                    else
                        currentConnections.add(chat.user1.userId)

                }
                db.collection(STATUS).whereGreaterThan("timestamp",cutOff).whereIn("user.userId" , currentConnections).addSnapshotListener{
                    value , error->
                    if(error!=null) handleException(error)
                    if(value!=null){
                        status.value = value.toObjects()
                        inProgressStatus.value = false
                    }
                }

            }
        }
    }


}