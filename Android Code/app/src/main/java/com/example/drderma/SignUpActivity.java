package com.example.drderma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "GOOGLE SIGN IN";
    private final static  int RC_SIGN_IN = 9999;
    private FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    ImageButton bt_googleSignIn;
    ProgressBar progressBar;
    GoogleSignInClient mGoogleSignInClient;
    Button bt_signup;
    TextView loginText;
    EditText et_email, et_password, et_confirmPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Objects.requireNonNull(getSupportActionBar()).hide();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        bt_googleSignIn = findViewById(R.id.bt_google);
        progressBar = findViewById(R.id.progressbar);
        bt_signup = findViewById(R.id.btnSignUp);
        et_email = findViewById(R.id.etEmail);
        et_password = findViewById(R.id.etPassword);
        et_confirmPassword = findViewById(R.id.etConfirmPassword);
        loginText = findViewById(R.id.tv_clickSignUp);

        bt_googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Button Clicked");
                showProgressBar();
                createGoogleSignInRequest();
                signIn();
            }
        });

        bt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString().trim();
                String password_1 = et_password.getText().toString().trim();
                String password_2 = et_confirmPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    et_email.setError("Email can't be empty");
                    et_email.requestFocus();
                }
                if (TextUtils.isEmpty(password_1)) {
                    et_password.setError("Password can't be empty");
                    et_password.requestFocus();
                }
                else if (TextUtils.isEmpty(password_2)) {
                    et_confirmPassword.setError("Please confirm your password");
                    et_confirmPassword.requestFocus();
                }
                else if (password_1.length() <= 6) {
                    et_password.setError("Password must be greater than 6 letters");
                    et_password.requestFocus();
                }
                else if (!password_1.equals(password_2)) {
                    et_confirmPassword.setError("Password didn't match");
                    et_confirmPassword.requestFocus();
                }
                else {
                    showProgressBar();
                    emailSignup(email, password_1);
                }
            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            }
        });

    }

    private void  emailSignup(String email, String password){
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideProgressBar();
                if (task.isSuccessful()){
                    Log.e("EMAIL SIGN UP","User Created");
                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(),"Account created successfully. Please verify your email before signing in.",Toast.LENGTH_LONG).show();
                                startActivity(new Intent(SignUpActivity.this,SignInActivity.class));
                                finish();
                            }
                        }
                    });
                }
                else{
                    Log.e("EMAIL SIGN UP",task.getException().getMessage());
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

    }



    private void createGoogleSignInRequest() {
        // Configure Google Sign In
        Log.e(TAG,"Building google sign in request");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("172454740982-smnukt96iqdt7ahk5l9cvmcvardd2dpa.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Log.e(TAG,"Inside SignIn method");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.e(TAG,"Inside firebase auth with google ");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
                            hideProgressBar();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            hideProgressBar();
                            Toast.makeText(getApplicationContext(),"Sign-in Failed",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        hideProgressBar();
        Log.e(TAG,"Inside on Activity result");
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
//                hideProgressBar();
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.e(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                hideProgressBar();
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    public void showProgressBar() {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}