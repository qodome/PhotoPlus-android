package com.qodome.photoplus_android

import android.widget.ImageView
import android.content.Context
import android.util.AttributeSet

class SquareImageView extends ImageView {	
	new(Context context) {
        super(context)
    }

    new(Context context, AttributeSet attrs) {
        super(context, attrs)
    }

    new(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle)
    }

    override onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()) //Snap to width
    }
}