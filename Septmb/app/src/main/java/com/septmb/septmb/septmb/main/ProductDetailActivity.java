package com.septmb.septmb.septmb.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.SeptemberApplication;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.commons.ReqConst;
import com.septmb.septmb.septmb.database.DBManager;
import com.septmb.septmb.septmb.models.ProductInfoEntity;
import com.septmb.septmb.septmb.models.UserEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailActivity extends Activity {

    NetworkImageView image;
    TextView seller, price, gender, title, description, brand;
    ProgressDialog progressDialog;
    int rotate=0;
    TextView buyButton, requestButton;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        image=(NetworkImageView)findViewById(R.id.productImage);
        seller=(TextView)findViewById(R.id.sale);
        title=(TextView)findViewById(R.id.title);
        brand=(TextView)findViewById(R.id.brand);
        gender=(TextView)findViewById(R.id.gender);
        description=(TextView)findViewById(R.id.description);
        price=(TextView)findViewById(R.id.price);

        dbManager = new DBManager(this);
        dbManager.open();

        ImageLoader _imageLoader = SeptemberApplication.getInstance().getImageLoader();

        setTitle("Product Detail");
        if(Commons.productInfo.getImage_url().length()>0)
            image.setImageUrl(Commons.productInfo.getImage_url(), _imageLoader);

        Bitmap img = drawableToBitmap(LoadImageFromWebOperations(Commons.productInfo.getImage_url()));

//        if(img.getWidth()>800 && img.getHeight()>800)
//            img=getResizedBitmap(img,800);

        final Bitmap finalImage = img;
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplication(), ProductViewActivity.class);
                Commons.productImage= finalImage;
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        seller.setText(Commons.productInfo.getSeller());
        title.setText(Commons.productInfo.getTitle());
        brand.setText(Commons.productInfo.getBrand());
        gender.setText(Commons.productInfo.getGender());
        description.setText(Commons.productInfo.getDescription());
        price.setText(Commons.productInfo.getPrice());
        buyButton=(TextView)findViewById(R.id.buyButton);
        if(Commons.searchFlag)buyButton.setVisibility(View.VISIBLE);
        else buyButton.setVisibility(View.GONE);
        buyButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        buyButton.setBackground(getDrawable(R.drawable.green_fillrect));
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        buyButton.setBackground(getDrawable(R.drawable.green_circle_background));
                        Intent intent=new Intent(getApplicationContext(), CoinbaseActivity.class);
                        startActivity(intent);
//                        finish();
                        overridePendingTransition(0,0);

                    case MotionEvent.ACTION_CANCEL: {
                        //clear the overlay
                        buyButton.getBackground().clearColorFilter();
                        buyButton.invalidate();
                        break;
                    }
                }

                return true;
            }
        });

        requestButton=(TextView)findViewById(R.id.requestButton);
        if(Commons.searchFlag)requestButton.setVisibility(View.VISIBLE);
        else requestButton.setVisibility(View.GONE);
        requestButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        requestButton.setBackground(getDrawable(R.drawable.green_fillrect));
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        requestButton.setBackground(getDrawable(R.drawable.green_circle_background));

//                        customizeChatUi();
//                        registerUserOnChatHistory();
                        Intent intent=new Intent(getApplicationContext(),RequestActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0,0);

                    case MotionEvent.ACTION_CANCEL: {
                        //clear the overlay
                        requestButton.getBackground().clearColorFilter();
                        requestButton.invalidate();
                        break;
                    }
                }

                return true;
            }
        });

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Wait...");

        ((ImageView)findViewById(R.id.rotate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotate+=90;
                image.setRotation(rotate);
            }
        });

        ((LinearLayout)findViewById(R.id.layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_menu_flag=false;
                ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);
            }
        });

        getUserInfo();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private Drawable LoadImageFromWebOperations(String url)
    {
        try
        {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        }catch (Exception e) {
            System.out.println("Exc="+e);
            return null;
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        if(Commons.searchFlag){
            menu.findItem(R.id.action_edit).setVisible(false);
        }else {
            menu.findItem(R.id.action_edit).setVisible(true);
        }
        return true;
    }

    boolean show_menu_flag=false;

    public void showMenu(MenuItem menuItem){
        if(!show_menu_flag) {
            show_menu_flag=true;
            ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.VISIBLE);
        }else {
            show_menu_flag=false;
            ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);
        }
    }

    public void updateDetail(View view){
        show_menu_flag=false;
        ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);
        Intent intent=new Intent(getApplicationContext(), UploadProductInfoActivity.class);
        intent.putExtra("flag",true);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    public void deleteDetail(View view){
        show_menu_flag=false;
        ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);
        deleteProductInfo();
    }

    public void deleteProductInfo() {

        String url = ReqConst.SERVER_URL + "deleteProductInfo";

        progressDialog.show();

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseUpdateResponse(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("product_id", Commons.productInfo.getIdx());

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(60000,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SeptemberApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseUpdateResponse(String json) {

        progressDialog.dismiss();

        try {

            JSONObject response = new JSONObject(json);   Log.d("Response=====> :",response.toString());

            String success = response.getString("result");

            Log.d("result=====> :",String.valueOf(success));

            if (success.equals("0")) {

                Toast.makeText(getApplicationContext(),"Product deleted! Please refresh",Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0,0);
            }
            else {

                Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            progressDialog.dismiss();
            e.printStackTrace();

            Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
        }
    }

    public void getUserInfo() {

        String url = ReqConst.SERVER_URL + "getUserInfo";

        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseGetUserInfoResponse(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());

                Toast.makeText(getApplicationContext(),"Loading failed", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("user_id", Commons.productInfo.getUser_id());

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(60000,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SeptemberApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseGetUserInfoResponse(String json) {

        try {

            JSONObject response = new JSONObject(json);

            String success = response.getString("result");

            Log.d("Rcode=====> :",success);

            if (success.equals("0")) {

                JSONArray userInfo = response.getJSONArray("user_info");

                JSONObject user = (JSONObject) userInfo.get(0);

                UserEntity userEntity = new UserEntity();

                userEntity.setIdx(user.getString("id"));
                userEntity.setName(user.getString("name"));
                userEntity.setEmail(user.getString("email"));
                userEntity.setPassword(user.getString("password"));
                userEntity.setPhone(user.getString("phone_number"));
                userEntity.setPhotoUrl(user.getString("photo_url"));

                Commons.user=userEntity;     Log.d("UserPhoto===>", Commons.user.getPhotoUrl());
            }
            else {
                Toast.makeText(getApplicationContext(),"User info issue", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),"User info issue", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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


































