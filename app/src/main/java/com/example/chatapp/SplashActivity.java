package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapp.model.UserModel;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

public class SplashActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    runOnUiThread(() -> {
      StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy(policy);
      String userId = getIntent().getStringExtra("userId");
Log.d("TAG", String.valueOf(userId));

      if (FirebaseUtil.isLoggedIn() && userId != null) {
        // Safe to use userId now
        FirebaseUtil.allUserCollectionReference().document(userId)
                .get()
                .addOnCompleteListener(task -> {
                  if (task.isSuccessful()) {
                    UserModel model = task.getResult().toObject(UserModel.class);

                    Intent mainIntent = new Intent(this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(mainIntent);

                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    AndroidUtil.passUserModelAsIntent(intent, model);
                    startActivity(intent);

                    finish();
                  }
                });
      } else {
        // Normal splash screen delay and login or main activity navigation
        new Handler().postDelayed(() -> {
          if (FirebaseUtil.isLoggedIn()) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
          } else {
            startActivity(new Intent(SplashActivity.this, LoginPhoneNumberActivity.class));
          }
          finish();
        }, 1000);
      }


    });


  }
}
