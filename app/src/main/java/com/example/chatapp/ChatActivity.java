package com.example.chatapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.adapter.ChatRecyclerAdapter;
import com.example.chatapp.model.ChatMessageModel;
import com.example.chatapp.model.ChatroomModel;
import com.example.chatapp.model.UserModel;
import com.example.chatapp.utils.AccessToken;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUser;
    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;
    RecyclerView recyclerView;
    ImageView imageView;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);

    otherUser= AndroidUtil.getUserModelFromIntent(getIntent());
    chatroomId= FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(),otherUser.getUserId());

      messageInput = findViewById(R.id.chat_message_input);
      sendMessageBtn = findViewById(R.id.message_send_btn);
      backBtn = findViewById(R.id.back_btn);
      otherUsername = findViewById(R.id.other_username);
      recyclerView = findViewById(R.id.chat_recycler_view);
      imageView = findViewById(R.id.profile_pic_image_view);

      otherUsername.setText(otherUser.getUserName());

      FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl().addOnCompleteListener(t -> {

          if (t.isSuccessful()){
              Uri uri=t.getResult();
              AndroidUtil.setProfilePic(this, uri, imageView);
          }
      });

      backBtn.setOnClickListener((v) -> {
          onBackPressed();
      });


      sendMessageBtn.setOnClickListener(view -> {
          String message=messageInput.getText().toString().trim();
          if (message.isEmpty())
              return;
          senMessageToUser(message);
      });

      getOrCreateChatroomModel();
      setupChatRecyclerView();
  }

    private void setupChatRecyclerView() {

        Query query= FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp",Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options=new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();

        adapter=new ChatRecyclerAdapter(options, getApplicationContext());
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void senMessageToUser(String message) {

      chatroomModel.setLastMessageTimestamp(Timestamp.now());
      chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
      chatroomModel.setLastMessage(message);
      FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

      ChatMessageModel chatMessageModel=new ChatMessageModel(message,FirebaseUtil.currentUserId(),Timestamp.now());
      FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
          @Override
          public void onComplete(@NonNull Task<DocumentReference> task) {

              if (task.isSuccessful()){
                  messageInput.setText("");
                  sendNotification(message);

              }
          }
      });
  }

    void sendNotification(String message){

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                UserModel currentUser = task.getResult().toObject(UserModel.class);
                try{
//                    JSONObject jsonObject  = new JSONObject();
//
//                    JSONObject notificationObj = new JSONObject();
//                    notificationObj.put("title",currentUser.getUserName());
//                    notificationObj.put("body",message);
//
//                    JSONObject dataObj = new JSONObject();
//                    dataObj.put("userId",currentUser.getUserId());
//
//                    jsonObject.put("notification",notificationObj);
//                    jsonObject.put("data",dataObj);
//                    AtomicReference<String> tk=new AtomicReference<>("");
//
//                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(t -> {
//                        if (t.isSuccessful()){
//                            tk.set(t.getResult());
//                            FirebaseUtil.currentUserDetails().update("fcmToken",tk);
//                        }
//                    });
//                    jsonObject.put("token",tk);
//
//                    callApi(jsonObject);

                    FCMNotificationSender.sendPushNotification(getApplicationContext(),currentUser.getFcmToken(), currentUser.getUserName(), message);

                }catch (Exception e){
                    Log.d("TAG", "sendNotification: "+e.getMessage());
                }

            }
        });

    }

  void callApi(JSONObject jsonObject) {

    MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    // String url = "https://fcm.googleapis.com/fcm/send";
    final String url =
        "https://fcm.googleapis.com/v1/projects/chat-app-backend-project/messages:send";
    RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
    Request request =
        new Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer "+ AccessToken.getAccessToken())
            .build();
    client
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(@NonNull Call call, @NonNull IOException e) {
                  Log.d("TAG", "onFailure: "+e.getMessage());
              }

              @Override
              public void onResponse(@NonNull Call call, @NonNull Response response)
                  throws IOException {}
            });
  }

  private void getOrCreateChatroomModel() {

      FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
          if (task.isSuccessful()){
              chatroomModel=task.getResult().toObject(ChatroomModel.class);
          }
          if (chatroomModel==null){
              //first time chat
              chatroomModel=new ChatroomModel(
                      chatroomId,
                      Arrays.asList(FirebaseUtil.currentUserId(),otherUser.getUserId()),
                      Timestamp.now(),
                      ""
              );
              FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
          }
      });
    }
}
