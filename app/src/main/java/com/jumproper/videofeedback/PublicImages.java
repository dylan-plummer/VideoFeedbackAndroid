package com.jumproper.videofeedback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;

public class PublicImages extends AppCompatActivity {
    private ImageView currentImage,rate;
    private TextView title,author,votes;
    public ImageData data;
    public ArrayList<ImageData> topImages=new ArrayList<>();
    int currentMax=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_images);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentImage=(ImageView)findViewById(R.id.current_image);
        rate=(ImageView)findViewById(R.id.image_rating);
        title=(TextView) findViewById(R.id.title);
        author=(TextView)findViewById(R.id.author);
        votes=(TextView)findViewById(R.id.num_ratings);

        data=getTop(1000);
    }

    public ImageData getTop(final int mLast) {
        FirebaseDatabase fb = FirebaseDatabase.getInstance();
        DatabaseReference myRef = fb.getReference("images");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int max=-1;
                int last=mLast;
                GenericTypeIndicator<ImageData> id = new GenericTypeIndicator<ImageData>() {};
                for (DataSnapshot users : dataSnapshot.getChildren()) {
                    for(DataSnapshot images : users.getChildren()){
                        if(Integer.parseInt(images.child("votes").getValue().toString())>max &&
                                Integer.parseInt(images.child("votes").getValue().toString())<last){
                            data=images.getValue(id);
                            max=data.getVotes();
                        }
                    }
                }
                topImages.add(data);
                new DownloadImageTask(currentImage)
                        .execute(data.getDownload());
                title.setText(data.getName());
                author.setText(data.getUser());
                votes.setText(""+data.getVotes());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
        return data;
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
        }
    }
    public void nextImage(View v){
        data=getTop(topImages.get(topImages.size()-1).getVotes());
    }
}
