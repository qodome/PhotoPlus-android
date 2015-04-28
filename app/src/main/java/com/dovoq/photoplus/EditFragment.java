package com.dovoq.photoplus;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EditFragment extends Fragment {
    private Bitmap show = null;
    private boolean initDone = false;

    public void recycleBitmap() {
        if (show != null) {
            show.recycle();
            show = null;
        }
    }

    public void setBitmap(final Bitmap b) {
        show = b;
        if (initDone == true) {
            ((SquareImageView) (findViewById(R.id.photo_grid_view))).setImageBitmap(show);
        }
    }

    public void init(final Bundle savedInstanceState) {
        if (show != null) {
            ((SquareImageView) (findViewById(R.id.photo_grid_view))).setImageBitmap(show);
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
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        return view;
    }
}
