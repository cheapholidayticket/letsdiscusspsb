package com.wong.letsdiscusspsb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 11;
    CircleImageView profileImageView;
    EditText inputUserName;
    EditText inputCity;
    EditText inputUniversity;
    EditText inputCountry;
    Button btnUpdateProfile;

    DatabaseReference mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    Uri imageUri;
    DatabaseReference mRef;
    StorageReference storageRef;

    ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profileCircleImageView);
        inputUserName = findViewById(R.id.inputUserName);
        inputCity = findViewById(R.id.inputCity);
        inputUniversity = findViewById(R.id.inputUniversity);
        inputCountry = findViewById(R.id.inputCountry);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);


        mLoadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profileImageUrl = snapshot.child("profileImage").getValue().toString();
                    final String city = snapshot.child("city").getValue().toString();
                    final String country = snapshot.child("country").getValue().toString();
                    final String university = snapshot.child("profession").getValue().toString();
                    final String username = snapshot.child("username").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(profileImageView);
                    inputCity.setText(city);
                    inputUserName.setText(username);
                    inputCountry.setText(country);
                    inputUniversity.setText(university);

                } else {
                    Toast.makeText(ProfileActivity.this, "Data does not exist", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });


        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveData();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }

    }
    private void showError(EditText input, String s) {
        input.setError(s);
        input.requestFocus();

    }

    private void SaveData() {




        final String username = inputUserName.getText().toString();
        final String city = inputCity.getText().toString();
        final String country = inputCountry.getText().toString();
        final String university = inputUniversity.getText().toString();

        if (username.isEmpty() || username.length() < 3)
        {
            showError(inputUserName, "Username must be at least 3 characters");
        }
        else if (city.isEmpty())
        {
            showError(inputCity, "City cannot be blank");
        }
        else if (country.isEmpty())
        {
            showError(inputCountry, "Country cannot be blank");
        }
        else if (university.isEmpty())
        {
            showError(inputUniversity,"University cannot be blank");
        }
        else if (imageUri == null)
        {
            Toast.makeText(this, "Select profile image", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mLoadingBar.setTitle("Updating User Profile");
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
                                                hashMap.put("profession", university);
                                                hashMap.put("profileImage", uri.toString());
                                                hashMap.put("status", "offline");

                                                mRef.child(mUser.getUid()).updateChildren(hashMap)
                                                        .addOnSuccessListener(new OnSuccessListener() {
                                                            @Override
                                                            public void onSuccess(Object o) {
                                                                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                                                startActivity(intent);
                                                                mLoadingBar.dismiss();
                                                                Toast.makeText(ProfileActivity.this, "User Profile Completed", Toast.LENGTH_SHORT).show();

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        mLoadingBar.dismiss();
                                                        Toast.makeText(ProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

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
}