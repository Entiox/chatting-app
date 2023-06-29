package hr.ferit.drazen.antunovic.chatier.data

data class KeyedUser(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val imagePath: String = "images/default.png",
)
