package hr.ferit.drazen.antunovic.chatier.ui.routes.signin

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.data.User
import hr.ferit.drazen.antunovic.chatier.viewmodel.DefaultViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewModel : DefaultViewModel() {
    private val _auth: MutableStateFlow<Result<AuthResult>> =
        MutableStateFlow(Result.Waiting())
    val auth = _auth.asStateFlow()

    private val _deviceTokenInsertion: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val deviceTokenInsertion = _deviceTokenInsertion.asStateFlow()

    private val _signIn: MutableStateFlow<Result<User>> = MutableStateFlow(Result.Waiting())
    val signIn = _signIn.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _signIn.value = Result.Loading()
            repository.signIn(email = email, password = password).collect {
                _auth.value = it
            }
            if (_auth.value is Result.Error) {
                _signIn.value = Result.Error(information = _auth.value.information!!)
                return@launch
            }
            repository.fetchUserOnce(uid = Firebase.auth.currentUser!!.uid).collect {
                _signIn.value = it
            }
        }
    }

    fun insertDeviceToken() {
        viewModelScope.launch {
            val token = Firebase.messaging.token.await()
            repository.insertDeviceToken(uid = Firebase.auth.currentUser!!.uid, token = token).collect {
                _deviceTokenInsertion.value = it
            }
        }
    }

    override fun refresh() {
        _auth.value = Result.Waiting()
        _signIn.value = Result.Waiting()
        _deviceTokenInsertion.value = Result.Waiting()
    }
}
