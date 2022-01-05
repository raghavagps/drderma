package com.example.drderma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button bt_signIn;
    FirebaseAuth mAuth;
    TextView signUpText;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Objects.requireNonNull(getSupportActionBar()).hide();
        bt_signIn=findViewById(R.id.btnSignIn);
        etEmail = findViewById(R.id.etEmail2);
        etPassword = findViewById(R.id.etPassword2);
        signUpText = findViewById(R.id.tv_clickSignIn);
        progressBar=findViewById(R.id.progressbar2);
        mAuth = FirebaseAuth.getInstance();



        bt_signIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = etEmail.getText().toString().trim();
                String password= etPassword.getText().toString().trim();
                if(TextUtils.isEmpty(email)){
                    etEmail.setError("Email can't be empty");
                }
                else if(TextUtils.isEmpty(password)){
                    etPassword.setError("Password can't be empty");
                }
                else {
                    showProgressBar();
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            hideProgressBar();
                            if (task.isSuccessful()) {
                                if(mAuth.getCurrentUser().isEmailVerified()){
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"Please verify your email before signing in",Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}