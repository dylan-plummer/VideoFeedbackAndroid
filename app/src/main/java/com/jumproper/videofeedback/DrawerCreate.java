package com.jumproper.videofeedback;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.io.InputStream;

/**
 * Created by jumpr_000 on 9/26/2016.
 */

public class DrawerCreate extends AppCompatActivity{
    ProfileDrawerItem currentProfile;
    FirebaseAuth mAuthCopy;
    Drawer result;
    AccountHeader headerResult;
    public void makeDrawer(final Context context, final Activity activity, final FirebaseAuth mAuth, Toolbar toolbar, String title){

        mAuthCopy=mAuth;
        new DrawerBuilder().withActivity(activity).build();
        PrimaryDrawerItem mainMenuItem=new PrimaryDrawerItem().withName("Video Feedback").withIcon(R.drawable.icon);
        PrimaryDrawerItem publicImagesItem=new PrimaryDrawerItem().withName("Public Gallery").withIcon(R.drawable.gallery);
        final PrimaryDrawerItem signIn=new PrimaryDrawerItem()
                .withName("Sign In")
                .withIcon(R.drawable.common_google_signin_btn_icon_dark);


        final PrimaryDrawerItem instagram=new PrimaryDrawerItem()
                .withName("@videofeedbackapp")
                .withIcon(R.drawable.logo)
                .withDescription("#videofeedback to be featured!");
        PrimaryDrawerItem removeAds=new PrimaryDrawerItem().withName("Remove Ads").withIcon(R.drawable.remove);



        if(mAuth.getCurrentUser()!=null){
            DownloadImageTask downloadImage=new DrawerCreate.DownloadImageTask(currentProfile);
            downloadImage.execute(mAuth.getCurrentUser().getPhotoUrl().toString());
            currentProfile=new ProfileDrawerItem()
                    .withName(mAuth.getCurrentUser().getDisplayName())
                    .withEnabled(true)
                    .withEmail(mAuth.getCurrentUser().getEmail());

            signIn.withName("Sign Out");
        }
        else{
            currentProfile=new ProfileDrawerItem()
                    .withName("Sign in")
                    .withEmail("Tap the icon to sign in")
                    .withEnabled(false);

        }



        headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .addProfiles(
                        currentProfile
                )
                .withHeaderBackground(new ColorDrawable(ContextCompat.getColor(context, R.color.colorAccent)))
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        if(mAuth.getCurrentUser()==null) {
                            Intent intent = new Intent(context, SignIn.class);
                            activity.startActivity(intent);
                        }
                        return false;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        if(mAuth.getCurrentUser()==null) {
                            Intent intent = new Intent(context, SignIn.class);
                            activity.startActivity(intent);
                        }
                        return false;
                    }
                })
                .withOnlyMainProfileImageVisible(true)
                .withPaddingBelowHeader(true)
                .build();



        result = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        mainMenuItem,
                        new DividerDrawerItem(),
                        publicImagesItem,
                        new DividerDrawerItem(),
                        signIn,
                        new DividerDrawerItem(),
                        instagram,
                        new DividerDrawerItem(),
                        removeAds,
                        new DividerDrawerItem()
                )
                .withSelectedItem(-1)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if(position==1){
                            Intent intent = new Intent(context, MainActivity.class);
                            if(context.getClass().getName().equals(intent.getComponent().getClassName()))
                                return true;
                            activity.startActivity(intent);
                        }
                        if(position==3){
                            Intent intent = new Intent(context, PublicImages.class);
                            if(context.getClass().getName().equals(intent.getComponent().getClassName()))
                                return true;
                            activity.startActivity(intent);
                        }
                        if(position==5){
                            if(signIn.getName().toString().equals("Sign In")){
                                Intent intent = new Intent(context, SignIn.class);
                                activity.startActivity(intent);
                            }
                            else{
                                mAuth.signOut();
                                Intent intent = new Intent(context, MainActivity.class);
                                MainActivity.ads=true;
                                finish();
                                activity.startActivity(intent);
                            }
                        }
                        if(position==7){
                            Uri uri = Uri.parse("http://www.instagram.com/p/BMAcvb1ArfO/");
                            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

                            likeIng.setPackage("com.instagram.android");

                            try {
                                activity.startActivity(likeIng);
                            } catch (ActivityNotFoundException e) {
                                Log.e("Activity not found",e.toString());
                                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://www.instagram.com/videofeedbackapp/")));
                            }
                        }
                        if(position==9){
                            Intent intent = new Intent(context, RemoveAds.class);
                            if(context.getClass().getName().equals(intent.getComponent().getClassName()))
                                return true;
                            if(mAuth.getCurrentUser()==null){
                                Toast.makeText(context,"Please sign in first", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            else {
                                activity.startActivity(intent);
                            }
                        }

                        return true;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        if((mAuth.getCurrentUser()!=null)&&(currentProfile.getName().toString().equals("Sign in"))){
                            signIn.withName("Sign Out");
                            headerResult.setActiveProfile(currentProfile);
                        }
                        else if((mAuth.getCurrentUser()==null)&&!(currentProfile.getName().toString().equals("Sign in"))){
                            signIn.withName("Sign In");
                            headerResult.setActiveProfile(new ProfileDrawerItem()
                                    .withName("Sign in")
                                    .withEmail("Tap the icon to sign in")
                                    .withEnabled(false));
                        }
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        return;
                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        return;
                    }
                })
                .build();
        toolbar.setTitle(title);
    }
    private class DownloadImageTask extends AsyncTask<String, Void, ProfileDrawerItem> {
        ProfileDrawerItem bmImage;

        public DownloadImageTask(ProfileDrawerItem bmImage) {
            this.bmImage = bmImage;
        }

        protected ProfileDrawerItem doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {

                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                currentProfile=new ProfileDrawerItem()
                        .withName(mAuthCopy.getCurrentUser().getDisplayName())
                        .withEnabled(true)
                        .withEmail(mAuthCopy.getCurrentUser().getEmail())
                        .withIcon(mIcon11);
            } catch (Exception e) {
                currentProfile=new ProfileDrawerItem()
                        .withName(mAuthCopy.getCurrentUser().getDisplayName())
                        .withEnabled(true)
                        .withEmail(mAuthCopy.getCurrentUser().getEmail());
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            return currentProfile;
        }

        protected void onPostExecute(ProfileDrawerItem current) {
            headerResult.setActiveProfile(current);
        }
    }
}
