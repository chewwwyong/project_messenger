package com.chewwwyong.project_messenger;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chewwwyong.project_messenger.Util.BitmapUtil;
import com.chewwwyong.project_messenger.Util.PermissionTool;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
//import com.google.firebase.installations.Utils;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.chewwwyong.project_messenger.Util.Utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "AiotMqtt";
    /* 设备三元组信息 */
    final private String PRODUCTKEY = "a11xsrWmW14";
    final private String DEVICENAME = "paho_android";
    final private String DEVICESECRET = "tLMT9QWD36U2SArglGqcHCDK9rK9nOrA";

    /* 自动Topic, 用于上报消息 */
    private String PUB_TOPIC = "who";
    /* 自动Topic, 用于接受消息 */
    private String SUB_TOPIC = "diderwei";


    final String host = "tcp://test.mosquitto.org:1883";
    private String clientId = "project_Messenger_Subcribe";
    //private String userName = "chewwwyong";
    //private String passWord = "123456";

    //TextView txv_Message_box;
    EditText edt_getText;

    ListView ltv_Message_box;
    // listview
    ArrayList item = new ArrayList();

    String who;
    String me;
    ArrayList<String> addFriend = new ArrayList<>();
    MqttAndroidClient mqttAndroidClient;

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
    public static final String AUTHORITY = "com.chewwwyong.project_messenger";

    private Toast toast;

    private FirebaseRecyclerAdapter<ChatMessage, ChatMessageHolder> adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private DatabaseReference reference;
    private StorageReference storageReference;
    private InputMethodManager imm;
    private String uuid;

    private Uri camera_uri;
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

        //txv_Message_box = findViewById(R.id.txv_Message_box);
        edt_getText = findViewById(R.id.edtInput);
        ltv_Message_box = findViewById(R.id.ltv_Message_box);
        Intent it = getIntent();
        me = it.getStringExtra("LoginName");
        who = it.getStringExtra("send_to_who");

        //addFriend = it.getStringArrayListExtra("FriendList");
        addFriend.add(me); // 要知道誰有私訊我

        PUB_TOPIC = who;

        context = MainActivity.this;

        //setTitle(getResources().getString(R.string.titleName));


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



        /* 获取Mqtt建连信息clientId, username, password */
        AiotMqttOption aiotMqttOption = new AiotMqttOption().getMqttOption(PRODUCTKEY, DEVICENAME, DEVICESECRET);
        if (aiotMqttOption == null) {
            Log.e(TAG, "device info error");
        } else {
            clientId = aiotMqttOption.getClientId();
            //userName = aiotMqttOption.getUsername();
            //passWord = aiotMqttOption.getPassword();
        }

        /* 创建MqttConnectOptions对象并配置username和password */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        //mqttConnectOptions.setUserName(userName);
        //mqttConnectOptions.setPassword(passWord.toCharArray());


        /* 创建MqttAndroidClient对象, 并设置回调接口 */
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
                Toast.makeText(MainActivity.this,"Topic: " + topic + "\n msg: \n" + new String(message.getPayload()),Toast.LENGTH_SHORT).show();

                //txv_Message_box.setText(
                //            txv_Message_box.getText() +
                //                    "\n" + who + " : " + new String(message.getPayload())
                //);

                // new message
                item.add("\n" + who + " : " + new String(message.getPayload()));
                ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, item);
                ltv_Message_box.setAdapter(adapter);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "msg delivered");
            }
        });

        /* Mqtt建连 */
        try {
            mqttAndroidClient.connect(mqttConnectOptions,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "connect succeed");
                    //    Toast.makeText(MainActivity.this,"connect succeed",Toast.LENGTH_SHORT).show();

                    for(int i=0;i<addFriend.size();i++) {
                        SUB_TOPIC = addFriend.get(i);
                        subscribeTopic(SUB_TOPIC);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "connect failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }



        /* 通过按键发布消息 */
        FloatingActionButton pubButton = findViewById(R.id.fabSend);
        pubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishMessage(edt_getText.getText().toString());
                //txv_Message_box.setText(txv_Message_box.getText().toString() +
                //        "\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + edt_getText.getText().toString());

                // new message
                item.add("\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + edt_getText.getText().toString());
                ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, item);
                ltv_Message_box.setAdapter(adapter);
                sendMsg();
            }
        });
    }

    /**
     * 订阅特定的主题
     * @param topic mqtt主题
     */
    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "subscribed succeed");
                    //   Toast.makeText(MainActivity.this,"subscribed succeed",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向默认的主题/user/update发布消息
     * @param payload 消息载荷
     */
    public void publishMessage(String payload) {
        try {
            if (mqttAndroidClient.isConnected() == false) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(PUB_TOPIC, message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "publish succeed!");
                    //   Toast.makeText(MainActivity.this,"publish succeed",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * MQTT建连选项类，输入设备三元组productKey, deviceName和deviceSecret, 生成Mqtt建连参数clientId，username和password.
     */
    class AiotMqttOption {
        private String username = "";
        private String password = "";
        private String clientId = "";

        public String getUsername() { return this.username;}
        public String getPassword() { return this.password;}
        public String getClientId() { return this.clientId;}

        /**
         * 获取Mqtt建连选项对象
         * @param productKey 产品秘钥
         * @param deviceName 设备名称
         * @param deviceSecret 设备机密
         * @return AiotMqttOption对象或者NULL
         */
        public AiotMqttOption getMqttOption(String productKey, String deviceName, String deviceSecret) {
            if (productKey == null || deviceName == null || deviceSecret == null) {
                return null;
            }

            try {
                String timestamp = Long.toString(System.currentTimeMillis());

                // clientId
                this.clientId = productKey + "." + deviceName + "|timestamp=" + timestamp +
                        ",_v=paho-android-1.0.0,securemode=2,signmethod=hmacsha256|";

                // userName
                this.username = deviceName + "&" + productKey;

                // password
                String macSrc = "clientId" + productKey + "." + deviceName + "deviceName" +
                        deviceName + "productKey" + productKey + "timestamp" + timestamp;
                String algorithm = "HmacSHA256";
                Mac mac = Mac.getInstance(algorithm);
                SecretKeySpec secretKeySpec = new SecretKeySpec(deviceSecret.getBytes(), algorithm);
                mac.init(secretKeySpec);
                byte[] macRes = mac.doFinal(macSrc.getBytes());
                password = String.format("%064x", new BigInteger(1, macRes));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return this;
        }
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
        for (String s : saveKeyList) {
            keyList.add(s);
        }
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
            if (Build.VERSION.SDK_INT >= 24) {
                camera_uri = FileProvider.getUriForFile(getApplicationContext(),
                        AUTHORITY, cameraFile);
            } else {
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
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        long time = new Date().getTime();

        String key = reference.push().getKey();
        keyList.add(key);
        if (TextUtils.isEmpty(avatarPath))
            avatarPath = "";
        reference.child(key).setValue(new ChatMessage(userName, msg, time, uuid, "", avatarPath));

        edt_getText.setText("");
        Set<String> saveKeyList = new HashSet<>();
        for (int i = 0; i < keyList.size(); i++) {
            saveKeyList.add(keyList.get(i));
        }
        sharedPreferences.edit().putStringSet("keyList", saveKeyList).commit();

    }

    @Override
    protected void onStart() {
        super.onStart();

//        adapter.startListening();//啟動監聽，訊息可即時更新

    }

    @Override
    protected void onStop() {
        super.onStop();
//        adapter.stopListening();
    }

    //秀出訊息
    private void displayChatMsg() {

        try {
            adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatMessageHolder>
                    (ChatMessage.class, R.layout.message, ChatMessageHolder.class, reference.limitToLast(10)) {

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
                    .into(img_avatar)).addOnFailureListener(e -> e.printStackTrace());
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
                    doCropPhoto(uri, 0, requestCode);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                toast.setText(getResources().getString(R.string.retryAgain));
                toast.show();
            }

        } else if (requestCode == ALBUM_REQUEST || requestCode == AVATAR_ALBUM_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                doCropPhoto(data.getData(), 0, requestCode);

            } else {
                toast.setText(getResources().getString(R.string.retryAgain));
                toast.show();
            }
        } else if (requestCode == AVATAR_CROP_REQUEST || requestCode == CROP_REQUEST) {
            if (data.hasExtra(CropImageActivity.EXTRA_IMAGE) && data != null) {
                //取得裁切後圖片的暫存位置
                String filePath = data.getStringExtra(CropImageActivity.EXTRA_IMAGE);
                if (filePath.indexOf(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM") != -1) {
                    File imgFile = new File(filePath);
                    if (imgFile.exists()) {
                        //代入已設定好的圖片size
                        int photoSize = getResources().getDimensionPixelSize(R.dimen.photo_size);
                        //使用寫好的方法將路徑檔做成bitmap檔
                        Bitmap realBitmap = BitmapUtil.decodeSampledBitmap(imgFile.getAbsolutePath(), photoSize, photoSize);
                        File file = Utils.getInstance().bitmapToFile(getCacheDir(), cameraFileName, realBitmap);

                        if (requestCode == AVATAR_CROP_REQUEST) {//改大頭貼的動作才換路徑
                            avatarPath = file.getName();
                            sharedPreferences.edit().putString(getResources().getString(R.string.avatarPath), avatarPath).commit();
                        }

                        uploadFile(Uri.fromFile(file), file.getName(), requestCode);
                    }
                }
            }
        }
    }

    private void doCropPhoto(Uri uri, int degree, int requestCode) {
        Intent intent = new Intent(context, CropImageActivity.class);
        intent.setData(uri);
        intent.putExtra(getResources().getString(R.string.degree), degree);
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
            String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            long time = new Date().getTime();

            if (requestCode == CAMERA_REQUEST || requestCode == ALBUM_REQUEST || requestCode == CROP_REQUEST) {//上傳圖片
                String key = reference.push().getKey();
                keyList.add(key);
                reference.child(key).setValue(new ChatMessage(userName, "", time, uuid, fileName, avatarPath));
                Set<String> saveKeyList = new HashSet<>();
                for (int i = 0; i < keyList.size(); i++) {
                    saveKeyList.add(keyList.get(i));
                }
                sharedPreferences.edit().putStringSet("keyList", saveKeyList).commit();

            } else if (requestCode == AVATAR_CROP_REQUEST) {//更新大頭貼

                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < keyList.size(); i++) {
                    map.put(keyList.get(i) + "/avatarPath", fileName);
                }
                reference.updateChildren(map);
                sharedPreferences.edit().putString(getResources().getString(R.string.avatarPath), fileName).commit();

            }


        }).addOnFailureListener(e -> e.printStackTrace());
    }

    //處理訊息的地方
    public class ChatMessageHolder extends RecyclerView.ViewHolder {
        private TextView txvUser_Other;
        private TextView txvMsg_Other;
        private TextView txvTime_Other;

        private TextView txvMsg_User;
        private TextView txvTime_User;
        private TextView txv_time_imgOther;
        private ImageView img_avatar_other, img_avatar_user;

        private TextView txv_time_imgUSer;
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
                                    .into(img_avatar_other)).addOnFailureListener(e -> e.printStackTrace());
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
                                    .into(img_avatar_user)).addOnFailureListener(e -> e.printStackTrace());
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
                                    .into(imgMsg_user)).addOnFailureListener(e -> e.printStackTrace());

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
}