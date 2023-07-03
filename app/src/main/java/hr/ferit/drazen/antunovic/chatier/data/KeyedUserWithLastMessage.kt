package hr.ferit.drazen.antunovic.chatier.data

data class KeyedUserWithLastMessage(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val imagePath: String = "",
    val lastMessage: String = "",
    val lastMessageTimeStamp: String = "00:00:00 01.01.2001.",
)
