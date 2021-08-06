package com.echo.library.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.echo.library.R;

import java.lang.ref.WeakReference;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/14
 * change   :
 * describe :
 */
public class DialogUtils {
    private static ProgressDialog progressDialog;
    private static WeakReference<Activity> weakReference;

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void showProgressDialog(Activity theActivity) {
        if (theActivity == null || theActivity.isFinishing()) {
            return;
        }
        weakReference = new WeakReference<>(theActivity);
        if (progressDialog == null || progressDialog.getOwnerActivity() != theActivity) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            progressDialog = new MyProgressDialog(theActivity);
            progressDialog.setMessage("Loading...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setOwnerActivity(theActivity);
        }
        if (progressDialog.isShowing()) {
            return;
        }
        theActivity.runOnUiThread(() -> {
            try {
                progressDialog.show();
            } catch (Throwable e) {
                e.printStackTrace();
                CommonUtils.log("error", e.getMessage());
            }
        });
        CommonUtils.log("ProgressDialog_show", progressDialog.hashCode());
        theActivity.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (weakReference == null || weakReference.get() == activity) {
                    activity.getApplication().unregisterActivityLifecycleCallbacks(this);
                    weakReference = null;
                }
                if (progressDialog == null) {
                    return;
                }
                if (getContext(progressDialog.getContext()) == activity) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        });
    }

    public static Context getContext(Context context) {
        if (context instanceof ContextWrapper) {
            return ((ContextWrapper) context).getBaseContext();
        }
        return context;
    }

    public static void hideProgressDialog() {
        CommonUtils.log("hideProgressDialog");
        if (progressDialog != null && progressDialog.isShowing()) {
            CommonUtils.log("ProgressDialog_hide", progressDialog.hashCode());
            progressDialog.dismiss();
        }
        progressDialog = null;
        weakReference = null;
    }


    public static Dialog show(AlertDialog.Builder builder) {
        return show(builder.create());
    }

    public static Dialog show(Dialog dialog) {
        Activity activity = null;
        Context context = dialog.getContext();
        if (context instanceof Activity) {
            activity = (Activity) context;

        } else if (context instanceof ContextThemeWrapper) {
            Context base = ((ContextThemeWrapper) context).getBaseContext();

            if (base instanceof Activity) {
                activity = (Activity) base;
            }
        }
        if (activity != null && !activity.isFinishing()) {
            dialog.show();
        }
        return dialog;
    }


    public static class MyProgressDialog extends ProgressDialog {

        public MyProgressDialog(Context context) {
            super(context);
        }


        public MyProgressDialog(Context context, int theme) {
            super(context, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            init(getContext());
        }

        private void init(Context context) {
            setContentView(R.layout.progress);
            if (drawable != null) {
                ImageView imageView = getWindow().getDecorView().findViewById(R.id.icon);
                imageView.setImageDrawable(drawable);
            }
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    static Drawable drawable;


    public static void setDrawable(Drawable d) {
        drawable = d;
    }


}