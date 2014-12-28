package com.tovsv.timmy.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;



/**
 * Created by kleist on 14-5-19.
 */
public class AsyncIconLoader {

    private LruCache<String, Bitmap> mMemoryCache;
    private PackageManager mPackageManager;
    private Context mContext;


    public AsyncIconLoader(Context context) {
        mPackageManager = context.getPackageManager();
        mContext = context;

        //mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        //mBitmapPaint.setShader(mBitmapShader);
        initCache();
    }

    private void initCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmap(String packageName, ImageView imageView) {
        Bitmap bitmap = getBitmapFromMemCache(packageName);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        } else {
            asyncLoadIcon(packageName, imageView);
        }
    }

    public void loadIcon(String packageName, ImageView imageView) {
        try {
            PackageInfo packageInfo = mPackageManager.getPackageInfo(packageName, 0);
            Picasso.with(mContext)
                    .load(packageInfo.applicationInfo.icon)
                    .into(imageView);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void asyncLoadIcon(String packageName, final ImageView imageView) {
        AsyncIconLoadTask task = new AsyncIconLoadTask(packageName, imageView);
        task.execute();
    }

    private Bitmap getIcon(String packageName) {
        try {
            PackageInfo packageInfo = mPackageManager.getPackageInfo(packageName, 0);
            Drawable icon = packageInfo.applicationInfo.loadIcon(mPackageManager);

            Bitmap APKicon;
            if(icon instanceof BitmapDrawable) {
                APKicon  = ((BitmapDrawable)icon).getBitmap();
            } else{
                Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(),icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                icon.draw(canvas);
                APKicon = bitmap;
            }

//            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//            Canvas canvas = new Canvas();
//            int mDrawableRadius = Math.min(bitmap.getHeight() / 2, bitmap.getWidth() / 2);
//            BitmapShader mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            Paint mBitmapPaint = new Paint();
//            mBitmapPaint.setShader(mBitmapShader);
//            mBitmapPaint.setAntiAlias(true);
//            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, mDrawableRadius, mBitmapPaint);

//            if (mBorderWidth != 0) {
//                canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBorderRadius, mBorderPaint);
//            }

            return APKicon;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }

    }

    public Bitmap roundAvatar(Bitmap src) {
        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        final Canvas canvas = new Canvas(result);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Rect rect = new Rect(0, 0, src.getWidth(), src.getHeight());
        paint.setColor(Color.BLACK);
        canvas.drawCircle(src.getWidth() / 2f, src.getHeight() / 2f, src.getWidth() / 2f * 0.9f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, rect, rect, paint);
        return result;
    }

    private class AsyncIconLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String packageName;
        private ImageView imageView;

        public AsyncIconLoadTask(String packageName, ImageView imageView) {
            this.packageName = packageName;
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageView.setTag(packageName);
            // imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.transparent));

        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            return roundAvatar(getIcon(packageName));
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                addBitmapToMemoryCache(packageName, bitmap);
                if (packageName.equals(imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
