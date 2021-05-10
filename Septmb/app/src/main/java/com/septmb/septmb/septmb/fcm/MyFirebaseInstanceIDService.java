package com.septmb.septmb.septmb.fcm;

/**
 * Created by a on 5/16/2017.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.commons.ReqConst;
import com.septmb.septmb.septmb.main.InteractionHistoryActivity;
import com.septmb.septmb.septmb.main.MainActivity;
import com.septmb.septmb.septmb.main.RequestActivity;
import com.septmb.septmb.septmb.models.UserEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    int notiusers = 0;
    String phone="", sender="", name="", photo="", message="";
    ArrayList<String> _emails=new ArrayList<>(10000);
    ArrayList<UserEntity> _datas_user=new ArrayList<>();
    Bitmap bitmapPhoto=null;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.

        if(token.length()>0){

            getRecentUsers();
        }
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
                bitmapPhoto=BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.messages);
            }catch (NetworkOnMainThreadException e){
                bitmapPhoto=BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.messages);
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































