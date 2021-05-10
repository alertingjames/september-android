package com.septmb.septmb.septmb.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
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
import com.septmb.septmb.septmb.utils.CircularNetworkImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends FragmentActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

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
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.WAKE_LOCK,
            android.Manifest.permission.CAPTURE_VIDEO_OUTPUT,
            android.Manifest.permission.LOCATION_HARDWARE};

    Bitmap bitmap;
    String _photoPath="";
    File file0;
    Uri _imageCaptureUri=null;
    Bitmap thumbnail;
    int notiusers = 0;
    String phone="", sender="", name="", photo="", message="";
    ArrayList<String> _emails=new ArrayList<>();
    ArrayList<UserEntity> _datas_user=new ArrayList<>();
    Bitmap bitmapPhoto=null;
    public static MainActivity inst=null;
    View background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAllPermission();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mImageView = (ImageView) findViewById(R.id.imageView1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
        gif = (GifView)findViewById(R.id.progress_gif);
        gif.setMovieResource(R.raw.progress_gif);
        gif.setPaused(true);
        gif.setVisibility(View.INVISIBLE);

        background=(View)findViewById(R.id.background3);

        if (Commons.userEntities.size()==0)
            ShortcutBadger.removeCount(getApplicationContext());

        _datas_user.clear();
        Commons.userEntities.clear();
        _emails.clear();

        ((LinearLayout)findViewById(R.id.layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_menu_flag=false;
                ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.cropDialog3)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.cropDialog2)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.searchDialog)).setVisibility(View.GONE);
                background.setVisibility(View.GONE);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                ((ImageView)findViewById(R.id.dress4)).setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin_fadeoff);
                ((ImageView)findViewById(R.id.dress4)).startAnimation(animation);
                ((ImageView)findViewById(R.id.dress3)).setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin_fadeoff);
                ((ImageView)findViewById(R.id.dress3)).startAnimation(animation);
                ((ImageView)findViewById(R.id.bag1)).setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin_fadeoff);
                ((ImageView)findViewById(R.id.bag1)).startAnimation(animation);
                ((ImageView)findViewById(R.id.shoes1)).setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin_fadeoff);
                ((ImageView)findViewById(R.id.shoes1)).startAnimation(animation);
                ((ImageView)findViewById(R.id.bag2)).setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin_fadeoff);
                ((ImageView)findViewById(R.id.bag2)).startAnimation(animation);
                ((ImageView)findViewById(R.id.glasses1)).setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin_fadeoff);
                ((ImageView)findViewById(R.id.glasses1)).startAnimation(animation);
                ((ImageView)findViewById(R.id.shoes2)).setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin_fadeoff);
                ((ImageView)findViewById(R.id.shoes2)).startAnimation(animation);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        ((ImageView)findViewById(R.id.dress4)).setVisibility(View.GONE);
                        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomout);
                        ((ImageView)findViewById(R.id.dress4)).startAnimation(animation);
                        ((ImageView)findViewById(R.id.dress3)).setVisibility(View.GONE);
                        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomout);
                        ((ImageView)findViewById(R.id.dress3)).startAnimation(animation);
                        ((ImageView)findViewById(R.id.bag1)).setVisibility(View.GONE);
                        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomout);
                        ((ImageView)findViewById(R.id.bag1)).startAnimation(animation);
                        ((ImageView)findViewById(R.id.shoes1)).setVisibility(View.GONE);
                        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomout);
                        ((ImageView)findViewById(R.id.shoes1)).startAnimation(animation);
                        ((ImageView)findViewById(R.id.bag2)).setVisibility(View.GONE);
                        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomout);
                        ((ImageView)findViewById(R.id.bag2)).startAnimation(animation);
                        ((ImageView)findViewById(R.id.glasses1)).setVisibility(View.GONE);
                        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomout);
                        ((ImageView)findViewById(R.id.glasses1)).startAnimation(animation);
                        ((ImageView)findViewById(R.id.shoes2)).setVisibility(View.GONE);
                        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomout);
                        ((ImageView)findViewById(R.id.shoes2)).startAnimation(animation);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView)findViewById(R.id.dress1)).setVisibility(View.GONE);
                                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_off);
                                ((ImageView)findViewById(R.id.dress1)).startAnimation(animation);
