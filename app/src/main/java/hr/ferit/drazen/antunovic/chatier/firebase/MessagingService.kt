package hr.ferit.drazen.antunovic.chatier.firebase

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.repository.Repository
import hr.ferit.drazen.antunovic.chatier.repository.RepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagingService(private val repository: Repository = RepositoryImpl()): FirebaseMessagingService() {
    private val _deviceTokenInsert: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val deviceTokenInsert = _deviceTokenInsert.asStateFlow()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if(Firebase.auth.currentUser != null) {
            scope.launch {
                repository.insertDeviceToken(uid = Firebase.auth.currentUser!!.uid, token = token).collect{
                    _deviceTokenInsert.value = it
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
