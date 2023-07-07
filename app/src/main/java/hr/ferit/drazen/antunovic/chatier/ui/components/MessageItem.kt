package hr.ferit.drazen.antunovic.chatier.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import hr.ferit.drazen.antunovic.chatier.data.Message

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    message: Message,
    arrangement: Arrangement.Horizontal,
    backgroundColor: Color,
    onLongClick: () -> Unit,
) {
    var isImageOpened by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.combinedClickable(
            onClick = { },
            onLongClick = onLongClick,
        ),
        horizontalArrangement = arrangement,
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = Dp.Unspecified, max = 280.dp)
                .padding(vertical = 10.dp, horizontal = 20.dp)
                .clip(shape = MaterialTheme.shapes.medium)
                .background(color = backgroundColor)
                .padding(all = 15.dp),
        ) {
            if (message.type == "image") {
                AsyncImage(
                    model = message.content,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(shape = RectangleShape)
                        .padding(bottom = 10.dp)
                        .clickable { isImageOpened = true },
                    contentScale = ContentScale.Fit,
                )
                if (isImageOpened) {
                    Dialog(onDismissRequest = { isImageOpened = false }) {
                        AsyncImage(
                            model = message.content,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            } else {
                Text(
                    modifier = Modifier.padding(bottom = 20.dp),
                    text = message.content,
                    color = MaterialTheme.colors.onSecondary,
                )
            }
            Text(
                modifier = Modifier,
                text = message.timeStamp,
                color = MaterialTheme.colors.onSecondary,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}
