package com.chewwwyong.project_messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.chewwwyong.project_messenger.Controller.MainActivity;

public class Chat_Type extends AppCompatActivity implements View.OnClickListener {

    Button btn_single, btn_multiple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_type);

        btn_single = findViewById(R.id.btn_single);
        btn_multiple = findViewById(R.id.btn_multiple);
        btn_single.setOnClickListener(this);
        btn_multiple.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_single:
                Intent its = new Intent(Chat_Type.this, who_login.class);
                startActivity(its);
                break;
            case R.id.btn_multiple:
                Intent itm = new Intent(Chat_Type.this, MainActivity.class);
                startActivity(itm);
                break;
        }
    }
}