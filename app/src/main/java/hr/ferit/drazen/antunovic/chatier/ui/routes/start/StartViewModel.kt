package hr.ferit.drazen.antunovic.chatier.ui.routes.start

import androidx.lifecycle.viewModelScope
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.data.User
import hr.ferit.drazen.antunovic.chatier.viewmodel.DefaultViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StartViewModel : DefaultViewModel() {
    private val _user: MutableStateFlow<Result<User>> = MutableStateFlow(Result.Waiting())
    val user = _user.asStateFlow()

    fun fetchUser(uid: String){
        viewModelScope.launch {
            repository.fetchUserOnce(uid).collect{
                _user.value = it
            }
        }
    }

    fun refreshUserResult(){
        _user.value = Result.Waiting()
    }

    override fun refresh(){
        _user.value = Result.Waiting()
    }
}
