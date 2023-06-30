package hr.ferit.drazen.antunovic.chatier.ui.routes.signin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.ui.routes.signup.SignScreen

@Composable
fun SignIn(
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel,
    onNavigateToFillPersonalInformation: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val signInResult by viewModel.signIn.collectAsState()
    val deviceTokenInsertionResult by viewModel.deviceTokenInsertion.collectAsState()
    if (deviceTokenInsertionResult is Result.Success) {
        viewModel.refresh()
        onNavigateToHome()
    } else if (signInResult is Result.Success && signInResult.data == null) {
        viewModel.refresh()
        onNavigateToFillPersonalInformation()
    } else if (signInResult is Result.Success && deviceTokenInsertionResult !is Result.Loading) {
        viewModel.insertDeviceToken()
    }

    SignScreen(
        modifier = modifier,
        onClick = { email, password ->
            viewModel.signIn(
                email = email,
                password = password,
            )
        },
        result = signInResult,
    )
}
