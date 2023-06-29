package hr.ferit.drazen.antunovic.chatier.ui.routes.fill_personal_information

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import hr.ferit.drazen.antunovic.chatier.data.Result
import coil.compose.AsyncImage
import hr.ferit.drazen.antunovic.chatier.R

@Composable
fun FillInformation(
    modifier: Modifier = Modifier,
    viewModel: FillIPersonalInformationViewModel,
    onNavigateToHome: () -> Unit,
) {
    val accountCreationResult by viewModel.accountCreation.collectAsState()
    if (accountCreationResult is Result.Success) {
        viewModel.refresh()
        onNavigateToHome()
    }

    FillInformationScreen(
        modifier = modifier.padding(top = 20.dp, bottom = 20.dp),
        accountCreationResult = accountCreationResult,
        onClick = { uri, firstName, lastName ->
            viewModel.insertUser(
                imageUri = uri,
                firstName = firstName,
                lastName = lastName
            )
        }
    )
}

@Composable
fun FillInformationScreen(
    modifier: Modifier = Modifier,
    accountCreationResult: Result<out Any?>,
    onClick: (Uri?, String, String) -> Unit
) {
    val context = LocalContext.current
    val imagesPermission: Boolean by rememberSaveable {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var imageUri: Uri? by rememberSaveable { mutableStateOf(value = null) }
    var firstName by rememberSaveable { mutableStateOf(value = "") }
    var lastName by rememberSaveable { mutableStateOf(value = "") }
    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.PickVisualMedia(), onResult = { imageUri = it })

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 30.dp),
            text = stringResource(id = R.string.profile_picture),
            style = MaterialTheme.typography.subtitle1,
        )
        Button(
            onClick = {
                if (imagesPermission) {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            },
            modifier = Modifier.padding(bottom = 30.dp),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(16.dp, 10.dp),
        ) {
            Text(text = stringResource(id = R.string.select_image))
        }
        if (imageUri != null) {
            Box(modifier = Modifier.padding(bottom = 50.dp)) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(id = R.string.profile_picture),
                    modifier = Modifier
                        .size(size = 200.dp)
                        .clip(shape = CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Text(
            text = stringResource(id = R.string.first_name),
            modifier = Modifier.padding(bottom = 10.dp),
            style = MaterialTheme.typography.subtitle1,
        )
        OutlinedTextField(
            modifier = Modifier.padding(bottom = 20.dp),
            value = firstName,
            onValueChange = { firstName = it },
            singleLine = true,
        )
        Text(
            text = stringResource(id = R.string.last_name),
            modifier = Modifier.padding(bottom = 10.dp),
            style = MaterialTheme.typography.subtitle1,
        )
        OutlinedTextField(
            modifier = Modifier.padding(bottom = 40.dp),
            value = lastName,
            onValueChange = { lastName = it },
            singleLine = true,
        )
        Button(
            onClick = {
                if (accountCreationResult !is Result.Loading) {
                    onClick(imageUri, firstName, lastName)
                }
            },
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(text = stringResource(id = R.string.create_account))
        }
        if (accountCreationResult is Result.Loading) {
            Text(
                text = stringResource(id = R.string.creating_account),
                modifier = Modifier.padding(vertical = 25.dp),
                textAlign = TextAlign.Center,
            )
        } else if (accountCreationResult is Result.Error) {
            Text(
                text = accountCreationResult.information!!,
                modifier = Modifier.padding(vertical = 25.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
