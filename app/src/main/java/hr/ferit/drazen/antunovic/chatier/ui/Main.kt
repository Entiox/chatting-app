package hr.ferit.drazen.antunovic.chatier.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import hr.ferit.drazen.antunovic.chatier.ui.routes.chat.Chat
import hr.ferit.drazen.antunovic.chatier.ui.routes.chat.ChatViewModelFactory
import hr.ferit.drazen.antunovic.chatier.ui.routes.fill_personal_information.FillInformation
import hr.ferit.drazen.antunovic.chatier.ui.routes.home.Home
import hr.ferit.drazen.antunovic.chatier.ui.routes.personal_information.PersonalInformation
import hr.ferit.drazen.antunovic.chatier.ui.routes.signin.SignIn
import hr.ferit.drazen.antunovic.chatier.ui.routes.signup.SignUp
import hr.ferit.drazen.antunovic.chatier.ui.routes.start.Start

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Main(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { })
    var isPermissionLauncherLaunched by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.background,
    ) {
        NavHost(
            navController = navController,
            startDestination = "start",
        ) {
            composable("start") {
                if(!isPermissionLauncherLaunched) {
                    isPermissionLauncherLaunched = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.POST_NOTIFICATIONS))
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                    }
                }
                Start(
                    onNavigateToSignIn = { navController.navigate("signin") },
                    onNavigateToSignOut = { navController.navigate("signup") },
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("start") {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToFillPersonalInformation = {
                        navController.navigate("fillpersonalinformation") {
                            popUpTo("start") {
                                inclusive = true
                            }
                        }
                    },
                    viewModel = viewModel(),
                )
            }
            composable("signin") {
                SignIn(
                    modifier = Modifier.padding(all = 25.dp),
                    viewModel = viewModel(),
                    onNavigateToFillPersonalInformation = {
                        navController.navigate("fillpersonalinformation") {
                            popUpTo("start") {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("start") {
                                inclusive = true
                            }
                        }
                    },
                )
            }
            composable("signup") {
                SignUp(
                    modifier = Modifier.padding(all = 25.dp),
                    viewModel = viewModel(),
                    onNavigateToFillPersonalInformation = {
                        navController.navigate("fillpersonalinformation") {
                            popUpTo("start") {
                                inclusive = true
                            }
                        }
                    },
                )
            }
            composable("fillpersonalinformation") {
                FillInformation(
                    modifier = Modifier.padding(all = 25.dp),
                    viewModel = viewModel(), onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("fillpersonalinformation") {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable("home") {
                Home(modifier = Modifier.padding(top = 20.dp),
                    viewModel = viewModel(),
                    onNavigateToStart = {
                        navController.navigate("start") {
                            popUpTo("home") {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToChat = { navController.navigate("chat/$it") },
                    onNavigateToPersonalInformation = { navController.navigate("personalinformation") }
                )
            }
            composable(
                route = "chat/{personUid}",
                arguments = listOf(navArgument("personUid") { type = NavType.StringType })
            ) {
                val personId = it.arguments!!.getString("personUid")!!
                Chat(viewModel = viewModel(factory = ChatViewModelFactory(personId)), onNavigateToStart = {
                        navController.navigate("start") {
                            popUpTo("home") {
                                inclusive = true
                            }
                        }
                    },
                )
            }
            composable("personalinformation") {
                PersonalInformation(
                    modifier = Modifier.fillMaxSize(), viewModel = viewModel(),
                    onNavigateToStart = {
                        navController.navigate("start") {
                            popUpTo("home") {
                                inclusive = true
                            }
                        }
                    },
                )
            }
        }
    }
}
