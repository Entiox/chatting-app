package hr.ferit.drazen.antunovic.chatier.service

interface NotificationService {
    suspend fun sendNotification(deviceToken: String, title: String, body: String)
}
