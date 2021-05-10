package com.septmb.septmb.septmb.adapters;

/**
 * Created by sonback123456 on 10/2/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.SeptemberApplication;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.main.MessagedUsersActivity;
import com.septmb.septmb.septmb.main.RecentUsersActivity;
import com.septmb.septmb.septmb.models.ChatEntity;
import com.septmb.septmb.septmb.models.UserEntity;
import com.septmb.septmb.septmb.utils.CircularNetworkImageView;

import java.util.ArrayList;

/**
 * Created by a on 4/27/2017.
 */

public class InteractionHistoryAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<ChatEntity> _datas = new ArrayList<>();
    private ArrayList<ChatEntity> _alldatas = new ArrayList<>();

    ImageLoader _imageLoader;

    public InteractionHistoryAdapter(Context context){

        super();
        this._context = context;

        _imageLoader = SeptemberApplication.getInstance().getImageLoader();
    }

    public void setDatas(ArrayList<ChatEntity> datas) {

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
            convertView = inflater.inflate(R.layout.interaction_list_item, parent, false);

            holder.photo = (CircularNetworkImageView) convertView.findViewById(R.id.photo);
            holder.message=(TextView)convertView.findViewById(R.id.message);
            holder.time=(TextView)convertView.findViewById(R.id.time);
            holder.background=(LinearLayout) convertView.findViewById(R.id.frame);
            holder.background2=(LinearLayout) convertView.findViewById(R.id.frame2);
            holder.dots1=(LinearLayout) convertView.findViewById(R.id.dots1);
            holder.dots2=(LinearLayout) convertView.findViewById(R.id.dots2);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final ChatEntity chatEntity = (ChatEntity) _datas.get(position);

        holder.message.setText(chatEntity.getMessage());

        holder.time.setText(chatEntity.getTime());


        if(chatEntity.getPhoto().length()>0) {
//            holder.photo.setImageUrl(Commons.user.getPhotoUrl(),_imageLoader);
            holder.photo.setVisibility(View.GONE);
            holder.background.setBackgroundResource(R.drawable.message_bubble_background1);
            holder.background2.setGravity(Gravity.LEFT);
            holder.dots1.setVisibility(View.VISIBLE);
            holder.dots2.setVisibility(View.INVISIBLE);
        }else {
            holder.photo.setVisibility(View.GONE);
            holder.background.setBackgroundResource(R.drawable.message_bubble_background2);
            holder.background2.setGravity(Gravity.RIGHT);
            holder.dots1.setVisibility(View.INVISIBLE);
            holder.dots2.setVisibility(View.VISIBLE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return convertView;
    }

    class CustomHolder {

        public CircularNetworkImageView photo;
        public TextView message;
        public TextView time;
        public LinearLayout background;
        public LinearLayout background2;
        public LinearLayout dots1;
        public LinearLayout dots2;
    }
}







