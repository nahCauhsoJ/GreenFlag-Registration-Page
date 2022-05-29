package com.example.greenflagregistrationpage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashSet;
import java.util.Set;

public class MainActivity2 extends AppCompatActivity {

    private EditText email_input;
    private EditText password_input;
    private EditText password_confirm_input;
    private Button register_btn;
    private RadioButton wipe_server_data_btn;

    private boolean goodEmail;
    private boolean goodPassword;
    private boolean matchingPassword;

    private MockServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        server = new MockServer();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeAsUpIndicator(R.drawable.cross);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        email_input = findViewById(R.id.email_input);
        password_input = findViewById(R.id.password_input);
        password_confirm_input = findViewById(R.id.password_confirm_input);
        register_btn = findViewById(R.id.register_btn);
        wipe_server_data_btn = findViewById(R.id.wipe_server_data_btn);


        email_input.addTextChangedListener(
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    goodEmail = ValidateEmail();
                    CheckGoodInfo();
                }
            }
        );
        password_input.addTextChangedListener(
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    goodPassword = ValidatePassword();
                    CheckGoodInfo();
                }
            }
        );
        password_confirm_input.addTextChangedListener(
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    matchingPassword = ConfirmPassword();
                    CheckGoodInfo();
                }
            }
        );

        register_btn.getBackground().setColorFilter(
                Color.GRAY, PorterDuff.Mode.MULTIPLY
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) finishCustom();
        return super.onOptionsItemSelected(item);
    }

    // clearColorFilter doesn't seem to work with onDestroy all
    //      the time. So why not doing that before finish()?
    private void finishCustom()
    {
        register_btn.getBackground().clearColorFilter();
        finish();
    }

    public boolean ValidateEmail() {
        String email = email_input.getText().toString();

        if (email.isEmpty()) {
            email_input.setError("Email is required.");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_input.setError("Provide a valid email address.");
            return false;
        }

        if (server.VerifyEmail(email)) {
            email_input.setError("Email already used.");
            return false;
        }

        email_input.setError(null);
        return true;
    }

    public boolean ValidatePassword() {
        String pwd = password_input.getText().toString();

        if (pwd.isEmpty()) {
            password_input.setError("Password is required.");
            return false;
        }
        if (pwd.length() < 8) {
            password_input.setError("Password needs at least 8 characters.");
            return false;
        }

        boolean hasNumber = false;
        boolean hasUpper = false;
        boolean hasLower = false;
        for (char i : pwd.toCharArray()) {
            if (Character.isDigit(i)) hasNumber = true;
            else if (Character.isUpperCase(i)) hasUpper = true;
            else if (Character.isLowerCase(i)) hasLower = true;
            if (hasNumber && hasUpper && hasLower) break;
        }

        String missing = "";
        if (!hasNumber) missing += "number" + ", ";
        if (!hasUpper) missing += "uppercase" + ", ";
        if (!hasLower) missing += "lowercase" + ", ";
        if (missing.length() > 0) {
            missing = missing.substring(0, missing.length()-2) + ".";
            password_input.setError("The password is missing: " + missing);
            return false;
        }

        password_input.setError(null);
        return true;
    }

    public boolean ConfirmPassword() {
        if (!password_input.getText().toString().equals(
                password_confirm_input.getText().toString())) {
            password_confirm_input.setError("Passwords do not match.");
            return false;
        }

        password_confirm_input.setError(null);
        return password_confirm_input.getText().
                toString().length() >= 1;
    }

    public void CheckGoodInfo() {
        if (goodEmail && goodPassword && matchingPassword) {
            if (!register_btn.isEnabled())
                register_btn.getBackground().setColorFilter(null);
            register_btn.setEnabled(true);
        } else {
            if (register_btn.isEnabled())
                register_btn.getBackground().setColorFilter(
                        Color.GRAY, PorterDuff.Mode.MULTIPLY
                );
            register_btn.setEnabled(false);
        }
    }

    public void RegisterInfo(View view) {
        server.RegisterEmail(email_input.getText().toString());
        Intent i = new Intent();
        i.putExtra("registered", true);
        setResult(RESULT_OK, i);
        finishCustom();
    }

    public void WipeData(View view) {
        server.ClearData();
        wipe_server_data_btn.setChecked(false);
        Snackbar.make(findViewById(R.id.MainActivity2Layout),
                "Data wiped",
                Snackbar.LENGTH_SHORT).show();
    }



    // One does not simply pull sensitive info and store in the
    //      client. If we are to mock email verification, might as
    //      well mock the client-server relationship as well. <3
    class MockServer {
        private final SharedPreferences pref = MainActivity2.this.
                getPreferences(Context.MODE_PRIVATE);
        private final SharedPreferences.Editor editor = pref.edit();

        public boolean VerifyEmail(String email) {
            Set<String> email_list = pref.getStringSet("email_list", new HashSet<>());
            return email_list.contains(email);
        }

        public void RegisterEmail(String email) {
            Set<String> email_list = new HashSet<>(
                    pref.getStringSet("email_list", new HashSet<>()));
            email_list.add(email);
            editor.putStringSet("email_list",email_list).commit();
        }

        // Just for testing purposes, here's an option to wipe the
        //      stored email list.
        public void ClearData() {
            editor.clear().commit();
        }
    }
}