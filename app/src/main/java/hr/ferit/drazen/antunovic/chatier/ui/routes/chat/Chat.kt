package hr.ferit.drazen.antunovic.chatier.ui.routes.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hr.ferit.drazen.antunovic.chatier.R
import hr.ferit.drazen.antunovic.chatier.data.Message
import hr.ferit.drazen.antunovic.chatier.data.User
import hr.ferit.drazen.antunovic.chatier.data.Result

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
    } else if(signOutResult is Result.Loading) {
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
            participant = if (participantResult.data != null) participantResult.data!! else User(
                fullName = stringResource(id = R.string.deleted_user)
            ),
            messages = chatResult.data!!,
            messagingResult = messagingResult,
            onMessageSend = viewModel::insertMessage
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = stringResource(id = R.string.loading_chat), modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    participant: User,
    messages: List<Message>,
    messagingResult: Result<out Any?>,
    onMessageSend: (String) -> Unit
) {
    var message by rememberSaveable { mutableStateOf(value = "") }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.padding(horizontal = 25.dp, vertical = 20.dp),
                text = participant.fullName,
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
                            text = stringResource(id = R.string.sending)
                        )
                    }
                } else if (messagingResult is Result.Error) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp),
                            text = messagingResult.information!!
                        )
                    }
                }
            }
            items(messages) {
                if (it.senderUid == Firebase.auth.currentUser!!.uid) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(min = Dp.Unspecified, max = 280.dp)
                                .padding(vertical = 10.dp, horizontal = 20.dp)
                                .clip(shape = MaterialTheme.shapes.medium)
                                .background(
                                    color = MaterialTheme.colors.secondary.copy(
                                        alpha = 0.8f,
                                        blue = 0.5f
                                    )
                                )
                                .padding(all = 15.dp),
                        ) {
                            Text(
                                modifier = Modifier.padding(bottom = 20.dp),
                                text = it.content,
                                color = MaterialTheme.colors.onSecondary,
                            )
                            Text(
                                modifier = Modifier,
                                text = it.timeStamp,
                                color = MaterialTheme.colors.onSecondary,
                                style = MaterialTheme.typography.caption,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(min = Dp.Unspecified, max = 280.dp)
                                .padding(vertical = 10.dp, horizontal = 20.dp)
                                .clip(shape = MaterialTheme.shapes.medium)
                                .background(color = MaterialTheme.colors.secondary)
                                .padding(all = 15.dp),
                        ) {
                            Text(
                                modifier = Modifier.padding(bottom = 20.dp),
                                text = it.content,
                                color = MaterialTheme.colors.onSecondary,
                            )
                            Text(
                                modifier = Modifier,
                                text = it.timeStamp,
                                color = MaterialTheme.colors.onSecondary,
                                style = MaterialTheme.typography.caption,
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(weight = 1f),
                placeholder = { Text(text = stringResource(id = R.string.type_message_here)) },
                value = message,
                onValueChange = { message = it },
                singleLine = true,
            )
            Icon(
                modifier = Modifier
                    .padding(start = 20.dp, end = 5.dp)
                    .clickable {
                        if (message.isNotEmpty()) {
                            onMessageSend(message)
                            message = ""
                        }
                    },
                painter = painterResource(id = R.drawable.ic_send),
                contentDescription = stringResource(id = R.string.send_message),
                tint = MaterialTheme.colors.onBackground,
            )
        }
    }
}
