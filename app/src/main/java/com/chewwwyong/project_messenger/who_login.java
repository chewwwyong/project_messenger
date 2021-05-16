package com.chewwwyong.project_messenger;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;

public class who_login extends AppCompatActivity {

    EditText input_username;
    Button btn_login;
    int SIGN_IN_REQUEST = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_login);



        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST);
        }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
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
}