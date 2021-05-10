package com.septmb.septmb.septmb.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.adapters.RecentUserListAdapter;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.database.DBManager;
import com.septmb.septmb.septmb.models.UserEntity;

import java.util.ArrayList;
import java.util.Locale;

public class RecentUsersActivity extends AppCompatActivity implements View.OnClickListener,SwipyRefreshLayout.OnRefreshListener {

    private DBManager dbManager;
    ListView listView;
    EditText ui_edtsearch;
    LinearLayout allclear;
    ProgressDialog pd=null;
    ProgressDialog progressDialog;
    SwipyRefreshLayout ui_RefreshLayout;
    ArrayList<UserEntity> _datas=new ArrayList<>(10000);
    RecentUserListAdapter recentUserListAdapter=new RecentUserListAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_users);

        dbManager = new DBManager(this);
        dbManager.open();

        _datas.clear();
        listView=(ListView)findViewById(R.id.list_contacts);
        for(int i=0; i<dbManager.getAllMembers().size();i++){
            _datas.add(dbManager.getAllMembers().get(i));
        }
        recentUserListAdapter.setDatas(_datas);
        recentUserListAdapter.notifyDataSetChanged();
        listView.setAdapter(recentUserListAdapter);

        pd = new ProgressDialog(RecentUsersActivity.this);
        pd.setMessage("Loading...");

        setTitle("Contacts");

        ui_RefreshLayout = (SwipyRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        ui_RefreshLayout.setOnRefreshListener(this);
        ui_RefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTTOM);

        ImageView speechButton=(ImageView)findViewById(R.id.search_button);

        ImageView delete=(ImageView)findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ui_edtsearch.setText("");
            }
        });

        ui_edtsearch = (EditText)findViewById(R.id.edt_search);
        ui_edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = ui_edtsearch.getText().toString().toLowerCase(Locale.getDefault());
                if (text.length() != 0) {
                    recentUserListAdapter.filter(text);
                    //   adapter.notifyDataSetChanged();
                }else  {
                    recentUserListAdapter.setDatas(_datas);
                    listView.setAdapter(recentUserListAdapter);
                }

            }
        });

        ui_RefreshLayout.post(new Runnable() {
            @Override

            public void run() {

                recentUserListAdapter.setDatas(_datas);
                recentUserListAdapter.notifyDataSetChanged();
                listView.setAdapter(recentUserListAdapter);

//                Intent intent=new Intent(getApplicationContext(),NotificationListActivity.class);
//                startActivity(intent);
//                overridePendingTransition(0,0);
            }

        });
    }

    public void deleteContacts(MenuItem item){
        for(int i=0;i<dbManager.getAllMembers().size();i++){
            dbManager.delete(Long.parseLong(dbManager.getAllMembers().get(i).getIdx()));
        }

        _datas.clear();
        _datas.addAll(dbManager.getAllMembers());
        recentUserListAdapter.setDatas(_datas);
        recentUserListAdapter.notifyDataSetChanged();
        listView.setAdapter(recentUserListAdapter);
    }

    private static final int REQ_PICK_CONTACT = 2 ;

    public void smsContacts(MenuItem item){
        Toast.makeText(getApplicationContext(),"You can select anyone of\nthe recent SMS contacts",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        startActivityForResult(intent, REQ_PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();

                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Commons.user.setPhone(number);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contact_delete_menu, menu);
        return true;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void createChatRoom(){
//        customizeChatUi();
//        registerUserOnChatHistory();
        Intent intent=new Intent(getApplicationContext(),RequestActivity.class);
        startActivity(intent);
        overridePendingTransition(0,0);
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
        for(int i=0; i<dbManager.getAllMembers().size();i++) {
            if (dbManager.getAllMembers().get(i).getEmail().equals(Commons.user.getEmail())) {
                dbManager.delete(Long.parseLong(dbManager.getAllMembers().get(i).getIdx()));
                break;
            }
        }
        dbManager.insert(Commons.user.getName(), Commons.user.getEmail(), Commons.user.getPhotoUrl(), Commons.user.getPhone(), "0");
        openChatWith(Commons.user);
    }
}









































