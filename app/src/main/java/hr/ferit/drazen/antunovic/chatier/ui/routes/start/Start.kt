package hr.ferit.drazen.antunovic.chatier.ui.routes.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hr.ferit.drazen.antunovic.chatier.R
import hr.ferit.drazen.antunovic.chatier.data.Result

@Composable
fun Start(
    modifier: Modifier = Modifier,
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignOut: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToFillPersonalInformation: () -> Unit,
    viewModel: StartViewModel,
) {
    var isUserVerified by rememberSaveable { mutableStateOf(value = false) }
    if (isUserVerified) {
        return
    }
    val userResult by viewModel.user.collectAsState()
    if (Firebase.auth.currentUser == null) {
        StartScreen(
            modifier = modifier,
            onSignInClick = onNavigateToSignIn,
            onSignUpClick = onNavigateToSignOut,
        )
    } else if (userResult is Result.Loading) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(id = R.string.loading),
                modifier = Modifier.align(alignment = Alignment.Center)
            )
        }
    } else if (userResult is Result.Waiting) {
        viewModel.fetchUser(Firebase.auth.currentUser!!.uid)
    } else if (userResult is Result.Success && userResult.data == null) {
        viewModel.refresh()
        isUserVerified = true
        onNavigateToFillPersonalInformation()
    } else if (userResult is Result.Success) {
        viewModel.refresh()
        isUserVerified = true
        onNavigateToHome()
    } else if (userResult is Result.Error) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = userResult.information!!,
                modifier = Modifier.padding(bottom = 30.dp)
            )
            Button(
                onClick = viewModel::refreshUserResult,
                modifier = Modifier.padding(bottom = 26.dp),
                shape = MaterialTheme.shapes.large,
                contentPadding = PaddingValues(16.dp, 10.dp)
            ) {
                Text(text = stringResource(id = R.string.retry))
            }
        }
    }
}

@Composable
fun StartScreen(
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.h1,
            modifier = Modifier.padding(bottom = 100.dp)
        )
        Button(
            onClick = onSignInClick,
            modifier = Modifier.padding(bottom = 26.dp),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text = stringResource(id = R.string.sign_in))
        }
        Button(
            onClick = onSignUpClick,
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text = stringResource(id = R.string.sign_up))
        }
        Text(modifier = Modifier.padding(top = 50.dp), text = stringResource(id = R.string.credits))
    }
}
