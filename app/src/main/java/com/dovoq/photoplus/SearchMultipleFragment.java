package com.dovoq.photoplus;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchMultipleFragment extends Fragment {
    private String folder = null;
    private boolean initDone = false;
    public Context mContext = null;
    public ImageAdapter adapter = null;

    private class ImageAdapter extends BaseAdapter {
        private String imgFolder = null;
        private int count = 0;
        public List<ImageView> viewList = new ArrayList<ImageView>();

        public void setImageFolder(String folder) {
            imgFolder = folder;
            File zipFolder = new File(folder);
            if (zipFolder.exists()) {
                File[] files = zipFolder.listFiles();
                count = files.length;
            }
        }

        public int getCount() {
            return count;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(240, 240));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                //imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
                ((BitmapDrawable)imageView.getDrawable()).getBitmap().recycle();
            }
            if (position >= viewList.size()) {
                viewList.add(imageView);
            } else {
                viewList.set(position, imageView);
            }

            File zipFolder = new File(imgFolder);
            File[] files = zipFolder.listFiles();
            imageView.setImageBitmap(BitmapFactory.decodeFile(files[position].getAbsolutePath()));
            return imageView;
        }
    }

    public void recycleBitmap() {
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                ((BitmapDrawable)adapter.viewList.get(i).getDrawable()).getBitmap().recycle();
            }
            adapter = new ImageAdapter();
            GridView gridview = (GridView) findViewById(R.id.search_multiple_view);
            gridview.setAdapter(adapter);
        }
    }

    public void setBitmap(String folderName) {
        folder = folderName;
        if (initDone == true) {
            adapter = new ImageAdapter();
            adapter.setImageFolder(folder);
            GridView gridview = (GridView)findViewById(R.id.search_multiple_view);
            gridview.setAdapter(adapter);
        }
    }

    public void init(final Bundle savedInstanceState) {
        if (folder != null) {
            adapter = new ImageAdapter();
            adapter.setImageFolder(folder);
            GridView gridview = (GridView)findViewById(R.id.search_multiple_view);
            gridview.setAdapter(adapter);
        }
        initDone = true;
    }

    public View findViewById(final int resId) {
        return getView().findViewById(resId);
    }

    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(savedInstanceState);
    }

    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_multiple, container, false);
        return view;
    }
}