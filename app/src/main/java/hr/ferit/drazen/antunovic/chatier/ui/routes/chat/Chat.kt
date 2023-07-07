package hr.ferit.drazen.antunovic.chatier.ui.routes.chat

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hr.ferit.drazen.antunovic.chatier.R
import hr.ferit.drazen.antunovic.chatier.data.Message
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.data.User
import hr.ferit.drazen.antunovic.chatier.ui.components.MessageItem

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Chat(modifier: Modifier = Modifier, viewModel: ChatViewModel, onNavigateToStart: () -> Unit) {
    var isUserDeleted by rememberSaveable { mutableStateOf(false) }
    if (isUserDeleted) {
        return
    }
    val currentUserResult by viewModel.currentUser.collectAsState()
    val participantResult by viewModel.participant.collectAsState()
    val chatResult by viewModel.chat.collectAsState()
    val deviceTokenResult by viewModel.deviceToken.collectAsState()
    val messagingResult by viewModel.messaging.collectAsState()
    val signOutResult by viewModel.signOut.collectAsState()

    if (signOutResult is Result.Success) {
        viewModel.refresh()
        isUserDeleted = true
        onNavigateToStart()
    } else if (signOutResult is Result.Loading) {
        return
    } else if (currentUserResult is Result.Success && currentUserResult.data == null) {
        viewModel.signOut()
    } else if (chatResult is Result.Error || currentUserResult is Result.Error || deviceTokenResult is Result.Error ||
        participantResult is Result.Error
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(id = R.string.error_loading_chat),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    } else if (chatResult is Result.Success && currentUserResult is Result.Success && deviceTokenResult is Result.Success &&
        participantResult is Result.Success
    ) {
        ChatScreen(
            modifier = modifier,
            participant = participantResult.data,
            messages = chatResult.data!!,
            messagingResult = messagingResult,
            onMessageSend = viewModel::insertMessage,
            onImageSend = viewModel::insertImage
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(id = R.string.loading_chat),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    participant: User?,
    messages: List<Message>,
    messagingResult: Result<out Any?>,
    onMessageSend: (String) -> Unit,
    onImageSend: (Uri?) -> Unit,
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
    var message by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange.Zero
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.PickVisualMedia(), onResult = {
        onImageSend(it)
    })

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.padding(horizontal = 25.dp, vertical = 20.dp),
                text = participant?.fullName ?: stringResource(id = R.string.deleted_user),
                style = MaterialTheme.typography.h5,
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(weight = 1f, fill = false),
            reverseLayout = true,
        ) {
            item {
                if (messagingResult is Result.Loading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp),
                            text = stringResource(id = R.string.sending),
                        )
                    }
                } else if (messagingResult is Result.Error) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp),
                            text = messagingResult.information!!,
                        )
                    }
                }
            }
            items(messages) {
                if (it.senderUid == Firebase.auth.currentUser!!.uid) {
                    MessageItem(
                        modifier = Modifier.fillMaxWidth(),
                        message = it,
                        arrangement = Arrangement.End,
                        backgroundColor = MaterialTheme.colors.secondary.copy(
                            alpha = 0.8f,
                            blue = 0.5f,
                        ),
                        onLongClick = {
                            focusRequester.requestFocus()
                            val text = "Reply to \"${it.content}\": "
                            message = message.copy(
                                text = text,
                                selection = TextRange(index = text.length)
                            )
                        },
                    )
                } else {
                    MessageItem(
                        modifier = Modifier.fillMaxWidth(),
                        message = it,
                        arrangement = Arrangement.Start,
                        backgroundColor = MaterialTheme.colors.secondary,
                        onLongClick = {
                            focusRequester.requestFocus()
                            val text = "Reply to \"${it.content}\": "
                            message = message.copy(
                                text = text,
                                selection = TextRange(index = text.length)
                            )
                        },
                    )
                }
            }
        }
        if (participant != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 20.dp)
                        .clickable {
                            if (imagesPermission) {
                                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        },
                    painter = painterResource(id = R.drawable.ic_insert_photo),
                    contentDescription = stringResource(id = R.string.send_image),
                    tint = MaterialTheme.colors.onBackground,
                )
                OutlinedTextField(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .focusRequester(focusRequester),
                    placeholder = { Text(text = stringResource(id = R.string.type_message_here)) },
                    value = message,
                    onValueChange = { message = it },
                    maxLines = 5,
                )
                Icon(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 5.dp)
                        .clickable {
                            if (message.text.isNotEmpty()) {
                                onMessageSend(message.text)
                                message = message.copy(text = "")
                            }
                        },
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = stringResource(id = R.string.send_message),
                    tint = MaterialTheme.colors.onBackground,
                )
            }
        }
    }
}
