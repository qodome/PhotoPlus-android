package com.qodome.photoplus_android;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.qodome.photoplus_android.R;
import com.qodome.photoplus_android.SquareImageView;

public class EditFragment extends Fragment {
    private Bitmap show = null;
    private boolean initDone = false;

    public void setBitmap(final Bitmap b) {
        this.show = b;
        if ((this.initDone == true)) {
            ((SquareImageView)(this.findViewById(R.id.photo_grid_view))).setImageBitmap(this.show);
        }
    }

    public void init(final Bundle savedInstanceState) {
        if (this.show != null) {
            ((SquareImageView)(this.findViewById(R.id.photo_grid_view))).setImageBitmap(this.show);
        }
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
