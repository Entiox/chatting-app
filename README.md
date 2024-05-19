# chatting-app
Android application made with Jetpack Compose that enables realtime chatting. Firebase functionalities were used in application and these are: Authentication, Realtime Database, Storage and Cloud Messaging.

To run this application properly you will need to setup and connect Firebase with Android Studio, copy link of your realtime database and put it inside this function:
```kotlin
private val database = Firebase.database()
```
Also enable legacy Cloud Messaging API and get your server key for Firebase Cloud Messaging and assign it to the:
```kotlin
private val serverKey
```
FCM API used inside this application is deprecated and you will need to migrate to the latest FCM API (HTTP v1) to use cloud messaging in the future.
