package com.wong.letsdiscusspsb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupUser extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    CircleImageView profileImageView;
    EditText inputUsername;
    EditText inputCity;
    EditText inputCountry;
    EditText inputProfession;
    Button buttonSave;
    Uri imageUri;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef;
    StorageReference storageRef;
    ProgressDialog mLoadingBar;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_user);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Setup Profile");


        profileImageView = findViewById(R.id.profile_image);
        inputUsername = findViewById(R.id.inputUserName);
        inputCity = findViewById(R.id.inputCity);
        inputCountry = findViewById(R.id.inputCountry);
        inputProfession = findViewById(R.id.inputProfession);
        buttonSave = findViewById(R.id.buttonSave);
        mLoadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveData();
            }
        });

    }

    private void SaveData() {
        final String username = inputUsername.getText().toString();
        final String city = inputCity.getText().toString();
        final String country = inputCountry.getText().toString();
        final String profession = inputProfession.getText().toString();

        if (username.isEmpty() || username.length() < 3)
        {
            showError(inputUsername, "Username must be at least 3 characters");
        }
        else if (city.isEmpty())
        {
            showError(inputCity, "City cannot be blank");
        }
        else if (country.isEmpty())
        {
            showError(inputCountry, "Country cannot be blank");
        }
        else if (profession.isEmpty())
        {
            showError(inputProfession,"Profession cannot be blank");
        }
        else if (imageUri == null)
        {
            Toast.makeText(this, "Select profile image", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mLoadingBar.setTitle("Uploading User Profile");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            storageRef.child(mUser.getUid()).putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful())
                    {
                        storageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                    //pushing data to storage
                                    storageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            HashMap hashMap = new HashMap();
                                            hashMap.put("username", username);
                                            hashMap.put("city", city);
                                            hashMap.put("country", country);
                                            hashMap.put("profession", profession);
                                            hashMap.put("profileImage", uri.toString());
                                            hashMap.put("status", "offline");

                                            mRef.child(mUser.getUid()).updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener() {
                                                        @Override
                                                        public void onSuccess(Object o) {
                                                            Intent intent = new Intent(SetupUser.this, MainActivity.class);
                                                            startActivity(intent);
                                                            mLoadingBar.dismiss();
                                                            Toast.makeText(SetupUser.this, "User Profile Completed", Toast.LENGTH_SHORT).show();

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    mLoadingBar.dismiss();
                                                    Toast.makeText(SetupUser.this, e.toString(), Toast.LENGTH_SHORT).show();

                                                }
                                            });

                                        }
                                    });
                                }
                        });
                    }
                }
            });
        }
    }

    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }

    }
}