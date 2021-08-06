package com.echo.library.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.echo.library.BaseBindingActivity;
import com.echo.library.GrantedCallBacK;
import com.echo.library.R;
import com.echo.library.databinding.EmptyFragmentActivityBinding;


/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2021/4/23
 * change   :
 * describe :
 */
public class EmptyFragmentActivity extends BaseBindingActivity<EmptyFragmentActivityBinding> {

    private static LaunchFragment theRunnable;
    private static ActivityResult theActivityResult;

    public static void invoke(Activity activity, LaunchFragment runnable) {
        invoke(activity, runnable, null);
    }

    public static void invoke(Activity activity, LaunchFragment runnable, ActivityResult activityResult) {
        if (runnable == null || activity == null) {
            return;
        }
        Intent intent = new Intent(activity, EmptyFragmentActivity.class);
        theRunnable = runnable;
        theActivityResult = activityResult;
        activity.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.empty_fragment_activity;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentViewDestroyed(fm, f);
                CommonUtils.log("EmptyFragmentActivity", getSupportFragmentManager().getFragments().size());
                if (getSupportFragmentManager().getFragments().size() == 0) {
                    finish();
                }
            }

            @Override
            public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
                super.onFragmentAttached(fm, f, context);
                CommonUtils.log("EmptyFragmentActivity", getSupportFragmentManager().getFragments().size());
            }
        }, false);

        if (theRunnable != null) {
            theRunnable.run(this);
            theRunnable = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonUtils.log("onDestroy");
        theRunnable = null;
        theActivityResult = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (theActivityResult != null) {
            theActivityResult.onActivityResult(this, requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (theActivityResult != null) {
            theActivityResult.onActivityResult(this, requestCode, 1, null);
        }
    }

    public interface LaunchFragment {
        void run(FragmentActivity fragmentActivity);
    }

    public interface ActivityResult {
        void onActivityResult(FragmentActivity fragmentActivity, int requestCode, int resultCode, @Nullable Intent data);
    }

    public static void runAfterCheck(Activity activity, final Runnable runnable, int tipStringID) {
        runAfterCheck(activity,
                runnable,
                () -> CommonUtils.showToast(activity, activity.getString(tipStringID)),
                true);
    }

    public static void runAfterCheck(Activity activity, final Runnable runnable, final GrantedCallBacK callBacK, final boolean showTip) {
        CommonUtils.log("  runAfterCheck  ");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            runnable.run();
            return;
        }
        EmptyFragmentActivity.invoke(activity, fragmentActivity -> {
            CommonUtils.log("  requestPermissions  ");
            fragmentActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }, (fragmentActivity, requestCode, resultCode, data) -> {
            boolean permission = ContextCompat.checkSelfPermission(fragmentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean shouldShowRequestPermissionRationale = ActivityCompat.shouldShowRequestPermissionRationale(fragmentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            CommonUtils.log("onActivityResumed runAfterCheck  permission:", permission, "shouldShowRequestPermissionRationale", shouldShowRequestPermissionRationale);
            if (permission) {
                runnable.run();
            } else {
                callBacK.noGranted();
            }
            fragmentActivity.finish();
        });
    }
}
