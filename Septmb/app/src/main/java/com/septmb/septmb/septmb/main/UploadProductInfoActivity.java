package com.septmb.septmb.septmb.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.ParcelFileDescriptor;
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
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.SeptemberApplication;
import com.septmb.septmb.septmb.classes.GifView;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.commons.Constants;
import com.septmb.septmb.septmb.commons.ReqConst;
import com.septmb.septmb.septmb.models.UserEntity;
import com.septmb.septmb.septmb.preference.PrefConst;
import com.septmb.septmb.septmb.preference.Preference;

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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.utils.BitmapUtils;
import com.septmb.septmb.septmb.utils.MultiPartRequest;

public class UploadProductInfoActivity extends Activity implements TextToSpeech.OnInitListener {

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
            android.Manifest.permission.CAPTURE_VIDEO_OUTPUT,
            android.Manifest.permission.LOCATION_HARDWARE};

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
    EditText title, brand, price, seller, description;
    TextView category, gender, symbol;
    FrameLayout uploadButton;
    File file;
    String product_id="";
    boolean updateFlag=false;
    NetworkImageView networkImageView;

    Bitmap bitmap;
    String _photoPath="";
    File file0;
    Uri _imageCaptureUri=null;
    Bitmap thumbnail;
    boolean currencyFlag=false;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_product_info);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try{
            updateFlag=getIntent().getBooleanExtra("flag", false);
        }catch (NullPointerException e){

        }

        mImageView = (ImageView) findViewById(R.id.imageView1);
        networkImageView = (NetworkImageView) findViewById(R.id.netImage);

        view=(View)findViewById(R.id.background);
        
        try{
            if(updateFlag){
                setTitle("Update your product!");
                networkImageView.setVisibility(View.VISIBLE);
                networkImageView.setImageUrl(Commons.productInfo.getImage_url(), SeptemberApplication.getInstance().getImageLoader());
                ((LinearLayout)findViewById(R.id.pictureFrame)).setBackgroundDrawable(getDrawable(R.drawable.gradient_image_background));
            }else {
                setTitle("Add your product!");
                networkImageView.setVisibility(View.GONE);
            }
        }catch (NullPointerException e){

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
        gif = (GifView)findViewById(R.id.progress_gif);
        gif.setMovieResource(R.raw.loading6);
        gif.setPaused(true);

        title=(EditText)findViewById(R.id.title);
        brand=(EditText)findViewById(R.id.brand);
        price=(EditText)findViewById(R.id.price);
        category=(TextView) findViewById(R.id.category);
        seller=(EditText)findViewById(R.id.sale);
        description=(EditText)findViewById(R.id.desctiption);
        gender=(TextView) findViewById(R.id.gender);
        gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoiceDialog();
            }
        });

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoiceDialogOfCategory();
            }
        });
        symbol=(TextView) findViewById(R.id.symbol);

        symbol.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        symbol.setBackground(getDrawable(R.drawable.white_fillrect));
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        symbol.setBackground(getDrawable(R.drawable.white_stroke));
                        if(!currencyFlag) {
                            currencyFlag=true;
                            showChoiceDialogOfSymbol2();
                        }else {
                            currencyFlag=false;
                            ((LinearLayout)findViewById(R.id.currencyLayout)).setVisibility(View.GONE);
                            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.space);
                            ((LinearLayout)findViewById(R.id.currencyLayout)).startAnimation(animation);
                        }

                    case MotionEvent.ACTION_CANCEL: {
                        //clear the overlay
                        symbol.getBackground().clearColorFilter();
                        symbol.invalidate();
                        break;
                    }
                }

                return true;
            }
        });
        
        try{
            if(updateFlag){
                title.setText(Commons.productInfo.getTitle());
                brand.setText(Commons.productInfo.getBrand());
                symbol.setText(Commons.productInfo.getPrice().substring(0,1));
                price.setText(Commons.productInfo.getPrice().substring(2,Commons.productInfo.getPrice().length()));
                category.setText(Commons.productInfo.getCategory());
                seller.setText(Commons.productInfo.getSeller());
                description.setText(Commons.productInfo.getDescription());
                gender.setText(Commons.productInfo.getGender());
            }
        }catch (NullPointerException e){}
        
        uploadButton=(FrameLayout)findViewById(R.id.upload);
        uploadButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        uploadButton.setBackground(getDrawable(R.drawable.green_fillrect));
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        uploadButton.setBackground(getDrawable(R.drawable.round_button_background));
                        try{
                            if(updateFlag)
                                updateProductInfo();
                            else {
                                if(!file0.equals(null) && title.getText().length()>0 && brand.getText().length()>0 && gender.getText().length()>0 &&
                                        price.getText().length()>0 && category.getText().length()>0 && seller.getText().length()>0 && description.getText().length()>0) {
                                    uploadProductInfo();
                                }
                                else if(file0.equals(null))Toast.makeText(getApplicationContext(), "Please retry product photo", Toast.LENGTH_SHORT).show();
                                else Toast.makeText(getApplicationContext(), "Please recheck empty field", Toast.LENGTH_SHORT).show();
                            }
                        }catch (NullPointerException e){

                        }

                    case MotionEvent.ACTION_CANCEL: {
                        //clear the overlay
                        uploadButton.getBackground().clearColorFilter();
                        uploadButton.invalidate();
                        break;
                    }
                }

                return true;
            }
        });

        ((LinearLayout)findViewById(R.id.frame)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LinearLayout) findViewById(R.id.loadDialog)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.cropDialog3)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.cropDialog2)).setVisibility(View.GONE);
                view.setVisibility(View.GONE);
            }
        });

        ((TextView)findViewById(R.id.symbol1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currencyFlag=false;
                ((LinearLayout)findViewById(R.id.currencyLayout)).setVisibility(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.space);
                ((LinearLayout)findViewById(R.id.currencyLayout)).startAnimation(animation);
                symbol.setText(((TextView)findViewById(R.id.symbol1)).getText().toString().trim());
                symbol.setBackgroundDrawable(getDrawable(R.drawable.orange_circle_background));
            }
        });
        ((TextView)findViewById(R.id.symbol2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currencyFlag=false;
                ((LinearLayout)findViewById(R.id.currencyLayout)).setVisibility(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.space);
                ((LinearLayout)findViewById(R.id.currencyLayout)).startAnimation(animation);
                symbol.setText(((TextView)findViewById(R.id.symbol2)).getText().toString().trim());
                symbol.setBackgroundDrawable(getDrawable(R.drawable.magenda_circle_background));
            }
        });
        ((TextView)findViewById(R.id.symbol3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currencyFlag=false;
                ((LinearLayout)findViewById(R.id.currencyLayout)).setVisibility(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.space);
                ((LinearLayout)findViewById(R.id.currencyLayout)).startAnimation(animation);
                symbol.setText(((TextView)findViewById(R.id.symbol3)).getText().toString().trim());
                symbol.setBackgroundDrawable(getDrawable(R.drawable.blue_circle_background));
            }
        });
        ((TextView)findViewById(R.id.symbol4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currencyFlag=false;
                ((LinearLayout)findViewById(R.id.currencyLayout)).setVisibility(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.space);
                ((LinearLayout)findViewById(R.id.currencyLayout)).startAnimation(animation);
                symbol.setText(((TextView)findViewById(R.id.symbol4)).getText().toString().trim());
                symbol.setBackgroundDrawable(getDrawable(R.drawable.green_circle_background));
            }
        });

        title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    makeSrongBackground();
                }else {}
            }
        });
        brand.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    makeSrongBackground();
                }else {}
            }
        });
        price.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    makeSrongBackground();
                }else {}
            }
        });
        seller.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    makeSrongBackground();
                }else {}
            }
        });
        description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    makeSrongBackground();
                }else {}
            }
        });

        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                makeSrongBackground();
            }
        });
        makeSrongBackground();
    }

    public void showChoiceDialog() {

        final String[] items = {"Men (Adult)","Women (Adult)","Men (Teens)", "Women (Teens)", "Boys (Kids)", "Girls (Kids)", "Unisex (Adults)", "Unisex (kids)"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select\n     gender...");
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
            }

        });
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                gender.setText(items[item]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alert.getWindow().getAttributes());
        lp.width = 850;
