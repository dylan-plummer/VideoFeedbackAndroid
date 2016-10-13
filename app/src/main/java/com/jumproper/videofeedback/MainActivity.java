package com.jumproper.videofeedback;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ImageView imgView;
    private SeekBar iterInput,rotateInput,offsetInput,centerInput,scaleInput;
    private TextView iterCount,rotateCount,offsetCount,centerCount,scaleCount;
    Bitmap img,overlay;
    int j=0;
    int iter;
    float rotate,offset,center,scale;
    boolean indefinite=false;
    boolean running=false;
    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE=36;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iterCount=(TextView)findViewById(R.id.iter_count);
        rotateCount=(TextView)findViewById(R.id.rotate_count);
        offsetCount=(TextView)findViewById(R.id.offset_count);
        centerCount=(TextView)findViewById(R.id.center_count);
        scaleCount=(TextView)findViewById(R.id.scale_count);
        iterInput=(SeekBar)findViewById(R.id.iterations);
        rotateInput=(SeekBar)findViewById(R.id.rotation);
        offsetInput=(SeekBar)findViewById(R.id.offset);
        centerInput=(SeekBar)findViewById(R.id.center);
        scaleInput=(SeekBar)findViewById(R.id.scale);
        iterCount.setText(""+iterInput.getProgress());
        rotateCount.setText(""+rotateInput.getProgress());
        offsetCount.setText(""+offsetInput.getProgress());
        rotateCount.setText(""+rotateInput.getProgress());
        scaleCount.setText("."+(100-scaleInput.getProgress()));
        iter=iterInput.getProgress();
        rotate=(float)(rotateInput.getProgress()*Math.PI/360)/50;
        offset=2+offsetInput.getProgress();
        center=2+centerInput.getProgress();
        scale=(float)((100-scaleInput.getProgress())/100.0);

        iterInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                iterCount.setText(""+i);
                indefinite=false;
                if(i==100){
                    indefinite=true;
                    iterCount.setText("∞");
                }
                iter=i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        rotateInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                rotateCount.setText(""+i);
                rotate=(float)(i*Math.PI/360)/50;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        offsetInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                offset=2+(float)(i/4.0);
                offsetCount.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        centerInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                center=(float)(2+i);
                centerCount.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        scaleInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                scale=(float)((100-i)/100.0);
                scaleCount.setText("."+(100-i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    public void process(ImageView v){
        if(v.getDrawable()==null){
            Log.e("nullDrawable","ImageView not yet updated!");
            return;
        }
        img = ((BitmapDrawable) v.getDrawable()).getBitmap();
        int w=img.getWidth();
        int h=img.getHeight();
        overlay=img.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas=new Canvas(overlay);
        Paint p=new Paint();
        Matrix matrix = new Matrix();
        matrix.setRotate(rotate*j,w/2, h/2);
        matrix.postScale(scale,scale,w/center,w/center);
        matrix.postTranslate(w/offset,h/offset);

        Matrix flipx = new Matrix();
        flipx.setScale(-1,1);
        flipx.postTranslate(w,0);

        Matrix flipy = new Matrix();
        flipy.setScale(1,-1);
        flipy.postTranslate(0,h);

        canvas.drawBitmap(img, matrix, p);
        canvas.drawBitmap(overlay,flipx,p);
        canvas.drawBitmap(overlay,flipy,p);
        j++;
    }
    public void feedback(View v){
        if(running){
            running=false;
            return;
        }

        new Thread(new Runnable() {
            public void run() {
                running=true;
                imgView = (ImageView) findViewById(R.id.image_view);
                if(indefinite&&running){
                    while(indefinite) {
                        process(imgView);
                        imgView.postDelayed(new Runnable() {
                            public void run() {
                                imgView.setImageBitmap(overlay);
                            }
                        },5);
                    }
                }
                else {
                    for (int i = 0; i < iter; i++) {
                        if(running) {
                            process(imgView);
                            imgView.post(new Runnable() {
                                public void run() {
                                    imgView.setImageBitmap(overlay);
                                }
                            });
                        }
                    }
                }
                running=false;
            }

        }).start();
        running=false;
    }
    public void refresh(View v){
        if(running) {
            running = false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgView.setImageDrawable(getDrawable(R.drawable.image));
        }
        else{
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.image));
        }
        j=0;

    }
    public void saveImage(View v){
        getPermission();
    }
    public void getPermission(){
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
            return;
        }

        if ((ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)){

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        else{
            galleryAddPic(((BitmapDrawable)imgView.getDrawable()).getBitmap());
            Toast.makeText(MainActivity.this,"Save image to gallery!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted, yay!
                    galleryAddPic(((BitmapDrawable)imgView.getDrawable()).getBitmap());
                    Toast.makeText(MainActivity.this,"Save image to gallery!", Toast.LENGTH_SHORT).show();
                    return;

                } else {
                    // permission denied, boo!
                    Toast.makeText(MainActivity.this,"Cannot save to gallery without permission.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    private void galleryAddPic(Bitmap img) {
        FileOutputStream out = null;
        String fileName="video_feedback_"+System.currentTimeMillis()+".jpg";
        String path=Environment.getExternalStorageDirectory().toString()+"/VideoFeedback";
        File dir=new File(path);
        dir.mkdir();
        File f=new File(dir,fileName);
        Log.e("filePath",path);
        Log.e("file",f.toString());

        try {
            out = new FileOutputStream(f);
            img.compress(Bitmap.CompressFormat.JPEG, 50, out);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA,f.getAbsolutePath());
            values.put(MediaStore.Images.Media.DESCRIPTION,"Created with Video Feedback app.");
            values.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
            MainActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
