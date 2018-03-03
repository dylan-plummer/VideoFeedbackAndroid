package com.jumproper.fractalfeedback;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ImageView imgView,openImage;
    private SeekBar iterInput,rotateInput,offsetInput,centerInput,scaleInput,rotateCenterInput,mirrorInput,delayInput,skewInput,skewCenterInput,qualityInput,randomFrameInput;
    private TextView iterCount,rotateCount,offsetCount,centerCount,scaleCount,rotateCenterCount,mirrorCount,delayCount,skewCount,skewCenterCount,qualityCount,randomFrameCount;
    private CheckBox invertRotation,invertScale, saveVideo;
    private Spinner spinner;
    Bitmap img,overlay,original;
    int j=0;
    int h,w,resizeRatio,iter,invert,mirrorIter,randomFrameNum,currentRandomFrame,color;
    private int flowerMirrorIter=1;
    float rotate,offset,center,scale,rotateCenter,mirror,skew,skewCenter,quality;
    long delay;
    boolean running=false;
    boolean upload=false;
    final int REQUEST_SAVE_IMAGE=36;
    final int REQUEST_OPEN_IMAGE=69;
    final int REQUEST_GALLERY_IMAGE=42;
    String uploadName;
    String uploadCreationName;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseStorage storage;
    StorageReference storageRef;
    private long fileSize;
    private int notifyId=1;
    public static boolean ads=false;
    Thread drawFrame;
    boolean openedResize=false;
    boolean isDefault=true;
    boolean scaleInvert=false;
    boolean firstKaleidoscope=true;
    float imageX=0;
    float imageY=0;
    private boolean isTouching=false;
    private File imgDir;
    private InterstitialAd mInterstitialAd;
    private AndroidSequenceEncoder encoder;
    private SeekableByteChannel out;
    private ArrayList<Bitmap> frames;
    private boolean saveFrames = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth=FirebaseAuth.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-2959515976305980/9559522726");
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("2CC2625EB00F3EB58B6E5BC0B53C5A1D").build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("2CC2625EB00F3EB58B6E5BC0B53C5A1D").build());
            }

        });
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://video-feedback-1bd67.appspot.com");

        iterCount=(TextView)findViewById(R.id.iter_count);
        rotateCount=(TextView)findViewById(R.id.rotate_count);
        offsetCount=(TextView)findViewById(R.id.offset_count);
        centerCount=(TextView)findViewById(R.id.center_count);
        scaleCount=(TextView)findViewById(R.id.scale_count);
        rotateCenterCount=(TextView)findViewById(R.id.rotate_center_count);
        mirrorCount=(TextView)findViewById(R.id.mirror_count);
        delayCount=(TextView)findViewById(R.id.delay_count);
        skewCount=(TextView)findViewById(R.id.skew_count);
        skewCenterCount=(TextView)findViewById(R.id.skew_center_count);
        qualityCount=(TextView)findViewById(R.id.quality_count);
        randomFrameCount=(TextView)findViewById(R.id.random_frame_count);

        iterInput=(SeekBar)findViewById(R.id.iterations);
        rotateInput=(SeekBar)findViewById(R.id.rotation);
        offsetInput=(SeekBar)findViewById(R.id.offset);
        centerInput=(SeekBar)findViewById(R.id.center);
        scaleInput=(SeekBar)findViewById(R.id.scale);
        rotateCenterInput=(SeekBar)findViewById(R.id.rotate_center);
        mirrorInput=(SeekBar)findViewById(R.id.mirror);
        delayInput=(SeekBar)findViewById(R.id.delay);
        skewInput=(SeekBar)findViewById(R.id.skew);
        skewCenterInput=(SeekBar)findViewById(R.id.skew_center);
        qualityInput=(SeekBar)findViewById(R.id.quality_input);
        randomFrameInput=(SeekBar)findViewById(R.id.random_frame_number);

        invertRotation=(CheckBox)findViewById(R.id.invert_rotation);
        invertScale=(CheckBox)findViewById(R.id.invert_scale);
        saveVideo=(CheckBox)findViewById(R.id.save_video);

        spinner=(Spinner)findViewById(R.id.spinner2);

        imgView=(ImageView)findViewById(R.id.image_view);
        openImage=(ImageView)findViewById(R.id.open_image);
        iterCount.setText(""+iterInput.getProgress());
        rotateCount.setText(""+rotateInput.getProgress());
        offsetCount.setText(""+offsetInput.getProgress());
        rotateCount.setText(""+rotateInput.getProgress());
        scaleCount.setText("."+(100-scaleInput.getProgress()));
        rotateCenterCount.setText(""+rotateCenterInput.getProgress());
        mirrorCount.setText(""+mirrorInput.getProgress());
        delayCount.setText(""+delayInput.getProgress());
        skewCount.setText(""+skewInput.getProgress());
        skewCenterCount.setText(""+skewCenterInput.getProgress());
        quality=1;

        iter=iterInput.getProgress();
        rotate=(float)(rotateInput.getProgress()*Math.PI/360)/50;
        offset=2+offsetInput.getProgress();
        center=0;
        mirrorIter=0;
        rotateCenter=0;
        mirror=0;
        scale=(float)((100-scaleInput.getProgress())/100.0);
        delay=0;
        invert=1;
        skew =(float)(skewInput.getProgress()/500.0);
        skewCenter=0;
        randomFrameNum=20;
        currentRandomFrame=0;
        color=getResources().getColor(R.color.white);
        img=((BitmapDrawable) imgView.getDrawable()).getBitmap();
        h=img.getHeight();
        w=img.getWidth();
        original=img.copy(Bitmap.Config.ARGB_8888,true);



        ArrayList<String> sortBy=new ArrayList<>();
        sortBy.add("Off");
        sortBy.add("2");
        sortBy.add("3");
        sortBy.add("4");
        sortBy.add("5");
        sortBy.add("6");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, sortBy);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setPopupBackgroundResource(R.color.background);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mirrorIter=i+1;
                if(mirrorIter>1){
                    iterInput.setProgress(0);
                    if(firstKaleidoscope) {
                        Toast.makeText(MainActivity.this, "The kaleidoscope effect works very quickly. A low iteration count is suggested.", Toast.LENGTH_SHORT).show();
                        firstKaleidoscope=false;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        saveVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    saveFrames = true;
                    if(w>512 || h>512){
                        qualityInput.setProgress((w + h) / 256);
                    }
                }else{
                    saveFrames = false;
                }
            }
        });

        //add listener for changes to the quality seek bar
        qualityInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(!openedResize) {
                    quality = (i / 50f + .02f);
                    qualityCount.setText(""+i);
                    img = resize(original, 1);
                    if (overlay != null) {
                        overlay.recycle();
                    }
                    overlay = img.copy(Bitmap.Config.ARGB_8888, true);
                    imgView.setImageBitmap(img);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        iterInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                iterCount.setText(""+(i+1));
                iter=i+1;
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
                offset=2+(float)(i/10.0);
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
                center=(float)(10*i);
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
                if(scaleInvert) {
                    scale = (float) (0.99 / ((100 - i) / 100.0));
                }
                else{
                    scale = (float) ((100 - i) / 100.0);
                }
                scaleCount.setText("."+(100-i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        rotateCenterInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                rotateCenter=(float)(10*i);
                rotateCenterCount.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mirrorInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mirror=i/10;
                mirrorCount.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        delayInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                delay=i;
                delayCount.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        skewInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                skew =(float)(skewInput.getProgress()/500.0);
                skewCount.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        skewCenterInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                skewCenter=(float)(10*i);
                skewCenterCount.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        randomFrameInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                randomFrameNum=i;
                randomFrameCount.setText(""+i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        invertRotation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    invert=-1;
                }
                else{
                    invert=1;
                }
            }
        });
        invertScale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                scaleInvert=b;
                if(scaleInvert) {
                    scale = (float) (0.99 / ((100 - scaleInput.getProgress()) / 100.0));
                }
                else{
                    scale = (float) ((100 - scaleInput.getProgress()) / 100.0);
                }
            }
        });
        imgView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                isTouching=true;
                imageX=dragEvent.getX();
                imageY=dragEvent.getY();
                return false;
            }
        });


        DrawerCreate drawer=new DrawerCreate();
        drawer.makeDrawer(this, this, mAuth, toolbar, "Fractal Feedback");
    }





    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    public void onResume(){
        super.onResume();
        if(mAuth.getCurrentUser()!=null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            DrawerCreate drawer=new DrawerCreate();
            drawer.makeDrawer(this, this, mAuth, toolbar, "Fractal Feedback");
            if(upload) {
                upload = false;
                uploadImage();
            }
        }
    }
    public void randomize(){
        rotateInput.setProgress((int)(Math.random()*rotateInput.getMax()));
        Boolean reverse;
        Random r=new Random();
        reverse=r.nextBoolean();
        if(reverse)
            invertRotation.setChecked(true);
        else
            invertRotation.setChecked(false);
        offsetInput.setProgress((int)(Math.random()*offsetInput.getMax()));
        centerInput.setProgress((int)(Math.random()*centerInput.getMax()));
        scaleInput.setProgress((int)(Math.random()*scaleInput.getMax()));
        rotateCenterInput.setProgress((int)(Math.random()*rotateCenterInput.getMax()));
        //mirrorInput.setProgress((int)(Math.random()*mirrorInput.getMax())/4);
        skewInput.setProgress((int)(Math.random()*skewInput.getMax()/2));
        skewCenterInput.setProgress((int)(Math.random()*skewCenterInput.getMax()/2));
        color = Color.argb(255, r.nextInt(256), r.nextInt(256), r.nextInt(256));
    }
    public void randomize(View v){
        randomize();
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
        matrix.setRotate((invert)*rotate*j,w/2+rotateCenter, h/2+rotateCenter);
        float centerX;
        float centerY;
        if(isTouching){
            centerX = imageX;
            centerY = imageY;
        }
        else {
            centerX = w / 2 + center;
            centerY = h / 2 + center;
        }
        matrix.postScale(scale,scale,centerX,centerY);
        matrix.postTranslate(w/offset,h/offset);
        matrix.postSkew(skew, skew, skewCenter,skewCenter);

        Matrix flipx = new Matrix();
        flipx.setScale(-1,1);//-(mirror/w/2),1f);
        flipx.postTranslate(w,0);//+mirror/w/2,0);

        Matrix flipy = new Matrix();
        flipy.setScale(1,-1);//-(mirror/h/2));
        flipy.postTranslate(0,h);//+mirror/h/2);

        Matrix zoom = new Matrix();
        zoom.setRotate(mirror/(w*h)*j,w/2,h/2);
        zoom.postScale(1+mirror/w,1+mirror/h,w/2,h/2);
        canvas.drawBitmap(overlay,zoom,p);
        canvas.drawBitmap(img, matrix, p);
        if(mirrorIter>0){
            flowerMirror(overlay,canvas,p,mirrorIter,w/2,h/2,flowerMirrorIter);
            flowerMirrorIter++;
            if(flowerMirrorIter>mirrorIter)
                flowerMirrorIter=1;
        }
        canvas.drawBitmap(overlay,flipx,p);
        canvas.drawBitmap(overlay,flipy,p);

        j++;
    }

    public void flowerMirror(Bitmap bitmap,Canvas canvas,Paint p,int iterations,float cx, float cy,int j){
        Matrix m=new Matrix();
        m.setRotate((float)(1.0*Math.PI*((j*1.0)/iterations)),cx,cy);
        m.postScale((float)(1.0-((j*1.0)/iterations)),(float)(1.0-((j*1.0)/iterations)),bitmap.getWidth(),bitmap.getHeight());
        //m.postTranslate(bitmap.getWidth(),bitmap.getHeight());
        canvas.drawBitmap(bitmap,m,p);
    }

    public void saveVideo(){
        File file = new File(getExternalFilesDir(null), File.separator+"tmp");
        if(!file.exists()){
            file.mkdirs();
        }
        File file2 = new File(file,"output.mp4");

        String path = file2.getPath();
        Log.e("Video",path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Video",e.getLocalizedMessage());
        }
        try {
            out = NIOUtils.writableFileChannel(path);
            if(out.isOpen()){
                Log.e("Video","channel open");
            }
            encoder = new AndroidSequenceEncoder(out, Rational.R(25, 1));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("Video",e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Video",e.getLocalizedMessage());
        }
        for(int i = 0; i <frames.size(); i++){
            try {
                encoder.encodeImage(frames.get(i));
                Log.e("Video","encoded "+ i + " of " + frames.size());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Video",e.getLocalizedMessage());
                i--;
            } catch (Exception e){
                e.printStackTrace();
                Log.e("Video",e.toString());
                i--;
            }
        }
        try {
            encoder.finish();
            Log.e("Video","Finished");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Video",e.getLocalizedMessage());
        } catch (Exception e){
            e.printStackTrace();
            Log.e("Video",e.getLocalizedMessage());
        } finally {
            NIOUtils.closeQuietly(out);
            Log.e("Video","closed");
        }
    }

    public void feedback(View v){

        if(running){
            running=false;
            return;
        }
        isDefault=false;
        drawFrame=new Thread(new Runnable() {
            public void run() {
                running=true;
                if(saveFrames){
                    if(frames != null){
                        frames.clear();
                    }
                    frames = new ArrayList<>();
                }
                imgView = (ImageView) findViewById(R.id.image_view);
                for (int i = 0; i < iter; i++) {
                    if(running) {
                        process(imgView);
                        imgView.post(new Runnable() {
                                public void run() {
                                    imgView.setImageBitmap(overlay);
                                    if(saveFrames) {
                                        frames.add(overlay.copy(Bitmap.Config.ARGB_8888, true));
                                    }
                                    if(randomFrameNum>0){
                                        if(currentRandomFrame==randomFrameNum) {
                                            randomize();
                                            currentRandomFrame=0;
                                        }
                                        else{
                                            currentRandomFrame++;
                                        }
                                    }
                                }
                            });
                    }
                    if(delay>0) {
                        try {
                            drawFrame.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                running=false;
                if(saveFrames){
                    saveVideo();
                }
            }

        });
        drawFrame.start();
        running=false;
    }
    public void refresh(View v){
        if(running) {
            running = false;
        }
        isDefault=true;
        imgView.setImageBitmap(original);
        j=0;
        if((int)(Math.random()*5)==1) {
            Log.e("Ads",""+mInterstitialAd.isLoaded());
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
        }
    }
    public void saveImage(View v){
        if(isDefault){
            Toast.makeText(MainActivity.this,"You must manipulate the image first!",Toast.LENGTH_SHORT).show();
            return;
        }
        running=false;
        getPermissionSave();
    }
    public void getPermissionSave(){
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
                    REQUEST_SAVE_IMAGE);
        }
        else{
            galleryAddPic(((BitmapDrawable)imgView.getDrawable()).getBitmap());
            Toast.makeText(MainActivity.this,"Saved image to gallery!", Toast.LENGTH_SHORT).show();
        }
    }
    public void getPermissionOpen(){
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
                    REQUEST_OPEN_IMAGE);
        }
        else{
            dispatchVideoFromGalleryIntent();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SAVE_IMAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted, yay!
                    galleryAddPic(((BitmapDrawable)imgView.getDrawable()).getBitmap());
                    Toast.makeText(MainActivity.this,"Saved image to gallery!", Toast.LENGTH_SHORT).show();
                    return;

                } else {
                    // permission denied, boo!
                    Toast.makeText(MainActivity.this,"Cannot save to gallery without permission.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case REQUEST_OPEN_IMAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted, yay!
                    dispatchVideoFromGalleryIntent();
                    return;

                } else {
                    // permission denied, boo!
                    Toast.makeText(MainActivity.this,"Cannot open from gallery without permission.", Toast.LENGTH_SHORT).show();
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
        imgDir= f;

        try {
            out = new FileOutputStream(f);
            img.compress(Bitmap.CompressFormat.JPEG, 50, out);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA,f.getAbsolutePath());
            values.put(MediaStore.Images.Media.DESCRIPTION,"Created with Fractal Feedback app.");
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
        askToUpload();
    }
    public void askToUpload(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); //new alert dialog
        TextView title=new TextView(MainActivity.this);
        title.setText("Upload your creation!");
        title.setTextSize(getResources().getDimension(R.dimen.textsize));
        title.setPadding(50,50,50,50);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            title.setTextColor(getColor(R.color.white));
        }
        else{
            title.setTextColor(getResources().getColor(R.color.white));
        }
        //builder.setCustomTitle(title); //dialog title

        LayoutInflater inflater = (LayoutInflater)MainActivity.this.getSystemService (Context.LAYOUT_INFLATER_SERVICE); //needed to display custom layout
        final View textBoxes=inflater.inflate(R.layout.upload_dialog_layout,null); //custom layout file now a view object
        builder.setView(textBoxes); //set view to custom layout
        final EditText name = (EditText)textBoxes.findViewById(R.id.upload_name);
        final EditText creationName = (EditText)textBoxes.findViewById(R.id.upload_creation_name);
        final CheckBox anon = (CheckBox)textBoxes.findViewById(R.id.anonymous_upload);
        if(mAuth.getCurrentUser()!=null){
            name.setText(mAuth.getCurrentUser().getDisplayName());
        }
        anon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    name.setVisibility(View.INVISIBLE);
                }
                else{
                    name.setVisibility(View.VISIBLE);
                }
            }
        });
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(anon.isChecked()){
                    uploadName="Anonymous";
                    uploadCreationName=creationName.getText().toString();
                }
                else{
                    uploadName=name.getText().toString();
                    uploadCreationName=creationName.getText().toString();
                }
                if(mAuth.getCurrentUser()==null){
                    upload=true;
                    Intent intent = new Intent(MainActivity.this, SignIn.class);
                    startActivity(intent);
                }
                else{
                    uploadImage();
                }
                askToShare();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                askToShare();
            }
        });
        builder.show().getWindow().setBackgroundDrawableResource(R.color.background);
    }
    public void askToShare(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imgDir));
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Made with Fractal Feedback Simulator: https://play.google.com/store/apps/details?id=com.jumproper.fractalfeedback\n" +
                                                "Follow us on Instagram: https://www.instagram.com/videofeedbackapp/\n" +
                                                "Tag your photos #videofeedback or tag us @videofeedbackapp to get featured on our page!");
        shareIntent.setType("image/jpeg");
        Intent choose=Intent.createChooser(shareIntent,"Share your creation!");
        startActivity(choose);
    }
    public void uploadImage(){
        Bitmap bitmap = ((BitmapDrawable) imgView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] data = baos.toByteArray();
        StorageReference uploadRef=storageRef.child(mAuth.getCurrentUser().getUid()).child("feedback_"+System.currentTimeMillis());
        Log.e("Upload","Path "+uploadRef.getPath());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("user", mAuth.getCurrentUser().getEmail())
                .setCustomMetadata("name", uploadName)
                .setCustomMetadata("description", uploadCreationName)
                .build();
        UploadTask uploadTask=uploadRef.putBytes(data,metadata);
        fileSize=uploadTask.getSnapshot().getTotalByteCount();
        final NotificationManager mNotifyManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Uploading image")
                .setContentText("Upload in progress...")
                .setSmallIcon(R.drawable.icon);
        Toast.makeText(MainActivity.this,"Starting upload.  You can see its progress in the notification bar.",Toast.LENGTH_LONG).show();
        mBuilder.setProgress((int)fileSize, 0, false);
        mNotifyManager.notify(notifyId, mBuilder.build()); //display notification
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Sorry your image could not be uploaded at this time.",Toast.LENGTH_SHORT).show();
                Log.e("Upload",e.toString()+" "+e.getCause().toString());
                Log.e("Upload","Auth: "+mAuth.getCurrentUser().getEmail());
            }
        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                fileSize=taskSnapshot.getTotalByteCount();
                mBuilder.setProgress((int)fileSize, (int)taskSnapshot.getBytesTransferred(), false);
                mNotifyManager.notify(notifyId, mBuilder.build()); //display notification
            }
        });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mBuilder.setProgress(0,0,false);
                mBuilder.setContentText("Upload complete, thank you!");
                mNotifyManager.notify(notifyId, mBuilder.build()); //display notification
                Toast.makeText(MainActivity.this,"Thank you for your creation! Check it out on the 'new' section of the public gallery!",Toast.LENGTH_SHORT).show();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("images").child(mAuth.getCurrentUser().getUid());
                String key=myRef.push().getKey();
                ImageData imageData=new ImageData(uploadName,uploadCreationName,0,taskSnapshot.getDownloadUrl().toString(),System.currentTimeMillis(),key,mAuth.getCurrentUser().getUid());
                myRef.child(key).setValue(imageData);
            }
        });
    }

    public void clearImage(View v){
        j=0;
        running=false;
        imgView.setVisibility(View.INVISIBLE);
        openImage.setVisibility(View.VISIBLE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                if(bitmap.getWidth()*bitmap.getHeight()>(1920*1080)){
                    bitmap=resize(bitmap,1);
                }
                imgView.setVisibility(View.VISIBLE);
                openImage.setVisibility(View.INVISIBLE);
                imgView.setImageBitmap(bitmap);
                original = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                openedResize=true;
                qualityInput.setProgress(resizeRatio);
                qualityCount.setText(""+resizeRatio);

                openedResize=false;
            } catch (IOException e) {
                Toast.makeText(MainActivity.this,"Could not load image",Toast.LENGTH_SHORT);
                e.printStackTrace();
            }
        }
    }
    public void openImage(View v){
        isDefault=true;
        getPermissionOpen();
    }
    private void dispatchVideoFromGalleryIntent(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_GALLERY_IMAGE);
    }
    public Bitmap resize(Bitmap img,int scale){
        int newW=(int)(img.getWidth()/(1+scale/20.0));
        int newH=(int)(img.getHeight()/(1+scale/20.0));
        Log.e("resize","quality:"+quality);
        if(newH*newW>1920*1080*quality){
            Log.e("resize","w="+newW+" h="+newH);
            return resize(img,(scale+1));
        }
        resizeRatio = (int)((newW*1.0/w + newH*1.0/h)/2 * 100);
        if(resizeRatio>100){
            resizeRatio=100;
        }
        if(newW % 2 != 0){
            newW += 1;
        }
        if(newH % 2 != 0){
            newH += 1;
        }
        Log.e("Video","w="+newW+" h="+newH+"ratio="+resizeRatio);

        return Bitmap.createScaledBitmap(img,newW,newH,false);
    }
    public void selectColor(View v){
        android.support.v7.app.AlertDialog pallete = ColorPickerDialogBuilder
                .with(this)
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {

                    }
                })
                .setPositiveButton("Select", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        color=selectedColor;
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build();
        pallete.getWindow().setBackgroundDrawableResource(R.color.background);
        pallete.show();
    }

}
