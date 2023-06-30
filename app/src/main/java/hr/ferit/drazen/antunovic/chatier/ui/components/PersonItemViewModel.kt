package hr.ferit.drazen.antunovic.chatier.ui.components

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.viewmodel.DefaultViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonItemViewModel : DefaultViewModel() {
    private val _friendRequest: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val friendRequest = _friendRequest.asStateFlow()

    fun addFriend(uidOfPersonWhoReceivesFriendshipRequest: String) {
        viewModelScope.launch {
            repository.addFriend(
                uid = Firebase.auth.currentUser!!.uid,
                friendUid = uidOfPersonWhoReceivesFriendshipRequest,
            ).collect {
                _friendRequest.value = it
            }
        }
    }

    override fun refresh() {
        _friendRequest.value = Result.Loading()
    }
}
