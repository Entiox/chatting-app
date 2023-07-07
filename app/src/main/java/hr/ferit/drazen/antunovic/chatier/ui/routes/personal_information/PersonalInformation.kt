package hr.ferit.drazen.antunovic.chatier.ui.routes.personal_information

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import hr.ferit.drazen.antunovic.chatier.R
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.data.User
import hr.ferit.drazen.antunovic.chatier.ui.components.CustomDialog

@Composable
fun PersonalInformation(
    modifier: Modifier = Modifier,
    viewModel: PersonalInformationViewModel,
    onNavigateToStart: () -> Unit
) {
    var isUserDeleted by rememberSaveable { mutableStateOf(false) }
    if (isUserDeleted) {
        return
    }
    val signOutResult by viewModel.signOut.collectAsState()
    val userUpdateResult by viewModel.userUpdate.collectAsState()
    val userResult by viewModel.currentUser.collectAsState()
    val profilePictureRemovalResult by viewModel.profilePictureRemoval.collectAsState()
    val userAccountDeletion by viewModel.userAccountDeletion.collectAsState()
    BackHandler(enabled = userAccountDeletion is Result.Loading) { }
    if (signOutResult is Result.Success) {
        viewModel.refresh()
        isUserDeleted = true
        onNavigateToStart()
    } else if (signOutResult is Result.Loading) {
        return
    } else if (userAccountDeletion is Result.Loading) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(id = R.string.loading),
                modifier = Modifier.align(alignment = Alignment.Center)
            )
        }
    } else if (userAccountDeletion is Result.Success) {
        viewModel.refresh()
        isUserDeleted = true
        onNavigateToStart()
    } else if (userResult is Result.Success && userResult.data == null) {
        viewModel.signOut()
    } else if (userResult is Result.Success) {
        PersonalInformationScreen(
            modifier = modifier, userUpdateResult = userUpdateResult,
            onSaveClick = { imageUri, firstName, lastName ->
                viewModel.updateUser(imageUri = imageUri, firstName = firstName, lastName = lastName)
            },
            refreshUserUpdateResult = viewModel::refreshUserUpdate,
            refreshProfilePictureRemovalResult = viewModel::refreshProfilePictureRemoval,
            user = userResult.data!!,
            onDeleteClick = viewModel::deleteUser,
            userAccountDeletionResult = userAccountDeletion,
            onProfilePictureRemoveClick = viewModel::removeProfilePicture,
            profilePictureRemovalResult = profilePictureRemovalResult
        )
    } else if (userResult is Result.Loading) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = stringResource(id = R.string.loading_profile), modifier = Modifier.align(alignment = Alignment.Center))
        }
    } else if (userResult is Result.Error) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = userResult.information!!, modifier = Modifier.align(alignment = Alignment.Center))
        }
    }
}

