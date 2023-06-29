package hr.ferit.drazen.antunovic.chatier.ui.components

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.viewmodel.DefaultViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatItemViewModel : DefaultViewModel() {
    private val _chatDeletion: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val chatDeletion = _chatDeletion.asStateFlow()

    fun deleteChat(personUid: String) {
        viewModelScope.launch {
            repository.deleteChat(uid = Firebase.auth.currentUser!!.uid, personUid = personUid).collect {
                _chatDeletion.value = it
            }
        }
    }

    override fun refresh() {
        _chatDeletion.value = Result.Waiting()
    }
}
