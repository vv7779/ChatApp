package com.example.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chatapp.model.UserModel;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class ProfileFragment extends Fragment {

    ImageView profilePic;
    EditText usernameInput;
    EditText phoneInput;
    Button updateProfileBtn;
    ProgressBar progressBar;
    TextView logoutBtn;
    UserModel currentUserModel;
    ActivityResultLauncher<Intent> imagePicklauncher;
    Uri selectedImageUri;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePicklauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result->{
            if (result.getResultCode()== Activity.RESULT_OK){
                Intent data=result.getData();
                if (data!=null && data.getData()!=null){
                    selectedImageUri=data.getData();
                    AndroidUtil.setProfilePic(getContext(), selectedImageUri, profilePic);
                }
            }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_profile, container, false);

        profilePic = view.findViewById(R.id.profile_image_view);
        usernameInput = view.findViewById(R.id.profile_username);
        phoneInput = view.findViewById(R.id.profile_phone);
        updateProfileBtn = view.findViewById(R.id.profle_update_btn);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        logoutBtn = view.findViewById(R.id.logout_btn);

        getuserData();

        updateProfileBtn.setOnClickListener(view1 -> {
            updatebtnClick();
        });

        logoutBtn.setOnClickListener(view1 -> {

            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){
                        FirebaseUtil.logout();
                        Intent intent=new Intent(getContext(), SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                }
            });

        });

        profilePic.setOnClickListener(v->{
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).
                    createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePicklauncher.launch(intent);
                            return null;
                        }
                    });
        });

        return view;
    }

    private void updatebtnClick() {
        String newusername=usernameInput.getText().toString();
        if (newusername.isEmpty()||newusername.length()<3){
            usernameInput.setError("username must be atleast 3 chars!");
            return;
        }
        currentUserModel.setUserName(newusername);
        setInProgress(true);

        if (selectedImageUri!=null){
            FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectedImageUri).
                    addOnCompleteListener(task -> {
                       updateToFirestore();
                    });
        }else {

                updateToFirestore();
            }
    }

    private void updateToFirestore(){
        FirebaseUtil.currentUserDetails().set(currentUserModel).addOnCompleteListener(task -> {

            setInProgress(false);
            if (task.isSuccessful()){
               AndroidUtil.showToast(getContext(), "Profile updated successfully");
           }else {
               AndroidUtil.showToast(getContext(), "Profile update failed");
           }
        });
    }

    private void getuserData() {

        setInProgress(true);

        FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl().addOnCompleteListener(task -> {

            if (task.isSuccessful()){
                Uri uri=task.getResult();
                AndroidUtil.setProfilePic(getContext(), uri, profilePic);
            }
        });

        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            currentUserModel=task.getResult().toObject(UserModel.class);
            usernameInput.setText(currentUserModel.getUserName());
            phoneInput.setText(currentUserModel.getPhone());

        });
    }

    void setInProgress(boolean inProgress){

        if (inProgress){

            progressBar.setVisibility(View.VISIBLE);
            updateProfileBtn.setVisibility(View.GONE);

        }else {
            progressBar.setVisibility(View.GONE);
            updateProfileBtn.setVisibility(View.VISIBLE);

        }
    }
}