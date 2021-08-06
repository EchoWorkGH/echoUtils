package com.echo.library.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.echo.library.network.ResponseMessage;
import com.echo.library.network.StateCallBack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/6
 * change   :
 * describe :
 */
public class CommonUtils {

    private static String TAG = "CommonUtils";
    private static boolean showLog = true;

    public static void setTAG(String tag) {
        TAG = tag;
        log("setTAG", TAG);
    }

    public static void setShowLog(boolean show) {
        showLog = true;
        log("setShowLog", show);
        showLog = show;
    }

    public static void log(Object... objects) {
        log2(false, TAG, objects);
    }

    public static void logECode(boolean show, Object... objects) {
        log2(!show, TAG, objects);
    }

    public static void logECodeT(Object... objects) {
        log2(true, TAG, objects);
    }

    public static void logIf(boolean show, Object... objects) {
        if (!show) {
            return;
        }
        log2(false, TAG, objects);
    }

    public static void log(Map map) {
        Set<Map.Entry> set = map.entrySet();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry entry : set) {
            sb.append(entry.getKey()).append(" :").append(entry.getValue()).append(";");
        }
        log2(false, TAG, sb.toString());
    }

    /**
     * @param eCode 是否加密
     * @param TAG   tag
     */
    public static void log2(boolean eCode, String TAG, Object... objects) {
        if (!showLog) {
            return;
        }
        if (objects == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        Throwable a = new Throwable();
        StackTraceElement[] traceElement = a.getStackTrace();
        sb.append(" \n╔═════════════════════════════════");
        sb.append("\n║➨➨at ");
        sb.append(traceElement[2]);
        sb.append("\n║➨➨➨➨at ");
        sb.append(traceElement[3]);
        sb.append("\n╟───────────────────────────────────\n");
        sb.append("║");
        for (Object o : objects) {
            if (o != null) {
                String s = o.toString();
                if (eCode) {
                    s = EncryptDES.eCode(s);
                }
                sb.append(s.replaceAll("\n", "\n║"));
            } else {
                sb.append("null");
            }
            sb.append("___");
        }
        sb.append("\n╚═════════════════════════════════");
        logE(TAG, sb.toString());
    }

    //复制到剪切板
    public static void copyToClipboard(@NonNull Context context, @NonNull CharSequence text, @Nullable StateCallBack<String> callBack) {
        try {
            //获取剪贴板管理器
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", text);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            if (callBack != null) {
                callBack.onSuccess("");
            }
        } catch (Exception e) {
            if (callBack != null) {
                callBack.onError(ResponseMessage.error(e.getMessage()));
            }
        }
    }

    public static void logStackTrace(Object... objects) {
        if (!showLog) {
            return;
        }
        if (objects == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        Throwable a = new Throwable();
        sb.append(" \n╔═════════════════════════════════");
        String jiantou = "";
        for (StackTraceElement traceElement1 : a.getStackTrace()) {
            jiantou = jiantou + "➨";
            sb.append("\n║");
            sb.append(jiantou);
            sb.append("at ");
            sb.append(traceElement1);
        }
        sb.append("\n╟───────────────────────────────────\n");
        sb.append("║");
        for (Object o : objects) {
            if (o != null) {
                sb.append(o.toString());
            } else {
                sb.append("null");
            }
            sb.append("___");
        }
        sb.append("\n╚═════════════════════════════════");
        logE("wgsdk", sb.toString());
    }

    //规定每段显示的长度
    private static int LOG_MAXLENGTH = 2 * 1024;

    public static void logE(String TAG, String msg) {
        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAXLENGTH;
        int i = 0;
        while (strLength > end) {
            _logE(TAG + i, msg.substring(start, end));
            start = end;
            end = end + LOG_MAXLENGTH;
            i++;
        }

        _logE(TAG + i, msg.substring(start, strLength));

    }

    static void _logE(String TAG, String msg) {
        Log.e(TAG, msg);
//        if (Config.showLogText && !msg.contains("RxBus.post")) {
//            RxBus.$().post(RxBus.Event.LOG, msg);
//        }
    }

    public static String getStringNotNull(Object... objects) {
        if (objects == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object o : objects) {
            if (o == null) {
                continue;
            }
            if (TextUtils.isEmpty(o.toString())) {
                continue;
            }
            if ("null".equals(o.toString())) {
                continue;
            }
            sb.append(o.toString());
        }
        return sb.toString();
    }

    static WeakReference<Toast> toastWeakReference;

    /**
     * Toast 提示框
     *
     * @param context
     * @param content
     */
    public static void showToast(Context context, String content) {
        if (context == null || TextUtils.isEmpty(content)) {
            return;
        }
        Toast toast;
        if (toastWeakReference == null || toastWeakReference.get() == null) {
            toast = Toast.makeText(context.getApplicationContext(), content, Toast.LENGTH_SHORT);
            toastWeakReference = new WeakReference<>(toast);
        }
        toast = toastWeakReference.get();
        toast.setText(content);
        toast.show();
    }

    /**
     * 返回一个非空的字符
     */
    public static String getShowString(String... order) {
        if (order == null) {
            return null;
        }
        for (String s : order) {
            if (!TextUtils.isEmpty(s)) {
                return s;
            }
        }
        return null;
    }

    public static boolean isNullList(List list) {
        if (list == null) {
            return true;
        }
        if (list.size() == 0) {
            return true;
        }
        return false;
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Resources resources, float pxValue) {
        final float scale = resources.getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static boolean isEmail(String email) {
        return Pattern.matches("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$", email);
    }

    /**
     * 图片压缩-质量压缩
     *
     * @param filePath 源图片路径
     * @return 压缩后的路径
     */

    public static final long IMG_MAX_SIZE = 1024 * 1024 * 2;

    public static String compressImage(Context context, String filePath) {
        File file = new File(filePath);
        long fileSize = getFileSize(file);
        if (fileSize < IMG_MAX_SIZE) {
            return filePath;
        }
        int quality = 90;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        String fileName = System.currentTimeMillis() + ".jpg";
        File dir = new File(context.getCacheDir(), "images");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File outputFile = new File(dir, fileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            while (baos.toByteArray().length / (1024 * 1024) > 5) {
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality - 10, baos);
            }
            FileOutputStream out = new FileOutputStream(outputFile);
            baos.writeTo(out);
            baos.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile.getPath();
    }

    /**
     * 获取指定文件大小
     */
    public static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }


    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的字符串
     * @param format  yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy/MM/dd HH:mm";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds + "000")));
    }

    public static String timeStamp2Date(String seconds) {
        return timeStamp2Date(seconds, null);
    }


    /**
     * 日期格式字符串转换成时间戳
     *
     * @param date_str 字符串日期
     * @param format   如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimeStamp(String date_str, String format) {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static int getScreenHeight(Resources resources) {
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int getScreenWidth(Resources resources) {
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    public static InputFilter getMaxInputFilter(int max) {
        return (source, start, end, dest, dstart, dend) -> {
            CommonUtils.log("getMaxInputFilter", source, start, end, dest, dstart, dend);
            if (dstart > max) {
                return "";
            }
            return null;
        };
    }

    public static boolean isPassword8(String password) {
        return Pattern.matches("^(?!([a-zA-Z]+|\\d+)$)[a-zA-Z\\d]{8,21}$", password);
    }

    public static StringBuilder logView(View view, StringBuilder sb, String deep) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        if (view == null) {
            return sb.append("\nnull");
        }
        if (deep == null) {
            deep = "";
        }
        deep += "——";
        sb.append("\n").append(deep).append(view.getClass().getSimpleName()).append(view.getTag() == null ? "" : (" " + view.getTag()));
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                sb = logView(((ViewGroup) view).getChildAt(i), sb, deep);
            }
        }
        return sb;
    }

    public static void logView(View view) {
        CommonUtils.log(logView(view, null, null).toString());
    }
}
