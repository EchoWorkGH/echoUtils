package com.echo.library.util;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.Locale;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/6
 * change   :
 * describe :
 */
public class SystemUtils {

    public static void hideKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        hideKeyboard(activity.getWindow());
    }

    public static void hideKeyboard(Fragment fragment) {
        hideKeyboard(fragment.getActivity());
    }

    // The current window, or null if the activity is not visual.
    public static void hideKeyboard(@Nullable Window window) {
        if (window == null) {
            return;
        }
        View view = window.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager)
                    window.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static boolean checkEditorPermissions(Activity activity,
                                                 Fragment fragment,
                                                 int requestCode) {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
        };
        return checkPermissions(activity, fragment, permissions, requestCode);
    }

    public static boolean checkPermissions(Activity activity,
                                           Fragment fragment,
                                           String[] permissions,
                                           int requestCode) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                fragment.requestPermissions(permissions, requestCode);
                return false;
            }
        }
        return true;
    }

    public static boolean checkPermissions(Activity activity,
                                           String permission,
                                           int requestCode) {
        return checkPermissions(activity, new String[]{permission}, requestCode);
    }

    public static boolean checkPermissions(Activity activity,
                                           String[] permissions,
                                           int requestCode) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
                return false;
            }
        }
        return true;
    }

    public static boolean isGrantPermissions(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Keep
    public static Context wrapLocale(Context newBase) {
        try {
            ApplicationInfo info = newBase.getPackageManager()
                    .getApplicationInfo(newBase.getPackageName(), PackageManager.GET_META_DATA);
            Configuration config = newBase.getResources().getConfiguration();
            String lang = info.metaData.getString("wg_locale");
            if (!TextUtils.isEmpty(lang)) {

                CommonUtils.log("use wg_locale=" + lang);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    config.setLocale(new Locale(lang));
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return newBase.createConfigurationContext(config);
            }

        } catch (Exception e) {
            return newBase;
        }
        return newBase;
    }

    /**
     * 复制内容到剪切板
     * Created by Vito on 2019/8/27.
     *
     * @param label   用户可见的标签
     * @param copyStr 需要写入剪切板的内容
     * @return result 是否成功
     */
    public static boolean copyString(Context context, String label, String copyStr) {
        try {
            //获取剪贴板管理器
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText(label, copyStr);
            // 将ClipData内容放到系统剪贴板里。
            if (cm == null) {
                return false;
            }
            cm.setPrimaryClip(mClipData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}