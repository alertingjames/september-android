package com.septmb.septmb.septmb.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.septmb.septmb.septmb.commons.Constants;
import com.septmb.septmb.septmb.commons.ReqConst;
import com.septmb.septmb.septmb.models.UserEntity;
import com.septmb.septmb.septmb.preference.PrefConst;
import com.septmb.septmb.septmb.preference.Preference;
import com.septmb.septmb.septmb.utils.BitmapUtils;
import com.septmb.septmb.septmb.utils.CircularImageView;
import com.septmb.septmb.septmb.utils.MultiPartRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends Activity {

    TextView registeButton, loginButton;
    EditText name, email, password, phone;
    GifView gif;

    Bitmap bitmap;
    String _photoPath="";
    File file0;
    Uri _imageCaptureUri=null;
    Bitmap thumbnail;
    CircularImageView mImageView;

    private static final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.INSTALL_PACKAGES,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.READ_CALENDAR,
            android.Manifest.permission.WRITE_CALENDAR,
            android.Manifest.permission.SET_TIME,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.WAKE_LOCK,
            android.Manifest.permission.CAPTURE_VIDEO_OUTPUT,
            android.Manifest.permission.LOCATION_HARDWARE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        checkAllPermission();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        name=(EditText)findViewById(R.id.username);
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.password);
        phone=(EditText)findViewById(R.id.phone);

        gif = (GifView)findViewById(R.id.progress_gif);
        gif.setMovieResource(R.raw.loading2);
        gif.setPaused(false);

        mImageView=(CircularImageView)findViewById(R.id.photo);

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
                        if(email.getText().length()==0)
                            Toast.makeText(getApplicationContext(),"Please input your email",Toast.LENGTH_SHORT).show();
                        else if(email.getText().length()>0 && ((!email.getText().toString().contains("@") || !email.getText().toString().endsWith(".com"))
                                || (email.getText().toString().contains("@") && email.getText().toString().startsWith("@")))){
                            Toast.makeText(getApplicationContext(),"Please input your valid email",Toast.LENGTH_SHORT).show();
                        }
                        else if(password.getText().length()==0)
                            Toast.makeText(getApplicationContext(),"Please input your passord",Toast.LENGTH_SHORT).show();
                        else if(name.getText().length()==0)
                            Toast.makeText(getApplicationContext(),"Please input your name",Toast.LENGTH_SHORT).show();
                        else if(file0.equals(null))
                            Toast.makeText(getApplicationContext(),"Please take your photo",Toast.LENGTH_SHORT).show();
                        else if(phone.getText().length()==0)
                            Toast.makeText(getApplicationContext(),"Please input your phone number",Toast.LENGTH_SHORT).show();
                        else
                            registerUserInfo();

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

                        Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);

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
    }

    public void registerUserInfo() {

        String url = ReqConst.SERVER_URL + "register";

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

                params.put("name", name.getText().toString().trim());
                params.put("email", email.getText().toString().trim());
                params.put("password", password.getText().toString().trim());
                params.put("phone", phone.getText().toString().trim());

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

                String user_id=response.getString("user_id");
                uploadUserPhoto(user_id);

            }else if (success.equals("1")) {

                Toast.makeText(getApplicationContext(),"You have already been registered before.\nPlease login...", Toast.LENGTH_SHORT).show();

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
                        Toast.makeText(RegisterActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);
                    }
                });
    }

    public void checkAllPermission() {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (hasPermissions(this, PERMISSIONS)){

        }else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 101);
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {

            for (String permission : permissions) {

                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onClickTakePhoto2(View v) {
        ((LinearLayout)findViewById(R.id.alert)).setVisibility(View.VISIBLE);
    }

    public void onClickTakePhotoCancel(View v) {
        ((LinearLayout)findViewById(R.id.alert)).setVisibility(View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ImageOptionDialog extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.pick_search_item)
                    .setItems(R.array.image_option_item, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            if(which == 0)
                                ((MainActivity)getActivity()).onClickCamera();
                            else if(which == 1)
                                ((MainActivity)getActivity()).onClickSelectImage();
                        }
                    });
            return builder.create();
        }
    }

    boolean isCrop=true;

    public void showCropDialog() {

        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);

        dialogBuilder.setTitle("Do you want to crop?");
        dialogBuilder.setIcon(R.drawable.noti);
//        dialogBuilder.setMessage("Do you want to crop?");

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
                //pass
                isCrop=false;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);//
                startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.PICK_FROM_ALBUM);
            }
        });
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
                //pass
                isCrop=true;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);//
                startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.PICK_FROM_ALBUM);
            }
        });
        android.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = 1100;
