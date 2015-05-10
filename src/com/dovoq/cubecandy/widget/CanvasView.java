package com.dovoq.cubecandy.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CanvasView extends FrameLayout {

	public CanvasView(Context context) {
		super(context);
	}

	public CanvasView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
	}
}
