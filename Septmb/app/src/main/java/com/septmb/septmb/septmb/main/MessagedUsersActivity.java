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
import com.septmb.septmb.septmb.adapters.MessagedUsersAdapter;
import com.septmb.septmb.septmb.adapters.RecentUserListAdapter;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.database.DBManager;
import com.septmb.septmb.septmb.models.UserEntity;

import java.util.ArrayList;
import java.util.Locale;

public class MessagedUsersActivity extends AppCompatActivity{

    ListView listView;
    ArrayList<UserEntity> _datas=new ArrayList<>(10000);
    MessagedUsersAdapter messagedUsersAdapter=new MessagedUsersAdapter(this);
    String intent="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaged_users);

        intent=getIntent().getStringExtra("intent");

        _datas.clear();
        listView=(ListView)findViewById(R.id.list_contacts);
        _datas.addAll(Commons.userEntities);
        if(_datas.isEmpty())
            Toast.makeText(getApplicationContext(),"No messaged user...",Toast.LENGTH_SHORT).show();
        messagedUsersAdapter.setDatas(_datas);
        messagedUsersAdapter.notifyDataSetChanged();
        listView.setAdapter(messagedUsersAdapter);

        setTitle("Messaged Users");
    }

    public void createChatRoom(){
        Intent intent=new Intent(getApplicationContext(),RequestActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
    }

    @Override
    public void onBackPressed() {
        if(intent.equals("interaction"))
            finish();
        else
            createChatRoom();
    }
}









































