package com.chewwwyong.project_messenger.Controller;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chewwwyong.project_messenger.BuildConfig;
import com.chewwwyong.project_messenger.ChatMessage;
import com.chewwwyong.project_messenger.MyFirebaseService;
import com.chewwwyong.project_messenger.R;
import com.chewwwyong.project_messenger.Util.BitmapUtil;
import com.chewwwyong.project_messenger.Util.PermissionTool;
import com.chewwwyong.project_messenger.Util.Utils;
import com.chewwwyong.project_messenger.talk_in_private;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

//import com.google.firebase.installations.Utils;

public class MainActivity extends AppCompatActivity {

    EditText edt_getText;
    ListView ltv_Message_box;

    NotificationManager manager;
    Bitmap largeIcon;
    PendingIntent pendingIntent;
    Notification notification;



    // fireBase 相關變數
    // 取得結果用的 Request Code
    private final int CAMERA_REQUEST = 5001;
    private final int ALBUM_REQUEST = 5002;
    private final int AVATAR_CAMERA_REQUEST = 5003;
    private final int AVATAR_ALBUM_REQUEST = 5004;
    private final int CROP_REQUEST = 5005;
    private final int AVATAR_CROP_REQUEST = 5006;
    public static final int SIGN_IN_REQUEST = 1;
    private static final int REQUEST_CAMERA_AND_WRITE_STORAGE = 5000;
    public static final String AUTHORITY = "com.chewwwyong.project_messenger.fileprovider";

    private Toast toast;

    private FirebaseRecyclerAdapter<ChatMessage, ChatMessageHolder> adapter;

    private FirebaseRecyclerAdapter<ChatMessage, ChatMessageHolder> testadapter;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private DatabaseReference reference;
    private StorageReference storageReference;
    private InputMethodManager imm;
    private String uuid;

    private File cameraFile;
    private String cameraFileName;
    private String cameraPath;

    FloatingActionButton fabCamera, fabAlbum, fabSend;
    private String filePath, avatarPath;

    ArrayList<String> keyList = new ArrayList<>();

    SharedPreferences sharedPreferences;

    private Context context;
    // fireBase 相關變數

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 取得NotificationManager物件
        //manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 建立大圖示需要的Bitmap物件
        //largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.tongshenduan_hotpot);

