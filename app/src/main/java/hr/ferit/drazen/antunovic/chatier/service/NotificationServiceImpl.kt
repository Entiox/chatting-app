package hr.ferit.drazen.antunovic.chatier.service

import hr.ferit.drazen.antunovic.chatier.data.Notification
import hr.ferit.drazen.antunovic.chatier.data.NotificationRequest
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class NotificationServiceImpl : NotificationService {
    private val messagingUrl = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "AAAAuoIa5zM:APA91bFANBVKM-lwCjo2cp2RaY1mKt4xAvHV8d3k5xo5L4Qq_neof3zigobUilJ-uSa_Y" +
                "HYX0K-y0kjEvuj_oGnCuI2Dn_5ucsl2M9IhtPMtcXEZJeLHw-ttfa7gK1Wb1QYTZFbcUUMK"

    private val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }

    override suspend fun sendNotification(deviceToken: String, title: String, body: String) {
        client.post {
            url(messagingUrl)
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "key=$serverKey")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(
                NotificationRequest(
                    notification = Notification(body = body, title = title),
                    to = deviceToken,
                )
            )
        }
    }
}