//        lp.height = 700;
//        lp.x=80;
//        lp.y=-1200;
        alertDialog.getWindow().setAttributes(lp);
    }

    public void fromGallery(View view) {

        showCropDialog();

    }

    public void takePhoto(View view) {

        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Constants.PICK_FROM_CAMERA);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CROP_FROM_CAMERA: {

                if (resultCode == RESULT_OK) {
                    try {
                        File saveFile = BitmapUtils.getOutputMediaFile(this);

                        InputStream in = getContentResolver().openInputStream(Uri.fromFile(saveFile));
                        BitmapFactory.Options bitOpt = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeStream(in, null, bitOpt);

                        in.close();

                        onGalleryImageResult(bitmap);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case Constants.PICK_FROM_ALBUM:

                if (resultCode == RESULT_OK) {
                    _imageCaptureUri = data.getData();   Log.d("PHOTOURL===",_imageCaptureUri.toString());
                }

            case Constants.PICK_FROM_CAMERA: {

                try {

                    try{
                        onCaptureImageResult(data);
                        return;
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    if(!isCrop){

                        bitmap=getBitmapFromUri(_imageCaptureUri);
                        onGalleryImageResult(bitmap);

                        return;
                    }


                    _imageCaptureUri = data.getData();

                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(_imageCaptureUri, "image/*");

                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


                    intent.putExtra("crop", true);
                    intent.putExtra("scale", true);
                    intent.putExtra("outputX", 256);
                    intent.putExtra("outputY", 256);
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("noFaceDetection", true);
//                    intent.putExtra("return-data", true);
                    intent.putExtra("output", Uri.fromFile(BitmapUtils.getOutputMediaFile(this)));

                    startActivityForResult(intent, Constants.CROP_FROM_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
        }

    }

    private void onCaptureImageResult(Intent data) {

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Pictures");
        if (!dir.exists())
            dir.mkdirs();
        thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        String ImagePath = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                thumbnail,
                "demo_image",
                "demo_image"
        );

        Uri URI = Uri.parse(ImagePath);

        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + "test.jpg");                   Log.d("FilePath===>",file.getPath());
        file0=file;

        FileOutputStream fo;
        try {
            file.createNewFile();
            fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageBitmap(thumbnail);
    }

    private void onGalleryImageResult(Bitmap bitmap) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + "test.jpg");                   Log.d("FilePath===>",file.getPath());
        file0=file;

        FileOutputStream fo;
        try {
            file.createNewFile();
            fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageBitmap(bitmap);
    }

    public void registerChatPortion(String email, String key, String name){
        Qiscus.setUser(email, key)
                .withUsername(name)
                .save(new Qiscus.SetUserListener()
                {
                    @Override
                    public void onSuccess(QiscusAccount qiscusAccount) {
                        Toast.makeText(RegisterActivity.this, "Registered in chat portion", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(RegisterActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void uploadUserPhoto(String user_id) {

        try {

            final Map<String, String> params = new HashMap<>();
            params.put("user_id", user_id);

            String url = ReqConst.SERVER_URL + "uploadUserPhoto";

            gif.setVisibility(View.VISIBLE);
            gif.setPaused(false);

            MultiPartRequest reqMultiPart = new MultiPartRequest(url, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    gif.setVisibility(View.GONE);
                    gif.setPaused(true);
                    Toast.makeText(getApplicationContext(),"Picture uploading failed",Toast.LENGTH_SHORT).show();
                }
            }, new Response.Listener<String>() {

                @Override
                public void onResponse(String json) {

                    ParseUploadImgBroadmoorLogoResponse(json);

                }
            }, file0, "file", params);

            reqMultiPart.setRetryPolicy(new DefaultRetryPolicy(
                    60000, 0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            SeptemberApplication.getInstance().addToRequestQueue(reqMultiPart, url);

        } catch (Exception e) {

            e.printStackTrace();
            gif.setVisibility(View.GONE);
            gif.setPaused(true);
            Toast.makeText(getApplicationContext(),"Picture uploading failed",Toast.LENGTH_SHORT).show();
        }
    }


    public void ParseUploadImgBroadmoorLogoResponse(String json) {

        gif.setVisibility(View.GONE);
        gif.setPaused(true);

        try {
            JSONObject response = new JSONObject(json);
            String result_code = response.getString("result");
            Log.d("result===>",String.valueOf(result_code));

            if (result_code.equals("0")) {

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

                Toast.makeText(getApplicationContext(),"Successfully registered...", Toast.LENGTH_SHORT).show();

                registerChatRoom();
            }
            else {
                Toast.makeText(getApplicationContext(),"Picture uploading failed",Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Picture uploading failed",Toast.LENGTH_SHORT).show();
        }
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

        RequestQueue rQueue = Volley.newRequestQueue(RegisterActivity.this);
        rQueue.add(request);

    }

}






























