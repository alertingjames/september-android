package com.septmb.septmb.septmb.adapters;

/**
 * Created by sonback123456 on 10/2/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.SeptemberApplication;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.main.MessagedUsersActivity;
import com.septmb.septmb.septmb.main.RecentUsersActivity;
import com.septmb.septmb.septmb.models.UserEntity;
import com.septmb.septmb.septmb.utils.CircularNetworkImageView;

import java.util.ArrayList;

/**
 * Created by a on 4/27/2017.
 */

public class MessagedUsersAdapter extends BaseAdapter {

    private MessagedUsersActivity _context;
    private ArrayList<UserEntity> _datas = new ArrayList<>();
    private ArrayList<UserEntity> _alldatas = new ArrayList<>();

    ImageLoader _imageLoader;

    public MessagedUsersAdapter(MessagedUsersActivity context){

        super();
        this._context = context;

        _imageLoader = SeptemberApplication.getInstance().getImageLoader();
    }

    public void setDatas(ArrayList<UserEntity> datas) {

        _alldatas = datas;
        _datas.clear();
        _datas.addAll(_alldatas);
    }

    @Override
    public int getCount(){
        return _datas.size();
    }

    @Override
    public Object getItem(int position){
        return _datas.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        final CustomHolder holder;

        if (convertView == null) {
            holder = new CustomHolder();

            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_user_list_item, parent, false);

            holder.photo = (CircularNetworkImageView) convertView.findViewById(R.id.photo);
            holder.name=(TextView)convertView.findViewById(R.id.name);
            holder.email=(TextView)convertView.findViewById(R.id.email);
            holder.phone=(TextView)convertView.findViewById(R.id.phone);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final UserEntity user = (UserEntity) _datas.get(position);

        holder.name.setText(user.getName());

        holder.email.setText(user.getEmail());

        holder.phone.setText(user.getPhone());

        if(user.getPhotoUrl().length()>0) {
            holder.photo.setImageUrl(user.getPhotoUrl(),_imageLoader);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.user=new UserEntity();
                Commons.user=user;
                _context.createChatRoom();
            }
        });

        return convertView;
    }

    class CustomHolder {

        public CircularNetworkImageView photo;
        public TextView name;
        public TextView email;
        public TextView phone;
    }
}







