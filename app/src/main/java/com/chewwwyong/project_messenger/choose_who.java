package com.chewwwyong.project_messenger;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class choose_who extends AppCompatActivity {

    int SIGN_IN_REQUEST = 10;

    EditText edt_addFriend;
    Button btn_subcribe;
    FloatingActionButton fab_return_chat_type;
    String LoginName;

    ArrayList<String> addFriend = new ArrayList<>();
    ArrayList<String> reFriendList = new ArrayList<>();

    // listview
    ListView ltv_Subscribe;
    ArrayList<String> item = new ArrayList<>();
    ArrayAdapter adapter;

    // 判斷是不是從聊天室回傳 是的話為1
    Integer return_choose_who = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_who);

        /*if(FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST);
        }*/

        checkIfLogin();//檢查是否已登入

        edt_addFriend = findViewById(R.id.edt_addFriend);
        btn_subcribe  = findViewById(R.id.btn_subcribe);
        ltv_Subscribe= findViewById(R.id.ltv_Subscribe);
        fab_return_chat_type = findViewById(R.id.fab_return_chat_type);

        //item = new ArrayList();
        Intent itget = getIntent();
        return_choose_who = itget.getIntExtra("return_choose_who", 0);
        //Toast.makeText(choose_who.this, need_to_new_adapter.toString(), Toast.LENGTH_SHORT).show();

        if (return_choose_who== 1)
        {
            //ArrayList<String> reFriendList = new ArrayList<>();
            //reFriendList = itget.getStringArrayListExtra("reFriendList");
            reFriendList = itget.getStringArrayListExtra("reFriendList");
            adapter = new ArrayAdapter(choose_who.this, android.R.layout.simple_list_item_1, reFriendList);
            ltv_Subscribe.setAdapter(adapter);
        }
        else
        {
            adapter = new ArrayAdapter(choose_who.this, android.R.layout.simple_list_item_1, item);
            ltv_Subscribe.setAdapter(adapter);
        }


        //Intent it = getIntent();
        //LoginName = it.getStringExtra("LoginName");

        btn_subcribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend.add(edt_addFriend.getText().toString());
                // new subscribe
                if (return_choose_who== 1)
                {
                    reFriendList.add(edt_addFriend.getText().toString());
                }
                else
                {
                    item.add(edt_addFriend.getText().toString());
                }
                adapter.notifyDataSetChanged();
                if (return_choose_who== 1)
                {
                    ltv_Subscribe.smoothScrollToPosition(reFriendList.size()-1);
                }
                else
                {
                    ltv_Subscribe.smoothScrollToPosition(item.size()-1);
                }
            }
        });

        ltv_Subscribe.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LoginName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                Intent it = new Intent(choose_who.this,talk_in_private.class);
                it.putExtra("LoginName", LoginName);
                String name = String.valueOf(adapterView.getItemAtPosition(i)); //  抓指定位置的名稱
                //Toast.makeText(choose_who.this, name, Toast.LENGTH_SHORT).show();
                it.putExtra("send_to_who", name); // 選擇要私訊的人
                if (return_choose_who== 1)
                {
                    it.putStringArrayListExtra("FriendList", reFriendList);
                }
                else
                {
                    it.putStringArrayListExtra("FriendList", addFriend);
                }
                startActivity(it);
                finish();
            }
        });

        fab_return_chat_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(choose_who.this, Chat_Type.class);
                if (return_choose_who  == 1)
                {
                    it.putStringArrayListExtra("reFriendList", reFriendList);
                    it.putExtra("return_choose_who", 1);
                }
                else
                {
                    it.putStringArrayListExtra("FriendList", addFriend);
                    it.putExtra("return_choose_who", 0);
                }
                it.putExtra("return_chat_type", 2);
                startActivity(it);
                finish();
            }
        });
    }

    /*@Override
    protected void onStop() {
        super.onStop();
        finish();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "登入成功", Toast.LENGTH_SHORT).show();
                displayChatMsg();
            }
            else{
                Toast.makeText(this, "登入失敗，請稍後再試", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void displayChatMsg() {
    }

    private void checkIfLogin() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST);

        } else {
            //toast.setText(getResources().getText(R.string.welcome) + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            //toast.show();
            Toast.makeText(choose_who.this, getResources().getText(R.string.welcome) + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
            displayChatMsg();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 設置要用哪個menu檔做為選單
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 依照id判斷點了哪個項目並做相應事件
        if (item.getItemId() == R.id.menu_logout) {

            // 按下「登出」要做的事
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false)
                    .setTitle("登出")
                    .setMessage("確定要登出了嗎？")
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> AuthUI.getInstance().signOut(choose_who.this)
                            .addOnCompleteListener(task -> {
                                Toast.makeText(choose_who.this, "已登出囉！", Toast.LENGTH_SHORT).show();
                                finish();
                            })).setNegativeButton(android.R.string.no, (dialogInterface, i) -> {

            }).create();
            builder.show();

        }
        return true;
    }
}