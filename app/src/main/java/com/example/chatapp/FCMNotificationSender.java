package com.example.chatapp;

import android.content.Context;

import com.example.chatapp.utils.FirebaseUtil;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class FCMNotificationSender {

    private static final String PROJECT_ID = "chat-app-backend-project"; // Replace with your actual Firebase project ID
    private static final String FCM_ENDPOINT = "https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send";
    private static final String SERVICE_ACCOUNT_FILE = "service.json"; // Path to your service account JSON

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    // Function to send notification
    public static void sendPushNotification(Context context,String fcmToken, String username, String message) throws IOException {
        String accessToken = getAccessToken(context);

        // Construct the FCM v1 request body
        JsonObject jsonMessage = new JsonObject();
        JsonObject messageObject = new JsonObject();
        JsonObject notification = new JsonObject();
        JsonObject data = new JsonObject(); // 👈 data field

        // Notification payload
        notification.addProperty("title", username); // show as notification title
        notification.addProperty("body", message);   // show as notification message

        // Data payload (this won't be shown in notification but available in intent)
        data.addProperty("userId", FirebaseUtil.currentUserId()); // 👈 userId from sender

        // Final message structure
        messageObject.add("notification", notification); // visual notification
        messageObject.add("data", data);                 // extra key-value data
        messageObject.addProperty("token", fcmToken);    // recipient's FCM token

        jsonMessage.add("message", messageObject);

        Request request = new Request.Builder()
                .url(FCM_ENDPOINT)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        gson.toJson(jsonMessage),
                        MediaType.get("application/json")))
                .build();


        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error sending FCM message: " + response.body().string());
            } else {
                System.out.println("Notification sent successfully: " + response.body().string());
            }
        }
    }

    // Function to get access token from service account
    private static String getAccessToken(Context context) throws IOException {
        InputStream serviceAccount = context.getAssets().open("service.json");

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(serviceAccount)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}

