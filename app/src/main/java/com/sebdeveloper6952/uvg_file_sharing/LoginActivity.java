package com.sebdeveloper6952.uvg_file_sharing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity
{
    private Button btnLogin;
    private Button btnRegister;
    private EditText eTxtEmail;
    private EditText eTxtPassword;
    private FirebaseAuth mAuth;
    private final int RC_SIGN_IN = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        prepareViews();
        mAuth = FirebaseAuth.getInstance();
        checkUserSignedIn();
    }

    protected void checkUserSignedIn()
    {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            // user is already signed in, proceed to home activity
        }
    }

    protected void createUser(final String email, final String password)
    {
        if(!isEmailValid(email) || !isPasswordValid(password))
        {
            Toast.makeText(getApplicationContext(), R.string.msg_create_new_user_failed,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            // user was created successfully, sign in
                            signInUser(email, password);
                        }
                        else
                        {
                            // creating user failed
                            Toast.makeText(getApplicationContext(),
                                    R.string.msg_create_new_user_failed, Toast.LENGTH_SHORT).show();
                            setLoginAndRegisterButtonsState(true);
                        }
                    }
                });
    }

    protected void signInUser(String email, String password)
    {
        if(!isEmailValid(email) || !isPasswordValid(password))
        {
            Toast.makeText(getApplicationContext(), R.string.msg_sign_in_failed, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            // sign in successful
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(intent);
                            setLoginAndRegisterButtonsState(true);
                        }
                        else
                        {
                            // sign in failed
                            Toast.makeText(getApplicationContext(), R.string.msg_sign_in_failed,
                                    Toast.LENGTH_SHORT).show();
                            setLoginAndRegisterButtonsState(true);
                        }
                    }
                });
    }

    protected boolean isEmailValid(String email)
    {
        if(!email.contains("@")) return false;
        String parts[] = email.split("@");
        if(parts[0].length() < 5) return false;
        if(!parts[1].equals("uvg.edu.gt")) return false;
        return true;
    }

    protected boolean isPasswordValid(String pass)
    {
        return pass.length() > 4;
    }

    protected void prepareViews()
    {
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        eTxtEmail = findViewById(R.id.eTxt_email);
        eTxtPassword = findViewById(R.id.eTxt_password);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sign in existent user
                setLoginAndRegisterButtonsState(false);
                signInUser(String.valueOf(eTxtEmail.getText()),
                        String.valueOf(eTxtPassword.getText()));
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View w) {
                // create new user and sign in
                setLoginAndRegisterButtonsState(false);
                createUser(String.valueOf(eTxtEmail.getText()),
                        String.valueOf(eTxtPassword.getText()));
            }
        });
    }

    protected void setLoginAndRegisterButtonsState(boolean on)
    {
        btnLogin.setEnabled(on);
        btnRegister.setEnabled(on);
    }
}
