package com.septmb.septmb.septmb.main;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.utils.BitmapUtils1;
import com.septmb.septmb.septmb.utils.ImageTouchView;
import com.septmb.septmb.septmb.utils.ImageViewTouchBase;


public class ProductViewActivity extends AppCompatActivity {

    private static final String LOG_TAG = "imageTouch";

    TextView zoomin,zoomout;
    ImageView cancel;
    ImageTouchView image;
    float dX, dY;
    float initScaleX=0,initScaleY=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);

        cancel=(ImageView) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        image = (ImageTouchView) findViewById(R.id.image);

        // set the default image display type
        image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
//        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.caybelle);
//        image.setImageBitmap(bitmap, null, -1, -1);


        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        int size = (int) (Math.min(metrics.widthPixels, metrics.heightPixels) / 0.35);

        Bitmap bitmap = BitmapUtils1.resizeBitmap(Commons.productImage,size,size);

        if (null != bitmap) {
            Log.d(LOG_TAG, "screen size: " + metrics.widthPixels + "x" + metrics.heightPixels);
            Log.d(LOG_TAG, "bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            image.setOnDrawableChangedListener(
                    new ImageViewTouchBase.OnDrawableChangeListener() {
                        @Override
                        public void onDrawableChanged(final Drawable drawable) {
                            Log.v(LOG_TAG, "image scale: " + image.getScale() + "/" + image.getMinScale());
                            Log.v(LOG_TAG, "scale type: " + image.getDisplayType() + "/" + image.getScaleType());

                        }
                    }
            );
            image.setImageBitmap(bitmap, null, 5, 5);

        } else {
            Toast.makeText(this, "Failed to load the image", Toast.LENGTH_LONG).show();
        }


        image.setSingleTapListener(
                new ImageTouchView.OnImageViewTouchSingleTapListener() {

                    @Override
                    public void onSingleTapConfirmed() {
                        Log.d(LOG_TAG, "onSingleTapConfirmed");
                    }
                }
        );

        image.setDoubleTapListener(
                new ImageTouchView.OnImageViewTouchDoubleTapListener() {

                    @Override
                    public void onDoubleTap() {
                        Log.d(LOG_TAG, "onDoubleTap");
                    }
                }
        );

        image.setOnDrawableChangedListener(
                new ImageViewTouchBase.OnDrawableChangeListener() {

                    @Override
                    public void onDrawableChanged(Drawable drawable) {
                        Log.i(LOG_TAG, "onBitmapChanged: " + drawable);
                    }
                }
        );
    }
}
