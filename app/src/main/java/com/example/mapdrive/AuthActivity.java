package com.example.mapdrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapdrive.Models.Flag;
import com.example.mapdrive.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class AuthActivity extends AppCompatActivity {
    //Global
// https://ihateregex.io/
    private final String EMAILREG = "[^@ \\t\\r\\n]+@[^@ \\t\\r\\n]+\\.[^@ \\t\\r\\n]+";
    private final String PASSREG ="^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$";
    private final String USERNAMEREG ="^[a-z0-9_-]{3,15}$";
    private User user;
    private TextView tvHeader;
    private Stack<BottomSheetDialog> SheetStack = new Stack<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private BottomSheetDialog signUpBottomSheet;
    private EditText signUpEmail;
    private EditText signUpUsername;
    private EditText signUpPassword;
    private EditText signUpConfirmPassword;
    private BottomSheetDialog logInBottomSheet;
    private EditText logInEmail;
    private EditText logInPassword;
    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        // set content views of bottomsheetdialog
        signUpBottomSheet = new BottomSheetDialog(this);
        logInBottomSheet = new BottomSheetDialog(this);
        signUpBottomSheet.setContentView(R.layout.bottom_sheet_signup);
        logInBottomSheet.setContentView(R.layout.bottom_sheet_login);
        //typecasting
        View btnStart = findViewById(R.id.startBtn);
        tvHeader = findViewById(R.id.header);
        // sign up typecasting elements
        signUpEmail = signUpBottomSheet.findViewById(R.id.edRegisterEmail);
        signUpUsername = signUpBottomSheet.findViewById(R.id.edRegisterUsername);
        signUpPassword = signUpBottomSheet.findViewById(R.id.edRegisterPassword);
        signUpConfirmPassword = signUpBottomSheet.findViewById(R.id.edConfirmPassword);
        Button signUpSignUpBtn = signUpBottomSheet.findViewById(R.id.signUpSignUpBtn);
        Button signUpLogInBtn = signUpBottomSheet.findViewById(R.id.signUpLogInBtn);
        Flag emailFlag = new Flag();
        Flag usernameFlag = new Flag();
        Flag passwordFlag = new Flag();
        Flag confirmPasswordFlag = new Flag();
        //log in typecasting elements
        logInEmail = logInBottomSheet.findViewById(R.id.edLoginEmail);
        logInPassword = logInBottomSheet.findViewById(R.id.edLoginPassword);
        forgotPassword = logInBottomSheet.findViewById(R.id.tvForgotPassword);
        Button logInLogInBtn = logInBottomSheet.findViewById(R.id.logInLogInBtn);
        Button logInSignUpBtn = logInBottomSheet.findViewById(R.id.logInSignUpBtn);
        CustomHeader();
        btnStart.setOnClickListener(v -> {
            CycleBottomSheets(logInBottomSheet);
        });
        signUpEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                emailFlag.setFlag(MatchPatternEditText(signUpEmail,EMAILREG,"Invalid Email"));
            }
        });
        signUpUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                usernameFlag.setFlag(MatchPatternEditText(signUpUsername,USERNAMEREG,"Username must not have spaces or special characters and be between 3 and 15 characters"));
            }
        });
        signUpPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                passwordFlag.setFlag(MatchPatternEditText(signUpPassword,PASSREG,"Password must be more than 8 characters with at least 1 uppercase, lowercase and special character"));
            }
        });
        signUpConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                confirmPasswordFlag.setFlag(MatchTextEditText(signUpConfirmPassword,signUpPassword.getText().toString(),"Password does not match"));
            }
        });
        assert signUpSignUpBtn != null;
        signUpSignUpBtn.setOnClickListener(v -> {
            if(emailFlag.isFlag() & passwordFlag.isFlag() & usernameFlag.isFlag() & confirmPasswordFlag.isFlag())  {
                SignUp(signUpEmail.getText().toString(),signUpConfirmPassword.getText().toString()).addOnSuccessListener(a -> {
                    AddUserDocument(Objects.requireNonNull(a.getUser()).getUid(),signUpUsername.getText().toString(),"metric","popular")
                            .addOnSuccessListener(d ->
                            {
                                Intent intent = new Intent(AuthActivity.this,MainActivity.class);
                                startActivity(intent);
                                signUpEmail.setText("");
                                signUpPassword.setText("");
                                signUpUsername.setText("");
                                signUpConfirmPassword.setText("");
                            }).addOnFailureListener(e -> {
                                Log.e("Firestore Exception: ",e.toString());
                            });
                    Toast.makeText(this, "User Created", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e ->
                {
                    if(e instanceof FirebaseNetworkException){
                        Toast.makeText(this, "Network Error, Please check your connectivity", Toast.LENGTH_SHORT).show();
                    }
                    else if (e instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "Account is already in use", Toast.LENGTH_SHORT).show();
                        signUpEmail.setError("Account is already in use");
                    }
                    else{
                        Log.e("Sign Up Error: ",e.toString());
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Invalid Fields", Toast.LENGTH_SHORT).show();
            }
        });
        assert signUpLogInBtn != null;
        signUpLogInBtn.setOnClickListener(v -> {
            CycleBottomSheets(logInBottomSheet);
        });
        assert logInLogInBtn != null;
        logInLogInBtn.setOnClickListener(v -> {
            //Log In
            if(!logInEmail.getText().toString().isEmpty() & !logInPassword.getText().toString().isEmpty()) {
                LogIn(logInEmail.getText().toString(), logInPassword.getText().toString())
                        .addOnSuccessListener(a -> {
                            Intent intent = new Intent(AuthActivity.this,MainActivity.class);
                            startActivity(intent);
                            logInEmail.setText("");
                            logInPassword.setText("");
                        }).addOnFailureListener(e -> {
                            if (e instanceof FirebaseNetworkException) {
                                Toast.makeText(this, "Network Error, Please check your connectivity", Toast.LENGTH_SHORT).show();
                            }
                            else if(e instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
                            }else {
                                Log.e("Log In Error: ", e.toString());
                            }
                        });
            }
            else{
                Toast.makeText(this, "Invalid Fields", Toast.LENGTH_SHORT).show();
            }
        });
        // TODO Add dialog
        forgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Under Construction", Toast.LENGTH_SHORT).show();
        });
        assert logInSignUpBtn != null;
        logInSignUpBtn.setOnClickListener(v -> {
            CycleBottomSheets(signUpBottomSheet);
        });
    }
    private void CustomHeader() {
        String text = "Click below to start exploring";
        SpannableString ss = new SpannableString(text);
        ForegroundColorSpan fcs = new ForegroundColorSpan(getColor(R.color.orange));
        ss.setSpan(fcs, 0,11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvHeader.setText(ss);
    }
    // Add it to a stack to cycle between each on button press>>
    private void CycleBottomSheets(BottomSheetDialog newSheetDialog) {
        if(!SheetStack.isEmpty()) {
            SheetStack.pop().cancel();
        }
        SheetStack.add(newSheetDialog);
        newSheetDialog.setCanceledOnTouchOutside(false);
        newSheetDialog.show();
    }

    @NonNull
    private Task<AuthResult> SignUp(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        return mAuth.createUserWithEmailAndPassword(email,password);
    }
    @NonNull
    private Task<DocumentReference> AddUserDocument(String userUID, String username, String preferredUnit, String preferredLandmark)
    {
        mStore = FirebaseFirestore.getInstance();
        Map<String,User> newUser = new HashMap<>();
        user = new User(username);
        user.setPreferredLandmark(preferredLandmark);
        user.setPreferredUnit(preferredUnit);
        user.setSavedLocations(new ArrayList<>());
        newUser.put(userUID,user);
        return mStore.collection("users").add(newUser);
    }
    @NonNull
    private Task<AuthResult> LogIn(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        return mAuth.signInWithEmailAndPassword(email,password);
    }

    private boolean MatchPatternEditText(@NonNull EditText editText, String pattern, String errorMessage){
        if(!editText.getText().toString().matches(pattern)) {
            editText.setError(errorMessage);
            return false;
        }
        return true;
    }
    private boolean MatchTextEditText(@NonNull EditText editText, String text , String errorMessage){
        if (!editText.getText().toString().equals(text)) {
            editText.setError(errorMessage);
            return false;
        }
        return true;
    }

}