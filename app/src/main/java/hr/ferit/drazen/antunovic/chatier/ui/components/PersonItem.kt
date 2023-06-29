package hr.ferit.drazen.antunovic.chatier.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
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
import hr.ferit.drazen.antunovic.chatier.data.KeyedUser
import hr.ferit.drazen.antunovic.chatier.data.Result

@Composable
fun PersonItem(modifier: Modifier = Modifier, user: KeyedUser, viewModel: PersonItemViewModel) {
    val friendRequestResult by viewModel.friendRequest.collectAsState()
    var isImageOpened by rememberSaveable { mutableStateOf(value = false) }

    Row(
        modifier = modifier,
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
            Button(
                onClick = {
                    if (friendRequestResult !is Result.Loading) {
                        viewModel.addFriend(user.uid)
                    }
                },
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(text = stringResource(id = R.string.add_friend))
            }
        }
    }
}
