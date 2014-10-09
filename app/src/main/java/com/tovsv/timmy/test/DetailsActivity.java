package com.tovsv.timmy.test;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tovsv.timmy.R;
import com.tovsv.timmy.util.DLog;

public class DetailsActivity extends Activity {

    private FadingImageView mImageView;
    private PackageManager mPackageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        String packageName = "";
        Bundle extras = getIntent().getExtras();
        if(extras != null)
            packageName = extras.getString("package_name");
        DLog.i(packageName);
        mPackageManager = this.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = mPackageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Drawable drawable = packageInfo.applicationInfo.loadIcon(mPackageManager);
        Bitmap album = ((BitmapDrawable) drawable).getBitmap();

        ColorArt colorArt = new ColorArt(album);

        mImageView = (FadingImageView) findViewById(R.id.image);
        mImageView.setImageBitmap(album);
        mImageView.setBackgroundColor(colorArt.getBackgroundColor(), FadingImageView.FadeSide.LEFT);
        mImageView.setFadeEnabled(false);

        View container = findViewById(R.id.container);
        container.setBackgroundColor(colorArt.getBackgroundColor());

        TextView primary = (TextView) findViewById(R.id.primary);
        primary.setTextColor(colorArt.getPrimaryColor());
        TextView secondary = (TextView) findViewById(R.id.secondary);
        secondary.setTextColor(colorArt.getSecondaryColor());
        TextView detail = (TextView) findViewById(R.id.detail);
        detail.setTextColor(colorArt.getDetailColor());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_fade) {
//            if(item.isChecked()){
//                mImageView.setFadeEnabled(true);
//                item.setChecked(false);
//            }else{
//                mImageView.setFadeEnabled(false);
//                item.setChecked(true);
//            }
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
