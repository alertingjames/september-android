package com.septmb.septmb.septmb.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.SeptemberApplication;
import com.septmb.septmb.septmb.classes.GifView;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.commons.ReqConst;
import com.septmb.septmb.septmb.models.UserEntity;
import com.septmb.septmb.septmb.preference.PrefConst;
import com.septmb.septmb.septmb.preference.Preference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {

    TextView registeButton, loginButton;
    EditText name, email, password;
    GifView gif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);

        gif = (GifView)findViewById(R.id.progress_gif);
        gif.setMovieResource(R.raw.loading6);
        gif.setPaused(false);

        registeButton=(TextView)findViewById(R.id.registerbutton);
        loginButton=(TextView)findViewById(R.id.loginbutton);

        registeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        registeButton.setBackground(getDrawable(R.drawable.green_fillrect));
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        registeButton.setBackground(getDrawable(R.drawable.white_thick_stroke));
                        Intent intent=new Intent(getApplicationContext(), RegisterActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);

                    case MotionEvent.ACTION_CANCEL: {
                        //clear the overlay
                        registeButton.getBackground().clearColorFilter();
                        registeButton.invalidate();
                        break;
                    }
                }

                return true;
            }
        });

        loginButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        loginButton.setBackground(getDrawable(R.drawable.green_fillrect));
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        loginButton.setBackground(getDrawable(R.drawable.white_thick_stroke));
                        if(email.getText().length()==0)
                            Toast.makeText(getApplicationContext(),"Please input your email",Toast.LENGTH_SHORT).show();
                        else if(email.getText().length()>0 && ((!email.getText().toString().contains("@") || !email.getText().toString().endsWith(".com"))
                                || (email.getText().toString().contains("@") && email.getText().toString().startsWith("@")))){
                            Toast.makeText(getApplicationContext(),"Please input your valid email",Toast.LENGTH_SHORT).show();
                        }
                        else if(password.getText().length()==0)
                            Toast.makeText(getApplicationContext(),"Please input your passord",Toast.LENGTH_SHORT).show();
                        else
                            login();

                    case MotionEvent.ACTION_CANCEL: {
                        //clear the overlay
                        loginButton.getBackground().clearColorFilter();
                        loginButton.invalidate();
                        break;
                    }
                }

                return true;
            }
        });

        String emailStr = Preference.getInstance().getValue(this, PrefConst.PREFKEY_USEREMAIL, "");
        String passwordStr = Preference.getInstance().getValue(this, PrefConst.PREFKEY_USERPASSWORD, "");
        if(emailStr.length()>0 && passwordStr.length()>0){
            email.setText(emailStr);
            password.setText(passwordStr);
            login();
        }

    }

    public void login() {

        String url = ReqConst.SERVER_URL + "login";

        gif.setVisibility(View.VISIBLE);


        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                Log.d("REST response========>", response);
                VolleyLog.v("Response:%n %s", response.toString());

                parseRegisterResponse(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("debug", error.toString());

                gif.setVisibility(View.GONE);

                Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("email", email.getText().toString().trim());
                params.put("password", password.getText().toString().trim());

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(60000,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SeptemberApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseRegisterResponse(String json) {

        gif.setVisibility(View.GONE);

        try {

            JSONObject response = new JSONObject(json);   Log.d("Response=====> :",response.toString());

            String success = response.getString("result");

            Log.d("result=====> :",String.valueOf(success));

            if (success.equals("0")) {

                JSONArray jsonArray=response.getJSONArray("user_info");
                JSONObject jsonObject=jsonArray.getJSONObject(0);
                UserEntity user=new UserEntity();
                user.setName(jsonObject.getString("name"));
                user.setEmail(jsonObject.getString("email"));
                user.setPassword(jsonObject.getString("password"));
                user.setPhotoUrl(jsonObject.getString("photo_url"));
                user.setPhone(jsonObject.getString("phone_number"));

                Commons.thisUser=user;

                Preference.getInstance().put(this,
                        PrefConst.PREFKEY_USEREMAIL, Commons.thisUser.getEmail());
                Preference.getInstance().put(this,
                        PrefConst.PREFKEY_USERPASSWORD, Commons.thisUser.getPassword());

                Toast.makeText(getApplicationContext(),"Successfully logged in...", Toast.LENGTH_SHORT).show();

                registerChatRoom();

            }else if (success.equals("1")) {

                Toast.makeText(getApplicationContext(),"You haven't been registered ever before.\nPlease register...", Toast.LENGTH_SHORT).show();

            }
            else {

                Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            gif.setVisibility(View.GONE);
            e.printStackTrace();

            Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
        }
    }

    public void registerChatPortion(String email, String key){
        Qiscus.setUser(email, key)
                .withUsername(Commons.thisUser.getName())
                .withAvatarUrl(Commons.thisUser.getPhotoUrl())
                .save(new Qiscus.SetUserListener()
                {
                    @Override
                    public void onSuccess(QiscusAccount qiscusAccount) {
                        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(LoginActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);
                    }
                });
    }

    public void registerChatRoom(){

        String url = ReqConst.FIREBASE_DATABASE_URL+"users.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                Firebase reference = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"users/"+Commons.thisUser.getEmail().replace(".com","").replace(".","ddoott"));

                if(s.equals("null")) {

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("email", Commons.thisUser.getEmail());

                    if(Commons.thisUser.getName().length()>0)
                        map.put("name", Commons.thisUser.getName());

                    map.put("photo", Commons.thisUser.getPhotoUrl());
                    map.put("phone", Commons.thisUser.getPhone());

                    reference.push().setValue(map);
                }
                else {
                    try {
                        JSONObject obj = new JSONObject(s);

                        if (!obj.has(Commons.thisUser.getEmail().replace(".com","").replace(".","ddoott"))) {

                            Map<String, String> map = new HashMap<String, String>();
                            map.put("email", Commons.thisUser.getEmail());

                            if(Commons.thisUser.getName().length()>0)
                                map.put("name", Commons.thisUser.getName());

                            map.put("photo", Commons.thisUser.getPhotoUrl());
                            map.put("phone", Commons.thisUser.getPhone());

                            reference.push().setValue(map);
                        } else {

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                registerChatPortion(Commons.thisUser.getEmail(), Commons.thisUser.getPassword());

            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError );
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(LoginActivity.this);
        rQueue.add(request);

    }

}






































