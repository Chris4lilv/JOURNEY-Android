package com.example.android.album;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignInActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    TextView signUpPage;

    Button signInButton;
    Button confirmButton;

    EditText inputEmail;
    EditText inputPassword;
    EditText inputUsername;

    CardView signInCardView;


    TextView hint;

    ImageView mCloseSignUp;

    CheckBox passwordReveal;

    Animation shake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        signUpPage = findViewById(R.id.signUpPage);
        signInButton = findViewById(R.id.signInButton);
        confirmButton = findViewById(R.id.confirm_button);

        signInCardView = findViewById(R.id.signin_card_view);

        hint = findViewById(R.id.hint);

        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        inputUsername = findViewById(R.id.input_username);
        mCloseSignUp = findViewById(R.id.close_sign_up);

        passwordReveal = findViewById(R.id.password_reveal);

        //Edit text animation when it is empty
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);


        //This will underline the SIGNUP text
        signUpPage.setPaintFlags(signUpPage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mCloseSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("SignIn");
            }
        });

        signUpPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUI("SignUp");

            }
        });

        //Disable password input if the email input is empty
        inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    inputPassword.setEnabled(true);
                    //This will hide password when type in
                    inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                }else{
                    inputPassword.setEnabled(false);
                }
            }
        });


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                if(email.isEmpty() && password.isEmpty()) {
                    inputEmail.startAnimation(shake);
                    inputPassword.startAnimation(shake);
                }else if(password.isEmpty()){
                    inputPassword.startAnimation(shake);
                }else{
                    signIn(email, password);
                }

            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                String username = inputUsername.getText().toString();
                if(email.isEmpty() && password.isEmpty() && username.isEmpty()) {
                    inputEmail.startAnimation(shake);
                    inputPassword.startAnimation(shake);
                    inputUsername.startAnimation(shake);
                }else if(username.isEmpty()){
                    inputPassword.startAnimation(shake);
                }else if(email.isEmpty()){
                    inputEmail.startAnimation(shake);
                }else if(password.isEmpty()){
                    inputPassword.startAnimation(shake);
                }else{
                    signUp(email, password, username);
                }

            }
        });

        passwordReveal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    inputPassword.setTransformationMethod(null);
                } else
                    inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        proceedToMain(currentUser);
    }

    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            proceedToMain(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            proceedToMain(null);
                        }
                    }
                });

    }

    /**
     * Sign up method
     */
    public void signUp(String email, String password, final String username){

        final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            updateUI("SignIn");

                            // Sign in success, update UI with the signed-in user's information
                            final FirebaseUser user = mAuth.getCurrentUser();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignInActivity.this, "Sign Up Success! " + username, Toast.LENGTH_SHORT).show();
                                                proceedToMain(user);
                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Proceed to Main Panel
     * @param user
     */
    private void proceedToMain(FirebaseUser user){
        if(user == null){
            inputPassword.setText("");
            inputEmail.setText("");
        }else{
            String userName = user.getDisplayName();
            String userEmail = user.getEmail();

            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            intent.putExtra("userName", userName);
            intent.putExtra("userEmail", userEmail);
            startActivity(intent);
        }
    }

    /**
     * update SignIn UI
     */
    private void updateUI(String viewStatus){

        inputEmail.setText("");
        inputPassword.setText("");
        inputUsername.setText("");

        if(viewStatus.equals("SignUp")){
            signInCardView.setCardBackgroundColor( getResources().getColor(R.color.signUpBackground));
            inputUsername.setVisibility(View.VISIBLE);
            hint.setVisibility(View.GONE);
            signUpPage.setVisibility(View.GONE);
            signInButton.setVisibility(View.GONE);
            confirmButton.setVisibility(View.VISIBLE);
        }else if(viewStatus.equals("SignIn")){
            signInCardView.setCardBackgroundColor( getResources().getColor(R.color.cardview_light_background));
            inputUsername.setVisibility(View.GONE);
            hint.setVisibility(View.VISIBLE);
            signUpPage.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.GONE);

        }



    }
}