//                        ((ImageView)findViewById(R.id.dress0)).setVisibility(View.VISIBLE);
//                        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
//                        ((ImageView)findViewById(R.id.dress0)).startAnimation(animation);
                            }
                        }, 1000);
                    }
                }, 3000);
            }
        }, 300);

        getRecentUsers();
        QiscusAccount account=Qiscus.getQiscusAccount();
        Log.d("Email===>",account.getEmail());
        Log.d("Name===>",account.getUsername());
        Log.d("Photo===>",account.getAvatar());
    }

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public void getRecentUsers(){

        _datas_user.clear();
        _emails.clear();
        Commons.userEntities.clear();

        try{
            pushNotification(Commons.thisUser.getEmail().toString());
        }catch (Exception e){
            e.printStackTrace();
            Log.d("BranchLog===>", e.getMessage());
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    public void showSettingPortion(View view){
        show_menu_flag=false;
        ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);
        showAlertOfBatteryOptimization("Are you at Android Samsung 5,6?\n" +
                "If you are and you want to receive\n" +
                "offline notifications, you will need\n" +
                "to remove this app from battery\n" +
                "usage optimization list of Setting.");
    }

    public void showAlertOfBatteryOptimization(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hint!");
        builder.setMessage(msg);
        builder.setIcon(R.drawable.noti);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                ResolveInfo resolveInfo = getPackageManager().resolveActivity(powerUsageIntent, 0);
                if(resolveInfo != null){
                    startActivity(powerUsageIntent);
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onInit (int status) {
        tts.setLanguage(Locale.ENGLISH);
        tts.setSpeechRate(1);
    }

    public void logout(View view){
        show_menu_flag=false;
//        Qiscus.clearUser();
        ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);
        Preference.getInstance().put(this,
                PrefConst.PREFKEY_USEREMAIL, "");
        Preference.getInstance().put(this,
                PrefConst.PREFKEY_USERPASSWORD, "");
        Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0,0);
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

    public void onClickIdentify(View view) {
        try{
            if(!file0.equals(null)){
                TextView txtView = (TextView) findViewById(R.id.textView1);
                txtView.setText("I'm uploading");
                txtView.setVisibility(View.VISIBLE);
                gif.setVisibility(View.VISIBLE);
                gif.setPaused(false);
                new IdentifyImage().execute();
            }else
                Toast.makeText(getApplicationContext(),"Please take a photo", Toast.LENGTH_SHORT).show();
        }catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"Please take a photo", Toast.LENGTH_SHORT).show();
        }
    }
    private class IdentifyImage extends AsyncTask<String, Integer, HttpResponse<JsonNode>> {

        protected HttpResponse<JsonNode> doInBackground(String... msg) {

            HttpResponse<JsonNode> request = null;
            try {
                request = Unirest.post("https://camfind.p.mashape.com/image_requests")
                        .header("X-Mashape-Key", "55vDTIMyfdmshoCvD6k39tT2BgVCp1LbMYHjsn2ubCVgH3QDBi")
                        .field("image_request[image]", new File(Environment.getExternalStorageDirectory()
                                + File.separator + "test.jpg"))
                        .field("image_request[language]", "en")
                        .field("image_request[locale]", "en_US")
                        .asJson();
            } catch (UnirestException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return request;
        }

        protected void onProgressUpdate(Integer...integers) {
        }

        protected void onPostExecute(HttpResponse<JsonNode> response) {
            try {
                token = response.getBody().getObject().getString("token");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("token", token);
            get_token = true;
            TextView txtView = (TextView) findViewById(R.id.textView1);
            txtView.setText("I'm identifying");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    gif.setPaused(true);
                    TextView txtView = (TextView) findViewById(R.id.textView1);
                    txtView.setText("Completed!");
                    new IdentifyImageDisplay().execute();
                }
            }, 20 * 1000);
        }
    }
    private class IdentifyImageDisplay extends AsyncTask<String, Integer, HttpResponse<JsonNode>> {
        private String tt;

        protected HttpResponse<JsonNode> doInBackground(String... msg) {
            HttpResponse<JsonNode> request = null;
            try {
                Log.d("tokentoken", "https://camfind.p.mashape.com/image_responses/" + token);
                String url = "https://camfind.p.mashape.com/image_responses/" + token;
                try {
                    url = new String(url.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                final String api_key = "55vDTIMyfdmshoCvD6k39tT2BgVCp1LbMYHjsn2ubCVgH3QDBi";
                request = Unirest.get(url)
                        .header("X-Mashape-Key", api_key)
                        .asJson();
            } catch (UnirestException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return request;
        }

        protected void onProgressUpdate(Integer... integers) {
        }

        protected void onPostExecute(HttpResponse<JsonNode> response) {
            HashMap<String, String> myHashAlarm = new HashMap();
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                    String.valueOf(AudioManager.STREAM_ALARM));
            String answer = null;
            try {
                answer = response.getBody().getObject().getString("name");     Log.d("Object===>",response.getBody().getObject().toString());
                keyWords = answer;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            TextView txtView = (TextView) findViewById(R.id.textView1);
            txtView.setText("I guess it is " + keyWords);
            get_keywords = true;
//            new SearchOptionDialog().show(getFragmentManager(),"search_option");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onClickTakePhoto2(View v) {
        ((LinearLayout)findViewById(R.id.alert)).setVisibility(View.VISIBLE);
    }

    public void onClickTakePhotoCancel(View v) {
        ((LinearLayout)findViewById(R.id.alert)).setVisibility(View.GONE);
    }

    public void onClickCamera(View view) {
        ((LinearLayout)findViewById(R.id.alert)).setVisibility(View.GONE);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }
        startActivityForResult(takePictureIntent, TAKE_IMAGE_REQUEST_CODE);
    }
    public void onClickSelectImage(View view) {
        ((LinearLayout)findViewById(R.id.alert)).setVisibility(View.GONE);
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");
        if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.KITKAT){
            startActivityForResult(intent, SELECT_PIC_KITKAT);
        }else{
            startActivityForResult(intent, SELECT_PIC);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onClickTakePhoto(View v) {
        new ImageOptionDialog().show(getFragmentManager(),"image_option");
    }
    public void onClickCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }
        startActivityForResult(takePictureIntent, TAKE_IMAGE_REQUEST_CODE);
    }
    public void onClickSelectImage() {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/jpeg");
        if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.KITKAT){
            startActivityForResult(intent, SELECT_PIC_KITKAT);
        }else{
            startActivityForResult(intent, SELECT_PIC);
        }
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
//    @Override
//    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case TAKE_IMAGE_REQUEST_CODE: {
//                get_keywords = false;
//                get_token = false;
//                if (resultCode == RESULT_OK) {
//                    try {
//                        handlePhoto();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                break;
//            }
//            case SELECT_PIC_KITKAT: {
//                Log.d("select", "1");
//                if(data != null && data.getData() != null) {
//                    Uri _uri = data.getData();
//                    //User had pick an image.
////                    Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
////                    cursor.moveToFirst();
//                    //Link to the image
//                    mCurrentPhotoPath = getPath(this,_uri);
//                    if(mCurrentPhotoPath != null) {
//                        Log.d("mCurrentPhotoPath", mCurrentPhotoPath);
//                    }else
//                        Log.d("mCurrentPhotoPath", "null");
////                    cursor.close();
//                    try {
//                        handlePhoto();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                break;
//            }
//            case SELECT_PIC: {
//                Log.d("select", "2");
//                break;
//            }
//        }
//    }
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
    private String getAlbumName() {
        return getString(R.string.album_name);
    }
    private File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()) {
                        return null;
                    }
                }
            }
        } else {
        }
        return storageDir;
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG" + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }
    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();
        mCurrentPhotoUri = Uri.fromFile(f);
        return f;
    }
    private void setPic() {
        mImageView.setImageURI(mCurrentPhotoUri);
        mImageView.setVisibility(View.VISIBLE);
        return;
    }
    private void handlePhoto() throws IOException {
        if (mCurrentPhotoPath != null) {                    // we'll start with the original picture already open to a file
            File imgFileOrig = new File(mCurrentPhotoPath); //change "getPic()" for whatever you need to open the image file.

            mCurrentPhotoUri = Uri.fromFile(imgFileOrig);

            ExifInterface exif = new ExifInterface(imgFileOrig.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            int angle = 0;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);

            Bitmap b = BitmapFactory.decodeFile(imgFileOrig.getAbsolutePath());

            b = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                    b.getHeight(), mat, true);

            // original measurements
            int origWidth = b.getWidth();
            int origHeight = b.getHeight();
            final int destWidth = 200;//or the width you need
            if (origWidth > destWidth) {
                // picture is wider than we want it, we calculate its target height
                int destHeight = origHeight / ( origWidth / destWidth ) ;
                // we create an scaled bitmap so it reduces the image, not just trim it
                Bitmap b2 = Bitmap.createScaledBitmap(b, destWidth, destHeight, false);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                // compress to the format you want, JPEG, PNG...
                // 70 is the 0-100 quality percentage
                b2.compress(Bitmap.CompressFormat.JPEG, 70 , outStream);
                // we save the file, at least until we have made use of it
                File f = new File(Environment.getExternalStorageDirectory()
                        + File.separator + "test.jpg");
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //write the bytes in file
                FileOutputStream fo = null;
                try {
                    fo = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    fo.write(outStream.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

//            setPic();
            mImageView.setImageBitmap(b);
            mImageView.setVisibility(View.VISIBLE);
            ((View)findViewById(R.id.background)).setBackgroundColor(Color.parseColor("#550A0A0A"));
            mImageView.setBackgroundResource(R.drawable.white_stroke);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onClickSearch(View view) {
        if(((TextView) findViewById(R.id.textView1)).getText().length()>0 && !((TextView) findViewById(R.id.textView1)).getText().toString().contains("null")){
//            new SearchOptionDialog().show(getFragmentManager(),"search_option");
            ((LinearLayout)findViewById(R.id.searchDialog)).setVisibility(View.VISIBLE);
            background.setVisibility(View.VISIBLE);
        }else {
            Toast.makeText(getApplicationContext(),"Please take a photo\nand try identifying...",Toast.LENGTH_SHORT).show();
        }
    }

    protected void onClickAwesomeSearch(View view) {
        ((LinearLayout)findViewById(R.id.searchDialog)).setVisibility(View.GONE);
        background.setVisibility(View.GONE);
        TextView txtView = (TextView) findViewById(R.id.textView1);
        txtView.setText("I'm working");
        if(!keyWords.equals("null"))
            direct_awesome(keyWords);
        else Toast.makeText(getApplicationContext(), "Try again...", Toast.LENGTH_SHORT).show();
    }

    protected void onClickGoogleSearch(View view) {
        ((LinearLayout)findViewById(R.id.searchDialog)).setVisibility(View.GONE);
        background.setVisibility(View.GONE);
        TextView txtView = (TextView) findViewById(R.id.textView1);
        txtView.setText("I'm working");
//        searchUsingGoogle();
        direct_google_shopping(keyWords);
    }
    protected void onClickAmazonSearch(View view) {
        ((LinearLayout)findViewById(R.id.searchDialog)).setVisibility(View.GONE);
        background.setVisibility(View.GONE);
        if(get_keywords) {
            startWebviewForSearchAmazon();
        }else {
            TextView txtView = (TextView) findViewById(R.id.textView1);
            txtView.setText("I'm uploading");
            gif.setPaused(false);
            new IdentifyImageAndSearchAmazon().execute();
        }
    }

    protected void onClickGoogleSearch() {
        TextView txtView = (TextView) findViewById(R.id.textView1);
        txtView.setText("I'm working");
//        searchUsingGoogle();
        direct_google_shopping(keyWords);
    }
    protected void onClickAmazonSearch() {
        if(get_keywords) {
            startWebviewForSearchAmazon();
        }else {
            TextView txtView = (TextView) findViewById(R.id.textView1);
            txtView.setText("I'm uploading");
            gif.setPaused(false);
            new IdentifyImageAndSearchAmazon().execute();
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SearchOptionDialog extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.pick_search_item)
                    .setItems(R.array.search_option_item, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            if(which == 0)
                                ((MainActivity)getActivity()).onClickAwesomeSearch();

                            else if(which == 1)
                                ((MainActivity)getActivity()).onClickAmazonSearch();
                            else if(which == 2)
                                ((MainActivity)getActivity()).onClickGoogleSearch();
                        }
                    });
            AlertDialog alert = builder.create();
            return alert;
        }
    }
    private void searchUsingGoogle() {
        new GoogleSearchByImage().execute();
    }
    private class GoogleSearchByImage extends AsyncTask<String, Integer, HttpResponse<JsonNode>> {
        protected HttpResponse<JsonNode> doInBackground(String... msg) {
            HttpResponse<JsonNode> request = null;
            try {
                request = Unirest.post("http://uploads.im/api?")
                        .field("upload", new File(Environment.getExternalStorageDirectory()
                                + File.separator + "test.jpg"))
                        .asJson();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            return request;
        }
        protected void onPostExecute(HttpResponse<JsonNode> response) {
            Log.d("response", response.getBody().toString());
            try {
                JSONObject o = response.getBody().getObject().getJSONObject("data");
                String url = o.getString("img_url");
                startWebviewForSearchUsingGoogle(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String create_url_google(String img_url) {
        final String base = "https://www.google.com.hk/searchbyimage?&image_url=";
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < img_url.length(); ++i) {
            if (img_url.charAt(i) != '\\')
                s.append(img_url.charAt(i));
        }
        return base + s.toString();
    }
    private class IdentifyImageAndSearchAmazon extends AsyncTask<String, Integer, HttpResponse<JsonNode>> {

        protected HttpResponse<JsonNode> doInBackground(String... msg) {

            HttpResponse<JsonNode> request = null;
            try {
                request = Unirest.post("https://camfind.p.mashape.com/image_requests")
                        .header("X-Mashape-Key", "55vDTIMyfdmshoCvD6k39tT2BgVCp1LbMYHjsn2ubCVgH3QDBi")
                        .field("image_request[image]", new File(Environment.getExternalStorageDirectory()
                                + File.separator + "test.jpg"))
                        .field("image_request[language]", "en")
                        .field("image_request[locale]", "en_US")
                        .asJson();
            } catch (UnirestException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return request;
        }

        protected void onProgressUpdate(Integer...integers) {
        }

        protected void onPostExecute(HttpResponse<JsonNode> response) {
            try {
                token = response.getBody().getObject().getString("token");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("token", token);
            get_token = true;
            TextView txtView = (TextView) findViewById(R.id.textView1);
            txtView.setText("I'm identifying");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    gif.setPaused(true);
                    TextView txtView = (TextView) findViewById(R.id.textView1);
                    txtView.setText("Completed!");
                    new IdentifyImageAndSearchAmazonGet().execute();
                }
            }, 20 * 1000);
        }
    }
    private class IdentifyImageAndSearchAmazonGet extends AsyncTask<String, Integer, HttpResponse<JsonNode>> {
        private String tt;

        protected HttpResponse<JsonNode> doInBackground(String... msg) {
            HttpResponse<JsonNode> request = null;
            try {
                Log.d("tokentoken", "https://camfind.p.mashape.com/image_responses/" + token);
                String url = "https://camfind.p.mashape.com/image_responses/" + token;
                try {
                    url = new String(url.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                final String api_key = "55vDTIMyfdmshoCvD6k39tT2BgVCp1LbMYHjsn2ubCVgH3QDBi";
                request = Unirest.get(url)
                        .header("X-Mashape-Key", api_key)
                        .asJson();
            } catch (UnirestException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return request;
        }

        protected void onProgressUpdate(Integer... integers) {
        }

        protected void onPostExecute(HttpResponse<JsonNode> response) {
            HashMap<String, String> myHashAlarm = new HashMap();
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                    String.valueOf(AudioManager.STREAM_ALARM));
            String answer = null;     Log.d("Response===>",response.toString());
            try {
                answer = response.getBody().getObject().getString("name");
                keyWords = answer;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            TextView txtView = (TextView) findViewById(R.id.textView1);
            txtView.setText("I guess it is " + keyWords);
            get_keywords = true;
            startWebviewForSearchAmazon();
        }
    }
    private void startWebviewForSearchAmazon() {
        String url_base = "http://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=";
        String ks = keyWords;
        String[] as = ks.split(" ");
        StringBuilder s = new StringBuilder(url_base);
        s.append(as[0]);
        for (int i = 1; i < as.length; ++i) {
            s.append('+' + as[i]);
        }
        String url = s.toString();
        Intent intent = new Intent(this, WebForSearch.class);
        intent.putExtra(SEARCH_URL_MESSAGE, url);
        Log.d("search_amazon_url", url);
        startActivity(intent);
    }
    private void startWebviewForSearchUsingGoogle(String url) {
        Intent intent = new Intent(this, WebForSearch.class);
        String s_url = create_url_google(url);
        intent.putExtra(SEARCH_URL_MESSAGE, s_url);
        Log.d("search_url", s_url);
        TextView txtView = (TextView) findViewById(R.id.textView1);
        txtView.setText("Completed!");
        startActivity(intent);
    }


    private String create_url_camfind() {
        //google search
        String base = "https://www.google.com.hk/#safe=strict&q=";
        String ks = keyWords;
        String[] as = ks.split(" ");
        StringBuilder s = new StringBuilder(base);
        s.append(as[0]);
        for (int i = 1; i < as.length; ++i) {
            s.append('+' + as[i]);
        }
        return s.toString();
    }
    protected void startWebviewForSearch() {
        Intent intent = new Intent(this, WebForSearch.class);
        String s_url = create_url_camfind();
        intent.putExtra(SEARCH_URL_MESSAGE, s_url);
        startActivity(intent);
    }

    private ImageView mImageView;
    private String mCurrentPhotoPath;
    private Uri mCurrentPhotoUri;
    private String token;
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private final int TAKE_IMAGE_REQUEST_CODE = 1;
    private String keyWords;
    private GifView gif;
    private boolean get_keywords = false;
    private boolean get_token = false;
    protected final static String SEARCH_URL_MESSAGE = "com.septmb.septmb.septmb.search_url_message";
    private static final int SELECT_PIC_KITKAT = 100;
    private static final int SELECT_PIC = 101;

    public void direct_google_shopping(String term){

        Uri uriUrl = Uri.parse("https://www.google.co.in/search?q="+term);

        try {

            Intent intent = new Intent(Intent.ACTION_VIEW, uriUrl);

            intent.putExtra(SearchManager.QUERY, term);
            startActivity(intent);

            TextView txtView = (TextView) findViewById(R.id.textView1);
            txtView.setText("I'm working");

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    protected void onClickAwesomeSearch() {
        TextView txtView = (TextView) findViewById(R.id.textView1);
        txtView.setText("I'm working");
        if(!keyWords.equals("null"))
            direct_awesome(keyWords);
        else Toast.makeText(getApplicationContext(), "Try again...", Toast.LENGTH_SHORT).show();
    }

    public void direct_awesome(String term){

        Intent intent = new Intent(getApplicationContext(), ReviewMyProductsActivity.class);

        intent.putExtra("keyword", term);
        intent.putExtra("awesome", true);
        startActivity(intent);
        overridePendingTransition(0,0);

        TextView txtView = (TextView) findViewById(R.id.textView1);
        txtView.setText("I'm working");
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

    public void uploadProductInfo(View view){
        show_menu_flag=false;
        ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);
        Intent intent=new Intent(getApplicationContext(),UploadProductInfoActivity.class);
        startActivity(intent);
        overridePendingTransition(0,0);
    }
    public void reviewProductInfo(View view){
        show_menu_flag=false;
        ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);
        Intent intent=new Intent(getApplicationContext(),ReviewMyProductsActivity.class);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    boolean isCrop=true;
    boolean isCamera=false;

    public void showCropDialog() {

        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);

        dialogBuilder.setMessage("Do you want to crop?");
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
        lp.width = 900;
//        lp.height = 700;
//        lp.x=80;
//        lp.y=-1200;
        alertDialog.getWindow().setAttributes(lp);
    }

    public void yes3(View view){
        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.cropDialog3)).setVisibility(View.GONE);
        background.setVisibility(View.GONE);
        //pass
        try {
            _imageCaptureUri=getImageUri(getApplicationContext(),thumbnail);
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
            intent.putExtra("output", Uri.fromFile(BitmapUtils.getOutputMediaFile(getApplicationContext())));

            startActivityForResult(intent, Constants.CROP_FROM_CAMERA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void no3(View view){
        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.cropDialog3)).setVisibility(View.GONE);
        background.setVisibility(View.GONE);
        //pass
        captureNonCrop(thumbnail);
    }

    public void yes2(View view){
        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.cropDialog2)).setVisibility(View.GONE);
        background.setVisibility(View.GONE);
        //pass
        try {

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
            intent.putExtra("output", Uri.fromFile(BitmapUtils.getOutputMediaFile(getApplicationContext())));

            startActivityForResult(intent, Constants.CROP_FROM_CAMERA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void no2(View view){
        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.cropDialog2)).setVisibility(View.GONE);
        background.setVisibility(View.GONE);
        //pass
        try {
            bitmap=getBitmapFromUri(_imageCaptureUri);
            onGalleryImageResult(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showCropDialog3() {

        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);

        dialogBuilder.setMessage("Do you want to crop?");
        dialogBuilder.setIcon(R.drawable.noti);
        dialogBuilder.setTitle("Hint!");

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
                //pass
                captureNonCrop(thumbnail);
            }
        });
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
                //pass
                try {
                    _imageCaptureUri=getImageUri(getApplicationContext(),thumbnail);
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
                    intent.putExtra("output", Uri.fromFile(BitmapUtils.getOutputMediaFile(getApplicationContext())));

                    startActivityForResult(intent, Constants.CROP_FROM_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        android.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = 900;
//        lp.height = 700;
//        lp.x=80;
//        lp.y=-1200;
        alertDialog.getWindow().setAttributes(lp);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void showCropDialog2() {

        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);

        dialogBuilder.setMessage("Do you want to crop?");
        dialogBuilder.setIcon(R.drawable.noti);
        dialogBuilder.setTitle("Hint!");

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
                //pass
                try {
                    bitmap=getBitmapFromUri(_imageCaptureUri);
                    onGalleryImageResult(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
                //pass
                try {

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
                    intent.putExtra("output", Uri.fromFile(BitmapUtils.getOutputMediaFile(getApplicationContext())));

                    startActivityForResult(intent, Constants.CROP_FROM_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        android.app.AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alertDialog.getWindow().getAttributes());
        lp.width = 900;
//        lp.height = 700;
//        lp.x=80;
//        lp.y=-1200;
        alertDialog.getWindow().setAttributes(lp);
    }

    public void pickFromGalley(){
        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
        //pass
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.PICK_FROM_ALBUM);
    }


    public void fromGallery(View view) {

        isCamera=false;
//        showCropDialog();
        pickFromGalley();

    }

    public void takePhoto(View view) {

        isCamera=true;
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

                    if(isCamera){
                        try{
                            onCaptureImageResult(data);
                            return;
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    else {
//                        showCropDialog2();
                        ((LinearLayout) findViewById(R.id.cropDialog2)).setVisibility(View.VISIBLE);
                        background.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
        }

    }


    private void onCaptureImageResult(Intent data) {

        thumbnail = (Bitmap) data.getExtras().get("data");
//        captureNonCrop(thumbnail);

//        showCropDialog3();
        ((LinearLayout)findViewById(R.id.cropDialog3)).setVisibility(View.VISIBLE);
        background.setVisibility(View.VISIBLE);
    }

    public void captureNonCrop(Bitmap thumbnail){
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Pictures");
        if (!dir.exists())
            dir.mkdirs();
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
        mImageView.setBackgroundResource(R.drawable.gradient_image_background);
        ((View)findViewById(R.id.background2)).setBackgroundColor(Color.parseColor("#8104becf"));
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
        mImageView.setBackgroundResource(R.drawable.gradient_image_background);
        ((View)findViewById(R.id.background2)).setBackgroundColor(Color.parseColor("#8104becf"));
    }

    public void registerChatPortion(String email, String key, String name){
        Qiscus.setUser(email, key)
                .withUsername(name)
                .save(new Qiscus.SetUserListener()
                {
                    @Override
                    public void onSuccess(QiscusAccount qiscusAccount) {
                        Toast.makeText(MainActivity.this, "Registered in chat portion", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getAllContacts(View view){
        show_menu_flag=false;
        ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);
        Intent intent=new Intent(getApplicationContext(),RecentUsersActivity.class);
        startActivity(intent);
        overridePendingTransition(0,0);
    }
    public void getMyProfile(View view){
        show_menu_flag=false;
        ((LinearLayout) findViewById(R.id.main_menu)).setVisibility(View.GONE);

        String info="Name: "+Commons.thisUser.getName()+"\n"+"\n"+
                "Email: "+Commons.thisUser.getEmail()+"\n"+"\n"+
                "Password: "+Commons.thisUser.getPassword()+"\n\n"+
                "Phone Number: + "+Commons.thisUser.getPhone();
        showMyProfile(Commons.thisUser.getName(),Commons.thisUser.getEmail(),Commons.thisUser.getPassword(),Commons.thisUser.getPhone());
    }

    private  void showMyProfile(String name, String email, String password, String phone) {

        ImageLoader _imageLoader = SeptemberApplication.getInstance().getImageLoader();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("My Profile");
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);
        final CircularNetworkImageView photo=(CircularNetworkImageView)dialogView.findViewById(R.id.photo);
        photo.setImageUrl(Commons.thisUser.getPhotoUrl(),_imageLoader);
        final TextView nameBox = (TextView) dialogView.findViewById(R.id.name);
        final TextView emailBox = (TextView) dialogView.findViewById(R.id.email);
        final TextView passwordBox = (TextView) dialogView.findViewById(R.id.password);
        final TextView phoneBox = (TextView) dialogView.findViewById(R.id.phoneNumber);
        nameBox.setText(name);
        emailBox.setText(email);
        passwordBox.setText(password);
        phoneBox.setText(phone);
        builder.setView(dialogView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void pushNotification(final String email) {

        final Firebase reference = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"notification/"+ email.replace(".com","").replace(".","ddoott"));

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Log.d("Count===>", String.valueOf(dataSnapshot.getChildrenCount()));
                notiusers = (int) dataSnapshot.getChildrenCount();

                final Firebase reference1 = new Firebase(ReqConst.FIREBASE_DATABASE_URL+"notification/"+ email.replace(".com","").replace(".","ddoott")+"/"+dataSnapshot.getKey());
                Log.d("Reference===>", reference1.toString());

                reference1.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Map map = dataSnapshot.getValue(Map.class);
                        try{
                            message = map.get("msg").toString();
                            sender = map.get("sender").toString();
                            photo = map.get("senderPhoto").toString();
                            name = map.get("senderName").toString();
                            phone = map.get("senderPhone").toString();
                            //        time = map.get("time").toString();
                            Commons.notiEmail = sender + ".com";
                            Commons.firebase = reference1;
                            Commons.mapping=map;

                            UserEntity user = new UserEntity();
                            user.setName(name);
                            user.setEmail(sender+".com");
                            user.setPhotoUrl(photo);
                            user.setPhone(phone);

                            if(user.getName().length()>0){

                                if(!_emails.contains(user.getEmail())){
                                    _emails.add(user.getEmail());
                                    Commons.userEntities.add(user);
                                    shownot();
                                }

                                if (Commons.userEntities.size()==0)
                                    ShortcutBadger.removeCount(getApplicationContext());

                                if(Commons.userEntities.size()>0){
                                    ShortcutBadger.applyCount(getApplicationContext(), Commons.userEntities.size());
                                }
                            }else {
                                if (Commons.userEntities.size()==0)
                                    ShortcutBadger.removeCount(getApplicationContext());

                                if(Commons.userEntities.size()>0){
                                    ShortcutBadger.applyCount(getApplicationContext(), Commons.userEntities.size());
                                }
                            }

                            //        showToast("You received a message!");
                        }catch (NullPointerException e){}
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

    }

    public void shownot() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        long[] v = {500,1000};

        Commons.user=new UserEntity();
        Commons.user.setPhotoUrl(photo);
        Commons.user.setName(name);
        Commons.user.setPhone(phone);
        Commons.user.setEmail(Commons.notiEmail);    Log.d("NotiEmail===>",Commons.notiEmail);

        if(photo.length()>0){
            try {
                bitmapPhoto= BitmapFactory.decodeStream((InputStream) new URL(photo).getContent());
            } catch (IOException e) {
                e.printStackTrace();
                bitmapPhoto=BitmapFactory.decodeResource(Resources.getSystem(),R.drawable.messages);
            }
        }else bitmapPhoto=BitmapFactory.decodeResource(Resources.getSystem(),R.drawable.messages);

        Intent intent = new Intent(this, InteractionHistoryActivity.class);
        intent.putExtra("notiflag",true);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        android.app.Notification n = new android.app.Notification.Builder(this)
                .setShowWhen(true)
                .setFullScreenIntent(pIntent, true)
                .setContentTitle(name)
                .setContentText(message)
                .setSmallIcon(R.drawable.noti).setLargeIcon(bitmapPhoto)
                .setContentIntent(pIntent)
                .setSound(uri)
//                .setVibrate(v)
                .setAutoCancel(true).build();

        notificationManager.notify(0, n);
    }

}





























