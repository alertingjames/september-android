package com.septmb.septmb.septmb.main;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.qiscus.sdk.Qiscus;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.SeptemberApplication;
import com.septmb.septmb.septmb.classes.UserDetails;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.commons.ReqConst;
import com.septmb.septmb.septmb.database.DBManager;
import com.septmb.septmb.septmb.models.UserEntity;
import com.septmb.septmb.septmb.utils.CircularNetworkImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.Manifest.permission.SEND_SMS;

public class RequestActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    DBManager dbManager;
    LinearLayout buttonPage, smsPage;
    TextView smsButton, chatButton, phoneNumberBox, userName, userName2, phoneNumberBox2, email;
    ImageView audio, cancel;
    EditText messageBox, messageBox2;
    CircularNetworkImageView photo, photo2;
    android.widget.PopupMenu popupMenu;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    Firebase reference1, reference2, reference3;
    boolean is_typing=false;

    private static final int REQUEST_SMS = 0;
    private static final int REQ_PICK_CONTACT = 2 ;

    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        dbManager = new DBManager(this);
        dbManager.open();

        UserDetails.username= Commons.thisUser.getEmail().replace(".com","").replace(".","ddoott");
        UserDetails.chatWith= Commons.user.getEmail().replace(".com","").replace(".","ddoott");

        Firebase.setAndroidContext(this);
//        reference1 = new Firebase("https://androidchatapp-76776.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference1 = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"messages/" + UserDetails.chatWith + "_" + UserDetails.username);
        reference3 = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"notification/" + UserDetails.chatWith + "/"+UserDetails.username);

        buttonPage=(LinearLayout)findViewById(R.id.buttonPage);
        smsPage=(LinearLayout)findViewById(R.id.smsLayout);
        smsButton=(TextView)findViewById(R.id.smsButton);
        chatButton=(TextView)findViewById(R.id.chatButton);
        phoneNumberBox=(TextView)findViewById(R.id.phoneNumber);
        phoneNumberBox2=(TextView)findViewById(R.id.phoneNumber2);
        userName=(TextView)findViewById(R.id.name);
        userName2=(TextView)findViewById(R.id.name2);
        photo=(CircularNetworkImageView)findViewById(R.id.photo);
        photo2=(CircularNetworkImageView)findViewById(R.id.photo2);
        email=(TextView)findViewById(R.id.email);
        audio=(ImageView)findViewById(R.id.audio);
        cancel=(ImageView)findViewById(R.id.cancel);
        messageBox=(EditText)findViewById(R.id.message);
        messageBox2=(EditText)findViewById(R.id.inviteMessage);
//        messageBox2.setFocusable(false);

        photo.setImageUrl(Commons.user.getPhotoUrl(), SeptemberApplication.getInstance().getImageLoader());
        phoneNumberBox.setText(Commons.user.getPhone());
        userName.setText(Commons.user.getName());

        photo2.setImageUrl(Commons.user.getPhotoUrl(), SeptemberApplication.getInstance().getImageLoader());
        phoneNumberBox2.setText(Commons.user.getPhone());
        userName2.setText(Commons.user.getName());
        email.setText(Commons.user.getEmail());

        buttonPage.setVisibility(View.VISIBLE);
        Animation animation=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.custom);
        buttonPage.setAnimation(animation);

        setTitle("Contact");

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customizeChatUi();
                registerUserOnChatHistory();
            }
        });
        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(getApplicationContext(),SMSMainActivity.class);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

