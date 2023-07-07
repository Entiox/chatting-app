package hr.ferit.drazen.antunovic.chatier.ui.routes.personal_information

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.viewmodel.DefaultViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PersonalInformationViewModel : DefaultViewModel() {
    private val _userUpdate: MutableStateFlow<Result<Nothing>> = MutableStateFlow(Result.Waiting())
    val userUpdate = _userUpdate.asStateFlow()

    private val _imageUpload: MutableStateFlow<Result<String>> = MutableStateFlow(Result.Waiting())
    val imageUpload = _imageUpload.asStateFlow()

    private val _signOut: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val signOut = _signOut.asStateFlow()

    private val _deviceTokenDeletion: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val deviceTokenDeletion = _deviceTokenDeletion.asStateFlow()

    private val _userDeletion: MutableStateFlow<Result<Nothing>> = MutableStateFlow(Result.Waiting())
    val userDeletion = _userDeletion.asStateFlow()

    private val _profilePictureRemoval: MutableStateFlow<Result<String>> = MutableStateFlow(Result.Waiting())
    val profilePictureRemoval = _profilePictureRemoval.asStateFlow()

    private val _userAccountDeletion: MutableStateFlow<Result<Nothing>> =
        MutableStateFlow(Result.Waiting())
    val userAccountDeletion = _userAccountDeletion.asStateFlow()

    val currentUser = repository.fetchUser(Firebase.auth.currentUser!!.uid).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Result.Waiting()
    )

    fun updateUser(imageUri: Uri?, firstName: String?, lastName: String?) {
        viewModelScope.launch {
            _userUpdate.value = Result.Loading()
            var imagePath: String? = null
            if (imageUri != null) {
                repository.uploadImage(imageUri = imageUri).collect {
                    if (it is Result.Success) {
                        imagePath = it.data!!
                    }
                    _imageUpload.value = it
                }
            }
            if (_imageUpload.value is Result.Error) {
                _userUpdate.value = Result.Error(information = _imageUpload.value.information!!)
                return@launch
            }
            repository.updateUser(
                firstName = firstName,
                lastName = lastName,
                imagePath = imagePath,
                originalFirstName = currentUser.value.data!!.firstName,
                originalLastName = currentUser.value.data!!.lastName,
                uid = Firebase.auth.currentUser!!.uid
            ).collect {
                _userUpdate.value = it
            }
        }
    }

    fun removeProfilePicture() {
        viewModelScope.launch {
            repository.updateUser(
                firstName = null,
                lastName = null,
                imagePath = "images/default",
                originalFirstName = currentUser.value.data!!.firstName,
                originalLastName = currentUser.value.data!!.lastName,
                uid = Firebase.auth.currentUser!!.uid
            ).collect {
                if(it is Result.Loading){
                    _profilePictureRemoval.value = Result.Loading()
                } else if(it is Result.Success) {
                    _profilePictureRemoval.value = Result.Success(Firebase.storage.reference.child(
                        "images/default"
                    ).downloadUrl.await().toString())
                } else if(it is Result.Error){
                    _profilePictureRemoval.value = Result.Error(information = it.information!!)
                }
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

    fun deleteUser() {
        viewModelScope.launch {
            _userAccountDeletion.value = Result.Loading()
            repository.deleteDeviceToken(uid = Firebase.auth.currentUser!!.uid).collect {
                _deviceTokenDeletion.value = it
            }
            if (_deviceTokenDeletion.value is Result.Error) {
                _userAccountDeletion.value =
                    Result.Error(information = _deviceTokenDeletion.value.information!!)
                return@launch
            }
            repository.deleteUser(uid = Firebase.auth.currentUser!!.uid).collect {
                _userDeletion.value = it
            }
            if (_userDeletion.value is Result.Error) {
                _userAccountDeletion.value =
                    Result.Error(information = _userDeletion.value.information!!)
                return@launch
            }
            Firebase.auth.signOut()
            _userAccountDeletion.value = Result.Success(data = null)
        }
    }

    fun refreshUserUpdate() {
        _userUpdate.value = Result.Waiting()
    }

    fun refreshProfilePictureRemoval() {
        _profilePictureRemoval.value = Result.Waiting()
    }

    override fun refresh() {
        _profilePictureRemoval.value = Result.Waiting()
        _imageUpload.value = Result.Waiting()
        _deviceTokenDeletion.value = Result.Waiting()
        _userUpdate.value = Result.Waiting()
        _userAccountDeletion.value = Result.Waiting()
        _userDeletion.value = Result.Waiting()
        _signOut.value = Result.Waiting()
    }
}
