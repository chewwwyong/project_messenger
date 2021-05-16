package com.chewwwyong.project_messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class who_login extends AppCompatActivity {

    EditText input_username;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_login);

        input_username = findViewById(R.id.input_username);
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(who_login.this, choose_who.class);
                it.putExtra("LoginName", input_username.getText().toString());
                startActivity(it);
            }
        });
    }
}