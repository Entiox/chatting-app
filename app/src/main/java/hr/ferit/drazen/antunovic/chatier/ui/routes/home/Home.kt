package hr.ferit.drazen.antunovic.chatier.ui.routes.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hr.ferit.drazen.antunovic.chatier.R
import hr.ferit.drazen.antunovic.chatier.data.KeyedUser
import hr.ferit.drazen.antunovic.chatier.data.KeyedUserWithLastMessage
import hr.ferit.drazen.antunovic.chatier.data.Result
import hr.ferit.drazen.antunovic.chatier.ui.components.ChatItem
import hr.ferit.drazen.antunovic.chatier.ui.components.Friends
import kotlinx.coroutines.launch

@Composable
fun Home(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onNavigateToStart: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToPersonalInformation: () -> Unit
) {
    val chatsResult by viewModel.chats.collectAsState()
    val signOutResult by viewModel.signOut.collectAsState()
    val usersResult by viewModel.users.collectAsState()
    val friendsResult by viewModel.friends.collectAsState()
    if (signOutResult is Result.Success) {
        viewModel.refresh()
        onNavigateToStart()
    } else {
        HomeScreen(
            modifier = modifier,
            onSignOut = viewModel::signOut,
            onUserSearch = { fullName -> viewModel.fetchUsers(fullName) },
            usersResult = usersResult,
            friendsResult = friendsResult,
            chatsResult = chatsResult,
            resetSearch = viewModel::resetSearch,
            onNavigateToChat = onNavigateToChat,
            onNavigateToPersonalInformation = onNavigateToPersonalInformation,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit,
    onUserSearch: (String) -> Unit,
    usersResult: Result<List<KeyedUser>>,
    friendsResult: Result<List<KeyedUser>>,
    chatsResult: Result<List<KeyedUserWithLastMessage>>,
    resetSearch: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToPersonalInformation: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val isDrawerOpen by remember {
        derivedStateOf {
            scaffoldState.drawerState.isOpen
        }
    }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopBar(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                onIconClick = {
                    coroutineScope.launch {
                        scaffoldState.drawerState.open()
                    }
                },
            )
        },
        drawerContent = {
            DrawerContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 25.dp),
                onSignOut = onSignOut,
                onNavigateToPersonalInformation = onNavigateToPersonalInformation,
            )
        },
        drawerShape = RectangleShape,
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
    ) {
        BackHandler(enabled = isDrawerOpen) {
            coroutineScope.launch {
                scaffoldState.drawerState.close()
            }
        }
        Column(modifier = Modifier.padding(paddingValues = it)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier
                    .width(intrinsicSize = IntrinsicSize.Min)
                    .clickable {
                        coroutineScope.launch {
                            pagerState.scrollToPage(0)
                        }
                    }
                ) {
                    if (pagerState.currentPage == 0) {
                        Text(text = stringResource(id = R.string.chats), fontWeight = FontWeight.Bold)
                        Divider(
                            modifier = Modifier.padding(top = 7.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colors.onBackground
                        )
                    } else {
                        Text(text = stringResource(id = R.string.chats))
                    }
                }
                Column(modifier = Modifier
                    .width(intrinsicSize = IntrinsicSize.Min)
                    .clickable {
                        coroutineScope.launch {
                            pagerState.scrollToPage(1)
                        }
                    }
                ) {
                    if (pagerState.currentPage == 1) {
                        Text(text = stringResource(id = R.string.friends), fontWeight = FontWeight.Bold)
                        Divider(
                            modifier = Modifier.padding(top = 7.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colors.onBackground
                        )
                    } else {
                        Text(text = stringResource(id = R.string.friends))
                    }
                }
            }
            HorizontalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp),
                state = pagerState,
                pageCount = 2,
            ) { page ->
                if (page == 0) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxSize()
                    ) {
                        if (chatsResult is Result.Success) {
                            items(chatsResult.data!!) { user ->
                                ChatItem(
                                    modifier = Modifier
                                        .padding(bottom = 15.dp)
                                        .fillMaxWidth()
                                        .padding(all = 10.dp),
                                    user = user,
                                    onNavigateToChat = onNavigateToChat,
                                    viewModel = viewModel(),
                                )
                            }
                        } else if (chatsResult is Result.Loading) {
                            item {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = stringResource(id = R.string.loading_chats),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        } else if (chatsResult is Result.Error) {
                            item {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = chatsResult.information!!,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Friends(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .fillMaxSize(),
                        usersResult = usersResult,
                        friendsResult = friendsResult,
                        onUserSearch = onUserSearch,
                        resetSearch = resetSearch,
                        onNavigateToChat = onNavigateToChat,
                    )
                }
            }
        }
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier, onIconClick: () -> Unit) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.h4)
            Icon(
                modifier = Modifier.clickable {
                    onIconClick()
                },
                painter = painterResource(id = R.drawable.ic_options),
                contentDescription = stringResource(id = R.string.options),
                tint = MaterialTheme.colors.onBackground
            )
        }
        Divider(
            modifier = Modifier.padding(top = 10.dp),
            color = MaterialTheme.colors.onBackground,
            thickness = 0.3.dp
        )
    }
}

@Composable
fun DrawerContent(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit,
    onNavigateToPersonalInformation: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            modifier = Modifier.padding(bottom = 50.dp),
            onClick = onNavigateToPersonalInformation,
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(text = stringResource(id = R.string.personal_information))
        }
        Button(
            onClick = onSignOut,
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(text = stringResource(id = R.string.sign_out))
        }
    }
}