//        registerChatRoom();
    }

    public void sendMySMS() {

        sendSMS(Commons.user.getPhone(),"");
    }

    protected void sendSMS(String phoneNumber, String message) {
        Log.i("Send SMS", "");
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);

        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address"  , phoneNumber);
        smsIntent.putExtra("sms_body"  , message);

        try {
            startActivity(smsIntent);
//            finish();
            Log.i("Finished sending SMS...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(RequestActivity.this,
                    "SMS faild, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    public void openMenuItems(View v) {
        View view = findViewById(R.id.menu);
//        PopupMenu popup = new PopupMenu(this, view);
//        getMenuInflater().inflate(R.menu.attach_menu, popup.getMenu());
        popupMenu = new android.widget.PopupMenu(this, view);
        popupMenu.inflate(R.menu.recent_user_menu);
        Object menuHelper;
        Class[] argTypes;
        try {
            Field fMenuHelper = android.widget.PopupMenu.class.getDeclaredField("mPopup");
            fMenuHelper.setAccessible(true);
            menuHelper = fMenuHelper.get(popupMenu);
            argTypes = new Class[]{boolean.class};
            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
        } catch (Exception e) {
            // Possible exceptions are NoSuchMethodError and NoSuchFieldError
            //
            // In either case, an exception indicates something is wrong with the reflection code, or the
            // structure of the PopupMenu class or its dependencies has changed.
            //
            // These exceptions should never happen since we're shipping the AppCompat library in our own apk,
            // but in the case that they do, we simply can't force icons to display, so log the error and
            // show the menu normally.

            Log.w("Error====>", "error forcing menu icons to show", e);
            popupMenu.show();
            return;
        }
        popupMenu.show();
    }

    public void showMessagedUsers(MenuItem item){
        Intent intent=new Intent(getApplicationContext(),MessagedUsersActivity.class);
        intent.putExtra("intent","request");
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    public void showInteractionHistory(MenuItem item){
        Intent intent=new Intent(getApplicationContext(),InteractionHistoryActivity.class);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    private void showButtons() {
        smsButton.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.custom);
        smsButton.startAnimation(animation);
        chatButton.setVisibility(View.VISIBLE);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translateup);
        chatButton.startAnimation(animation);
    }

    private void customizeChatUi() {
        Qiscus.getChatConfig()
                .setStatusBarColor(android.R.color.black)
                .setAppBarColor(R.color.qiscus_transparent_black)
                .setTitleColor(android.R.color.white)
                .setLeftBubbleColor(R.color.leftbubble)
                .setRightBubbleColor(R.color.rightbubble)
                .setRightBubbleTextColor(android.R.color.black)
                .setLeftBubbleTextColor(android.R.color.black)
                .setRightBubbleTimeColor(R.color.time)
                .setLeftBubbleTimeColor(R.color.time);
    }

    public void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait...");
        }
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void dismissLoading() {
        progressDialog.dismiss();
    }

    private void openChatWith(final UserEntity user) {
        showLoading();
        Qiscus.buildChatWith(user.getEmail())
                .withTitle(user.getName())
                .build(this, new Qiscus.ChatActivityBuilderListener() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivity(intent);
                        dismissLoading();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        showError("Failed to create chatroom, make sure " + user.getEmail() + " is registered!");
                        dismissLoading();
                    }
                });
    }
    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    public void registerUserOnChatHistory(){
        if(!Commons.user.getEmail().equals(Commons.thisUser.getEmail())){
            for(int i=0; i<dbManager.getAllMembers().size();i++) {
                if (dbManager.getAllMembers().get(i).getEmail().equals(Commons.user.getEmail())) {
                    dbManager.delete(Long.parseLong(dbManager.getAllMembers().get(i).getIdx()));
                    break;
                }
            }
            dbManager.insert(Commons.user.getName(), Commons.user.getEmail(), Commons.user.getPhotoUrl(), Commons.user.getPhone(), "0");
        }
        openChatWith(Commons.user);
    }

    void sendSmsMsgFnc(String mblNumVar, String smsMsgVar)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
        {
            try
            {
                SmsManager smsMgrVar = SmsManager.getDefault();
                smsMgrVar.sendTextMessage(mblNumVar, null, smsMsgVar, null, null);
                Toast.makeText(getApplicationContext(), "Message Sent",
                        Toast.LENGTH_LONG).show();
                if(!Commons.user.getEmail().equals(Commons.thisUser.getEmail())){
                    for(int i=0; i<dbManager.getAllMembers().size();i++) {
                        if (dbManager.getAllMembers().get(i).getEmail().equals(Commons.user.getEmail())) {
                            dbManager.delete(Long.parseLong(dbManager.getAllMembers().get(i).getIdx()));
                            break;
                        }
                    }
                    dbManager.insert(Commons.user.getName(), Commons.user.getEmail(), Commons.user.getPhotoUrl(), Commons.user.getPhone(), "0");
                }
            }
            catch (Exception ErrVar)
            {
                Toast.makeText(getApplicationContext(),ErrVar.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                ErrVar.printStackTrace();
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 10);
            }
        }
    }

    public void deleteMessage(View view){
        messageBox.setText("");
    }

    public void cancelSMSpage(View view){
        smsPage.setVisibility(View.GONE);
        buttonPage.setVisibility(View.VISIBLE);
        showButtons();
    }
    public void startSpeechRecognition(View view){
        startVoiceRecognitionActivity();
    }
    public void sendSMS(View view){
        if(messageBox.getText().length()>0)
            sendSmsMsgFnc(Commons.user.getPhone(),messageBox.getText().toString());
        else Toast.makeText(getApplicationContext(),"Please input your SMS message", Toast.LENGTH_SHORT).show();
    }

    public void sendRequest(View view){
        if(messageBox2.getText().length()>0){
            if(!Commons.user.getEmail().equals(Commons.thisUser.getEmail())){
                for(int i=0; i<dbManager.getAllMembers().size();i++) {
                    if (dbManager.getAllMembers().get(i).getEmail().equals(Commons.user.getEmail())) {
                        dbManager.delete(Long.parseLong(dbManager.getAllMembers().get(i).getIdx()));
                        break;
                    }
                }
                dbManager.insert(Commons.user.getName(), Commons.user.getEmail(), Commons.user.getPhotoUrl(), Commons.user.getPhone(), "0");
            }
            sendRequestMessage();
        }
        else Toast.makeText(getApplicationContext(),"Please enter your request message", Toast.LENGTH_SHORT).show();
    }


    public void startVoiceRecognitionActivity() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        intent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{"en","ko","de","ja","fr"});

