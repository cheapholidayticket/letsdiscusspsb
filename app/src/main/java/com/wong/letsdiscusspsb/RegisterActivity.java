package com.wong.letsdiscusspsb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    //initialize variables
    private TextInputLayout inputEmail;
    private TextInputLayout inputPassword;
    private TextInputLayout inputConfirmPassword;
    Button buttonRegister;
    TextView alreadyHaveAccount;
    FirebaseAuth mAuth;
    ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.confirmPassword);
        buttonRegister = findViewById(R.id.buttonLogin);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);
        mAuth = FirebaseAuth.getInstance();
        mLoadingBar = new ProgressDialog(this);

        //button register click listener
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Registration();
            }
        });

        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    //registration method
    private void Registration() {

        String email = inputEmail.getEditText().getText().toString();
        String password = inputPassword.getEditText().getText().toString();
        String confirmPassword = inputConfirmPassword.getEditText().getText().toString();

        //validate email string only accepts @outlook.com during test,
        //we can also accept only @coventry email to accept only coventry students registration
        if (email.isEmpty() & !email.contains("@outlook.com") || !email.contains("@uni.coventry.ac.uk"))
        {
            showError(inputEmail, "Student email or outlook users accepted only");

            //password length must be > 5 characters
        }
        else if (password.isEmpty() || password.length() < 5 )
        {
            showError(inputPassword, "Password length must be greater than 5 characters");
            //confirm password must be = to password
        }
        else if (!confirmPassword.equals(password))
        {
            showError(inputConfirmPassword, "Password does not Match");
        }
        else
        {
            mLoadingBar.setTitle("Registration");
            mLoadingBar.setMessage("Registration in Progress");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()) //registration success
                    {
                        mLoadingBar.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration is Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, SetupUser.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else  //if registration fail
                    {
                        mLoadingBar.dismiss();
                        Toast.makeText(RegisterActivity.this,
                                "Registration Unsuccessful, Please try again.",
                                Toast.LENGTH_SHORT).show();
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