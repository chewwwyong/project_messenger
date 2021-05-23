package com.chewwwyong.project_messenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.chewwwyong.project_messenger.Controller.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class talk_in_private extends AppCompatActivity {
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

    TextView tv_who;
    EditText edt_input;
    ListView ltv_message;
    FloatingActionButton fab_send, fab_return_choose_who;
    // listview
    ArrayList item = new ArrayList();
    ArrayAdapter adapter;

    String who;
    String me;
    ArrayList<String> addFriend = new ArrayList<>();
    ArrayList<String> additem = new ArrayList<>();
    MqttAndroidClient mqttAndroidClient;

    NotificationManager manager;
    Bitmap largeIcon;
    PendingIntent pendingIntent;
    Notification notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_in_private);

        // 隱藏標題
        getSupportActionBar().hide();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = "default_notification_channel_id";
            String channelName = "default_notification_channel_name";
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        // 取得NotificationManager物件
        manager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        // 建立大圖示需要的Bitmap物件
        largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.tongshenduan_hotpot);

        // 點擊時要啟動的PendingIntent，當中包含一個Intent設置要開啟的Activity
        pendingIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        tv_who = findViewById(R.id.tv_who);
        edt_input = findViewById(R.id.edt_input);
        ltv_message = findViewById(R.id.ltv_message);
        fab_send = findViewById(R.id.fab_send_private);
        fab_return_choose_who = findViewById(R.id.fab_return_choose_who);

        Intent it = getIntent();
        me = it.getStringExtra("LoginName");
        who = it.getStringExtra("send_to_who");

        // 把好友列表傳過來，等等要再傳回去，這樣才可以記錄下來
        additem = it.getStringArrayListExtra("FriendList");
        //Toast.makeText(talk_in_private.this, additem.toString(), Toast.LENGTH_SHORT).show();
        //

        addFriend.add(me); // 要知道誰有私訊我

        PUB_TOPIC = who;
        tv_who.setText(who);

        //item = new ArrayList();
        adapter = new ArrayAdapter(talk_in_private.this, android.R.layout.simple_list_item_1, item);
        ltv_message.setAdapter(adapter);

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
                //Toast.makeText(talk_in_private.this,"Topic: " + topic + "\n msg: \n" + new String(message.getPayload()),Toast.LENGTH_SHORT).show();

                // new message
                item.add(who + " : " + new String(message.getPayload()));
                adapter.notifyDataSetChanged();
                ltv_message.smoothScrollToPosition(item.size()-1);

                // 建立通知物件，設定小圖示、大圖示、內容標題、內容訊息、時間
                notification = new NotificationCompat.Builder(talk_in_private.this)
                        .setSmallIcon(R.drawable.tongshenduan_hotpot)
                        .setLargeIcon(largeIcon)
                        .setContentTitle("標題")
                        .setContentText(Arrays.toString(message.getPayload()))
                        .setWhen(System.currentTimeMillis())
                        // 訊息內容較長會超過一行時預設會將訊息結尾變成...而不能完整顯示，此時可以再加入一行BigText讓長訊息能完整顯示
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(Arrays.toString(message.getPayload())))
                        .setDefaults(Notification.DEFAULT_VIBRATE) // 加上提醒效果
                        .setContentIntent(pendingIntent)  // 設置Intent
                        .addAction(R.drawable.tongshenduan_hotpot, "查看", pendingIntent)  // 增加「查看」
                        .setAutoCancel(true)    // 點擊後讓Notification消失
                        .build();
                // 使用0為編號發出通知
                manager.notify(0, notification);
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
        fab_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishMessage(edt_input.getText().toString());
                item.add("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + edt_input.getText().toString());
                adapter.notifyDataSetChanged();
                ltv_message.smoothScrollToPosition(item.size()-1);
            }
        });

        // 返回
        fab_return_choose_who.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(talk_in_private.this, choose_who.class);
                it.putExtra("return_choose_who", 1);
                it.putStringArrayListExtra("reFriendList", additem);
                startActivity(it);
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
}