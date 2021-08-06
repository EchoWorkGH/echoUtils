package com.echo.library.rv;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/15
 * change   :
 * describe :
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.echo.library.R;
import com.echo.library.util.CommonUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

import me.drakeet.multitype.ItemViewProvider;
import me.drakeet.multitype.MultiTypeAdapter;

/**
 * Author      :    DongJunJie
 * Date        :    2018/12/11
 * E-mail      :    dongjunjie.mail@qq.com
 * Description :   适用于MultiTypeAdapter的ViewHolder 基类
 * 建议使用{@link BaseBindMultiTypeViewHolder}
 */
public abstract class BaseMultiTypeViewHolder<T> extends RecyclerView.ViewHolder {
    public static <E> MultiTypeAdapter inject(MultiTypeAdapter adapter, final int id, final Class<? extends BaseMultiTypeViewHolder<E>> holder) {
        ItemViewProvider itemViewProvider = new ItemViewProvider<E, BaseMultiTypeViewHolder<E>>() {
            @NonNull
            @Override
            protected BaseMultiTypeViewHolder<E> onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
                View v = inflater.inflate(id, parent, false);
                BaseMultiTypeViewHolder<E> rt = null;
                try {
                    Constructor<? extends BaseMultiTypeViewHolder<E>> constructor = holder.getConstructor(View.class);
                    constructor.setAccessible(true);
                    rt = constructor.newInstance(v);
                    rt.clearViewState();
                } catch (NoSuchMethodException e) {
                    CommonUtils.log(e.getMessage());
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    CommonUtils.log(e.getMessage());
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    CommonUtils.log(e.getMessage());
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    CommonUtils.log(e.getMessage());
                    e.printStackTrace();
                }
                return rt;
            }

            @Override
            protected void onBindViewHolder(@NonNull BaseMultiTypeViewHolder<E> holder, @NonNull E t) {
                holder.clearViewState();
                holder.bind(t);
            }
        };
//        Class<E> eClass = getEntryClass(holder);
//        CommonUtils.log(eClass.getName());
        adapter.register(getEntryClass(holder), itemViewProvider);
        return adapter;
    }

    public static <B> Class<B> getEntryClass(Class<? extends BaseMultiTypeViewHolder<B>> holder) {
        ParameterizedType parameterizedType = (ParameterizedType) holder.getGenericSuperclass();
        Type[] types = parameterizedType.getActualTypeArguments();
        Type type = types[0];
        while (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }
//        if (types[0] instanceof WildcardType) {
//            WildcardType wildcardType = (WildcardType) types[0];
//            types = wildcardType.getUpperBounds();
//        }
        return (Class<B>) type;
    }

    public BaseMultiTypeViewHolder(View itemView) {
        super(itemView);
        itemView.setTag(R.id.view_holder, this);
    }

    public HashMap<String, HeaderAndFooterAdapter.ClickCallBack> itemclickMap;

    public int getColor(int colorId) {
        return getContext().getResources().getColor(colorId);
    }


    public boolean onClickItem(String name) {
        if (bindData == null) {
            return false;
        }
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        if (itemclickMap == null) {
            return false;
        }
        HeaderAndFooterAdapter.ClickCallBack callBack = itemclickMap.get(name);
        if (callBack == null) {
            return false;
        }
        callBack.onClick(bindData);
        return true;
    }


    public Context getContext() {
        return itemView.getContext();
    }

    public static BaseMultiTypeViewHolder getViewHolder(View view) {
        if (view == null) {
            return null;
        }
        Object object = view.getTag(R.id.view_holder);
        if (object == null || !(object instanceof BaseMultiTypeViewHolder)) {
            return null;
        }
        return (BaseMultiTypeViewHolder) object;
    }

    public T bindData;

    public void bind(T t) {
        bindData = t;
    }

    public BaseMultiTypeViewHolder<T> bindNew(T t) {
        bind(t);
        return this;
    }


    public void clearViewState() {
    }

    public void showToast(String s) {
        CommonUtils.showToast(itemView.getContext(), s);
    }

}