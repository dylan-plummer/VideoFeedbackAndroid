package com.jumproper.videofeedback;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;

public class RemoveAds extends AppCompatActivity {
    IInAppBillingService mService;
    FirebaseAuth mAuth;
    Thread purchase;
    ServiceConnection mServiceConn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_ads);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth=FirebaseAuth.getInstance();





        purchase=new Thread(new Runnable() {
                @Override
                public void run() {
                    if(MainActivity.ads==false){
                        Toast.makeText(RemoveAds.this,"You have already removed ads!", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    ArrayList<String> purchaseList = new ArrayList<String>();
                    purchaseList.add("remove_ads");
                    Bundle purchaseQuery = new Bundle();
                    purchaseQuery.putStringArrayList("ITEM_ID_LIST", purchaseList);
                    try {
                        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "remove_ads", "inapp", mAuth.getCurrentUser().getUid());
                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                        try {
                            startIntentSenderForResult(pendingIntent.getIntentSender(),
                                    420, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                    Integer.valueOf(0));
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    } catch (RemoteException e) {
                        Toast.makeText(RemoveAds.this, "Could not complete purchase.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
        });

        if(mAuth.getCurrentUser()==null){
            Intent intent = new Intent(RemoveAds.this, SignIn.class);
            startActivity(intent);
        }

        mServiceConn = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Toast.makeText(RemoveAds.this,"Cannot connect",Toast.LENGTH_SHORT).show();
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name,
                                           IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
                purchase.run();
            }
        };
        checkPurchase();
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);



    }
    public void checkPurchase(){
        if(MainActivity.ads==false){
            Toast.makeText(RemoveAds.this,"You have already removed ads!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(mAuth.getCurrentUser()==null){
            return;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("images").child(mAuth.getCurrentUser().getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("remove_ads")) {
                    if (dataSnapshot.child("remove_ads").getValue().toString().equals("true")) {
                        MainActivity.ads = false;
                        Toast.makeText(RemoveAds.this,"You have already removed ads!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                return;
            }
        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("IAP","request "+requestCode+" result " + resultCode);
        checkPurchase();
        if(resultCode==RESULT_OK && requestCode==420){
            checkPurchase();
            FirebaseDatabase fb = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = fb.getReference("images");
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            myRef.child(mAuth.getCurrentUser().getUid()).child("remove_ads").setValue(true);
            myRef.child(mAuth.getCurrentUser().getUid()).child("purchase_data").setValue(purchaseData);
            MainActivity.ads=false;
            Intent intent = new Intent(RemoveAds.this, MainActivity.class);
            startActivity(intent);
            finish();

        }
        else{
            Intent intent = new Intent(RemoveAds.this, MainActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        if(mAuth.getCurrentUser()!=null && mService!=null){
            purchase.run();
        }
        else if (mAuth.getCurrentUser()!=null){
            mServiceConn = new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Toast.makeText(RemoveAds.this,"Cannot connect",Toast.LENGTH_SHORT).show();
                    mService = null;
                }

                @Override
                public void onServiceConnected(ComponentName name,
                                               IBinder service) {
                    mService = IInAppBillingService.Stub.asInterface(service);
                    checkPurchase();
                    purchase.run();
                }
            };
            checkPurchase();
            Intent serviceIntent =
                    new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }


}
