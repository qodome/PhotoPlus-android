package com.dovoq.photoplus;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Environment;
import android.util.Log;

import com.google.common.base.Objects;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public class OverlayManager {
    private final String[] bgDesc = { "bg0_0", "bg2_e6e6e6", 
    								"bg10_0", "bg20_0", 
    								"bg30_0", "bg40_0", 
    								"bg50_ffffff", "bg60_ffffff", 
    								"bg70_ffffff", "bg80_ffffff", 
    								"bg90_ffffff" };
    private final Integer[] bgColor = { Integer.valueOf(0xFF000000), Integer.valueOf(0xFFe6e6e6), 
    									Integer.valueOf(0xFF000000), Integer.valueOf(0xFF000000), 
    									Integer.valueOf(0xFF000000), Integer.valueOf(0xFF000000), 
    									Integer.valueOf(0xFFFFFFFF), Integer.valueOf(0xFFFFFFFF), 
    									Integer.valueOf(0xFFFFFFFF), Integer.valueOf(0xFFFFFFFF), 
    									Integer.valueOf(0xFFFFFFFF)};
    private final int barcodeSize = 128;
    private final int boarcodeBoarderGap = 32;
    private int bgIdx = 0;
    private Typeface[] textTF;
    private int tfIdx = 0;
    private Bitmap gridMap;
    private Bitmap photoMap;
    private Bitmap textMapDefault;
    private Bitmap textMap;
    private Bitmap appIntroMap;
    private List<Bitmap> bg;
    private List<Bitmap> cardFrame;
    private Bitmap noShareCardFrame;
    private Activity parent;
    private CharSequence textCS;
    private String folderName;
    private boolean resetState = true;

    public OverlayManager(final Activity activity) {
        parent = activity;
        photoMap = null;
        bg = new ArrayList<Bitmap>();
        for (final String desc : bgDesc) {
            int id = parent.getResources().getIdentifier(desc, "drawable", parent.getPackageName());
			Bitmap bitmapElement = ((BitmapDrawable)(parent.getResources().getDrawable(id))).getBitmap();
            int[] elementArray = Utils.getIntArray(bitmapElement.getWidth() * bitmapElement.getHeight());
            bitmapElement.getPixels(elementArray, 0, bitmapElement.getWidth(), 0, 0, bitmapElement.getWidth(), bitmapElement.getWidth());
            int[] finalArray = Utils.getIntArray((bitmapElement.getWidth() * 3) * (bitmapElement.getHeight() * 3));
            for (int row = 0; (row < bitmapElement.getHeight()); row++) {
                for (int i = 0; (i < 3); i++) {
                    for (int j = 0; (j < 3); j++) {
                        System.arraycopy(elementArray, (row * bitmapElement.getWidth()), finalArray, ((i * (bitmapElement.getHeight()) + row) * (bitmapElement.getWidth() * 3) + j * bitmapElement.getWidth()), bitmapElement.getWidth());
                    }
                }
            }
            Bitmap finalMap = Bitmap.createBitmap(finalArray, (bitmapElement.getWidth() * 3), (bitmapElement.getHeight() * 3), Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true);
            bg.add(finalMap);
        }
        int[] gridArray = Utils.getIntArray(bg.get(0).getWidth() * bg.get(0).getHeight());
        for (int i = 0; (i < (bg.get(0).getWidth() * bg.get(0).getHeight())); i++) {
            gridArray[i] = 0x00000000;
        }
        for (int i = 0; (i < bg.get(0).getHeight()); i++) {
            gridArray[i * bg.get(0).getWidth() + (bg.get(0).getWidth() / 3 - 1)] = 0xFFFFFFFF;
            gridArray[i * bg.get(0).getWidth() + (bg.get(0).getWidth() / 3)] = 0xFFFFFFFF;
            gridArray[i * bg.get(0).getWidth() + (2 * (bg.get(0).getWidth() / 3) - 1)] = 0xFFFFFFFF;
            gridArray[i * bg.get(0).getWidth() + (2 * (bg.get(0).getWidth() / 3))] = 0xFFFFFFFF;
        }
        for (int i = 0; (i < bg.get(0).getWidth()); i++) {
            gridArray[(bg.get(0).getHeight() / 3 - 1) * bg.get(0).getWidth() + i] = 0xFFFFFFFF;
            gridArray[(bg.get(0).getHeight() / 3) * bg.get(0).getWidth() + i] = 0xFFFFFFFF;
            gridArray[(2 * (bg.get(0).getHeight() / 3) - 1) * bg.get(0).getWidth() + i] = 0xFFFFFFFF;
            gridArray[(2 * (bg.get(0).getHeight() / 3)) * bg.get(0).getWidth() + i] = 0xFFFFFFFF;
        }
        gridMap = Bitmap.createBitmap(gridArray, bg.get(0).getWidth(), bg.get(0).getHeight(), Bitmap.Config.ARGB_8888);
        for (int i = 0; (i < (bg.get(0).getWidth() * bg.get(0).getHeight())); i++) {
            gridArray[i] = 0x00000000;
        }
        Bitmap bg0 = ((BitmapDrawable)(parent.getResources().getDrawable(R.drawable.bg0))).getBitmap();
        Bitmap bg1 = ((BitmapDrawable)(parent.getResources().getDrawable(R.drawable.bg1))).getBitmap();
        Bitmap bg8 = ((BitmapDrawable)(parent.getResources().getDrawable(R.drawable.bg8))).getBitmap();
        cardFrame = new ArrayList<Bitmap>(Arrays.asList(bg0, bg1, bg0, bg1, bg0, bg1, bg0, bg1, bg8));
        noShareCardFrame = ((BitmapDrawable)(parent.getResources().getDrawable(R.drawable.bg))).getBitmap();
        appIntroMap = ((BitmapDrawable)(parent.getResources().getDrawable(R.drawable.app_introduce))).getBitmap();
		textMapDefault = Bitmap.createBitmap(gridArray, bg.get(0).getWidth(), bg.get(0).getHeight(), Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true);
        textTF = new Typeface[] { Typeface.DEFAULT
                                ,Typeface.createFromAsset(parent.getAssets(), "fonts/hkwwt.TTF")
                                ,Typeface.createFromAsset(parent.getAssets(), "fonts/xjl.ttf")
                                ,Typeface.createFromAsset(parent.getAssets(), "fonts/whxw.ttf")
                                ,Typeface.createFromAsset(parent.getAssets(), "fonts/fzjt.TTF")
                                ,Typeface.createFromAsset(parent.getAssets(), "fonts/ys.otf")};
        textCS = new String("");
		folderName = new String(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhotoPlus/");
        resetState = true;
    }

    public void toggleTF() {
        tfIdx++;
        if (tfIdx >= textTF.length) {
            tfIdx = 0;
        }
    }

    public void toggleBG() {
        bgIdx++;
        if (bgIdx >= bg.size()) {
            bgIdx = 0;
        }
    }

    public void reset() {
        if (photoMap != null) {
            photoMap.recycle();
            photoMap = null;
        }
        resetState = true;
    }

    public Bitmap disableGrid(final Bitmap bp, final int i) {
		int row = i / 3;
		int col = i % 3;
        int[] tmpArray = Utils.getIntArray(bp.getWidth() * bp.getHeight());
        bp.getPixels(tmpArray, 0, bp.getWidth(), col * (bp.getWidth() / 3), row * (bp.getHeight() / 3), (bp.getWidth() / 3), (bp.getHeight() / 3));
        for (int intVal = 0; (intVal < tmpArray.length); intVal++) {
    		tmpArray[intVal] = Utils.getMaskedValue(tmpArray[intVal], 0x00FFFFFF);
        }
    	bp.setPixels(tmpArray, 0, bp.getWidth(), col * (bp.getWidth() / 3), row * (bp.getHeight() / 3), (bp.getWidth() / 3), (bp.getHeight() / 3));
        return bp;
    }

    public void inputString(final CharSequence input) {
        resetState = false;
        textCS = new String(input.toString());
        Log.i("PhotoPlus", "Input string: " + textCS.toString());
    }

    public Bitmap getBitmapForDraw(final boolean withGrid) {
        Drawable[] layers = null;
        Bitmap bgMap = null;
        int outputWidth = bg.get(0).getWidth();
        int outputHeight = bg.get(0).getHeight();
        if ((resetState == true)) {
            layers = new Drawable[] {new BitmapDrawable(parent.getResources(), appIntroMap), new BitmapDrawable(parent.getResources(), gridMap)};
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            Bitmap b = Bitmap.createBitmap(appIntroMap.getWidth(), appIntroMap.getHeight(), Bitmap.Config.ARGB_8888);
            layerDrawable.setBounds(0, 0, appIntroMap.getWidth(), appIntroMap.getHeight());
            layerDrawable.draw(new Canvas(b));
            return b;
        }
        bgMap = bg.get(bgIdx).copy(Bitmap.Config.ARGB_8888, true);
        textMap = textMapDefault.copy(Bitmap.Config.ARGB_8888, true);
        for (int i = 0; (i < 9); i++) {
            if (i < textCS.length() && !Character.isWhitespace(textCS.charAt(i))) {
                Canvas canvas = new Canvas(textMap);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(bgColor[bgIdx]);
                paint.setTextSize(64);
                paint.setShadowLayer(1f, 0f, 1f, Color.TRANSPARENT);
                paint.setTypeface(textTF[tfIdx]);
                Rect bounds = new Rect();
                paint.getTextBounds(Character.valueOf(textCS.charAt(i)).toString(), 0, 1, bounds);
                final int row = (i / 3);
                final int col = (i % 3);
                int x = col * (bg.get(0).getWidth() / 3) + ((bg.get(0).getWidth() / 3) - bounds.width())/2;
                int y = row * (bg.get(0).getHeight() / 3) + ((bg.get(0).getHeight() / 3) + bounds.height())/2;
                canvas.drawText(Character.valueOf(textCS.charAt(i)).toString(), x, y, paint);
            } else if (i < textCS.length() && Character.isWhitespace(textCS.charAt(i))) {
                bgMap = disableGrid(bgMap, i);
            } else {
                bgMap = disableGrid(bgMap, i);
            }
        }

        if (!Objects.equal(photoMap, null)) {
            if ((withGrid == true)) {
                layers = new Drawable[] { new BitmapDrawable(parent.getResources(), photoMap), new BitmapDrawable(parent.getResources(), bgMap), new BitmapDrawable(parent.getResources(), textMap), new BitmapDrawable(parent.getResources(), gridMap) };
            } else {
                layers = new Drawable[] { new BitmapDrawable(parent.getResources(), photoMap), new BitmapDrawable(parent.getResources(), bgMap), new BitmapDrawable(parent.getResources(), textMap) };
            }
            if (photoMap.getWidth() > outputWidth) {
                outputWidth = photoMap.getWidth();
            }
            if (photoMap.getHeight() > outputHeight) {
                outputHeight = photoMap.getHeight();
            }
        } else {
            if ((withGrid == true)) {
                layers = new Drawable[] { new BitmapDrawable(parent.getResources(), bgMap), new BitmapDrawable(parent.getResources(), textMap), new BitmapDrawable(parent.getResources(), gridMap) };
            } else {
                layers = new Drawable[] { new BitmapDrawable(parent.getResources(), bgMap), new BitmapDrawable(parent.getResources(), textMap) };
            }
        }
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        Bitmap b = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888);
        layerDrawable.setBounds(0, 0, outputWidth, outputHeight);
        layerDrawable.draw(new Canvas(b));
        return b;
    }

    public void recyclePhoto() {
        if (photoMap != null) {
            photoMap.recycle();
            photoMap = null;
        }
    }

    public void setPhoto(final Bitmap photo) {
        resetState = false;
        photoMap = photo;
    }

    public String getUniqueID() {
        Calendar c = Calendar.getInstance();
        long sec = (c.getTimeInMillis() + c.getTimeZone().getOffset(c.getTimeInMillis())) / 1000L;
        Random r = new Random();
        return "a" + String.valueOf((sec - MainActivity.SECONDS_OFFSET)) + Integer.valueOf(r.nextInt(10)) + Integer.valueOf(r.nextInt(10)) + Integer.valueOf(r.nextInt(10));
    }

    public Bitmap addIDtoBitmap(final Bitmap b, final String id) {
        Canvas canvas = new Canvas(b);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFF000000);
        paint.setTextSize(20);
        paint.setShadowLayer(1f, 0f, 1f, Color.TRANSPARENT);
        canvas.drawText(id, 217, 712, paint);
        return b;
    }

    public void dumpSearchResultToFile(final Bitmap sharedBitmap) {
        Bitmap bitContent = Bitmap.createBitmap(sharedBitmap, 0, (cardFrame.get(0).getHeight() - cardFrame.get(0).getWidth()) / 2, cardFrame.get(0).getWidth(), cardFrame.get(0).getWidth(), null, false);
        Bitmap b = Bitmap.createScaledBitmap(bitContent, (cardFrame.get(0).getWidth() * 3), (cardFrame.get(0).getWidth() * 3), false);
        int[] intArray = Utils.getIntArray(b.getWidth() * b.getHeight());
        b.getPixels(intArray, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        Bitmap output = null;
        File fn = null;
        FileOutputStream out = null;
        for (int i = 0; (i < 3); i++) {
            for (int j = 0; (j < 3); j++) {
                if (((((i * 3) + j) % 2) == 1)) {
                    output = sharedBitmap.copy(Bitmap.Config.ARGB_8888, true);
                } else {
                    output = cardFrame.get((i * 3) + j).copy(Bitmap.Config.ARGB_8888, true);
                }
				output.setPixels(intArray, (i * cardFrame.get(0).getWidth() * cardFrame.get(0).getWidth() * 3  + j * cardFrame.get(0).getWidth()), (cardFrame.get(0).getWidth() * 3), 0, (cardFrame.get(0).getHeight() - cardFrame.get(0).getWidth()) / 2, cardFrame.get(0).getWidth(), cardFrame.get(0).getWidth());
                fn = new File(folderName + "test" + Integer.valueOf(i) + Integer.valueOf(j) + ".png");
                if (!fn.exists()) {
                    try {
						fn.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                try {
					out = new FileOutputStream(folderName + "test" + Integer.valueOf(i) + Integer.valueOf(j) + ".png");
	                output.compress(Bitmap.CompressFormat.PNG, 100, out);
	                out.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }

    public String dumpToFile(final boolean enableShared) throws IOException, WriterException {
        Bitmap b = Bitmap.createScaledBitmap(getBitmapForDraw(false), (cardFrame.get(0).getWidth() * 3), (cardFrame.get(0).getWidth() * 3), false);
        int[] intArray = Utils.getIntArray(b.getWidth() * b.getHeight());
        int[] barArray = Utils.getIntArray((barcodeSize * barcodeSize));
        b.getPixels(intArray, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        String id = getUniqueID();
        Bitmap output = null;
        Bitmap barcode = null;
        File fn = null;
        FileOutputStream out = null;
        Log.i("PhotoPlus", id);
        for (int i = 0; (i < 3); i++) {
            for (int j = 0; (j < 3); j++) {
                if (enableShared) {
                    output = cardFrame.get((i * 3) + j).copy(Bitmap.Config.ARGB_8888, true);
                } else {
                    output = noShareCardFrame.copy(Bitmap.Config.ARGB_8888, true);
                }
                output.setPixels(intArray, (i * cardFrame.get(0).getWidth() * cardFrame.get(0).getWidth() * 3  + j * cardFrame.get(0).getWidth()), (cardFrame.get(0).getWidth() * 3), 0, (cardFrame.get(0).getHeight() - cardFrame.get(0).getWidth()) / 2, cardFrame.get(0).getWidth(), cardFrame.get(0).getWidth());
                if (((i * 3) + j) % 2 == 1 && enableShared) {
					barcode = encodeAsBitmap(id, BarcodeFormat.QR_CODE, barcodeSize, barcodeSize);
                    barcode.getPixels(barArray, 0, barcode.getWidth(), 0, 0, barcodeSize, barcodeSize);
					output.setPixels(barArray, 0, barcodeSize, boarcodeBoarderGap, (cardFrame.get(0).getHeight() - boarcodeBoarderGap - barcodeSize), barcodeSize, barcodeSize);
                    output = addIDtoBitmap(output, ("转发ID: " + id));
                }
                fn = new File(((((folderName + "test") + Integer.valueOf(i)) + Integer.valueOf(j)) + ".png"));
                if (!fn.exists()) {
                    fn.createNewFile();
                }
                out = new FileOutputStream(folderName + "test" + Integer.valueOf(i) + Integer.valueOf(j) + ".png");
                output.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            }
        }
        if ((enableShared == true)) {
            b = Bitmap.createScaledBitmap(getBitmapForDraw(false), cardFrame.get(1).getWidth(), cardFrame.get(1).getWidth(), false);
            intArray = Utils.getIntArray(b.getWidth() * b.getHeight());
            b.getPixels(intArray, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
			output = cardFrame.get(1).copy(Bitmap.Config.ARGB_8888, true);
			output.setPixels(intArray, 0, cardFrame.get(1).getWidth(), 0, (cardFrame.get(1).getHeight() - cardFrame.get(1).getWidth()) / 2, cardFrame.get(1).getWidth(), cardFrame.get(1).getWidth());

			barcode = encodeAsBitmap(id, BarcodeFormat.QR_CODE, barcodeSize, barcodeSize);
			barcode.getPixels(barArray, 0, barcode.getWidth(), 0, 0, barcodeSize, barcodeSize);
			output.setPixels(barArray, 0, barcodeSize, boarcodeBoarderGap, (cardFrame.get(0).getHeight() - boarcodeBoarderGap - barcodeSize), barcodeSize, barcodeSize);
			output = addIDtoBitmap(output, "转发ID: " + id);
            fn = new File(((folderName + id) + ".jpg"));
            if (!fn.exists()) {
                fn.createNewFile();
            }
            out = new FileOutputStream(((folderName + id) + ".jpg"));
            output.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            return (id + ".jpg");
        } else {
            return null;
        }
    }

    public Bitmap encodeAsBitmap(final String contents, final BarcodeFormat format, final int img_width, final int img_height) throws WriterException {
        String encoding = "UTF-8";
        EnumMap<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, encoding);
        hints.put(EncodeHintType.MARGIN, Integer.valueOf(0));
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
		result = writer.encode(contents, format, img_width, img_height, hints);
	    int width = result.getWidth();
	    int height = result.getHeight();
        int[] pixels = Utils.getIntArray((width * height));
        int value = 0;
        for (int y = 0; (y < height); y++) {
            {
                int offset = (y * width);
                for (int x = 0; (x < width); x++) {
                    if (result.get(x, y)) {
                        value = 0xFF000000;
                    } else {
                        value = 0xFFFFFFFF;
                    }
                    pixels[(offset + x)] = value;
                }
            }
        }
        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        ret.setPixels(pixels, 0, width, 0, 0, width, height);
        return ret;
    }
}