//        lp.height = 700;
//        lp.x=80;
//        lp.y=-1200;
        alert.getWindow().setAttributes(lp);
        makeSrongBackground();
    }

    @Override
    public void onInit (int status) {
        tts.setLanguage(Locale.ENGLISH);
        tts.setSpeechRate(1);
    }

    public void showChoiceDialogOfCategory() {

        final String[] items = {"Accessories", "Activewear","Denim","Dresses & Skirts", "Handbags", "Health & Beauty", "Intimates & Loungewear", "Jewelry & Watches",
                "Maternity","Outerwear","Pants", "Pants & Shorts","Polos & Tees", "Shirts", "Shirts & Sweaters", "Shoes", "Shorts & Swimwear", "Socks, Underwear & Sleepwear", "Suits & Blazers",
                "Suits & Sportcoats", "Sweaters & Hoodies", "Swimwear"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select category...");
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
            }

        });
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                category.setText(items[item]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alert.getWindow().getAttributes());
        lp.width = 850;
//        lp.height = 700;
//        lp.x=80;
//        lp.y=-1200;
        alert.getWindow().setAttributes(lp);
        makeSrongBackground();
    }

    public void showChoiceDialogOfSymbol() {

        final String[] items = {"$", "€","£","¥"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select...");
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
            }

        });
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                symbol.setText(items[item]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alert.getWindow().getAttributes());
        lp.width = 700;
