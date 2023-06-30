package hr.ferit.drazen.antunovic.chatier.ui.components

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.viewmodel.DefaultViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FriendItemViewModel : DefaultViewModel() {
    private val _friendRemoval: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val friendRemoval = _friendRemoval.asStateFlow()

    fun removeFriend(uidOfFriend: String) {
        viewModelScope.launch {
            repository.removeFriend(uid = Firebase.auth.currentUser!!.uid, friendUid = uidOfFriend).collect {
                _friendRemoval.value = it
            }
        }
    }

    override fun refresh() {
        _friendRemoval.value = Result.Loading()
    }
}
