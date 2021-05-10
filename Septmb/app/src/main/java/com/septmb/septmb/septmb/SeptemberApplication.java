package com.septmb.septmb.septmb;

/**
 * Created by sonback123456 on 9/18/2017.
 */

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.firebase.client.Firebase;
import com.qiscus.sdk.Qiscus;
import com.septmb.septmb.septmb.network.SharedPref;
import com.septmb.septmb.septmb.utils.LruBitmapCache;

/**
 * Created by a on 3/15/2017.
 */

public class SeptemberApplication extends Application {
    public static final String TAG = SeptemberApplication.class.getSimpleName();

    public RequestQueue _requestQueue;
    public ImageLoader _imageLoader;

    private String m_gsmToken = "";

    private static SeptemberApplication _instance;

    @Override

    public void onCreate(){

        super.onCreate();
        _instance = this;

        SharedPref.init(this);

        Qiscus.init(this, "dragongo");

        Firebase.setAndroidContext(this);
        FirebaseApp.initializeApp(this);

       /* FacebookSdk.sdkInitialize(getApplicationContext());
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.android.facebookloginsample",  // replace with your unique package name
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/
    }

    public String getGcmToken() {
        return m_gsmToken;
    }

    public void setGcmToken(String p_strGsmToken) {
        m_gsmToken = p_strGsmToken;
    }


    public static synchronized SeptemberApplication getInstance(){

        return _instance;
    }

    public RequestQueue getRequestQueue(){

        if(_requestQueue == null){
            _requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return _requestQueue;
    }

    public ImageLoader getImageLoader(){

        getRequestQueue();
        if(_imageLoader == null){
            _imageLoader = new ImageLoader(this._requestQueue, new LruBitmapCache());
        }
        return this._imageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag){

        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (_requestQueue != null) {
            _requestQueue.cancelAll(tag);
        }
    }

}




