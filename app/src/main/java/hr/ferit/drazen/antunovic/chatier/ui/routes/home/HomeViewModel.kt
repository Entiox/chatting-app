package hr.ferit.drazen.antunovic.chatier.ui.routes.home

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hr.ferit.drazen.antunovic.chatier.data.KeyedUser
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.viewmodel.DefaultViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel : DefaultViewModel() {
    private val _users: MutableStateFlow<Result<List<KeyedUser>>> =
        MutableStateFlow(Result.Waiting())
    val users = _users.asStateFlow()

    private val _signOut: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val signOut = _signOut.asStateFlow()

    private val _deviceTokenDeletion: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val deviceTokenDeletion = _deviceTokenDeletion.asStateFlow()

    val chats = repository.fetchChats(uid = Firebase.auth.currentUser!!.uid, scope = viewModelScope).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Result.Waiting()
    )

    val friends = repository.fetchFriends(uid = Firebase.auth.currentUser!!.uid, scope = viewModelScope).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Result.Waiting()
    )

    private var userFetchJob: Job? = null

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

    fun fetchUsers(fullName: String) {
        userFetchJob?.cancel()
        userFetchJob = viewModelScope.launch {
            repository.fetchUsers(uid = Firebase.auth.currentUser!!.uid, fullName = fullName, scope = viewModelScope)
                .collect {
                    _users.value = it
                }
        }
    }

    fun resetSearch() {
        userFetchJob?.cancel()
        _users.value = Result.Waiting()
    }

    override fun refresh() {
        _deviceTokenDeletion.value = Result.Waiting()
        _users.value = Result.Waiting()
        _signOut.value = Result.Waiting()
    }
}
