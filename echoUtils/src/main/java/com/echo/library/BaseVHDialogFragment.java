package com.echo.library;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.CallSuper;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.echo.library.util.CommonUtils;


/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/8/3
 * change   :
 * describe : 跟布局为{@link ConstraintLayout}的时候，用于切换横竖屏
 * 要使用必须重载 {@link  #vhGetRootConstraintLayout}
 */
@Keep
public class BaseVHDialogFragment extends AppCompatDialogFragment {
    public ConstraintSet constraintLayoutH;
    public ConstraintSet constraintLayoutV;
    public ConstraintLayout constraintLayout;


    @CallSuper
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CommonUtils.log(view.getWidth());
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                vhInit();
                vhSwitch(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /**
     * 重置横竖布局
     * eg：有时候布局会有变化，需要重置
     */
    final public void resetVh() {
        constraintLayoutV = null;
        constraintLayoutH = null;
        if (getView() == null) {
            return;
        }
        getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                vhInit();
                getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /**
     * 需要切换布局的时候，根布局是ConstraintLayout的时候使用
     */
    @CallSuper
    public void vhInit() {
        if (constraintLayoutV != null) {
            return;
        }
        constraintLayout = vhGetRootConstraintLayout();
        if (constraintLayout == null) {
            return;
        }
        constraintLayoutV = new ConstraintSet();
        constraintLayoutV.clone(constraintLayout);
        constraintLayoutH = new ConstraintSet();
        constraintLayoutH.clone(constraintLayout);
    }

    /**
     * 标准的统一的额背景
     * 竖屏 345*507
     * 横屏 507*331
     */
    public View getStandardBgView() {
        return null;
    }

    /**
     * @param isv 竖向布局
     */
    @CallSuper
    public void vhSwitch(boolean isv) {
        CommonUtils.log(isv, constraintLayout);
        if (constraintLayout == null) {
            return;
        }
        ConstraintSet cu = isv ? constraintLayoutV : constraintLayoutH;
        cu.applyTo(constraintLayout);
    }

    /**
     * 需要切换布局的时候，根布局是ConstraintLayout的时候使用
     */
    public ConstraintLayout vhGetRootConstraintLayout() {
        return null;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        vhSwitch(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public String getFragmentTitle() {
        return "";
    }
}
