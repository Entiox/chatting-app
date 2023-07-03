package hr.ferit.drazen.antunovic.chatier.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    content: String,
    timeStamp: String,
    arrangement: Arrangement.Horizontal,
    backgroundColor: Color,
    onLongClick: () -> Unit,
) {
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
            Text(
                modifier = Modifier.padding(bottom = 20.dp),
                text = content,
                color = MaterialTheme.colors.onSecondary,
            )
            Text(
                modifier = Modifier,
                text = timeStamp,
                color = MaterialTheme.colors.onSecondary,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}
