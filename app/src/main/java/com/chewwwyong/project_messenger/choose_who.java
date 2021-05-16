package com.chewwwyong.project_messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class choose_who extends AppCompatActivity {

    EditText edt_addFriend;
    Button btn_subcribe;
    String LoginName;
    ArrayList<String> addFriend = new ArrayList<>();

    // listview
    ArrayList item = new ArrayList();
    ListView ltv_Subscribe;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_who);

        edt_addFriend = findViewById(R.id.edt_addFriend);
        btn_subcribe  = findViewById(R.id.btn_subcribe);
        ltv_Subscribe= findViewById(R.id.ltv_Subscribe);

        item = new ArrayList();
        adapter = new ArrayAdapter(choose_who.this, android.R.layout.simple_list_item_1, item);
        ltv_Subscribe.setAdapter(adapter);

        Intent it = getIntent();
        LoginName = it.getStringExtra("LoginName");

        btn_subcribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend.add(edt_addFriend.getText().toString());
                // new subscribe
                item.add(edt_addFriend.getText().toString());
                adapter.notifyDataSetChanged();
                ltv_Subscribe.smoothScrollToPosition(item.size()-1);
            }
        });

        ltv_Subscribe.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent it = new Intent(choose_who.this,talk_in_private.class);
                it.putExtra("LoginName", LoginName);
                String name = String.valueOf(adapterView.getItemAtPosition(i)); //  抓指定位置的名稱
                //Toast.makeText(choose_who.this, name, Toast.LENGTH_SHORT).show();
                it.putExtra("send_to_who", name); // 選擇要私訊的人
                it.putStringArrayListExtra("FriendList", addFriend);
                startActivity(it);
            }
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 設置要用哪個menu檔做為選單
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 依照id判斷點了哪個項目並做相應事件
        if (item.getItemId() == R.id.menu_logout) {

            // 按下「登出」要做的事
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false)
                    .setTitle("登出")
                    .setMessage("確定邀登出了嗎？")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AuthUI.getInstance().signOut(choose_who.this)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(choose_who.this, "已登出囉！", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                        }
                    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).create();
            builder.show();

        }
        return true;
    }*/
}