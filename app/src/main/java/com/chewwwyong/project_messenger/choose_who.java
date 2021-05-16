package com.chewwwyong.project_messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class choose_who extends AppCompatActivity {

    TextView txv_test,txv_test2;
    EditText edt_addFriend;
    Button btn_subcribe;
    String LoginName;
    ArrayList<String> addFriend = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_who);

        txv_test = findViewById(R.id.txv_test);
        txv_test2 = findViewById(R.id.txv_test2);
        edt_addFriend = findViewById(R.id.edt_addFriend);
        btn_subcribe  = findViewById(R.id.btn_subcribe);

        Intent it = getIntent();
        LoginName = it.getStringExtra("LoginName");

        btn_subcribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend.add(edt_addFriend.getText().toString());
                txv_test.setText(edt_addFriend.getText().toString());
            }
        });

        txv_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(choose_who.this,MainActivity.class);
                it.putExtra("LoginName", LoginName);
                it.putStringArrayListExtra("FriendList", addFriend);
                startActivity(it);

            }
        });
    }
}