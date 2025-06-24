package com.example.chatapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapp.utils.AndroidUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {

  String phoneNumber;
  Long timeoutSeconds = 30L;
  String verificationCode;
  PhoneAuthProvider.ForceResendingToken resendingToken;

  EditText otpInput;
  Button nextBtn;
  ProgressBar progressBar;
  TextView resendOtpTextView;
  FirebaseAuth mAuth = FirebaseAuth.getInstance();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login_otp);

    otpInput = findViewById(R.id.login_otp);
    nextBtn = findViewById(R.id.login_next_btn);
    progressBar = findViewById(R.id.login_progress_bar);
    resendOtpTextView = findViewById(R.id.resend_otp_textview);

    phoneNumber=getIntent().getStringExtra("phone");
    AndroidUtil.showToast(getApplicationContext(), phoneNumber);
    sendOtp(phoneNumber,false);
    setInProgress(true);

    nextBtn.setOnClickListener(view -> {

      String enteredotp=otpInput.getText().toString();
      PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationCode, enteredotp);
      signIn(credential);
    });

    resendOtpTextView.setOnClickListener(view -> {
      sendOtp(phoneNumber, true);
    });

  }

  void sendOtp(String phoneNumber,boolean isResend){
    startResendTimer();
    setInProgress(true);
    PhoneAuthOptions.Builder builder =
            PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                      @Override
                      public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signIn(phoneAuthCredential);
                        AndroidUtil.showToast(getApplicationContext(),"OTP verification s");
                        setInProgress(false);
                      }

                      @Override
                      public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e("OTP_ERROR", "Verification failed", e);
                        AndroidUtil.showToast(getApplicationContext(),"OTP verification failed");
                        setInProgress(false);
                      }

                      @Override
                      public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationCode = s;
                        resendingToken = forceResendingToken;
                        AndroidUtil.showToast(getApplicationContext(),"OTP sent successfully");
                        setInProgress(false);
                      }
                    });
    if(isResend){
      PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
    }else{
      PhoneAuthProvider.verifyPhoneNumber(builder.build());
    }

  }

  private void startResendTimer() {
    resendOtpTextView.setEnabled(false);
    Timer timer=new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        timeoutSeconds--;
        resendOtpTextView.setText("Resend OTP in "+timeoutSeconds+" seconds");
        if (timeoutSeconds<=0){
          timeoutSeconds=60L;
          timer.cancel();
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              resendOtpTextView.setEnabled(true);
              SpannableString spannable = new SpannableString("Resend OTP");
              spannable.setSpan(
                      new ForegroundColorSpan(Color.BLUE), // apply to 'OTP'
                      0,
                      10,
                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
              );
              resendOtpTextView.setText(spannable);
            }
          });
        }
      }
    }, 0, 1000);
  }

  void signIn(PhoneAuthCredential phoneAuthCredential){
    //log in and got to next activity

    setInProgress(true);
    mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
      @Override
      public void onComplete(@NonNull Task<AuthResult> task) {

        setInProgress(false);
        if (task.isSuccessful()){
          Intent intent=new Intent(LoginOtpActivity.this,LoginUsernameActivity.class);
          intent.putExtra("phone", phoneNumber);
          AndroidUtil.showToast(getApplicationContext(), "OTP verification successful!");
          startActivity(intent);
        }else {
          AndroidUtil.showToast(getApplicationContext(), "OTP verification failed!");
        }
      }
    });

  }

  void setInProgress(boolean inProgress){

    if (inProgress){

      progressBar.setVisibility(View.VISIBLE);
      nextBtn.setVisibility(View.GONE);

    }else {
      progressBar.setVisibility(View.GONE);
      nextBtn.setVisibility(View.VISIBLE);

    }
  }
}
