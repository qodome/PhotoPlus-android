package com.qodome.photoplus_android

import org.xtendroid.annotations.AndroidFragment
import org.xtendroid.app.OnCreate
import android.os.Bundle
import android.graphics.Bitmap
import java.util.List
import java.util.ArrayList
import org.eclipse.xtend.lib.annotations.Accessors
import org.xtendroid.adapter.BeanAdapter
import android.content.Context

@Accessors class GridBitmaps {
  Bitmap pictureView
}

@AndroidFragment(R.layout.fragment_edit) class EditFragment {
	public var List<Bitmap> p
	public var Context c
	
	@OnCreate
    def init(Bundle savedInstanceState) {
		var List<GridBitmaps> gridList = new ArrayList<GridBitmaps>()
		for (var i = 0; i < p.size(); i++) {
			var gridElement = new GridBitmaps()
			gridElement.pictureView = p.get(i)
			gridList.add(gridElement)
		}
		var adapter = new BeanAdapter<GridBitmaps>(c, R.layout.element_edit, gridList)
		photoGridView.adapter = adapter
    }	
	
}