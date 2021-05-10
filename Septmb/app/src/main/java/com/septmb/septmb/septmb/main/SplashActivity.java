package com.septmb.septmb.septmb.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.septmb.septmb.septmb.R;

public class SplashActivity extends Activity {
    ProgressBar progressBar;
    ImageView image1, image2, image3;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        image1=(ImageView)findViewById(R.id.image1);
        image2=(ImageView)findViewById(R.id.image2);
        image3=(ImageView)findViewById(R.id.image3);

        image1.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        image1.startAnimation(animation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                image1.setVisibility(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_off);
                image1.startAnimation(animation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        image2.setVisibility(View.VISIBLE);
                        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
                        image2.startAnimation(animation);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                image2.setVisibility(View.GONE);
                                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.space);
                                image2.startAnimation(animation);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        image3.setVisibility(View.VISIBLE);
                                        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
                                        image3.startAnimation(animation);
                                        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom);
                                        image3.startAnimation(animation);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                image3.setVisibility(View.GONE);
                                                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomout);
                                                image3.startAnimation(animation);
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                        overridePendingTransition(0,0);
                                                    }
                                                }, 1200);
                                            }
                                        }, 3000);
                                    }
                                }, 700);
                            }
                        }, 4000);
                    }
                }, 900);
            }
        }, 4000);
    }
}




































