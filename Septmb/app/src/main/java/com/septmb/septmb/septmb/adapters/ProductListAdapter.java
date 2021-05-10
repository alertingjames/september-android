package com.septmb.septmb.septmb.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.SeptemberApplication;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.main.ProductDetailActivity;
import com.septmb.septmb.septmb.main.ReviewMyProductsActivity;
import com.septmb.septmb.septmb.models.ProductInfoEntity;

import java.util.ArrayList;

/**
 * Created by sonback123456 on 9/19/2017.
 */

public class ProductListAdapter extends BaseAdapter {

    private static final int TYPE_INDEX = 0;
    private static final int TYPE_USER = 1;

    private Context _context;
    private ArrayList<ProductInfoEntity> _datas = new ArrayList<>();
    private ArrayList<ProductInfoEntity> _alldatas = new ArrayList<>();

    ImageLoader _imageLoader;

    public ProductListAdapter(Context context){

        super();
        this._context = context;

        _imageLoader = SeptemberApplication.getInstance().getImageLoader();
    }

    public void setDatas(ArrayList<ProductInfoEntity> datas) {

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

        CustomHolder holder;

        if (convertView == null) {
            holder = new CustomHolder();

            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.product_list_item, parent, false);

            holder.productImage = (NetworkImageView) convertView.findViewById(R.id.product_image_net);
            holder.productName = (TextView) convertView.findViewById(R.id.productName);
            holder.productPrice = (TextView) convertView.findViewById(R.id.price);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final ProductInfoEntity productEntity = (ProductInfoEntity) _datas.get(position);


        holder.productName.setText(productEntity.getTitle());
        holder.productPrice.setText(productEntity.getPrice());

        if (productEntity.getImage_url().length() > 0) {
            holder.productImage.setImageUrl(productEntity.getImage_url(),_imageLoader);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Commons.productInfo=productEntity;
                Intent intent=new Intent(_context, ProductDetailActivity.class);
                _context.startActivity(intent);
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                return false;
            }
        });

        return convertView;
    }

    public void filter(String charText){

        charText = charText.toLowerCase();
        _datas.clear();

        if(charText.length() == 0){
            _datas.addAll(_alldatas);
        }else {

            for (ProductInfoEntity productInfoEntity : _alldatas){

                if (productInfoEntity instanceof ProductInfoEntity) {

                    String value = ((ProductInfoEntity) productInfoEntity).getTitle().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(productInfoEntity);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {

        public NetworkImageView productImage;
        public TextView productName;
        public TextView productPrice;
    }
}




