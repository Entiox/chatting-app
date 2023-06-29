package hr.ferit.drazen.antunovic.chatier.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CustomDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onClick: () -> Unit,
    text: String,
    buttonText: String
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(modifier = modifier) {
            Text(
                text = text,
                style = MaterialTheme.typography.subtitle1,
            )
            Divider(modifier = Modifier.padding(vertical = 15.dp))
            Button(
                onClick = onClick,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = buttonText)
            }
        }
    }
}
