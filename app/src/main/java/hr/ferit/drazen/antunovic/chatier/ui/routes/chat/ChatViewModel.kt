package hr.ferit.drazen.antunovic.chatier.ui.routes.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.viewmodel.DefaultViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(private val participantUid: String) : DefaultViewModel() {
    private val _signOut: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val signOut = _signOut.asStateFlow()

    private val _deviceTokenDeletion: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val deviceTokenDeletion = _deviceTokenDeletion.asStateFlow()

    private val _messaging: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val messaging = _messaging.asStateFlow()

    private val _messageInsertion: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val messageInsertion = _messageInsertion.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    val chat = repository.fetchChat(
        uid = Firebase.auth.currentUser!!.uid,
        personUid = participantUid,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Result.Waiting(),
    )

    val participant = repository.fetchUser(uid = participantUid).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Result.Waiting(),
    )

    val currentUser = repository.fetchUser(uid = Firebase.auth.currentUser!!.uid).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Result.Waiting(),
    )

    val deviceToken =
        repository.fetchDeviceToken(uid = participantUid).stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = Result.Waiting(),
        )

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertMessage(content: String) {
        viewModelScope.launch {
            _messaging.value = Result.Loading()
            repository.insertMessage(
                uid = Firebase.auth.currentUser!!.uid,
                personUid = participantUid,
                content = content
            ).collect {
                _messageInsertion.value = it
            }
            if (_messageInsertion.value is Result.Error) {
                _messaging.value =
                    Result.Error(information = _messageInsertion.value.information!!)
                return@launch
            }
            if (deviceToken.value.data != null) {
                repository.sendNotification(
                    token = deviceToken.value.data!!,
                    title = currentUser.value.data!!.fullName + " sent you a message",
                    body = if (content.length <= 30) content else (content.substring(startIndex = 0, endIndex = 30) + "..."),
                ).collect {
                    _messaging.value = it
                }
            } else {
                _messaging.value = Result.Success(data = null)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _signOut.value = Result.Loading()
            repository.deleteDeviceToken(uid = Firebase.auth.currentUser!!.uid).collect {
                _deviceTokenDeletion.value = it
            }
            if (_deviceTokenDeletion.value is Result.Error) {
                _signOut.value =
                    Result.Error(information = _deviceTokenDeletion.value.information!!)
                return@launch
            }
            Firebase.auth.signOut()
            _signOut.value = Result.Success(data = null)
        }
    }

    override fun refresh() {
        _deviceTokenDeletion.value = Result.Waiting()
        _messaging.value = Result.Waiting()
        _messageInsertion.value = Result.Waiting()
        _signOut.value = Result.Waiting()
    }
}
