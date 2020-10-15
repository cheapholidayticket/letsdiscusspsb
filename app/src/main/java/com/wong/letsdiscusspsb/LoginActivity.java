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

public class LoginActivity extends AppCompatActivity {

    //initialize variables
    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    Button buttonLogin;
    TextView forgotPassword;
    TextView signUp;
    ProgressDialog mLoadingBar;
    FirebaseAuth mAuth;


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
        if (email.isEmpty() || !email.contains("@outlook.com"))
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
                        Toast.makeText(LoginActivity.this, "Log In Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, SetupUser.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else
                    {
                        mLoadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    private void showError(TextInputLayout inputEmail, String s) {
        inputEmail.setError(s);
        inputEmail.requestFocus();

    }
}