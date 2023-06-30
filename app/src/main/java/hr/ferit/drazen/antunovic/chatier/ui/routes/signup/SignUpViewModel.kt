package hr.ferit.drazen.antunovic.chatier.ui.routes.signup

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.viewmodel.DefaultViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpViewModel : DefaultViewModel() {
    private val _auth: MutableStateFlow<Result<AuthResult>> = MutableStateFlow(Result.Waiting())
    val auth: StateFlow<Result<AuthResult>> = _auth.asStateFlow()

    fun signUp(email: String, password: String){
        viewModelScope.launch {
            repository.signUp(email = email, password = password).collect{
                _auth.value = it
            }
        }
    }

    override fun refresh(){
        _auth.value = Result.Waiting()
    }
}
