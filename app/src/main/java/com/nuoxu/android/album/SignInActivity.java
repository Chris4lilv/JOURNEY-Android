package com.nuoxu.android.album;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.view.animation.AccelerateDecelerateInterpolator;
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
    Button cancelSignUpButton;

    EditText signInEmail;
    EditText signInPassword;
    EditText signUpUsername;
    EditText signupEmail;
    EditText signupPassword;


    CardView signInCardView;


    TextView hint;

    ImageView mCloseSignUp;

    CheckBox passwordReveal;

    Animation shake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_new);

        mAuth = FirebaseAuth.getInstance();

        signUpPage = findViewById(R.id.signUpPage);
        signInButton = findViewById(R.id.signInButton);
        confirmButton = findViewById(R.id.confirm_button);

        final EditText signUpPassWordEditText = findViewById(R.id.signup_password_edittext);

        signInCardView = findViewById(R.id.signin_card_view);

        hint = findViewById(R.id.hint);

        //Initialize edit text
        signInEmail = findViewById(R.id.signin_email_edittext);
        signInPassword = findViewById(R.id.signin_password_edittext);
        signUpUsername = findViewById(R.id.signup_username_edittext);
        signupEmail = findViewById(R.id.signup_email_edittext);
        signupPassword = findViewById(R.id.signup_password_edittext);
        mCloseSignUp = findViewById(R.id.close_sign_up);


        cancelSignUpButton = findViewById(R.id.cancel_button);

        passwordReveal = findViewById(R.id.password_reveal);

        //Edit text animation when it is empty
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);


        //This will underline the SIGNUP text
        signUpPage.setPaintFlags(signUpPage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        signUpPage.setOnClickListener(new NavigationIconClickListener(this,findViewById(R.id.upper_layer),new AccelerateDecelerateInterpolator()));
        cancelSignUpButton.setOnClickListener(new NavigationIconClickListener(this,findViewById(R.id.upper_layer)));
        //Disable password input if the email input is empty
        signInEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().isEmpty()){
                    signInPassword.setEnabled(true);
                    //This will hide password when type in
                    signInPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    signInPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                }else{
                    signInPassword.setEnabled(false);
                }
            }
        });


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = signInEmail.getText().toString();
                String password = signInPassword.getText().toString();
                if(email.isEmpty() && password.isEmpty()) {
                    signInEmail.startAnimation(shake);
                    signInPassword.startAnimation(shake);
                }else if(password.isEmpty()){
                    signInPassword.startAnimation(shake);
                }else{
                    signIn(email, password);
                }

            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isValid(signUpPassWordEditText.getText())){
                    signupPassword.setError("Password must contain at least 8 characters.");
                }else {
                    String email = signupEmail.getText().toString();
                    String password = signupPassword.getText().toString();
                    String username = signUpUsername.getText().toString();
                    if(email.isEmpty() && password.isEmpty() && username.isEmpty()) {
                        signupEmail.startAnimation(shake);
                        signupPassword.startAnimation(shake);
                        signUpUsername.startAnimation(shake);
                    }else if(username.isEmpty()){
                        signupPassword.startAnimation(shake);
                    }else if(email.isEmpty()){
                        signupEmail.startAnimation(shake);
                    }else if(password.isEmpty()){
                        signupPassword.startAnimation(shake);
                    }else{
                        signUp(email, password, username);
                    }
                }

            }
        });

        passwordReveal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    signInPassword.setTransformationMethod(null);
                } else
                    signInPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        proceedToMain(currentUser, false);
    }

    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            proceedToMain(user, false);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            proceedToMain(null, false);
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
                            clearEdittext();

                            // Sign in success, update UI with the signed-in user's information
                            final FirebaseUser user = mAuth.getCurrentUser();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignInActivity.this, "Sign Up Success! " + username, Toast.LENGTH_SHORT).show();
                                                proceedToMain(user, true);
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
    private void proceedToMain(FirebaseUser user, boolean justSignUp){
        if(user == null){
            signInEmail.setText("");
            signInPassword.setText("");
        }else{
            String userName = user.getDisplayName();
            String userEmail = user.getEmail();

            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            intent.putExtra("userName", userName);
            intent.putExtra("userEmail", userEmail);
            intent.putExtra("signUp", justSignUp);
            startActivity(intent);
        }
    }
    /**
     * Clear input scope.
     */
    private void clearEdittext(){
        signupEmail.setText("");
        signupPassword.setText("");
        signUpUsername.setText("");
        signInEmail.setText("");
        signInPassword.setText("");
    }

    private boolean isValid(@Nullable Editable text) {
        return text != null && text.length() >= 8;
    }
}
