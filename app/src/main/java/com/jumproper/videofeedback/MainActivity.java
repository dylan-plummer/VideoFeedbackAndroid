package com.jumproper.videofeedback;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ImageView imgView;
    private SeekBar iterInput,rotateInput;
    private TextView iterCount,rotateCount;
    Bitmap img,overlay;
    int j=0;
    int iter;
    float rotate,scale;
    boolean indefinite=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iterCount=(TextView)findViewById(R.id.iter_count);
        rotateCount=(TextView)findViewById(R.id.rotate_count);
        iterInput=(SeekBar)findViewById(R.id.iterations);
        rotateInput=(SeekBar)findViewById(R.id.rotation);
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
                rotate=(float)(i*Math.PI/180)/50;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //img=((BitmapDrawable)imgView.getDrawable()).getBitmap();

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
        matrix.postScale(.9f,.9f,w/2,w/2);
        matrix.postTranslate(w/2,h/2);

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
        new Thread(new Runnable() {
            public void run() {
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
            }
        }).start();

    }

}
