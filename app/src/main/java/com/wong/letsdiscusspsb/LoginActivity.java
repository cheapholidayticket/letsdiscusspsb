package com.wong.letsdiscusspsb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    //initialize variables
    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    Button buttonLogin;
    TextView forgotPassword;
    TextView signUp;
    ProgressDialog mLoadingBar;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUp = findViewById(R.id.signUp);
        mLoadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        mRef = FirebaseDatabase.getInstance().getReference().child("Users");

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));

            }
        });
    }

    private void Login() {
        String email = inputEmail.getEditText().getText().toString();
        String password = inputPassword.getEditText().getText().toString();

        //validate email string only accepts @outlook.com during test,
        //we can also accept only @coventry email to accept only coventry students registration
        if (!email.contains("@uni.coventry.ac.uk") & !email.contains("@outlook.com"))  //if does not show @outlook it will show error
        {
            showError(inputEmail, "Student email or outlook users accepted only");
            //password length must be > 5 characters
        }
        else if (password.isEmpty() || password.length() < 5 )
        {
            showError(inputPassword, "Password length must be greater than 5 characters");
            //confirm password must be = to password
        }
        else
        {
            mLoadingBar.setTitle("Log In");
            mLoadingBar.setMessage("Logging In Progress");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        mLoadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "Log in Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, SetupUser.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        mUser=mAuth.getCurrentUser();
                        CheckUserExistance();
                    } else
                    {
                        mLoadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    private void CheckUserExistance() {

        if(mUser != null)
        {
            mRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists())
                    {
                        mLoadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, "Sending to Main Activity", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent (LoginActivity.this, MainActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {

                        Toast.makeText(LoginActivity.this, "Sending to Setup Activity", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent (LoginActivity.this, ProfileActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    Toast.makeText(LoginActivity.this, ""+error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            Toast.makeText(this, "User not Exist", Toast.LENGTH_SHORT).show();
        }

    }

    private void showError(TextInputLayout inputEmail, String s) {
        inputEmail.setError(s);
        inputEmail.requestFocus();

    }
}