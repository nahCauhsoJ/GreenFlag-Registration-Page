package com.example.greenflagregistrationpage;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent i = result.getData();
                    if (i != null && i.getBooleanExtra("registered",false)) {
                        Snackbar.make(findViewById(R.id.MainActivityLayout),
                                "Account Registered",
                                Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void CreateAccount(View view) {
        Intent i = new Intent(this, MainActivity2.class);
        activityResultLauncher.launch(i);
    }
}