package hr.ferit.drazen.antunovic.chatier.ui.routes.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hr.ferit.drazen.antunovic.chatier.R
import hr.ferit.drazen.antunovic.chatier.data.Result

@Composable
fun SignUp(
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel,
    onNavigateToFillPersonalInformation: () -> Unit,
) {
    val authResult by viewModel.auth.collectAsState()
    if (authResult is Result.Success) {
        viewModel.refresh()
        onNavigateToFillPersonalInformation()
    }

    SignScreen(
        modifier = modifier,
        result = authResult,
        onClick = { email, password ->
            viewModel.signUp(email, password)
        },
    )
}

@Composable
fun SignScreen(
    modifier: Modifier = Modifier,
    result: Result<out Any?>,
    onClick: (String, String) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf(value = "") }
    var password by rememberSaveable { mutableStateOf(value = "") }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(bottom = 10.dp),
            text = stringResource(id = R.string.email),
            style = MaterialTheme.typography.subtitle1,
        )
        OutlinedTextField(
            modifier = Modifier.padding(bottom = 20.dp),
            value = email,
            onValueChange = { email = it },
            singleLine = true,
        )
        Text(
            modifier = Modifier.padding(bottom = 10.dp),
            text = stringResource(id = R.string.password),
            style = MaterialTheme.typography.subtitle1,
        )
        OutlinedTextField(
            modifier = Modifier.padding(bottom = 25.dp),
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
        )
        Button(
            onClick = {
                if (result !is Result.Loading) {
                    onClick(email, password)
                }
            },
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text = stringResource(id = R.string.confirm))
        }
        if (result is Result.Loading) {
            Text(
                modifier = Modifier.padding(vertical = 25.dp),
                text = stringResource(id = R.string.loading),
                textAlign = TextAlign.Center
            )
        } else if (result is Result.Error) {
            Text(
                modifier = Modifier.padding(vertical = 25.dp),
                text = result.information!!,
                textAlign = TextAlign.Center
            )
        }
    }
}
