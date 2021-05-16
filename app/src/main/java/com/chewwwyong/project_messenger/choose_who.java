package com.chewwwyong.project_messenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class choose_who extends AppCompatActivity {

    //TextView txv_test,txv_test2;
    EditText edt_addFriend;
    Button btn_subcribe;
    String LoginName;
    ArrayList<String> addFriend = new ArrayList<>();

    ListView ltv_Subscribe;
    // listview
    ArrayList item = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_who);

        //txv_test = findViewById(R.id.txv_test);
        //txv_test2 = findViewById(R.id.txv_test2);
        edt_addFriend = findViewById(R.id.edt_addFriend);
        btn_subcribe  = findViewById(R.id.btn_subcribe);

        Intent it = getIntent();
        LoginName = it.getStringExtra("LoginName");

        btn_subcribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend.add(edt_addFriend.getText().toString());
                //txv_test.setText(edt_addFriend.getText().toString());
                // new subscribe
                item.add(edt_addFriend.getText().toString());
                ArrayAdapter adapter = new ArrayAdapter(choose_who.this, android.R.layout.simple_list_item_1, item);
                ltv_Subscribe.setAdapter(adapter);
            }
        });

        ltv_Subscribe= findViewById(R.id.ltv_Subscribe);
        ltv_Subscribe.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent it = new Intent(choose_who.this,MainActivity.class);
                it.putExtra("LoginName", LoginName);
                String name = String.valueOf(adapterView.getItemAtPosition(i)); //  抓指定位置的名稱
                //Toast.makeText(choose_who.this, name, Toast.LENGTH_SHORT).show();
                it.putExtra("send_to_who", name); // 選擇要私訊的人
                it.putStringArrayListExtra("FriendList", addFriend);
                startActivity(it);
            }
        });


        /*txv_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(choose_who.this,MainActivity.class);
                it.putExtra("LoginName", LoginName);
                it.putExtra("send_to_who", txv_test.getText().toString()); // 選擇要私訊的人
                it.putStringArrayListExtra("FriendList", addFriend);
                startActivity(it);

            }
        });*/
    }
}