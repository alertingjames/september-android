package com.septmb.septmb.septmb.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.SeptemberApplication;
import com.septmb.septmb.septmb.adapters.InteractionHistoryAdapter;
import com.septmb.septmb.septmb.adapters.MessagedUsersAdapter;
import com.septmb.septmb.septmb.classes.UserDetails;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.commons.ReqConst;
import com.septmb.septmb.septmb.database.DBManager;
import com.septmb.septmb.septmb.models.ChatEntity;
import com.septmb.septmb.septmb.models.UserEntity;
import com.septmb.septmb.septmb.utils.CircularNetworkImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InteractionHistoryActivity extends AppCompatActivity implements View.OnClickListener,SwipyRefreshLayout.OnRefreshListener  {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    ListView listView;
    ArrayList<ChatEntity> _datas=new ArrayList<>(10000);
    InteractionHistoryAdapter interactionHistoryAdapter=new InteractionHistoryAdapter(this);
    Firebase reference1,reference2,reference3,reference7;
    SwipyRefreshLayout ui_RefreshLayout;
    long i=0;
    DBManager dbManager;
    EditText messageBox;
    ImageView sendButton;
    boolean is_typing=false;
    CircularNetworkImageView photo, photoMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interaction_history);

        setTitle(Commons.user.getName());

        _datas.clear();

        listView=(ListView)findViewById(R.id.list_contacts);

        sendButton=(ImageView)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(messageBox.getText().length()>0)
                    sendRequestMessage();
                else
                    Toast.makeText(getApplicationContext(),"Please enter message...",Toast.LENGTH_SHORT).show();
            }
        });

        ui_RefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        ui_RefreshLayout.setOnRefreshListener(this);
        ui_RefreshLayout.setDirection(SwipyRefreshLayoutDirection.TOP);

        photo=(CircularNetworkImageView)findViewById(R.id.photo);
        photo.setImageUrl(Commons.user.getPhotoUrl(), SeptemberApplication.getInstance()._imageLoader);

        photoMe=(CircularNetworkImageView)findViewById(R.id.photoMe);
        photoMe.setImageUrl(Commons.thisUser.getPhotoUrl(), SeptemberApplication.getInstance()._imageLoader);

        dbManager = new DBManager(this);
        dbManager.open();


        UserDetails.username= Commons.thisUser.getEmail().replace(".com","").replace(".","ddoott");
        UserDetails.chatWith= Commons.user.getEmail().replace(".com","").replace(".","ddoott");

        Firebase.setAndroidContext(this);
