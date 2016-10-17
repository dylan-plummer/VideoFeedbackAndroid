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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PublicImages extends AppCompatActivity {
    private ImageView currentImage,rate;
    private TextView title,author,votes;
    private ProgressBar loading;
    public ImageData data;
    public ArrayList<ImageData> topImages=new ArrayList<>();
    boolean rated=false;
    int index=0;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_images);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth=FirebaseAuth.getInstance();

        currentImage=(ImageView)findViewById(R.id.current_image);
        rate=(ImageView)findViewById(R.id.image_rating);
        title=(TextView) findViewById(R.id.title);
        author=(TextView)findViewById(R.id.author);
        votes=(TextView)findViewById(R.id.num_ratings);
        loading=(ProgressBar)findViewById(R.id.loading_image);

        fillImageList();

    }

    public void fillImageList() {
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
                sortByTop();
                setData(topImages.get(index));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }
    public void sortByTop(){
        Log.e("Image List",topImages.toString());
        Collections.sort(topImages, new Comparator<ImageData>() {
            @Override public int compare(ImageData p1, ImageData p2) {
                return p2.getVotes() - p1.getVotes(); // descending
            }
        });
        Log.e("Image List","Sorted "+topImages.toString());
    }
    public void setData(ImageData data){

        new DownloadImageTask(currentImage)
                .execute(data.getDownload());
        title.setText(data.getName());
        author.setText(data.getUser());
        votes.setText(""+data.getVotes());
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
            currentImage.setVisibility(View.INVISIBLE);
            loading.setVisibility(View.VISIBLE);
            index++;
            setData(topImages.get(index));
        }

    }
    public void previousImage(View v){
        if(index-1>-1){
            currentImage.setVisibility(View.INVISIBLE);
            loading.setVisibility(View.VISIBLE);
            index--;
            setData(topImages.get(index));
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
