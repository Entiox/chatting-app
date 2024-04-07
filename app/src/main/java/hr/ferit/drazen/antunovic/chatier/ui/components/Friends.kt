package hr.ferit.drazen.antunovic.chatier.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.ferit.drazen.antunovic.chatier.R
import hr.ferit.drazen.antunovic.chatier.data.KeyedUser
import hr.ferit.drazen.antunovic.chatier.data.Result

@Composable
fun Friends(
    modifier: Modifier = Modifier,
    usersResult: Result<List<KeyedUser>>,
    friendsResult: Result<List<KeyedUser>>,
    onUserSearch: (String) -> Unit,
    resetSearch: () -> Unit,
    onNavigateToChat: (String) -> Unit,
) {
    var searchValue by rememberSaveable { mutableStateOf("") }
    var isSearchEnabled by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    BackHandler(enabled = isSearchEnabled) {
        isSearchEnabled = false
        searchValue = ""
        focusManager.clearFocus()
        resetSearch()
    }
    LazyColumn(modifier = modifier) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .onFocusChanged { if (it.isFocused) isSearchEnabled = true }
                        .weight(1f),
                    value = searchValue,
                    onValueChange = { searchValue = it },
                    singleLine = true,
                    placeholder = { Text(text = "Search friends") },
                )
                Icon(
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .clickable {
                            if (isSearchEnabled && usersResult !is Result.Loading) {
                                onUserSearch(searchValue)
                            }
                        },
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "Search friends",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }
        if (isSearchEnabled) {
            if (usersResult is Result.Success) {
                items(usersResult.data!!) {
                    PersonItem(
                        modifier = Modifier
                            .padding(bottom = 15.dp)
                            .fillMaxWidth()
                            .padding(all = 10.dp),
                        user = it,
                        viewModel = viewModel(),
                    )
                }
            } else if (usersResult is Result.Loading) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(text = "Searching", modifier = Modifier.align(Alignment.Center))
                    }
                }
            } else if (usersResult is Result.Error) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = usersResult.information!!,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        } else {
            if (friendsResult is Result.Success) {
                item {
                    Text(
                        modifier = Modifier.padding(bottom = 25.dp),
                        text = "Friends",
                        style = MaterialTheme.typography.h5,
                    )
                }
                items(friendsResult.data!!) {
                    FriendItem(
                        modifier = Modifier
                            .padding(bottom = 15.dp)
                            .fillMaxWidth()
                            .padding(all = 10.dp),
                        friend = it,
                        viewModel = viewModel(),
                        onClick = { onNavigateToChat(it.uid) },
                    )
                }
            } else if (friendsResult is Result.Loading) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(text = "Loading friends", modifier = Modifier.align(Alignment.Center))
                    }
                }
            } else if (friendsResult is Result.Error) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = friendsResult.information!!,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        }
    }
}
