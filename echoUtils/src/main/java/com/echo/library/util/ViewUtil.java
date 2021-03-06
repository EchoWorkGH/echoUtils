package com.echo.library.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.BindingAdapter;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Author:Jenny
 * Date:16/5/16 16:09
 * E-mail:fishloveqin@gmail.com
 * View ???????????????
 **/
@Keep
public final class ViewUtil {
    ///??????bindding android:src???????????????
    @BindingAdapter({"android:src"})
    public static void setImageViewResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    ///??????bindding android:src???????????????
    @BindingAdapter({"android:src"})
    public static void setImageViewResource(ImageView imageView, String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Glide.with(imageView.getContext()).load(path).into(imageView);
    }

    ///??????bindding android:text ?????????????????????view
    @BindingAdapter({"android:text"})
    public static void setTextView(TextView textView, CharSequence charSequence) {
        ViewUtil.setTextShow(textView, charSequence);
    }

    @BindingAdapter({"layout_constraintWidth_percent"})
    public static void layoutConstraintWidthPercent(View view, float widthPercent) {
        if (!(view.getLayoutParams() instanceof ConstraintLayout.LayoutParams)) {
            return;
        }
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        lp.matchConstraintPercentWidth = widthPercent;
        view.setLayoutParams(lp);
    }

    @BindingAdapter({"layout_constraintHeight_percent"})
    public static void layoutConstraintHeightPercent(View view, float heightPercent) {
        if (!(view.getLayoutParams() instanceof ConstraintLayout.LayoutParams)) {
            return;
        }
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        lp.matchConstraintPercentHeight = heightPercent;
        view.setLayoutParams(lp);
    }

    public static int getVisibleInt(boolean show) {
        return show ? View.VISIBLE : View.GONE;
    }

    public static int getTint(boolean show, int color) {
        if (!show) {
            return Color.TRANSPARENT;
        }
        return color;
    }

    //??????????????????
    private ViewUtil() {
    }

    public static void switchSelect(View view) {
        view.setSelected(!view.isSelected());
    }

    public static Point getLocation(View view) {
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        return new Point(locations[0], locations[1]);
    }

    /**
     * ????????????-????????????
     *
     * @param filePath ???????????????
     * @return ??????????????????
     */

    public static final long IMG_MAX_SIZE = 1024 * 1024 * 5;

    public static String compressImage(Context context, String filePath) {
        int quality = 90;
        Bitmap bitmap;
        if (!filePath.startsWith("http") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            bitmap = ViewUtil.getBitmapFromUri(context, Uri.parse(filePath));
        } else {
            bitmap = BitmapFactory.decodeFile(filePath);
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File dir = new File(context.getCacheDir(), "images");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File outputFile = new File(dir, fileName);
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            while (baos.toByteArray().length > IMG_MAX_SIZE) {
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality - 10, baos);
            }
            FileOutputStream out = new FileOutputStream(outputFile);
            baos.writeTo(out);
            baos.close();
            out.flush();
            out.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return outputFile.getPath();
    }


    // ??????uri????????????
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Uri getImageContentUri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            // ????????????????????????????????????????????????????????????????????????
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public static GradientDrawable getGradientDrawable(boolean isStroke, int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(radius);
        if (isStroke) {
            shape.setStroke(2, Color.GRAY);
        }
        shape.setColor(color);
        return shape;
    }

