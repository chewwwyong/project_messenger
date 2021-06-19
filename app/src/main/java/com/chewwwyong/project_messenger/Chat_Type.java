package com.chewwwyong.project_messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chewwwyong.project_messenger.Controller.MainActivity;

import java.util.ArrayList;

public class Chat_Type extends AppCompatActivity implements View.OnClickListener {

    Button btn_single, btn_multiple;
    ArrayList<String> additem = new ArrayList<>();
    // 判斷是不是從"選擇誰"選單回傳 是的話為2
    Integer return_chat_type = 0;
    // 判斷是不是有從"私人聊天"選單回傳 是的話為1
    Integer return_choose_who = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_type);

        btn_single = findViewById(R.id.btn_single);
        btn_multiple = findViewById(R.id.btn_multiple);
        btn_single.setOnClickListener(this);
        btn_multiple.setOnClickListener(this);

        Intent it = getIntent();
        return_chat_type = it.getIntExtra("return_chat_type", 0);
        return_choose_who = it.getIntExtra("return_choose_who", 0);
        if (return_chat_type == 2 && return_choose_who == 1)
        {
            additem = it.getStringArrayListExtra("reFriendList");
        }
        else if(return_chat_type == 2 && return_choose_who == 0)
        {
            additem = it.getStringArrayListExtra("FriendList");
        }
    }

    /*@Override
    protected void onStop() {
        super.onStop();
        finish();
    }*/

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_single:
                //Intent its = new Intent(Chat_Type.this, who_login.class);
                Intent its = new Intent(Chat_Type.this, choose_who.class);
                its.putExtra("return_choose_who", 1);
                its.putStringArrayListExtra("reFriendList", additem);
                startActivity(its);
                finish();
                break;
            case R.id.btn_multiple:
                Intent itm = new Intent(Chat_Type.this, MainActivity.class);
                startActivity(itm);
                finish();
                break;
        }
    }
}