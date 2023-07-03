package hr.ferit.drazen.antunovic.chatier.repository

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.AuthResult
import hr.ferit.drazen.antunovic.chatier.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface Repository {
    fun signIn(email: String, password: String): Flow<Result<AuthResult>>

    fun signUp(email: String, password: String): Flow<Result<AuthResult>>

    fun uploadImage(imageUri: Uri): Flow<Result<String>>

    fun insertUser(firstName: String, lastName: String, imagePath: String, uid: String): Flow<Result<Nothing>>

    fun updateUser(firstName: String? = null, lastName: String? = null, imagePath: String? = null,
        originalFirstName: String, originalLastName: String, uid: String): Flow<Result<Nothing>>

    fun fetchUserOnce(uid: String): Flow<Result<User>>

    fun deleteUser(uid: String): Flow<Result<Nothing>>

    fun fetchUser(uid: String): Flow<Result<out User>>

    fun insertDeviceToken(uid: String, token: String): Flow<Result<Nothing>>

    fun fetchDeviceToken(uid: String): Flow<Result<out String>>

    fun deleteDeviceToken(uid: String): Flow<Result<Nothing>>

    fun sendNotification(token: String, title: String, body: String): Flow<Result<Nothing>>

    fun addFriend(uid: String, friendUid: String): Flow<Result<Nothing>>

    fun removeFriend(uid: String, friendUid: String): Flow<Result<Nothing>>

    fun fetchChats(uid: String, scope: CoroutineScope): Flow<Result<List<KeyedUserWithLastMessage>>>

    fun fetchFriends(uid: String, scope: CoroutineScope): Flow<Result<List<KeyedUser>>>

    fun fetchUsers(uid: String, fullName: String, scope: CoroutineScope): Flow<Result<List<KeyedUser>>>

    fun insertChat(uid: String, participantUid: String): Flow<Result<Nothing>>

    fun fetchMessages(uid: String, participantUid: String): Flow<Result<out List<Message>>>

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertMessage(uid: String, participantUid: String, content: String): Flow<Result<Nothing>>

    fun deleteChat(uid: String, participantUid: String): Flow<Result<Nothing>>

    fun deleteMessages(uid: String, participantUid: String): Flow<Result<Nothing>>
}

