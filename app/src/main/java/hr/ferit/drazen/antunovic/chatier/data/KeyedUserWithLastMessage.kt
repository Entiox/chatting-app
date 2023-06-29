package hr.ferit.drazen.antunovic.chatier.data

data class KeyedUserWithLastMessage(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val imagePath: String = "images/default.png",
    val lastMessage: String = "",
    val lastMessageTimeStamp: String = "",
)
