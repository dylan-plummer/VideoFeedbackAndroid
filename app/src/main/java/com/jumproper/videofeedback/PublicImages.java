package com.jumproper.videofeedback;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class PublicImages extends AppCompatActivity {
    private ImageView currentImage,rate;
    private TextView title,author,votes,date;
    private ProgressBar loading;
    private Spinner spinner;
    public ImageData data;
    public ArrayList<ImageData> topImages=new ArrayList<>();
    boolean rated=false;
    int index=0;
    private FirebaseAuth mAuth;
    DownloadImageTask downloadImage;
    InterstitialAd mInterstitialAd;
    int adCOunt=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_images);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Public Gallery");
        mAuth=FirebaseAuth.getInstance();

        currentImage=(ImageView)findViewById(R.id.current_image);
        rate=(ImageView)findViewById(R.id.image_rating);
        title=(TextView) findViewById(R.id.title);
        author=(TextView)findViewById(R.id.author);
        votes=(TextView)findViewById(R.id.num_ratings);
        date=(TextView)findViewById(R.id.date);
        loading=(ProgressBar)findViewById(R.id.loading_image);
        spinner=(Spinner)findViewById(R.id.spinner);


        ArrayList<String>sortBy=new ArrayList<>();
        sortBy.add("Top");
        sortBy.add("Popular");
        sortBy.add("New");
        if(mAuth.getCurrentUser()!=null){
            sortBy.add("My Images");
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, sortBy);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setPopupBackgroundResource(R.color.background);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                adCOunt++;
                if(MainActivity.ads && mInterstitialAd.isLoaded() && adCOunt==3) {
                    displayInterstitial();
                    adCOunt=0;
                }
                fillImageList(adapterView.getItemAtPosition(i).toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        DrawerCreate drawer=new DrawerCreate();
        drawer.makeDrawer(this, this, mAuth, toolbar, "Public Gallery");

        displayAd();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-2959515976305980/2116452261");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                nextImage(currentImage);
            }
        });
        if(MainActivity.ads) {
            requestNewInterstitial();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            DrawerCreate drawer = new DrawerCreate();
            drawer.makeDrawer(this, this, mAuth, toolbar, "Public Gallery");
        }
        if(MainActivity.ads) {
            displayAd();
        }
    }

    public void displayAd(){
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-2959515976305980~5488721062");
        AdView mAdView = (AdView) findViewById(R.id.adView2);
        if(MainActivity.ads) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("FA57EBFCDEDF2178CC5E01649E78FEEF")
                    .build();
            mAdView.loadAd(adRequest);
        }
        else{
            mAdView.setVisibility(View.GONE);
        }
    }

    public void displayInterstitial(){
        mInterstitialAd.show();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("FA57EBFCDEDF2178CC5E01649E78FEEF")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    public void fillImageList(final String sort) {
        topImages.clear();
        FirebaseDatabase fb = FirebaseDatabase.getInstance();
        DatabaseReference myRef = fb.getReference("images");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<ImageData> id = new GenericTypeIndicator<ImageData>() {};
                for (DataSnapshot users : dataSnapshot.getChildren()) {
                    for(DataSnapshot images : users.getChildren()){
                            topImages.add(images.getValue(id));
                    }
                }
                if(sort.equals("Top")) {
                    index=0;
                    sortByTop();
                }
                else if(sort.equals("Popular")){
                    index=0;
                    sortByPopular();
                }
                else if(sort.equals("New")){
                    index=0;
                    sortByNew();
                }
                else if(sort.equals("My Images")){
                    index=0;
                    sortByMine();
                }
                setData(topImages.get(index));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }
    public void sortByTop(){
        Collections.sort(topImages, new Comparator<ImageData>() {
            @Override public int compare(ImageData p1, ImageData p2) {
                return p2.getVotes() - p1.getVotes(); // descending
            }
        });
    }
    public void sortByPopular(){
        Collections.sort(topImages, new Comparator<ImageData>() {
            @Override public int compare(ImageData p1, ImageData p2) {
                return (int)(p2.getDate() - p1.getDate()); // descending
            }
        });
        long newest=topImages.get(0).getDate();
        for(int j=0;j<topImages.size();j++){
            if(topImages.size()<11){
                break;
            }
            if(newest-topImages.get(j).getDate()>86400000/2){
                topImages.remove(j);
            }
        }
        Collections.sort(topImages, new Comparator<ImageData>() {
            @Override public int compare(ImageData p1, ImageData p2) {
                return p2.getVotes() - p1.getVotes(); // descending
            }
        });
    }
    public void sortByNew(){
        Collections.sort(topImages, new Comparator<ImageData>() {
            @Override public int compare(ImageData p1, ImageData p2) {
                return (int)(p2.getDate() - p1.getDate()); // descending
            }
        });
    }
    public void sortByMine(){
        if(mAuth.getCurrentUser()!=null) {
            for (int j = 0; j < topImages.size(); j++) {
                if (topImages.get(j).getuId() != mAuth.getCurrentUser().getUid()){
                    topImages.remove(j);
                }
            }
            Collections.sort(topImages, new Comparator<ImageData>() {
                @Override public int compare(ImageData p1, ImageData p2) {
                    return p2.getVotes() - p1.getVotes(); // descending
                }
            });
        }
    }
    public void setData(ImageData data){

        downloadImage=new DownloadImageTask(currentImage);
        downloadImage.execute(data.getDownload());
        title.setText(data.getName());
        author.setText(data.getUser());
        votes.setText(""+data.getVotes());
        date.setText(formatEpoch(data.getDate()));
        if(mAuth.getCurrentUser()!=null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("votes").child(mAuth.getCurrentUser().getUid()).child(data.getKey());
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null) {
                        if ((boolean) (dataSnapshot.getValue()) == false) {
                            rated=false;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                rate.setImageDrawable(getDrawable(R.drawable.star_empty));
                            } else {
                                rate.setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
                            }
                        } else {
                            rated=true;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                rate.setImageDrawable(getDrawable(R.drawable.star_filled));
                            } else {
                                rate.setImageDrawable(getResources().getDrawable(R.drawable.star_filled));
                            }
                        }
                    }
                    else{
                        rated=false;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            rate.setImageDrawable(getDrawable(R.drawable.star_empty));
                        } else {
                            rate.setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    public String formatEpoch(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.format(new Date(time));
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {

                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            loading.setVisibility(View.INVISIBLE);
            currentImage.setVisibility(View.VISIBLE);
        }
    }
    public void nextImage(View v){
        if(index+1<topImages.size()){
            downloadImage.cancel(true);
            currentImage.setVisibility(View.INVISIBLE);
            loading.setVisibility(View.VISIBLE);
            index++;
            setData(topImages.get(index));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rate.setImageDrawable(getDrawable(R.drawable.star_empty));
            } else {
                rate.setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
            }
        }

    }
    public void previousImage(View v){
        if(index-1>-1){
            downloadImage.cancel(true);
            currentImage.setVisibility(View.INVISIBLE);
            loading.setVisibility(View.VISIBLE);
            index--;
            setData(topImages.get(index));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rate.setImageDrawable(getDrawable(R.drawable.star_empty));
            } else {
                rate.setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
            }
            rated=false;
        }
    }
    public void rateImage(View v){
        if(mAuth.getCurrentUser()==null){
            Intent intent = new Intent(PublicImages.this, SignIn.class);
            startActivity(intent);
            return;
        }
        if(!rated) {
            ImageData data=topImages.get(index);
            data.incrementVotes();
            votes.setText("" + data.getVotes());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rate.setImageDrawable(getDrawable(R.drawable.star_filled));
            } else {
                rate.setImageDrawable(getResources().getDrawable(R.drawable.star_filled));
            }
            rated=true;
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("images").child(data.getuId()).child(data.getKey()).child("votes");
            myRef.setValue(data.getVotes());
            myRef = database.getReference("votes").child(mAuth.getCurrentUser().getUid());
            myRef.child(data.getKey()).setValue(true);
        }
        else{
            ImageData data=topImages.get(index);
            data.decrementVotes();
            votes.setText("" + data.getVotes());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rate.setImageDrawable(getDrawable(R.drawable.star_empty));
            } else {
                rate.setImageDrawable(getResources().getDrawable(R.drawable.star_empty));
            }
            rated=false;
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("images").child(data.getuId()).child(data.getKey()).child("votes");
            myRef.setValue(data.getVotes());
            myRef = database.getReference("votes").child(mAuth.getCurrentUser().getUid());
            myRef.child(data.getKey()).setValue(false);
        }
    }
}