//        lp.height = 700;
//        lp.x=80;
//        lp.y=-1200;
        alert.getWindow().setAttributes(lp);
    }

    public void showChoiceDialogOfSymbol2() {

        final String[] items = {"$", "€","£","¥"};

        ((LinearLayout)findViewById(R.id.currencyLayout)).setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.translatefromright);
        ((LinearLayout)findViewById(R.id.currencyLayout)).startAnimation(animation);
    }

    public void onClickIdentify(View view) {
        try{
            if(!mCurrentPhotoUri.equals(null)){
//                TextView txtView = (TextView) findViewById(R.id.textView1);
//                txtView.setText("I'm uploading");
//                txtView.setVisibility(View.VISIBLE);
                gif.setVisibility(View.VISIBLE);
                gif.setPaused(false);
                new IdentifyImage().execute();
            }else {
                Toast.makeText(getApplicationContext(),"Please take a photo", Toast.LENGTH_SHORT).show();
            }
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
//            TextView txtView = (TextView) findViewById(R.id.textView1);
//            txtView.setText("I'm identifying");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    gif.setPaused(true);
//                    TextView txtView = (TextView) findViewById(R.id.textView1);
//                    txtView.setText("Completed!");
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
//            TextView txtView = (TextView) findViewById(R.id.textView1);
//            txtView.setText("I guess it is " + keyWords);
            get_keywords = true;
//            Toast.makeText(getApplicationContext(),"Picture loaded!", Toast.LENGTH_SHORT).show();
            if(keyWords.equals("null"))
//                showAlertDialog("Loading failed.\n    Try again");
                showAlertDialogOfLoading("Loading failed.\n    Try again");
            else
//                showAlertDialog("Successfully loaded!");
                showAlertDialogOfLoading("Successfully loaded!");

//            new SearchOptionDialog().show(getFragmentManager(),"search_option");
        }
    }

    public void showAlertDialogOfLoading(String msg){
        ((LinearLayout)findViewById(R.id.loadDialog)).setVisibility(View.VISIBLE);
        view.setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.msg)).setText(msg);
    }

    public void okay(View v){
        gif.setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.loadDialog)).setVisibility(View.GONE);
        view.setVisibility(View.GONE);
    }

    public void showAlertDialog(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(msg);
        builder.setIcon(R.drawable.noti);
//        builder.setMessage(msg);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                gif.setVisibility(View.GONE);
            }
        });
        AlertDialog alert=builder.create();
        alert.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(alert.getWindow().getAttributes());
        lp.width = 1150;
