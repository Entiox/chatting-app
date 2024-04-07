package hr.ferit.drazen.antunovic.chatier.ui.routes.fill_personal_information

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.viewmodel.DefaultViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FillIPersonalInformationViewModel : DefaultViewModel() {
    private val _imageUpload: MutableStateFlow<Result<String>> = MutableStateFlow(Result.Waiting())
    val imageUpload = _imageUpload.asStateFlow()

    private val _userInsertion: MutableStateFlow<Result<Nothing>> = MutableStateFlow(Result.Waiting())
    val userInsertion = _userInsertion.asStateFlow()

    private val _accountCreation: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val accountCreation = _accountCreation.asStateFlow()

    fun insertUser(imageUri: Uri?, firstName: String, lastName: String) {
        viewModelScope.launch {
            _accountCreation.value = Result.Loading()
            var imagePath = "images/default"
            if (imageUri != null) {
                repository.uploadImage(imageUri = imageUri).collect {
                    if (it is Result.Success) {
                        imagePath = it.data!!
                    }
                    _imageUpload.value = it
                }
            }
            if (_imageUpload.value is Result.Error) {
                _accountCreation.value = Result.Error(information = _imageUpload.value.information!!)
                return@launch
            }
            repository.insertUser(firstName = firstName, lastName = lastName, imagePath = imagePath, uid = Firebase.auth.currentUser!!.uid)
                .collect {
                    _userInsertion.value = it
                }
            if (_userInsertion.value is Result.Error) {
                _accountCreation.value = Result.Error(information = _userInsertion.value.information!!)
                return@launch
            }
            val token = Firebase.messaging.token.await()
            repository.insertDeviceToken(uid = Firebase.auth.currentUser!!.uid, token = token).collect {
                _accountCreation.value = it
            }
        }
    }

    override fun refresh() {
        _imageUpload.value = Result.Waiting()
        _userInsertion.value = Result.Waiting()
        _accountCreation.value = Result.Waiting()
    }
}
