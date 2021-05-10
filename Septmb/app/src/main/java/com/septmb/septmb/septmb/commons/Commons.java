package com.septmb.septmb.septmb.commons;

import android.graphics.Bitmap;

import com.firebase.client.Firebase;
import com.septmb.septmb.septmb.models.ProductInfoEntity;
import com.septmb.septmb.septmb.models.UserEntity;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sonback123456 on 9/18/2017.
 */

public class Commons {
    public static UserEntity thisUser = new UserEntity();
    public static UserEntity user = new UserEntity();
    public static ProductInfoEntity productInfo = new ProductInfoEntity();
    public static boolean searchFlag=false;
    public static Firebase firebase=null;
    public static String notiEmail="";
    public static Map mapping=null;
    public static ArrayList<UserEntity> userEntities=new ArrayList<>();
    public static Bitmap productImage=null;
}
