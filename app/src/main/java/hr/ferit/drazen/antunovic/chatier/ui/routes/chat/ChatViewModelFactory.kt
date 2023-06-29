package hr.ferit.drazen.antunovic.chatier.ui.routes.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatViewModelFactory(private val personUid: String): ViewModelProvider.NewInstanceFactory() {
     @Suppress("UNCHECKED_CAST")
     override fun <T : ViewModel> create(modelClass: Class<T>): T = ChatViewModel(personUid) as T
}
