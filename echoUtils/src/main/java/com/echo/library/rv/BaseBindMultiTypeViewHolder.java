package com.echo.library.rv;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.Keep;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;


import com.echo.library.Data2;
import com.echo.library.util.CommonUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/15
 * change   :
 * describe :  添加针对设置了  holder 为数据的处理
 */
@Keep
public abstract class BaseBindMultiTypeViewHolder<T, V extends ViewDataBinding> extends BaseMultiTypeViewHolder<T> {


    private boolean showLog = false;
    public V binding;

    public BaseBindMultiTypeViewHolder(View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
    }

    @Override
    @CallSuper
    public void bind(T t) {
        super.bind(t);
        bindData(t);
    }

    private void bindData(T t) {
        Data2<Method, Object> data2 = findMethod(t);
        if (data2 == null) {
            return;
        }
        try {
            data2.a.invoke(binding, data2.b);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Data2<Method, Object> findMethod(T t) {
        Method method;
        try {
            method = binding.getClass().getDeclaredMethod("setHolder", this.getClass());
            return new Data2(method, this);
        } catch (NoSuchMethodException e) {
            CommonUtils.logIf(showLog, binding.getClass().getName(), e.getMessage());
        }
        try {
            method = binding.getClass().getDeclaredMethod("setHolder", t.getClass());
            return new Data2(method, t);
        } catch (NoSuchMethodException e) {
            CommonUtils.logIf(showLog, binding.getClass().getName(), e.getMessage());
        }
        Method[] methods = binding.getClass().getMethods();
        for (Method mt : methods) {
//            CommonUtils.log(mt.getName());
            if (!mt.getName().startsWith("set")) {
                continue;
            }
            Class<?>[] parames = mt.getParameterTypes();
//            CommonUtils.log(parames.length, parames[0].getName());
            if (parames.length != 1) {
                continue;
            }
            if (parames[0] == this.getClass()) {
                return new Data2(mt, this);
            }
            if (parames[0].isAssignableFrom(t.getClass())) {
                return new Data2(mt, t);
            }
        }
        return null;
    }
}
