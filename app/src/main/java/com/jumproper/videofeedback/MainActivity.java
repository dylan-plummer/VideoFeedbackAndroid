package com.jumproper.videofeedback;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ImageView imgView;
    private SeekBar iterInput,rotateInput,offsetInput,centerInput;
    private TextView iterCount,rotateCount,offsetCount,centerCount;
    Bitmap img,overlay;
    int j=0;
    int iter;
    float rotate,offset,center;
    boolean indefinite=false;
    boolean running=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iterCount=(TextView)findViewById(R.id.iter_count);
        rotateCount=(TextView)findViewById(R.id.rotate_count);
        offsetCount=(TextView)findViewById(R.id.offset_count);
        centerCount=(TextView)findViewById(R.id.center_count);
        iterInput=(SeekBar)findViewById(R.id.iterations);
        rotateInput=(SeekBar)findViewById(R.id.rotation);
        offsetInput=(SeekBar)findViewById(R.id.offset);
        centerInput=(SeekBar)findViewById(R.id.center);
        offset=2;
        center=2;
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
                offset=(float)(2+i/50.0);
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
                center=(float)(2+i/10.0);
                centerCount.setText(""+i);
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
        img=((BitmapDrawable)v.getDrawable()).getBitmap();
        int w=img.getWidth();
        int h=img.getHeight();
        overlay=img.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas=new Canvas(overlay);
        Paint p=new Paint();
        Matrix matrix = new Matrix();
        matrix.setRotate(rotate*j,w/2, h/2);
        matrix.postScale(.95f,.95f,w/center,w/center);
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
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                running=true;
                imgView = (ImageView) findViewById(R.id.image_view);
                if(indefinite){
                    while(indefinite) {
                        process(imgView);
                        imgView.post(new Runnable() {
                            public void run() {
                                imgView.setImageBitmap(overlay);
                            }
                        });
                    }
                }
                else {
                    for (int i = 0; i < iter; i++) {
                        process(imgView);
                        imgView.post(new Runnable() {
                            public void run() {
                                imgView.setImageBitmap(overlay);
                            }
                        });
                    }
                }
                running=false;
            }

        }).start();

    }
    public void refresh(View v){
        iter=0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgView.setImageDrawable(getDrawable(R.drawable.image));
        }
        else{
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.image));
        }
    }

}
