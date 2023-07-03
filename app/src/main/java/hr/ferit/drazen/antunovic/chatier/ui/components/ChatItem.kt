package hr.ferit.drazen.antunovic.chatier.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import hr.ferit.drazen.antunovic.chatier.R
import hr.ferit.drazen.antunovic.chatier.data.KeyedUserWithLastMessage
import hr.ferit.drazen.antunovic.chatier.data.Result

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    modifier: Modifier = Modifier,
    user: KeyedUserWithLastMessage,
    onNavigateToChat: (String) -> Unit,
    viewModel: ChatItemViewModel,
) {
    val chatDeleteResult by viewModel.chatDeletion.collectAsState()
    var isImageOpened by rememberSaveable { mutableStateOf(false) }
    var isLongPressed by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.combinedClickable(
            onLongClick = { isLongPressed = true },
            onClick = { onNavigateToChat(user.uid) },
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 30.dp)
    ) {
        AsyncImage(
            model = user.imagePath,
            contentDescription = stringResource(id = R.string.profile_picture),
            modifier = Modifier
                .size(size = 75.dp)
                .clip(shape = CircleShape)
                .clickable { isImageOpened = true },
            contentScale = ContentScale.Crop,
        )
        if (isImageOpened) {
            Dialog(onDismissRequest = { isImageOpened = false }) {
                AsyncImage(
                    model = user.imagePath,
                    contentDescription = stringResource(id = R.string.profile_picture),
                    modifier = Modifier.size(size = 280.dp),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            Text(text = user.fullName, style = MaterialTheme.typography.subtitle1)
            Text(
                text = if (user.lastMessage.length <= 50) user.lastMessage else user.lastMessage.substring(startIndex = 0, endIndex = 50) + "..."
            )
        }
        if (isLongPressed) {
            CustomDialog(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colors.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(all = 20.dp),
                onDismissRequest = { isLongPressed = false },
                onClick = {
                    if (chatDeleteResult !is Result.Loading) {
                        viewModel.deleteChat(user.uid)
                        isLongPressed = false
                    }
                },
                text = stringResource(id = R.string.delete_chat_approval),
                buttonText = stringResource(id = R.string.delete_chat),
            )
        }
    }
}