//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languagePref);
//        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languagePref);

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,

                "AndroidBite Voice Recognition...");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),"Sorry! Your device doesn\'t support speech input",Toast.LENGTH_SHORT).show();
        }catch (NullPointerException a) {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK) {

            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            messageBox2.setText(matches.get(0));

        }
    }

    public void registerChatRoom(){

        String url = ReqConst.FIREBASE_DATABASE_URL+"users.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                Firebase reference = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"users/"+Commons.user.getEmail().replace(".com","").replace(".","ddoott"));

                if(s.equals("null")) {

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("email", Commons.user.getEmail());

                    if(Commons.user.getName().length()>0)
                        map.put("name", Commons.user.getName());

                    map.put("photo", Commons.user.getPhotoUrl());
                    map.put("phone", Commons.user.getPhone());

                    reference.push().setValue(map);
                }
                else {
                    try {
                        JSONObject obj = new JSONObject(s);

                        if (!obj.has(Commons.user.getEmail().replace(".com","").replace(".","ddoott"))) {

                            Map<String, String> map = new HashMap<String, String>();
                            map.put("email", Commons.user.getEmail());

                            if(Commons.user.getName().length()>0)
                                map.put("name", Commons.user.getName());

                            map.put("photo", Commons.user.getPhotoUrl());
                            map.put("phone", Commons.user.getPhone());

                            reference.push().setValue(map);
                        } else {

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError );
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(RequestActivity.this);
        rQueue.add(request);

    }

    public void sendRequestMessage(){
        String messageText = messageBox2.getText().toString().trim();

        if(messageText.length()>0) {

            Map<String, String> map = new HashMap<String, String>();
            map.put("message", messageText);
            map.put("istyping", "false");
            map.put("online", "true");
            map.put("time", String.valueOf(new Date().getTime()));
            map.put("image", "");
            map.put("video", "");
            map.put("lat", "");
            map.put("lon", "");
            map.put("user", UserDetails.username);
            reference1.push().setValue(map);
            reference2.push().setValue(map);

            is_typing = false;

            Map<String, String> map0 = new HashMap<String, String>();
            map0.put("sender", UserDetails.username.replace("ddoott", "."));
            if (Commons.thisUser.getName().length() > 0)
                map0.put("senderName", Commons.thisUser.getName());
            map0.put("senderPhoto", Commons.thisUser.getPhotoUrl());
            map0.put("senderPhone", Commons.thisUser.getPhone());
            map0.put("msg", messageText);

            reference3.push().setValue(map0);

//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                int hasSMSPermission = checkSelfPermission(Manifest.permission.SEND_SMS);
//                if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
//                    if (!shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
//                        showMessageOKCancel("You need to allow access to Send SMS",
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                            requestPermissions(new String[] {Manifest.permission.SEND_SMS},
//                                                    REQUEST_SMS);
//                                        }
//                                    }
//                                });
//                        return;
//                    }
//                    requestPermissions(new String[] {Manifest.permission.SEND_SMS},
//                            REQUEST_SMS);
//                    return;
//                }
//                sendMySMS2();
//            }

            Toast.makeText(getApplicationContext(),"Message sent!", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendMySMS2() {

        String phone = phoneNumberBox2.getText().toString().trim();
        String message = messageBox2.getText().toString().trim();

        //Check if the phoneNumber is empty
        if (phone.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Enter a Valid Phone Number", Toast.LENGTH_SHORT).show();
        } else if(message.isEmpty())
            Toast.makeText(getApplicationContext(), "Please Enter your message", Toast.LENGTH_SHORT).show();
        else {

            android.telephony.SmsManager sms = android.telephony.SmsManager.getDefault();
            // if message length is too long messages are divided
            List<String> messages = sms.divideMessage(message);
            for (String msg : messages) {

                PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
                PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
                sms.sendTextMessage(phone, null, msg, sentIntent, deliveredIntent);

            }
            messageBox2.setText("");
        }
    }

    public void onResume() {
        super.onResume();
        sentStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Unknown Error";
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Sent Successfully !!";
                        break;
                    case android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        s = "Generic Failure Error";
                        break;
                    case android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE:
                        s = "Error : No Service Available";
                        break;
                    case android.telephony.SmsManager.RESULT_ERROR_NULL_PDU:
                        s = "Error : Null PDU";
                        break;
                    case android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF:
                        s = "Error : Radio is off";
                        break;
                    default:
                        break;
                }
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
            }
        };

        deliveredStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Message Not Delivered";
                switch(getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Delivered Successfully";
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
            }
        };
        registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));
        registerReceiver(deliveredStatusReceiver, new IntentFilter("SMS_DELIVERED"));
    }


    public void onPause() {
        super.onPause();
        unregisterReceiver(sentStatusReceiver);
        unregisterReceiver(deliveredStatusReceiver);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SMS:
                if (grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access sms", Toast.LENGTH_SHORT).show();
                    sendMySMS2();

                }else {
                    Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and sms", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(SEND_SMS)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{SEND_SMS},
                                                        REQUEST_SMS);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(RequestActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}





