//        lp.height = 700;
//        lp.x=80;
//        lp.y=-1200;
        alert.getWindow().setAttributes(lp);
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
                                ((UploadProductInfoActivity)getActivity()).onClickCamera();
                            else if(which == 1)
                                ((UploadProductInfoActivity)getActivity()).onClickSelectImage();
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

            file=imgFileOrig;

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
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(b);
            networkImageView.setVisibility(View.GONE);

            try{
                if(!mCurrentPhotoUri.equals(null)){
//                TextView txtView = (TextView) findViewById(R.id.textView1);
//                txtView.setText("I'm uploading");
//                txtView.setVisibility(View.VISIBLE);
                    gif.setVisibility(View.VISIBLE);
                    gif.setPaused(false);
                    new IdentifyImage().execute();
                }else {
                    Toast.makeText(getApplicationContext(),"Please take a photo", Toast.LENGTH_SHORT).show();
                }
            }catch (NullPointerException e){
                Toast.makeText(getApplicationContext(),"Please take a photo", Toast.LENGTH_SHORT).show();
            }
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
//            TextView txtView = (TextView) findViewById(R.id.textView1);
//            txtView.setText("I'm identifying");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    gif.setPaused(true);
//                    TextView txtView = (TextView) findViewById(R.id.textView1);
//                    txtView.setText("Completed!");
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
//            TextView txtView = (TextView) findViewById(R.id.textView1);
//            txtView.setText("I guess it is " + keyWords);
            get_keywords = true;
        }
    }

    public void uploadProductImage(String product_id) {

        try {

            final Map<String, String> params = new HashMap<>();
            params.put("product_id", product_id);

            String url = ReqConst.SERVER_URL + "uploadProductImage";

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
                    Log.d("imageJsonBroadmoor===",json.toString());
                    Log.d("paramsBroadmoor====",params.toString());
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
                if(updateFlag)
                    Toast.makeText(getApplicationContext(),"Successfully updated!",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(),"Successfully uploaded!",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplicationContext(),"Picture uploading failed",Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Picture uploading failed",Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadProductInfo() {

        String url = ReqConst.SERVER_URL + "uploadProductInfo";

        gif.setVisibility(View.VISIBLE);
        gif.setPaused(false);


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
                gif.setPaused(true);
                Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("email", Commons.thisUser.getEmail());
                params.put("keyword", keyWords);
                params.put("title", title.getText().toString().trim());
                params.put("brand", brand.getText().toString().trim());
                params.put("gender", gender.getText().toString().trim());
                params.put("price", symbol.getText().toString().trim()+" "+price.getText().toString().trim());
                params.put("category", category.getText().toString().trim());
                params.put("seller", seller.getText().toString().trim());
                if(description.getText().length()>0)
                    params.put("description", description.getText().toString().trim());
                else params.put("description", "None");

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(60000,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SeptemberApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseRegisterResponse(String json) {

        gif.setVisibility(View.GONE);
        gif.setPaused(true);

        try {

            JSONObject response = new JSONObject(json);   Log.d("Response=====> :",response.toString());

            String success = response.getString("result");

            Log.d("result=====> :",String.valueOf(success));

            if (success.equals("0")) {

                product_id=response.getString("product_id");
                uploadProductImage(product_id);

            }
            else {

                Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            gif.setVisibility(View.GONE);
            gif.setPaused(true);
            e.printStackTrace();

            Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
        }
    }

    public void updateProductInfo() {

        String url = ReqConst.SERVER_URL + "updateProductInfo";

        gif.setVisibility(View.VISIBLE);
        gif.setPaused(false);


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
                gif.setVisibility(View.GONE);
                gif.setPaused(true);
                Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("product_id", Commons.productInfo.getIdx());
                try{
                    if(keyWords.length()>0)
                        params.put("keyword", keyWords);
                    else
                        params.put("keyword", Commons.productInfo.getKeyword());
                }catch (NullPointerException e){
                    params.put("keyword", Commons.productInfo.getKeyword());
                }
                params.put("title", title.getText().toString().trim());
                params.put("brand", brand.getText().toString().trim());
                params.put("gender", gender.getText().toString().trim());
                params.put("price", symbol.getText().toString().trim()+" "+price.getText().toString().trim());
                params.put("category", category.getText().toString().trim());
                params.put("seller", seller.getText().toString().trim());
                params.put("description", description.getText().toString().trim());

                return params;
            }
        };

        post.setRetryPolicy(new DefaultRetryPolicy(60000,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        SeptemberApplication.getInstance().addToRequestQueue(post, url);

    }

    public void parseUpdateResponse(String json) {

        gif.setVisibility(View.GONE);
        gif.setPaused(true);

        try {

            JSONObject response = new JSONObject(json);   Log.d("Response=====> :",response.toString());

            String success = response.getString("result");

            Log.d("result=====> :",String.valueOf(success));

            if (success.equals("0")) {

                try{
                    if(!file0.equals(null))
                        uploadProductImage(Commons.productInfo.getIdx());
                }catch (NullPointerException e){
                    Toast.makeText(getApplicationContext(),"Successfully updated!",Toast.LENGTH_SHORT).show();
                }

            }
            else {

                Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            gif.setVisibility(View.GONE);
            gif.setPaused(true);
            e.printStackTrace();

            Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_SHORT).show();
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
        lp.width = 900;
//        lp.height = 700;
//        lp.x=80;
//        lp.y=-1200;
        alertDialog.getWindow().setAttributes(lp);
    }

    public void yes3(View v){
        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.cropDialog3)).setVisibility(View.GONE);
        view.setVisibility(View.GONE);
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

    public void no3(View v){
        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.cropDialog3)).setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        //pass
        captureNonCrop(thumbnail);
    }

    public void yes2(View v){
        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.cropDialog2)).setVisibility(View.GONE);
        view.setVisibility(View.GONE);
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

    public void no2(View v){
        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
        ((LinearLayout)findViewById(R.id.cropDialog2)).setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        //pass
        try {
            bitmap=getBitmapFromUri(_imageCaptureUri);
            onGalleryImageResult(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
        ((LinearLayout)findViewById(R.id.pictureFrame)).setBackgroundDrawable(getDrawable(R.drawable.gradient_image_background));
        networkImageView.setVisibility(View.GONE);

        try{
            if(!file0.equals(null)){
//                TextView txtView = (TextView) findViewById(R.id.textView1);
//                txtView.setText("I'm uploading");
//                txtView.setVisibility(View.VISIBLE);
                gif.setVisibility(View.VISIBLE);
                gif.setPaused(false);
                new IdentifyImage().execute();
            }else {
                Toast.makeText(getApplicationContext(),"Please take a photo", Toast.LENGTH_SHORT).show();
            }
        }catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"Please take a photo", Toast.LENGTH_SHORT).show();
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


    public void pickFromGalley(){
        ((LinearLayout) findViewById(R.id.alert)).setVisibility(View.GONE);
        //pass
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), Constants.PICK_FROM_ALBUM);
    }

    public void fromGallery(View view) {

        isCamera = false;
//        showCropDialog();
        pickFromGalley();
    }

    boolean isCamera=false;

    public void takePhoto(View view) {

        isCamera = true;
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
                        //            showCropDialog2();
                        ((LinearLayout) findViewById(R.id.cropDialog2)).setVisibility(View.VISIBLE);
                        view.setVisibility(View.VISIBLE);
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
        view.setVisibility(View.VISIBLE);
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
        ((LinearLayout)findViewById(R.id.pictureFrame)).setBackgroundDrawable(getDrawable(R.drawable.gradient_image_background));

        networkImageView.setVisibility(View.GONE);

        try{
            if(!file0.equals(null)){
//                TextView txtView = (TextView) findViewById(R.id.textView1);
//                txtView.setText("I'm uploading");
//                txtView.setVisibility(View.VISIBLE);
                gif.setVisibility(View.VISIBLE);
                gif.setPaused(false);
                new IdentifyImage().execute();
            }else {
                Toast.makeText(getApplicationContext(),"Please take a photo", Toast.LENGTH_SHORT).show();
            }
        }catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"Please take a photo", Toast.LENGTH_SHORT).show();
        }
    }

    public void makeSrongBackground(){

        final String[] items = {"$", "€","£","¥"};

        if(title.getText().toString().trim().length()>0)title.setBackgroundDrawable(getDrawable(R.drawable.green_edit_field_background));
        else if(title.getText().length()==0)title.setBackgroundDrawable(getDrawable(R.drawable.white_stroke));
        if(brand.getText().toString().trim().length()>0)brand.setBackgroundDrawable(getDrawable(R.drawable.green_edit_field_background));
        else if(brand.getText().length()==0)brand.setBackgroundDrawable(getDrawable(R.drawable.white_stroke));
        if(gender.getText().toString().trim().length()>0)gender.setBackgroundDrawable(getDrawable(R.drawable.green_edit_field_background));
        else if(gender.getText().length()==0)gender.setBackgroundDrawable(getDrawable(R.drawable.white_stroke));
        if(price.getText().toString().trim().length()>0)price.setBackgroundDrawable(getDrawable(R.drawable.green_edit_field_background));
        else if(price.getText().length()==0)price.setBackgroundDrawable(getDrawable(R.drawable.white_stroke));
        if(category.getText().toString().trim().length()>0)category.setBackgroundDrawable(getDrawable(R.drawable.green_edit_field_background));
        else if(category.getText().length()==0)category.setBackgroundDrawable(getDrawable(R.drawable.white_stroke));
        if(seller.getText().toString().trim().length()>0)seller.setBackgroundDrawable(getDrawable(R.drawable.green_edit_field_background));
        else if(seller.getText().length()==0)seller.setBackgroundDrawable(getDrawable(R.drawable.white_stroke));
        if(description.getText().toString().trim().length()>0)description.setBackgroundDrawable(getDrawable(R.drawable.green_edit_field_background));
        else if(description.getText().length()==0)description.setBackgroundDrawable(getDrawable(R.drawable.white_stroke));
        if(symbol.getText().equals("$"))symbol.setBackgroundDrawable(getDrawable(R.drawable.orange_circle_background));
        else if(symbol.getText().equals("€"))symbol.setBackgroundDrawable(getDrawable(R.drawable.magenda_circle_background));
        else if(symbol.getText().equals("£"))symbol.setBackgroundDrawable(getDrawable(R.drawable.blue_circle_background));
        else if(symbol.getText().equals("¥"))symbol.setBackgroundDrawable(getDrawable(R.drawable.green_circle_background));
    }

}






































