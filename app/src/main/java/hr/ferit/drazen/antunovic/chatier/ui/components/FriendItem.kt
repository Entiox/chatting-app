package hr.ferit.drazen.antunovic.chatier.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import hr.ferit.drazen.antunovic.chatier.data.KeyedUser
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendItem(
    modifier: Modifier = Modifier,
    friend: KeyedUser,
    viewModel: FriendItemViewModel,
    onClick: () -> Unit
) {
    val friendRemovalResult by viewModel.friendRemoval.collectAsState()
    var isImageOpened by rememberSaveable { mutableStateOf(false) }
    var isLongPressed by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.combinedClickable(
            onLongClick = { isLongPressed = true },
            onClick = onClick
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 30.dp)
    ) {
        AsyncImage(
            model = friend.imagePath,
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
                    model = friend.imagePath,
                    contentDescription = stringResource(id = R.string.profile_picture),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        Text(
            modifier = Modifier.padding(bottom = 10.dp),
            text = friend.fullName,
            style = MaterialTheme.typography.subtitle1
        )
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
                    if (friendRemovalResult !is Result.Loading) {
                        viewModel.removeFriend(friend.uid)
                        isLongPressed = false
                    }
                },
                text = stringResource(id = R.string.remove_friend_approval),
                buttonText = stringResource(id = R.string.remove_friend),
            )
        }
    }
}