//        reference1 = new Firebase("https://androidchatapp-76776.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference1 = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"messages/" + UserDetails.chatWith + "_" + UserDetails.username);
        reference3 = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"notification/" + UserDetails.chatWith + "/"+UserDetails.username);
        reference7 = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"notification/" + UserDetails.username +"/"+UserDetails.chatWith);

        messageBox=(EditText)findViewById(R.id.messageBox);

        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String messageText = messageBox.getText().toString();
                if(!is_typing){
                    is_typing=true;
                    if(!messageText.equals("")){
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("message", "");
                        map.put("istyping", "true");
                        map.put("image", "");
                        map.put("video", "");
                        map.put("lat", "");
                        map.put("lon", "");
                        map.put("online", "true");
                        map.put("time", String.valueOf(new Date().getTime()));
                        map.put("user", UserDetails.username);
                        reference1.push().setValue(map);
                        reference2.push().setValue(map);
                    }
                }else {
                    if(messageText.length()==0){
                        is_typing=false;
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("message", "");
                        map.put("istyping", "false");
                        map.put("image", "");
                        map.put("video", "");
                        map.put("lat", "");
                        map.put("lon", "");
                        map.put("online", "true");
                        map.put("time", String.valueOf(new Date().getTime()));
                        map.put("user", UserDetails.username);
                        reference1.push().setValue(map);
                        reference2.push().setValue(map);
                    }
                }
            }
        });

        online("true");


        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                long count= (long) dataSnapshot.getChildrenCount(); Log.d("Count===>",String.valueOf(count));

                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String status = map.get("istyping").toString();
                String image = map.get("image").toString();
                String video = map.get("video").toString();
                String latStr = map.get("lat").toString();
                String lonStr = map.get("lon").toString();
                String online = map.get("online").toString();
                String userName = map.get("user").toString();
                String time = map.get("time").toString();

                Log.d("Name===>",userName);
                Log.d("Message===>",message);
                Log.d("Time===>",time);

                ++i; Log.d("Count2===>", String.valueOf(i));

                if(userName.equals(UserDetails.username)){
                    ChatEntity chatEntity=new ChatEntity();
                    chatEntity.setMessage(message);
                    chatEntity.setTime((String) DateFormat.format("MM/dd/yyyy (HH:mm:ss)",
                            Long.parseLong(time)));
                    chatEntity.setPhoto("");
                    if(message.length()>0)
                        _datas.add(chatEntity);
                }
                else{
                    if(status.equals("true"))
                        setTitle(Commons.user.getName()+" "+"is typing...");
                    else {
                        if(online.equals("true"))
                            setTitle(Commons.user.getName() + " " + "is online");
                        else
                            setTitle(Commons.user.getName()+" "+"is offline at "+(String) DateFormat.format("MM/dd/yyyy (HH:mm)",
                                    Long.parseLong(time)));
                    }
                    ChatEntity chatEntity=new ChatEntity();
                    chatEntity.setMessage(message);
                    chatEntity.setTime((String) DateFormat.format("MM/dd/yyyy (HH:mm:ss)",
                            Long.parseLong(time)));
                    chatEntity.setPhoto(Commons.user.getPhotoUrl());
                    if(message.length()>0)
                        _datas.add(chatEntity);
                }
                interactionHistoryAdapter.setDatas(_datas);
                interactionHistoryAdapter.notifyDataSetChanged();
                listView.setAdapter(interactionHistoryAdapter);

                listView.post(new Runnable(){
                    public void run() {
                        listView.setSelection(listView.getCount() - 1);
                    }});
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        Log.d("Len===>",String.valueOf(_datas.size()));
        registerChatRoom();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recent_user_menu, menu);
        menu.getItem(1).setVisible(false);
        return true;
    }

    public void showMessagedUsers(MenuItem item){
        Intent intent=new Intent(getApplicationContext(),MessagedUsersActivity.class);
        intent.putExtra("intent","interaction");
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    public void showInteractionHistory(MenuItem item){

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }

    public void startSpeechRecognition(View view){
        startVoiceRecognitionActivity();
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

            messageBox.setText(matches.get(0));

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

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError );
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(InteractionHistoryActivity.this);
        rQueue.add(request);

    }

    public void sendRequestMessage(){
        String messageText = messageBox.getText().toString().trim();

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
            messageBox.setText("");
            is_typing = false;

            Map<String, String> map0 = new HashMap<String, String>();
            map0.put("sender", UserDetails.username.replace("ddoott", "."));
            if (Commons.thisUser.getName().length() > 0)
                map0.put("senderName", Commons.thisUser.getName());
            map0.put("senderPhoto", Commons.thisUser.getPhotoUrl());
            map0.put("senderPhone", Commons.thisUser.getPhone());
            map0.put("msg", messageText);

            reference3.push().setValue(map0);
        }
    }
    public void online(String status){
        Map<String, String> map = new HashMap<String, String>();
        map.put("message", "");
        map.put("istyping", "false");
        map.put("image", "");
        map.put("video", "");
        map.put("lat", "");
        map.put("lon", "");
        map.put("online", status);
        map.put("time", String.valueOf(new Date().getTime()));
        map.put("user", UserDetails.username);
        reference1.push().setValue(map);
        reference2.push().setValue(map);
        if(status.equals("false")){
//            Intent intent=new Intent(this, ChatHistoryActivity.class);
//            startActivity(intent);
            finish();
            overridePendingTransition(0,0);
        }
    }

    @Override
    public void onBackPressed() {
        reference7.removeValue();
        finish();
        online("false");
    }
}








































