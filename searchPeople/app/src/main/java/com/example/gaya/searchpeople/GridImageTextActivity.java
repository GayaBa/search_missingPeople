package com.example.gaya.searchpeople;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GridImageTextActivity extends BaseAdapter {

    public Context mContext;
    private ArrayList<String> usersName;
    private ArrayList<String> usersBitmaps;

    public GridImageTextActivity(Context context, ArrayList<String> usersName,  ArrayList<String> usersBitmaps) {
        mContext = context;
        this.usersName = usersName;
        this.usersBitmaps = usersBitmaps;
    }

    @Override
    public int getCount() {
        return usersName.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View gridViewAndroid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            gridViewAndroid = new View(mContext);
            gridViewAndroid = inflater.inflate(R.layout.activity_users, null);
            TextView textViewAndroid = (TextView) gridViewAndroid.findViewById(R.id.android_gridview_text);
            ImageView imageViewAndroid = (ImageView) gridViewAndroid.findViewById(R.id.android_gridview_image);
            textViewAndroid.setText(usersName.get(i));
            Picasso.with(mContext)
                    .load(usersBitmaps.get(i))
                    .fit()
                    .error(R.drawable.user)
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .into(imageViewAndroid, new Callback() {
                @Override
                public void onSuccess()
                {
                    // Update card
                }

                @Override
                public void onError()
                {
                    Log.e("App","Failed to load company logo");
                }
            });

         //   imageViewAndroid.setImageBitmap(usersBitmaps.get(i));
        } else {
            gridViewAndroid = (View) convertView;
        }

        return gridViewAndroid;
    }

}