    //?????????????????????
    public static StateListDrawable getSelector(Drawable normal, Drawable press) {
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_pressed}, press);
        selector.addState(new int[]{}, normal);
        return selector;
    }

    //?????????????????????
    public static StateListDrawable getSelector(boolean isStroke, int normal, int press,
                                                int radius) {
        GradientDrawable bgNormal = getGradientDrawable(isStroke, normal, radius);
        GradientDrawable bgPress = getGradientDrawable(isStroke, press, radius);
        StateListDrawable selector = getSelector(bgNormal, bgPress);
        return selector;
    }


    public static void setEnabled(View view, boolean enabled) {
        if (null == view) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            LinkedList<ViewGroup> queue = new LinkedList<ViewGroup>();
            queue.add(viewGroup);
            // ??????viewGroup
            while (!queue.isEmpty()) {
                ViewGroup current = queue.removeFirst();
                current.setEnabled(enabled);
                for (int i = 0; i < current.getChildCount(); i++) {
                    if (current.getChildAt(i) instanceof ViewGroup) {
                        queue.addLast((ViewGroup) current.getChildAt(i));
                    } else {
                        current.getChildAt(i).setEnabled(enabled);
                    }
                }
            }
        } else {
            view.setEnabled(enabled);
        }
    }

    public static void setEnabled(boolean enabled, View... views) {


        if (null == views) {
            return;
        }

        for (View view : views) {
            if (null == view) {
                continue;
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                LinkedList<ViewGroup> queue = new LinkedList<ViewGroup>();
                queue.add(viewGroup);
                // ??????viewGroup
                while (!queue.isEmpty()) {
                    ViewGroup current = queue.removeFirst();
                    current.setEnabled(enabled);
                    for (int i = 0; i < current.getChildCount(); i++) {
                        if (current.getChildAt(i) instanceof ViewGroup) {
                            queue.addLast((ViewGroup) current.getChildAt(i));
                        } else {
                            current.getChildAt(i).setEnabled(enabled);
                        }
                    }
                }
            } else {
                view.setEnabled(enabled);
            }
        }

    }


    /***
     * ???????????????????????????TEXTVIEW ??????????????????????????????????????????????????????
     * @param accompanyShow ???textview?????????????????????
     * */
    public static void setTextShow(TextView textView, CharSequence charSequence, View... accompanyShow) {
        if (textView == null) {
            return;
        }
        if (charSequence == null) {
            charSequence = "";
        }
        int isShow = TextUtils.isEmpty(charSequence.toString()) ? View.GONE : View.VISIBLE;
        textView.setText(charSequence);
        textView.setVisibility(isShow);
//        CommonUtils.log("setTextShow", charSequence, isShow);
        if (accompanyShow == null) {
            return;
        }
        for (View v : accompanyShow) {
            v.setVisibility(isShow);
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param all    ????????????
     * @param change ?????????????????????
     * @param spans  ??????span
     *               ?????? {@link ForegroundColorSpan }
     *               ???????????? {@link AbsoluteSizeSpan}
     *               ????????? {@link StrikethroughSpan}
     *               ????????? {@link android.text.style.UnderlineSpan}
     *               ??????{@link StyleSpan}
     *               <p>
     *               this.editHint =  ViewUtil.getSpannableString(editHint,
     *               new ForegroundColorSpan(0xffCDCDCD),
     *               new AbsoluteSizeSpan(10, true));
     */
    public static SpannableString getSpannableString(CharSequence all, CharSequence change, Object... spans) {
        if (change == null) {
            return new SpannableString(all);
        }
        if (all == null) {
            return null;
        }
        SpannableString spannableString = new SpannableString(all);
        int index = all.toString().indexOf(change.toString());
        int end = index + change.length();
        for (Object o : spans) {
            spannableString.setSpan(o, index, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;

    }

    public static SpannableString getSpannableString(CharSequence change, Object... spans) {
        if (change == null) {
            return null;
        }
        SpannableString spannableString = new SpannableString(change);
        int index = 0;
        int end = index + change.length();
        for (Object o : spans) {
            spannableString.setSpan(o, index, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;

    }

    public static void setTextShow(TextView textView, boolean show, CharSequence charSequence, View... accompanyShow) {
        if (show) {
            setTextShow(textView, charSequence, accompanyShow);
        } else {
            textView.setVisibility(View.GONE);
        }

    }


    /***
     * @param head ???????????????????????????????????????
     * @param  charSequence textView?????????????????????
     *
     *
     * */
    public static void setTextShow(TextView textView, CharSequence head, CharSequence charSequence, View... accompanyShow) {
        ViewUtil.setGone(textView);
        String show = "" + head + charSequence;
        if (charSequence == null || TextUtils.isEmpty(charSequence.toString())) {
            show = null;
        }
        setTextShow(textView, show, accompanyShow);
    }


    /***
     * @param  charSequence textView?????????????????????
     * @param end ???????????????????????????????????????
     *
     *
     *
     * */
    public static void setTextShowE(TextView textView, CharSequence charSequence, CharSequence end, View... accompanyShow) {
        ViewUtil.setGone(textView);
        String show = "" + charSequence + end;
        if (charSequence == null || TextUtils.isEmpty(charSequence.toString())) {
            show = null;
        }
        setTextShow(textView, show, accompanyShow);
    }

    /***
     *
     * ????????????  A???/A01 ???????????????
     * @param head ???????????????????????????????????????
     * @param  div textView?????????????????????
     *@param end ??????
     *
     * */
    public static void setTextShow(TextView textView, CharSequence head, CharSequence div, CharSequence end, View... accompanyShow) {
        if (textView == null) {
            return;
        }
        String show;
        if (isEmptyString(head) || isEmptyString(end)) {
            div = "";
        }
        show = new StringBuilder()
                .append(getString(head))
                .append(div)
                .append(getString(end))
                .toString();
        setTextShow(textView, show, accompanyShow);
    }

    public static boolean isEmptyString(Object object) {
        if (object == null) {
            return true;
        }
        return TextUtils.isEmpty(object.toString());
    }

    public static String getString(Object object) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }

    public static void setGone(View... views) {
        if (views == null) {
            return;
        }
        for (View v : views) {
            if (v == null) {
                continue;
            }
            v.setVisibility(View.GONE);
        }
    }

    public static void setVisible(View... views) {
        if (views == null) {
            return;
        }
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    //??????????????????????????????????????????
    public static void setVisibleSwitch(boolean showFirst, View first, View... views) {
        ViewUtil.setVisible(showFirst, first);
        ViewUtil.setVisible(!showFirst, views);
    }

    public static void setVisible(boolean show, View... views) {
        if (views == null) {
            return;
        }
        int state = (show ? View.VISIBLE : View.GONE);
        for (View v : views) {
            v.setVisibility(state);
        }
    }

    public static void setVisibleINVisible(boolean show, View... views) {
        if (views == null) {
            return;
        }
        int state = (show ? View.VISIBLE : View.INVISIBLE);
        for (View v : views) {
            v.setVisibility(state);
        }
    }

    public static void setVisible(String show, View... views) {
        setVisible(!TextUtils.isEmpty(show), views);
    }

    public static int getPx(Context context, int diamID) {
        return context.getResources().getDimensionPixelOffset(diamID);
    }

    public static String getUnit(String show, String unit) {
        if (TextUtils.isEmpty(show)) {
            return "";
        }
        if (TextUtils.isEmpty(unit)) {
            return show;
        }
        if (show.contains(unit)) {
            return show;
        }
        return show + unit;
    }


    /**
     * ???????????????????????????????????? ??????????????????????????? ????????????????????????????????????
     */
    private static String formatStr(String str, int insetSize) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        int len = str.length();
        String insert = "";
        for (int i = 0; i < insetSize; i++) {
            insert = insert + "???";
        }
        StringBuilder sb = new StringBuilder(str);
        for (int i = len - 1; i > 0; i--) {
            sb.insert(i, insert);
        }
        return sb.toString();
    }

    /**
     * ???????????????????????????
     *
     * @param str
     * @param maxSize ??????????????????
     * @return
     */
    public static SpannableString formatText(String str, int maxSize) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        int len = str.length();
        SpannableString spannableString = new SpannableString(str);
        if (len == 1) {
            return spannableString;
        }
        if (len >= maxSize) {
            spannableString.setSpan(new RelativeSizeSpan((float) (maxSize * 1.0) / len), 0, len, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            return spannableString;
        }
        str = formatStr(str, maxSize - 2);
        double multiple = (maxSize - len) * 1.0 / ((len - 1) * (maxSize - 2));
        spannableString = new SpannableString(str);
        for (int i = 1; i < str.length(); i = i + maxSize - 1) {
            spannableString.setSpan(new RelativeSizeSpan((float) multiple), i, i + maxSize - 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(Color.TRANSPARENT), i, i + maxSize - 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }


    /**
     * ??????TextView????????????
     *
     * @param tv               ??????????????????????????????
     * @param content          ????????????
     * @param paragraphSpacing ??????????????????????????????dp???
     */
    public static void setParagraphSpacing(TextView tv, String content, int paragraphSpacing) {
        if (!content.contains("\n")) {
            tv.setText(content);
            return;
        }
        content = content.replace("\n", "\n\r");
        int previousIndex = content.indexOf("\n\r");
        //???????????????????????????index???????????????????????????????????????
        List<Integer> nextParagraphBeginIndexes = new ArrayList<>();
        nextParagraphBeginIndexes.add(previousIndex);
        while (previousIndex != -1) {
            int nextIndex = content.indexOf("\n\r", previousIndex + 2);
            previousIndex = nextIndex;
            if (previousIndex != -1) {
                nextParagraphBeginIndexes.add(previousIndex);
            }
        }
        //???\r??????????????????????????????:1px???????????????+?????????
        SpannableString spanString = new SpannableString(content);
        ColorDrawable gd = new ColorDrawable();
        gd.setColor(Color.RED);
        gd.setAlpha(1);
        //int???????????????????????? - ?????? + ??????
        int height = (int) (tv.getLineHeight() - tv.getLineSpacingExtra() + paragraphSpacing);
        gd.setBounds(0, 0, 1, height);
        for (int index : nextParagraphBeginIndexes) {
            spanString.setSpan(new ImageSpan(gd), index + 1, index + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(spanString);
    }


    //?????????
    public static InputFilter[] getIDcardInputFilter(final EditText editText) {
        String[] IDCARD = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "x", "X",};
        final List<String> idCardList = Arrays.asList(IDCARD);
        InputFilter inputFilter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                // ??????????????????????????????????????????????????????null??????????????????
                for (int i = 0; i < source.toString().length(); i++) {
                    if (!idCardList.contains(String.valueOf(source.charAt(i)))) {
//                        CommonUtils.showToast(editText.getContext(), "?????????????????????????????????????????????");
                        return "";
                    }
                    if (editText.getText().toString().length() < 17) {
                        if ("x".equals(String.valueOf(source.charAt(i))) || "X".equals(String.valueOf(source.charAt(i)))) {
                            return "";
                        }
                    }
                }
                return null;
            }
        };
        return new InputFilter[]{new InputFilter.LengthFilter(18), inputFilter};
    }

    public static Activity castToActivity(View view) {
        return castToActivity(view.getContext());
    }

    public static Activity castToActivity(Context context) {
        Context c = context;
        while (c instanceof ContextWrapper) {
            if (c instanceof Activity) {
                return (Activity) c;
            }
            c = ((ContextWrapper) c).getBaseContext();
        }
        return null;
    }


    /**
     * ???????????????bitmap
     *
     * @param rootView
     * @return
     */
    public static Bitmap screenshot(View rootView) {
        int me = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        rootView.measure(me, me);
        rootView.layout(0, 0, rootView.getMeasuredWidth(), rootView.getMeasuredHeight());
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache();
        Bitmap bm = rootView.getDrawingCache();
        if (bm != null) {
            bm = Bitmap.createBitmap(bm);
        } else {
            Log.e("--bm---", (bm == null) + "");
        }
        rootView.destroyDrawingCache();
        rootView.setDrawingCacheEnabled(false);
        return bm;
    }

    /**
     * ???????????????bitmap ???????????????????????????
     */
    public static Bitmap justScreenshot(View rootView) {
        if (rootView.getHeight() <= 0) {
            getCurrentScreenPx(rootView);
        }
        if (rootView.getHeight() <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmapByView(NestedScrollView scrollView) {
        int me = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        scrollView.measure(me, me);
        scrollView.layout(0, 0, scrollView.getMeasuredWidth(), scrollView.getMeasuredHeight());
        int h = 0;
        Bitmap bitmap = null;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
        }
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmapByView(LinearLayout scrollView) {
        int me = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        scrollView.measure(me, me);
        scrollView.layout(0, 0, scrollView.getMeasuredWidth(), scrollView.getMeasuredHeight());
        int h = 0;
        Bitmap bitmap = null;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
        }
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        return bitmap;
    }

    /*
     * ???????????????????????????????????????
     */
    public static void saveBitmap(Context context, Bitmap bitmap, String bitName) {
        String fileName;
        File file;
        if (Build.BRAND.equals("xiaomi")) { // ????????????
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + bitName;
        } else if (Build.BRAND.equals("Huawei")) {
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + bitName;
        } else {  // Meizu ???Oppo
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + bitName;
        }
        file = new File(fileName);

        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            // ????????? JPEG??????????????????????????????JPEG????????????PNG?????????????????????????????????
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
                // ????????????
                MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), bitName, null);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
        // ??????????????????????????????????????????
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));

    }

    public static void adjustmentSpanEllipsize(TextView view, CharSequence content) {
        int maxLine = view.getMaxLines();
        if (maxLine == Integer.MAX_VALUE || TextUtils.isEmpty(content) || !(content instanceof Spannable)) {
            return;
        }

        view.setMaxLines(Integer.MAX_VALUE);
        if (view.getLineCount() <= maxLine) {
            view.setMaxLines(maxLine);
            return;
        }
        int lastCharShown = view.getLayout().getLineVisibleEnd(maxLine - 1);
        int numCharsToChop = 3;
        SpannableStringBuilder ssb = new SpannableStringBuilder(content.subSequence(0, lastCharShown - numCharsToChop));
        ssb.append("???");
        view.setMaxLines(maxLine);
        view.setText(ssb);

    }


    private static void getCurrentScreenPx(View mPrintView) {
        DisplayMetrics metric = new DisplayMetrics();
        ((Activity) mPrintView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metric);
        int mScreenWidth = metric.widthPixels;     // ????????????????????????
        int mScreenHeight = metric.heightPixels;   // ????????????????????????
        layoutView(mPrintView, mScreenWidth, mScreenHeight);//????????????view??????
    }

    private static void layoutView(View mPrintView, int mPrintWidth, int mPrintHeight) {
        CommonUtils.log(mPrintWidth, mPrintHeight);
        //????????????View????????? ?????????????????? ?????????????????????
        mPrintView.layout(0, 0, mPrintWidth, mPrintHeight);
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(mPrintWidth, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(mPrintHeight, View.MeasureSpec.EXACTLY);
        //measure??????????????????????????????View????????????????????????View.layout?????????????????????
        //??????layout????????????View????????????????????????????????????????????????
        mPrintView.measure(measuredWidth, measuredHeight);
        CommonUtils.log(mPrintView.getMeasuredWidth(), mPrintView.getMeasuredHeight());
        mPrintView.layout(0, 0, mPrintView.getMeasuredWidth(), mPrintView.getMeasuredHeight());
    }

    public static FragmentManager getFragmentManager(View view) {
        return getFragmentManager(view.getContext());
    }

    public static FragmentManager getFragmentManager(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof FragmentActivity)
            return ((FragmentActivity) cont).getSupportFragmentManager();
        else if (cont instanceof ContextWrapper)
            return getFragmentManager(((ContextWrapper) cont).getBaseContext());
        return null;
    }

    public static AppCompatActivity getAppCompatActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof AppCompatActivity)
            return (AppCompatActivity) cont;
        else if (cont instanceof ContextWrapper)
            return getAppCompatActivity(((ContextWrapper) cont).getBaseContext());

        return null;
    }

    public static Drawable tintDrawable(Drawable drawable, int color) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(color));
        return wrappedDrawable;
    }

    public static String getStringMoney(double money) {

        String showValue = "";
//        if (money < 10000) {
//            showValue ="??"+ String.format("%.2f", money) + "???";
//        } else {
//            showValue ="??"+  String.format("%.2f", money / 10000) + "??????";
//        }
        if (money < 10000) {
            showValue = "??" + (int) money + "???";
        } else {
            showValue = "??" + (int) (money / 10000.0) + "??????";
        }
        return showValue;
    }


    public static String getStringMoneyNoUnit(double money) {
        String showValue = "";

        if (money < 10000) {
            showValue = "??" + (int) money;
        } else {
            showValue = "??" + String.format("%.2f", money / 10000.0);
        }
        return showValue;
    }


    public static String getStringMoneyWith2Dot(double money) {

        String showValue = "";
//        if (money < 10000) {
//            showValue ="??"+ String.format("%.2f", money) + "???";
//        } else {
//            showValue ="??"+  String.format("%.2f", money / 10000) + "??????";
//        }
        if (money < 10000) {
            showValue = "??" + (int) money + "???";
        } else {
            showValue = "??" + String.format("%.2f", (money / 10000.0)) + "???";
        }
        return showValue;
    }

    public static String getStringMoneyNoIcon(double money) {

        String showValue = "";
//        if (money < 10000) {
//            showValue ="??"+ String.format("%.2f", money) + "???";
//        } else {
//            showValue ="??"+  String.format("%.2f", money / 10000) + "??????";
//        }
        if (money < 10000) {
            showValue = (int) money + "???";
        } else {
            showValue = (int) (money / 10000.0) + "??????";
        }
        return showValue;
    }

    public static String[] getStringMoneys(double money) {

        String showValue[] = new String[2];
//        if (money < 10000) {
//            showValue ="??"+ String.format("%.2f", money) + "???";
//        } else {
//            showValue ="??"+  String.format("%.2f", money / 10000) + "??????";
//        }
        if (money < 10000) {
            showValue[0] = (int) money + "";
            showValue[1] = "???";
        } else {
            showValue[0] = (int) (money / 10000.0) + "";
            showValue[1] = "??????";
        }
        return showValue;
    }

    /**
     * 0, ???
     * 1,???
     * 2,???
     * 3,???
     */
    public static void scaleAnimationShow(final View view, boolean show, int di) {
        ScaleAnimation scaleAnimation;
        int duration = 100;
        float start = show ? 0 : 1;
        float end = show ? 1 : 0;
        if (show) {
            ViewUtil.setVisible(view);
        } else {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.GONE);
                }
            }, duration);
        }
        switch (di) {
            case 1:
                view.setPivotX(0.5f);
                view.setPivotY(0);
                scaleAnimation = new ScaleAnimation(1, 1, start, end);
                break;
            case 2:
                view.setPivotX(1);
                view.setPivotY(0.5f);
                scaleAnimation = new ScaleAnimation(start, end, 1, 1);
                break;
            case 3:
                view.setPivotX(0.5f);
                view.setPivotY(1);
                scaleAnimation = new ScaleAnimation(1, 1, start, end);
                break;
            default:
                view.setPivotX(0);
                view.setPivotY(0.5f);
                scaleAnimation = new ScaleAnimation(start, end, 1, 1);
        }
        scaleAnimation.setDuration(duration);
        view.startAnimation(scaleAnimation);
    }


    @SuppressLint("DefaultLocale")
    public static String getdistance(double distance) {
        if (distance <= 0) {
            return null;
        }
        if (distance > 1000) {
            return "" + String.format("%.1f", distance / 1000) + "km";
        }
        return (int) distance + "m";
    }

    public static String getSubString(String string, int len, String end) {
        if (TextUtils.isEmpty(string)) {
            return string;
        }
        if (string.length() < len) {
            return string;
        }
        return string.substring(0, len) + end;
    }


    public static Activity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }


    private static final String EXTRA_SETACTIVITYNEEDUPDATE = "extra_setActivityNeedUpdate";

    //??????activity??????????????????????????????????????????
    public static void setActivityNeedUpdate(Activity activity) {
        if (activity == null) {
            return;
        }
        Intent intent = activity.getIntent();
        intent.putExtra(EXTRA_SETACTIVITYNEEDUPDATE, true);
        CommonUtils.log("updateData  setActivityNeedUpdate");
    }

    public static void setActivityNeedUpdate(View view) {
        setActivityNeedUpdate(getActivityFromView(view));
    }

    public static boolean isNeedUpdate(Activity activity) {
        if (activity == null) {
            return false;
        }
        boolean ans = activity.getIntent().getBooleanExtra(EXTRA_SETACTIVITYNEEDUPDATE, false);
        activity.getIntent().putExtra(EXTRA_SETACTIVITYNEEDUPDATE, false);
        CommonUtils.log("updateData  isNeedUpdate", ans);
        return ans;
    }

    public static boolean isNeedUpdate(View view) {
        return isNeedUpdate(getActivityFromView(view));
    }

    public static Bitmap drawableToBitmap(Drawable drawable, String name, String path) {
        // ??? drawable ?????????
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // ??? drawable ???????????????
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // ???????????? bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // ???????????? bitmap ?????????
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // ??? drawable ?????????????????????
        drawable.draw(canvas);
        return bitmap;
    }
}