@Composable
fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    user: User,
    onSaveClick: (Uri?, String?, String?) -> Unit,
    userUpdateResult: Result<out Any?>,
    profilePictureRemovalResult: Result<out Any?>,
    onDeleteClick: () -> Unit,
    onProfilePictureRemoveClick: () -> Unit,
    userAccountDeletionResult: Result<out Any?>,
    refreshUserUpdateResult: () -> Unit,
    refreshProfilePictureRemovalResult: () -> Unit,
) {
    val context = LocalContext.current
    val imagesPermission: Boolean by rememberSaveable {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var imageUri by rememberSaveable { mutableStateOf(Uri.parse(user.imagePath)) }
    var isProfilePictureRemoved by rememberSaveable { mutableStateOf(value = false) }
    var firstName by rememberSaveable { mutableStateOf(user.firstName) }
    var lastName by rememberSaveable { mutableStateOf(user.lastName) }

    var wasImageUriModified by rememberSaveable { mutableStateOf(value = false) }
    var wasFirstNameModified by rememberSaveable { mutableStateOf(value = false) }
    var wasLastNameModified by rememberSaveable { mutableStateOf(value = false) }

    var isDeleteRequested by rememberSaveable { mutableStateOf(value = false) }

    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.PickVisualMedia(), onResult = {
        imageUri = it
        if(it != null) {
            wasImageUriModified = true
        }
    })
    if(profilePictureRemovalResult is Result.Success) {
        imageUri = Uri.parse(profilePictureRemovalResult.data!! as String)
        refreshProfilePictureRemovalResult()
    }
    Column(
        modifier = modifier.verticalScroll(state = rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier
            .padding(top = 35.dp, bottom = 40.dp)
            .clickable {
                if (imagesPermission) {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }) {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(id = R.string.profile_picture),
                modifier = Modifier
                    .size(size = 200.dp)
                    .clip(shape = CircleShape),
                contentScale = ContentScale.Crop,
            )
            Icon(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = stringResource(id = R.string.edit_profile_picture),
                tint = Color.White
            )
        }
        Button(
            modifier = Modifier.padding(top = 5.dp, bottom = 40.dp),
            onClick = {
                if (userUpdateResult !is Result.Loading) {
                    onProfilePictureRemoveClick()
                }
            },
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(text = stringResource(id = R.string.remove_profile_picture))
        }
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = stringResource(id = R.string.first_name),
            style = MaterialTheme.typography.subtitle1,
        )
        TextField(
            modifier = Modifier.padding(start = 40.dp, end = 40.dp, bottom = 40.dp),
            value = firstName,
            onValueChange = {
                firstName = it
                wasFirstNameModified = true
            },
            shape = RectangleShape,
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
            singleLine = true,
        )
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = stringResource(id = R.string.last_name),
            style = MaterialTheme.typography.subtitle1,
        )
        TextField(
            modifier = Modifier.padding(start = 40.dp, end = 40.dp, bottom = 50.dp),
            value = lastName,
            onValueChange = {
                lastName = it
                wasLastNameModified = true
            },
            shape = RectangleShape,
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
            singleLine = true,
        )
        if (wasFirstNameModified || wasLastNameModified || wasImageUriModified) {
            Button(
                modifier = Modifier.padding(vertical = 40.dp),
                onClick = {
                    if (userUpdateResult !is Result.Loading) {
                        onSaveClick(
                            if (wasImageUriModified) imageUri else null,
                            if (wasFirstNameModified) firstName else null,
                            if (wasLastNameModified) lastName else null,
                        )
                    }
                },
                shape = MaterialTheme.shapes.large,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
        if (userUpdateResult is Result.Loading) {
            Text(
                text = stringResource(id = R.string.loading),
            )
        } else if (userUpdateResult is Result.Error) {
            Text(
                text = userUpdateResult.information!!,
            )
        }
        if (userUpdateResult is Result.Success) {
            wasImageUriModified = false
            wasFirstNameModified = false
            wasLastNameModified = false
            refreshUserUpdateResult()
        }
        Button(
            modifier = Modifier.padding(vertical = 40.dp),
            onClick = {
                if (userAccountDeletionResult !is Result.Loading) {
                    isDeleteRequested = true
                }
            },
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.error,
                contentColor = MaterialTheme.colors.onError,
            )
        ) {
            Text(text = stringResource(id = R.string.delete_account))
        }
        if (userAccountDeletionResult is Result.Loading) {
            Text(
                modifier = Modifier.padding(bottom = 25.dp),
                text = stringResource(id = R.string.loading),
            )
        } else if (userAccountDeletionResult is Result.Error) {
            Text(
                modifier = Modifier.padding(bottom = 25.dp),
                text = userAccountDeletionResult.information!!,
            )
        }
        if (isDeleteRequested) {
            CustomDialog(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colors.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(all = 20.dp),
                onDismissRequest = { isDeleteRequested = false },
                onClick = {
                    onDeleteClick()
                    isDeleteRequested = false
                },
                text = stringResource(id = R.string.delete_account_approval),
                buttonText = stringResource(id = R.string.delete_account),
            )
        }
    }
}