        // 點擊時要啟動的PendingIntent，當中包含一個Intent設置要開啟的Activity
        //pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);


        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = "default_notification_channel_id";
            String channelName = "default_notification_channel_name";
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }*/

        edt_getText = findViewById(R.id.edtInput);
        ltv_Message_box = findViewById(R.id.ltv_Message_box);

        context = MainActivity.this;

        //================ firebase ================
        setUUID();//取得裝置uuid

        findViewAndGetInstance();//綁定各種view與實體化

        checkIfLogin();//檢查是否已登入

        //訊息框
        edt_getText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!TextUtils.isEmpty(edt_getText.getText())) {
                    sendMsg();
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);//送出後鍵盤收起
                }
            }
            return true;
        });
        //================ firebase ================

        /* 通过按键发布消息 */
        FloatingActionButton pubButton = findViewById(R.id.fabSend);
        pubButton.setOnClickListener(view -> {
            if (edt_getText.getText().toString().trim().length() > 0)   //trim 把前後空格 tab 清除
            {
                sendMsg();
                //Toast.makeText(MainActivity.this, "!" + edt_getText.getText().toString() + "!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //Toast.makeText(MainActivity.this, edt_getText.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast

                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("MainActivity", token);
                        //Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //取回user的發話keyList
        keyList = new ArrayList<>();
        Set<String> saveKeyList = new HashSet<>();
        saveKeyList = sharedPreferences.getStringSet("keyList", saveKeyList);
        keyList.addAll(saveKeyList);
        avatarPath = sharedPreferences.getString(getResources().getString(R.string.avatarPath), "");

    }

    private void findViewAndGetInstance() {
        sharedPreferences = getSharedPreferences("chatTool", MODE_PRIVATE);
        toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
        reference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        edt_getText = findViewById(R.id.edtInput);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        fabCamera = findViewById(R.id.fabCamera);
        fabAlbum = findViewById(R.id.fabAlbum);

        fabCamera.setOnClickListener(v -> fabTakePhoto(v, CAMERA_REQUEST));
        fabAlbum.setOnClickListener(v -> fabAlbum(v, ALBUM_REQUEST));

        reference.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override//收到新訊息時自動往下捲
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (adapter != null)
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setUUID() {
        if (Build.VERSION.SDK_INT < 28) {
            uuid = Build.SERIAL;
        } else {
            uuid = Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

    //檢查是否已登入，若沒登入會導頁至登入畫面
    private void checkIfLogin() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST);

        } else {
            toast.setText(getResources().getText(R.string.welcome) + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            toast.show();
            displayChatMsg();

        }
    }

    //送出訊息
    public void fabSend(View v) {
        try {
            if (!TextUtils.isEmpty(edt_getText.getText())) {
                sendMsg();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);//送出後鍵盤收起
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //拍照
    public void fabTakePhoto(View v, int requestCode) {
        //檢查權限後拍照
        if (checkPhotoPermission()) {

            cameraFileName = Utils.getInstance().getFileName();

            File dir = Utils.getInstance().getFireDir();
            cameraFile = new File(dir, cameraFileName);
            cameraPath = cameraFile.getPath();
            Uri camera_uri;

            if (Build.VERSION.SDK_INT >= 24) {
                camera_uri = FileProvider.getUriForFile(getApplicationContext(),
                        AUTHORITY, cameraFile);
            }

            /*
            if (Build.VERSION.SDK_INT >= 24) {
                camera_uri = FileProvider.getUriForFile(getApplicationContext(),
                        BuildConfig.APPLICATION_ID + ".provider", cameraFile);
            }
            */
            else {
                camera_uri = Uri.fromFile(cameraFile);
            }

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//使用拍照
            intent.putExtra(MediaStore.EXTRA_OUTPUT, camera_uri);
            startActivityForResult(intent, requestCode);

        } else {
            // 要求權限
            PermissionTool.getInstance()
                    .requestMultiPermission(this,
                            new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA
                            },
                            REQUEST_CAMERA_AND_WRITE_STORAGE);

        }
    }


    public void fabAlbum(View v, int requestCode) {//開啟相簿
        if (checkAlbumPermission()) {

            cameraFileName = Utils.getInstance().getFileName();
            File dir = Utils.getInstance().getFireDir();
            cameraFile = new File(dir, cameraFileName);
            cameraPath = cameraFile.getPath();

            Intent albumIntent = new Intent();
            albumIntent.setType("image/*");//設定只顯示圖片區，不要秀其它的資料夾
            albumIntent.setAction(Intent.ACTION_GET_CONTENT);//取得本機相簿的action
            startActivityForResult(albumIntent, requestCode);

        } else {
            PermissionTool.getInstance().requestReadExternalStoragePermission(this);
        }
    }


    private boolean checkPhotoPermission() {
        if (PermissionTool.getInstance().isWriteExternalStorageGranted(this)
                && PermissionTool.getInstance().isCameraGranted(this)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkAlbumPermission() {
        if (PermissionTool.getInstance().isWriteExternalStorageGranted(this)
                && PermissionTool.getInstance().isCameraGranted(this)) {
            return true;
        } else {
            return false;
        }
    }


    private void sendMsg() {
        String msg = edt_getText.getText().toString();
        String userName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
        long time = new Date().getTime();

        String key = reference.push().getKey();
        keyList.add(key);
        if (TextUtils.isEmpty(avatarPath))
            avatarPath = "";
        assert key != null;
        reference.child(key).setValue(new ChatMessage(userName, msg, time, uuid, "", avatarPath));

        edt_getText.setText("");
        Set<String> saveKeyList = new HashSet<>(keyList);
        sharedPreferences.edit().putStringSet("keyList", saveKeyList).apply();

        // 送出訊息後要先重置介面 才能讀取到最新的聊天紀錄
        //displayChatMsg();
        try {
            adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatMessageHolder>
                    (ChatMessage.class, R.layout.message, ChatMessageHolder.class, reference.limitToLast(20)) {

                public ChatMessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(context).inflate(R.layout.message, parent, false);
                    ChatMessageHolder holder = new ChatMessageHolder(view);

                    return holder;
                }


                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                protected void populateViewHolder(ChatMessageHolder viewHolder, ChatMessage model, final int position) {

                    viewHolder.setValues(model);

                    viewHolder.img_avatar_other.setOnClickListener(v -> showInfo(position));
                    viewHolder.img_avatar_user.setOnClickListener(v -> showInfo(position));

                }
            };

            //這邊註解是因為會一直刷新介面 很醜
            //recyclerView.setLayoutManager(linearLayoutManager);
            //recyclerView.setHasFixedSize(true);
            //recyclerView.setAdapter(adapter);
            //recyclerView.scrollToPosition(adapter.getItemCount() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 之後有更新內容就判斷是不是別人傳訊息 就通知
        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (adapter != null && adapter.getItemCount() > 0) //這邊補後面的判斷式是因為他會說下面的程式碼就不能執行 因為還沒run過
                {
                    //recyclerView.scrollToPosition(adapter.getItemCount() - 1);

                    //判斷是不是最後的訊息再來看是不是本人傳的 是就不顯示 不是就移動到最下面
                    if (!adapter.getItem(adapter.getItemCount() - 1).getUuid().equals(uuid)) //使用裝置id讓判斷訊息來自使用者或對方
                    {
                        Toast.makeText(MainActivity.this, adapter.getItem(adapter.getItemCount() - 1).getMessage(), Toast.LENGTH_SHORT).show();
                        // 取得NotificationManager物件
                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        Notification.BigPictureStyle bigPictureStyle = new Notification.BigPictureStyle();
                        bigPictureStyle.setBigContentTitle("Photo");
                        bigPictureStyle.setSummaryText("SummaryText");
                        // 建立大圖示需要的Bitmap物件
                        Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.b123)).getBitmap();
                        bigPictureStyle.bigPicture(bitmap);

                        NotificationChannel notificationChannel = new NotificationChannel("0", "notice", NotificationManager.IMPORTANCE_HIGH);
                        Notification.Builder builder = new Notification.Builder(MainActivity.this, "0")
                                .setSmallIcon(R.drawable.b123)
                                .setColor(Color.BLUE)
                                .setContentTitle(adapter.getItem(adapter.getItemCount() - 1).getUserName()) //顯示別人名字
                                .setContentText(adapter.getItem(adapter.getItemCount() - 1).getMessage())
                                .setWhen(System.currentTimeMillis())
                                // 訊息內容較長會超過一行時預設會將訊息結尾變成...而不能完整顯示，此時可以再加入一行BigText讓長訊息能完整顯示
                                .setChannelId("0")
                                .setDefaults(Notification.DEFAULT_VIBRATE) // 加上提醒效果
                                .setContentIntent(pendingIntent)  // 設置Intent
                                .addAction(R.drawable.b123, "查看", pendingIntent)  // 增加「查看」
                                .setAutoCancel(true)    // 點擊後讓Notification消失
                                .setStyle(bigPictureStyle);
                        notificationManager.createNotificationChannel(notificationChannel);
                        notificationManager.notify(0, builder.build());

                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

                    } else {
                        //Toast.makeText(MainActivity.this, adapter.getItem(adapter.getItemCount() - 1).getMessage(), Toast.LENGTH_SHORT).show();
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        //adapter.startListening();//啟動監聽，訊息可即時更新

    }

    @Override
    protected void onStop() {
        super.onStop();
        //adapter.stopListening();

    }

    //秀出訊息
    private void displayChatMsg() {

        try {
            adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatMessageHolder>
                    (ChatMessage.class, R.layout.message, ChatMessageHolder.class, reference.limitToLast(20)) {

                public ChatMessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(context).inflate(R.layout.message, parent, false);
                    ChatMessageHolder holder = new ChatMessageHolder(view);

                    return holder;
                }


                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                protected void populateViewHolder(ChatMessageHolder viewHolder, ChatMessage model, final int position) {

                    viewHolder.setValues(model);

                    viewHolder.img_avatar_other.setOnClickListener(v -> showInfo(position));
                    viewHolder.img_avatar_user.setOnClickListener(v -> showInfo(position));

                }
            };

            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(adapter);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showInfo(int position) {
        final Dialog dialog = new Dialog(context, R.style.edit_AlertDialog_style);
        dialog.setContentView(R.layout.dialog_avatar_info);
        dialog.setCanceledOnTouchOutside(false);

        final ImageView img_avatar = dialog.findViewById(R.id.img_info_avatar);
        ImageView img_close = dialog.findViewById(R.id.img_close);
        ImageView img_changeAvatar = dialog.findViewById(R.id.img_changeAvatar);
        TextView txv_name = dialog.findViewById(R.id.txv_dialog_name);

        final ChatMessage data = adapter.getItem(position);
        txv_name.setText(data.getUserName());

        img_close.setOnClickListener(v -> dialog.dismiss());

        if (!TextUtils.isEmpty(data.getAvatarPath())) {
            storageReference = FirebaseStorage.getInstance().getReference();
            storageReference = storageReference.child(data.getAvatarPath());
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                    .load(uri)
                    .into(img_avatar)).addOnFailureListener(Throwable::printStackTrace);
        }

        if (data.getUuid().equals(uuid)) {//如果是user才問要不要換
            img_changeAvatar.setVisibility(View.VISIBLE);
        }
        img_changeAvatar.setOnClickListener(v -> {

            ArrayList<String> typeList = new ArrayList<>();
            typeList.add(getResources().getString(R.string.takePhoto));
            typeList.add(getResources().getString(R.string.fromAlbum));

            View view = LayoutInflater.from(context).inflate(R.layout.popup_window, null, false);
            ListView listView = view.findViewById(R.id.type_listview);
            ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_list_item_1,
                    typeList);
            listView.setAdapter(nameAdapter);

            PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setBackgroundDrawable(getDrawable(android.R.color.white));
            popupWindow.setTouchable(true);
            popupWindow.showAsDropDown(v, 0, 0);
            popupWindow.setContentView(view);

            listView.setOnItemClickListener((parent, view1, position1, id) -> {
                switch (position1) {
                    case 0: //拍照
                        fabTakePhoto(view1, AVATAR_CAMERA_REQUEST);
                        dialog.dismiss();
                        break;
                    case 1://從相簿選
                        fabAlbum(view1, AVATAR_ALBUM_REQUEST);
                        dialog.dismiss();
                        break;
                }
            });
        });

        dialog.show();

    }

    @Override//獲取登入結果
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST) {
            if (resultCode == RESULT_OK) {
                toast.setText(getResources().getText(R.string.loginSuccess));
                toast.show();
                displayChatMsg();
            } else {
                toast.setText(getResources().getText(R.string.loginFail));
                toast.show();
                finish();
            }
        } else if (requestCode == CAMERA_REQUEST || requestCode == AVATAR_CAMERA_REQUEST) {//獲取拍照結果
            if (resultCode == RESULT_OK) {
                try {
                    //https://www.itread01.com/content/1547700324.html
                    //https://givemepass.blogspot.com/2017/03/firebase-storage.html
                    File tempFile = getCacheDir();

                    final Uri uri = Uri.fromFile(Utils.getInstance().compressUploadPhoto(tempFile, cameraPath, cameraFileName));
                    doCropPhoto(uri, requestCode);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                toast.setText(getResources().getString(R.string.retryAgain));
                toast.show();
            }

        } else if (requestCode == ALBUM_REQUEST || requestCode == AVATAR_ALBUM_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                doCropPhoto(data.getData(), requestCode);

            } else {
                toast.setText(getResources().getString(R.string.retryAgain));
                toast.show();
            }
        } else if (requestCode == AVATAR_CROP_REQUEST || requestCode == CROP_REQUEST) {
            if (data.hasExtra(CropImageActivity.EXTRA_IMAGE)) {
                //取得裁切後圖片的暫存位置
                String filePath = data.getStringExtra(CropImageActivity.EXTRA_IMAGE);
                if (filePath.contains(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM")) {
                    File imgFile = new File(filePath);
                    if (imgFile.exists()) {
                        //代入已設定好的圖片size
                        int photoSize = getResources().getDimensionPixelSize(R.dimen.photo_size);
                        //使用寫好的方法將路徑檔做成bitmap檔
                        Bitmap realBitmap = BitmapUtil.decodeSampledBitmap(imgFile.getAbsolutePath(), photoSize, photoSize);
                        File file = Utils.getInstance().bitmapToFile(getCacheDir(), cameraFileName, realBitmap);
                        if (requestCode == AVATAR_CROP_REQUEST) {//改大頭貼的動作才換路徑
                            avatarPath = file.getName();
                            sharedPreferences.edit().putString(getResources().getString(R.string.avatarPath), avatarPath).apply();
                        }

                        uploadFile(Uri.fromFile(file), file.getName(), requestCode);
                    }
                }
            }
        }
    }

    private void doCropPhoto(Uri uri, int requestCode) {
        Intent intent = new Intent(context, CropImageActivity.class);
        intent.setData(uri);
        intent.putExtra(getResources().getString(R.string.degree), 0);
        if (requestCode == ALBUM_REQUEST || requestCode == CAMERA_REQUEST) {
            startActivityForResult(intent, CROP_REQUEST);
        } else if (requestCode == AVATAR_ALBUM_REQUEST || requestCode == AVATAR_CAMERA_REQUEST) {
            startActivityForResult(intent, AVATAR_CROP_REQUEST);
        }
    }

    //上傳圖片訊息至firebase雲端
    private void uploadFile(Uri uri, final String fileName, final int requestCode) {

        storageReference = FirebaseStorage.getInstance().getReference();
        storageReference = storageReference.child(fileName);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();

        UploadTask uploadTask = storageReference.putFile(uri, metadata);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            String userName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
            long time = new Date().getTime();

            if (requestCode == CAMERA_REQUEST || requestCode == ALBUM_REQUEST || requestCode == CROP_REQUEST) {//上傳圖片
                String key = reference.push().getKey();
                keyList.add(key);
                assert key != null;
                reference.child(key).setValue(new ChatMessage(userName, "", time, uuid, fileName, avatarPath));
                Set<String> saveKeyList = new HashSet<>(keyList);
                sharedPreferences.edit().putStringSet("keyList", saveKeyList).apply();

            } else if (requestCode == AVATAR_CROP_REQUEST) {//更新大頭貼

                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < keyList.size(); i++) {
                    map.put(keyList.get(i) + "/avatarPath", fileName);
                }
                reference.updateChildren(map);
                sharedPreferences.edit().putString(getResources().getString(R.string.avatarPath), fileName).apply();

            }


        }).addOnFailureListener(Throwable::printStackTrace);
    }

    //處理訊息的地方
    public class ChatMessageHolder extends RecyclerView.ViewHolder {
        private final TextView txvUser_Other;
        private final TextView txvMsg_Other;
        private final TextView txvTime_Other;

        private final TextView txvMsg_User;
        private final TextView txvTime_User;
        private final TextView txv_time_imgOther;
        private final ImageView img_avatar_other;
        private final ImageView img_avatar_user;

        private final TextView txv_time_imgUSer;
        RelativeLayout userLayout, otherUserLayout;

        ImageView imgMsg_user, imgMsg_other;


        public ChatMessageHolder(@NonNull View v) {
            super(v);
            txvUser_Other = v.findViewById(R.id.txv_user_other);
            txvMsg_Other = v.findViewById(R.id.txv_msg_other);
            txvTime_Other = v.findViewById(R.id.txv_time_other);

            txvMsg_User = v.findViewById(R.id.txv_msg_user);
            txvTime_User = v.findViewById(R.id.txv_time_user);

            userLayout = v.findViewById(R.id.userLayout);
            otherUserLayout = v.findViewById(R.id.otherUserLayout);
            img_avatar_other = v.findViewById(R.id.img_avatar_other);
            img_avatar_user = v.findViewById(R.id.img_avatar_user);

            imgMsg_user = v.findViewById(R.id.imgmsg_user);
            imgMsg_other = v.findViewById(R.id.imgmsg_otheruser);

            txv_time_imgUSer = v.findViewById(R.id.txv_time_imgUSer);
            txv_time_imgOther = v.findViewById(R.id.txv_time_imgOther);

        }

        public void setValues(final ChatMessage chatMessage) {
            if (chatMessage != null) {

                String sendTime = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(chatMessage.getTime());
                String filePath = chatMessage.getFilePath();
                String avatarPath = chatMessage.getAvatarPath();
                String chatMsg = chatMessage.getMessage();
                if (chatMsg == null) {
                    otherUserLayout.setVisibility(View.GONE);
                    userLayout.setVisibility(View.GONE);
                } else {
                    if (!chatMessage.getUuid().equals(uuid)) {//使用裝置id讓判斷訊息來自使用者或對方
                        otherUserLayout.setVisibility(View.VISIBLE);
                        userLayout.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(avatarPath)) {
                            storageReference = FirebaseStorage.getInstance().getReference();
                            storageReference = storageReference.child(avatarPath);
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                                    .load(uri)
                                    .into(img_avatar_other)).addOnFailureListener(Throwable::printStackTrace);
                        }

                        if (TextUtils.isEmpty(filePath)) {//如果有圖片訊息就秀圖
                            txvMsg_Other.setVisibility(View.VISIBLE);
                            imgMsg_other.setVisibility(View.GONE);
                            txv_time_imgOther.setVisibility(View.GONE);

                            txvMsg_Other.setText(chatMsg);
                            txvTime_Other.setText(sendTime);
                        } else {
                            txvMsg_Other.setVisibility(View.GONE);
                            imgMsg_other.setVisibility(View.VISIBLE);

                            txvTime_Other.setVisibility(View.GONE);
                            txv_time_imgOther.setVisibility(View.VISIBLE);

                            storageReference = FirebaseStorage.getInstance().getReference();
                            storageReference = storageReference.child(filePath);
                            //讀取圖片
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                                    .load(uri)
                                    .into(imgMsg_other)).addOnFailureListener(e -> {
                                e.printStackTrace();
                                toast.setText(getResources().getString(R.string.retryAgain));
                                toast.show();
                            });
                            txv_time_imgOther.setText(sendTime);
                            imgMsg_other.setOnClickListener(v -> showPhoto(chatMessage));
                        }

                        txvUser_Other.setText(chatMessage.getUserName());

                    } else {//自己
                        userLayout.setVisibility(View.VISIBLE);
                        otherUserLayout.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(avatarPath)) {
                            storageReference = FirebaseStorage.getInstance().getReference();
                            storageReference = storageReference.child(avatarPath);
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                                    .load(uri)
                                    .into(img_avatar_user)).addOnFailureListener(Throwable::printStackTrace);
                        }

                        if (TextUtils.isEmpty(filePath)) {//如果有圖片訊息就秀圖
                            txvMsg_User.setVisibility(View.VISIBLE);
                            txvMsg_User.setText(chatMsg);

                            imgMsg_user.setVisibility(View.GONE);

                            txv_time_imgUSer.setVisibility(View.GONE);
                            txvTime_User.setVisibility(View.VISIBLE);
                            txvTime_User.setText(sendTime);

                        } else {

                            txvMsg_User.setVisibility(View.GONE);
                            imgMsg_user.setVisibility(View.VISIBLE);

                            storageReference = FirebaseStorage.getInstance().getReference();
                            storageReference = storageReference.child(filePath);
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                                    .load(uri)
                                    .into(imgMsg_user)).addOnFailureListener(Throwable::printStackTrace);

                            txv_time_imgUSer.setVisibility(View.VISIBLE);
                            txvTime_User.setVisibility(View.GONE);
                            txv_time_imgUSer.setText(sendTime);

                            imgMsg_user.setOnClickListener(v -> showPhoto(chatMessage));
                        }
                    }
                }
            }
        }

        public void showPhoto(ChatMessage chatMessage) {
            Dialog dialog = new Dialog(context, R.style.edit_AlertDialog_style);
            dialog.setContentView(R.layout.dialog_photo);
            final ImageView img_photo = dialog.findViewById(R.id.img_dialog_photo);
            storageReference = FirebaseStorage.getInstance().getReference();
            storageReference = storageReference.child(chatMessage.getFilePath());
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                    .load(uri)
                    .into(img_photo));

            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
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
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> AuthUI.getInstance().signOut(MainActivity.this)
                            .addOnCompleteListener(task -> {
                                Toast.makeText(MainActivity.this, "已登出囉！", Toast.LENGTH_SHORT).show();
                                finish();
                            })).setNegativeButton(android.R.string.no, (dialogInterface, i) -> {

                            }).create();
            builder.show();

        }
        return true;
    }
}