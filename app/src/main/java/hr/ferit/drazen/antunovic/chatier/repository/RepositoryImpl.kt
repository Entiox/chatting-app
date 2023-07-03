package hr.ferit.drazen.antunovic.chatier.repository

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.snapshots
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import hr.ferit.drazen.antunovic.chatier.data.*
import hr.ferit.drazen.antunovic.chatier.firebase.FirebaseInstances
import hr.ferit.drazen.antunovic.chatier.service.NotificationService
import hr.ferit.drazen.antunovic.chatier.service.NotificationServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class RepositoryImpl(private val service: NotificationService = NotificationServiceImpl()) :
    Repository {
    @RequiresApi(Build.VERSION_CODES.O)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy.")

    override fun signIn(email: String, password: String): Flow<Result<AuthResult>> {
        return flow {
            emit(Result.Loading())
            val response =
                Firebase.auth.signInWithEmailAndPassword(email.trim(), password)
                    .await()
            emit(Result.Success(response))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun signUp(email: String, password: String): Flow<Result<AuthResult>> {
        return flow {
            emit(Result.Loading())
            val response =
                Firebase.auth.createUserWithEmailAndPassword(email.trim(), password)
                    .await()
            emit(Result.Success(response))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun uploadImage(imageUri: Uri): Flow<Result<String>> {
        return flow {
            emit(Result.Loading())
            val path = "images/${UUID.randomUUID()}"
            Firebase.storage.reference.child(path).putFile(imageUri).await()
            emit(Result.Success(path))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun insertUser(
        firstName: String,
        lastName: String,
        imagePath: String,
        uid: String
    ): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("users")
                .child(uid)
                .setValue(
                    User(
                        firstName = firstName,
                        lastName = lastName,
                        fullName = "$firstName $lastName",
                        imagePath = imagePath
                    )
                ).await()
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun updateUser(
        firstName: String?,
        lastName: String?,
        imagePath: String?,
        originalFirstName: String,
        originalLastName: String,
        uid: String
    ): Flow<Result<Nothing>> {
        suspend fun <T> updateUserInFirebase(path: String, value: T) {
            FirebaseInstances.getDatabase().getReference("users")
                .child(uid)
                .child(path)
                .setValue(value)
                .await()
        }
        return flow {
            emit(Result.Loading())
            if (firstName != null) {
                updateUserInFirebase(path = "firstName", value = firstName)
                updateUserInFirebase(path = "fullName", value = "$firstName $originalLastName")
            }
            if (lastName != null) {
                updateUserInFirebase(path = "lastName", lastName)
                if (firstName == null) {
                    updateUserInFirebase(path = "fullName", value = "$originalFirstName $lastName")
                } else {
                    updateUserInFirebase(path = "fullName", value = "$firstName $lastName")
                }
            }
            if (imagePath != null) {
                updateUserInFirebase(path = "imagePath", value = imagePath)
            }
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun fetchUserOnce(uid: String): Flow<Result<User>> {
        return flow {
            emit(Result.Loading())
            val result = FirebaseInstances.getDatabase().getReference("users")
                .child(uid).get().await()
            emit(Result.Success(data = result.getValue<User>()))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun deleteUser(uid: String): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("users")
                .child(uid).removeValue().await()
            emit(Result.Success(data = null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun fetchUser(uid: String): Flow<Result<out User>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("users")
                .child(uid).snapshots.collect {
                    if (it.value == null) {
                        emit(Result.Success(null))
                        return@collect
                    }
                    val user = it.getValue<User>()!!.copy(
                        imagePath = Firebase.storage.reference.child(it.getValue<User>()!!.imagePath).downloadUrl.await()
                            .toString()
                    )
                    emit(Result.Success(user))
                }
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun insertDeviceToken(uid: String, token: String): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("tokens").child(uid).setValue(token)
                .await()
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun fetchDeviceToken(uid: String): Flow<Result<out String>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("tokens").child(uid).snapshots.collect {
                if (it.value == null) {
                    emit(Result.Success(null))
                    return@collect
                }
                val token = it.getValue<String>()
                emit(Result.Success(token))
            }
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun deleteDeviceToken(uid: String): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("tokens").child(uid).removeValue().await()
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun sendNotification(
        token: String,
        title: String,
        body: String
    ): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            service.sendNotification(deviceToken = token, title = title, body = body)
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun addFriend(uid: String, friendUid: String): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("friends").child(uid).child(friendUid)
                .setValue(friendUid).await()
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun removeFriend(uid: String, friendUid: String): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("friends").child(uid).child(friendUid)
                .removeValue().await()
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun fetchChats(
        uid: String,
        scope: CoroutineScope,
    ): Flow<Result<List<KeyedUserWithLastMessage>>> {
        return callbackFlow {
            val jobs = mutableListOf<Job?>()
            val mutex = Mutex()
            val chats = mutableListOf<KeyedUserWithLastMessage>()
            trySend(Result.Loading())
            FirebaseInstances.getDatabase().getReference("chats")
                .child(uid).snapshots.collect {
                    jobs.forEach { job ->
                        job?.cancel()
                    }
                    jobs.clear()
                    chats.clear()
                    if (it.value == null) {
                        trySend(Result.Success(chats.toList()))
                        return@collect
                    }
                    it.children.forEach { chat ->
                        jobs.add(null)
                        jobs[jobs.indexOf(jobs.last())] = scope.launch {
                            FirebaseInstances.getDatabase().getReference("messages").child(uid)
                                .child(chat.key!!)
                                .limitToLast(1).snapshots.collect { messagesSnapshot ->
                                    if (messagesSnapshot.value == null) {
                                        mutex.withLock {
                                            if (chats.any { listItem -> listItem.uid == chat.key!! }) {
                                                val index = chats.indexOf(
                                                    chats
                                                        .first { listItem -> listItem.uid == chat.key })
                                                chats[index] = chats[index].copy(
                                                    lastMessage = "",
                                                )
                                            }
                                        }
                                    } else {
                                        val message =
                                            messagesSnapshot.children.mapNotNull { message -> message.getValue<Message>()!! }
                                        mutex.withLock {
                                            if (chats.none { listItem -> listItem.uid == chat.key }) {
                                                chats.add(
                                                    KeyedUserWithLastMessage(
                                                        uid = chat.key!!,
                                                        lastMessage = if (message[0].senderUid == uid
                                                        ) {
                                                            "You: " + message[0].content
                                                        } else {
                                                            message[0].content
                                                        }, lastMessageTimeStamp = message[0].timeStamp
                                                    )
                                                )
                                            } else {
                                                val index = chats.indexOf(chats.first { listItem -> listItem.uid == chat.key })
                                                chats[index] =
                                                    chats[index].copy(
                                                        lastMessage = if (message[0].senderUid == uid
                                                        ) {
                                                            "You: " + message[0].content
                                                        } else {
                                                            message[0].content
                                                        }, lastMessageTimeStamp = message[0].timeStamp
                                                    )
                                            }
                                        }
                                    }
                                    trySend(
                                        Result.Success(
                                            chats.toList().sortedByDescending { chat1 ->
                                                LocalDateTime.parse(
                                                    chat1.lastMessageTimeStamp,
                                                    dateTimeFormatter
                                                )
                                            })
                                    )
                                }
                        }

                        jobs.add(null)
                        jobs[jobs.indexOf(jobs.last())] = scope.launch {
                            FirebaseInstances.getDatabase().getReference("users")
                                .child(chat.key!!).snapshots.collect { userSnapshot ->
                                    if (userSnapshot.value == null) {
                                        mutex.withLock {
                                            if (chats.any { listItem -> listItem.uid == chat.key!! }) {
                                                val index = chats.indexOf(
                                                    chats
                                                        .first { listItem -> listItem.uid == chat.key })
                                                chats[index] = chats[index].copy(
                                                    firstName = "",
                                                    lastName = "",
                                                    fullName = "Deleted user",
                                                    imagePath = ""
                                                )
                                            }
                                            else {
                                                chats.add(
                                                    KeyedUserWithLastMessage(
                                                        uid = chat.key!!,
                                                        firstName = "",
                                                        lastName = "",
                                                        fullName = "Deleted user",
                                                        imagePath = "",
                                                    )
                                                )
                                            }
                                        }
                                    } else {
                                        val user = userSnapshot.getValue<User>()!!
                                        mutex.withLock {
                                            if (chats.none { listItem -> listItem.uid == chat.key }) {
                                                chats.add(
                                                    KeyedUserWithLastMessage(
                                                        uid = chat.key!!,
                                                        firstName = user.firstName,
                                                        lastName = user.lastName,
                                                        fullName = user.fullName,
                                                        imagePath = Firebase.storage.reference.child(
                                                            user.imagePath
                                                        ).downloadUrl.await()
                                                            .toString(),
                                                    )
                                                )
                                            } else {
                                                val index = chats.indexOf(chats.first { listItem -> listItem.uid == chat.key })
                                                chats[index] =
                                                    chats[index].copy(
                                                        firstName = user.firstName,
                                                        lastName = user.lastName,
                                                        fullName = user.fullName,
                                                        imagePath = Firebase.storage.reference.child(
                                                            user.imagePath
                                                        ).downloadUrl.await()
                                                            .toString(),
                                                    )
                                            }
                                        }
                                    }
                                    trySend(
                                        Result.Success(
                                            chats.toList().sortedByDescending { chat1 ->
                                                LocalDateTime.parse(
                                                    chat1.lastMessageTimeStamp,
                                                    dateTimeFormatter
                                                )
                                            })
                                    )
                                }
                        }
                    }
                }
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun fetchFriends(uid: String, scope: CoroutineScope): Flow<Result<List<KeyedUser>>> {
        return callbackFlow {
            val jobs = mutableListOf<Job?>()
            val mutex = Mutex()
            val friends = mutableListOf<KeyedUser>()
            trySend(Result.Loading())
            FirebaseInstances.getDatabase().getReference("friends")
                .child(uid).snapshots.collect {
                    jobs.forEach { job ->
                        job?.cancel()
                    }
                    jobs.clear()
                    friends.clear()
                    if (it.value == null) {
                        trySend(Result.Success(friends.toList()))
                        return@collect
                    }
                    it.children.forEach { friend ->
                        jobs.add(null)
                        jobs[jobs.indexOf(jobs.last())] = scope.launch {
                            FirebaseInstances.getDatabase().getReference("users")
                                .child(friend.getValue<String>()!!).snapshots.collect { userSnapshot ->
                                    if (userSnapshot.value == null) {
                                        if (friends.any { listItem -> listItem.uid == friend.getValue<String>()!! }) {
                                            friends.removeAt(friends.indexOf(friends.find { listItem -> listItem.uid == friend.getValue<String>()!! }))
                                        }
                                    } else {
                                        val user = userSnapshot.getValue<User>()!!
                                        val keyedUser = KeyedUser(
                                            uid = userSnapshot.key!!,
                                            firstName = user.firstName,
                                            lastName = user.lastName,
                                            fullName = user.fullName,
                                            imagePath = Firebase.storage.reference.child(user.imagePath).downloadUrl.await()
                                                .toString(),
                                        )
                                        mutex.withLock {
                                            if (friends.none { friend -> friend.uid == userSnapshot.key }) {
                                                friends.add(keyedUser)
                                            } else {
                                                friends[friends.indexOf(friends.first { friend -> friend.uid == userSnapshot.key })] =
                                                    keyedUser
                                            }
                                        }
                                    }
                                    trySend(
                                        Result.Success(
                                            friends.toList()
                                                .sortedBy { friend1 -> friend1.fullName })
                                    )
                                }
                        }
                    }
                }
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun fetchUsers(
        uid: String,
        fullName: String,
        scope: CoroutineScope
    ): Flow<Result<List<KeyedUser>>> {
        return callbackFlow {
            var job: Job? = null
            trySend(Result.Loading())
            FirebaseInstances.getDatabase().getReference("friends")
                .child(uid).snapshots.collect { friendsSnapshot ->
                    job?.cancel()
                    val friends = friendsSnapshot.children.mapNotNull {
                        it.getValue<String>()
                    }
                    job = scope.launch {
                        FirebaseInstances.getDatabase().getReference("users")
                            .orderByChild("fullName").startAt(fullName.trim())
                            .endAt(fullName.trim() + "\uf8ff").limitToFirst(50)
                            .snapshots.collect { userSnapshot ->
                                val users = userSnapshot.children.mapNotNull {
                                    val user = it.getValue<User>()
                                    KeyedUser(
                                        uid = it.key!!,
                                        firstName = user!!.firstName,
                                        lastName = user.lastName,
                                        fullName = user.fullName,
                                        imagePath = Firebase.storage.reference.child(
                                            user.imagePath
                                        ).downloadUrl.await().toString(),
                                    )
                                }
                                trySend(Result.Success(users.filter { user ->
                                    friends.none { user.uid == it } && user.uid != uid
                                }.sortedBy { user -> user.fullName }))
                            }
                    }
                }
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun insertChat(
        uid: String,
        participantUid: String
    ): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("chats").child(uid).child(participantUid)
                .setValue(participantUid)
                .await()
            FirebaseInstances.getDatabase().getReference("chats").child(participantUid).child(uid)
                .setValue(uid)
                .await()
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun fetchMessages(
        uid: String,
        participantUid: String
    ): Flow<Result<out List<Message>>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("messages").child(uid)
                .child(participantUid).limitToLast(100).snapshots.collect { chatSnapshot ->
                    val messages = chatSnapshot.children.mapNotNull { it.getValue<Message>() }
                    emit(Result.Success(messages.reversed()))
                }
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun insertMessage(
        uid: String,
        participantUid: String,
        content: String
    ): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("messages").child(uid)
                .child(participantUid)
                .push()
                .setValue(Message(dateTimeFormatter.format(LocalDateTime.now()), content, uid))
                .await()
            FirebaseInstances.getDatabase().getReference("messages").child(participantUid)
                .child(uid)
                .push()
                .setValue(Message(dateTimeFormatter.format(LocalDateTime.now()), content, uid))
                .await()
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun deleteChat(uid: String, participantUid: String): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("chats").child(uid).child(participantUid)
                .removeValue()
                .await()
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }

    override fun deleteMessages(uid: String, participantUid: String): Flow<Result<Nothing>> {
        return flow {
            emit(Result.Loading())
            FirebaseInstances.getDatabase().getReference("messages").child(uid)
                .child(participantUid)
                .removeValue()
                .await()
            emit(Result.Success(null))
        }.catch { e ->
            emit(
                Result.Error(
                    information = if (e.message != null) e.message.toString() else "Unknown error"
                )
            )
        }
    }
}

