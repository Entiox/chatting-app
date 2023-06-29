package hr.ferit.drazen.antunovic.chatier.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    @SerialName("notification")
    val notification: Notification = Notification(),
    @SerialName("to")
    val to: String = "",
)

@Serializable
data class Notification(
    @SerialName("body")
    val body: String = "",
    @SerialName("title")
    val title: String = "",
)
