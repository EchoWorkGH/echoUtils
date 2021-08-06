package com.echo.library.network;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Keep;
import androidx.fragment.app.Fragment;

import com.echo.library.util.CommonUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/16
 * change   :
 * describe :
 */
@Keep
public class ParamBuilder extends Subscription {

    transient ArrayList<String> ignoreParams;

    public ParamBuilder(@NotNull Activity mActivity) {
        super(mActivity);
    }

    public ParamBuilder(@NotNull Fragment fragment) {
        super(fragment);
    }

    public ParamBuilder(@NotNull View view) {
        super(view);
    }


    private ArrayList<String> getIgnoreParams() {
        if (ignoreParams == null) {
            ignoreParams = new ArrayList<>();
            ignoreParams.add("serialVersionUID");
            ignoreParams.add("$change");
            ignoreParams.add("$assertionsDisabled");
        }
        return ignoreParams;
    }

    public ParamBuilder addIgnoreParams(String name) {
        if (TextUtils.isEmpty(name)) {
            return this;
        }
        if (getIgnoreParams().contains(name)) {
            return this;
        }
        getIgnoreParams().add(name);
        return this;
    }

    public HashMap<String, String> buildParamsMap() {
        preBuildParamsMap();
        HashMap<String, String> map = new HashMap<>();
        Field[] allFields = this.getClass().getDeclaredFields();
        for (Field field : allFields) {
            field.setAccessible(true);
            String key = field.getName();
            CommonUtils.log(key, field.getModifiers());
            if ((Modifier.TRANSIENT & field.getModifiers()) > 0) {
                continue;
            }
            if (getIgnoreParams().contains(key)) {
                continue;
            }
            Object value = null;
            try {
                value = field.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (value != null && !TextUtils.isEmpty(value.toString())) {
                map.put(key, value.toString());
            } else if ((Modifier.FINAL & field.getModifiers()) > 0) {
                map.put(key, "");
            }
        }
        return map;
    }

    public void preBuildParamsMap() {

    }


    public static class BaseItemHolder {
        public String value;
        public String nullTipWord;

        public BaseItemHolder value(String value) {
            this.value = value;
            return this;
        }

        public BaseItemHolder nullTipWord(String var) {
            this.nullTipWord = var;
            return this;
        }

        public boolean valueIsNull() {
            return TextUtils.isEmpty(value);
        }


        public BaseItemHolder clear() {
            this.value = null;
            return this;
        }

        @Override
        public String toString() {
            return value;
        }

        public Runnable ans;

        public BaseItemHolder setAns(Runnable ans) {
            this.ans = ans;
            return this;
        }
    }

    /**
     * @param ignoreDataNull 只判断状态码，不判断数据是否为空
     */
    private <T> void checkAndCallBack(
            ResponseMessage<T> rmg,
            DataCallBack<T> success,
            DataCallBack<ResponseMessage<String>> error,
            String emptyString,
            boolean ignoreDataNull) {
        if (emptyString == null) {
            emptyString = "网络错误，数据为空";
        }
        if (error == null) {
            error = (s) -> CommonUtils.log("null CallBack error", s);
        }
        if (success == null) {
            success = (data) -> CommonUtils.log("null CallBack data", data);
        }
        if (rmg == null) {
            error.onSuccess(ResponseMessage.error(emptyString));
            return;
        }
        if (rmg.isSuccess()) {
            if (ignoreDataNull) {
                success.onSuccess(rmg.getData());
                return;
            } else if (rmg.getData() != null) {
                success.onSuccess(rmg.getData());
                return;
            }
        }
        error.onSuccess(ResponseMessage.error(rmg, emptyString));
    }

    /**
     * 状态码错误或者data为空会返回错误
     *
     * @param dataCallBack 数据回调
     * @param DataError    数据为空
     * @param netWorkError 网络错误
     * @param end          无论成功或者失败都会回调
     */
    public <T> void getData(Observable<ResponseMessage<T>> observable,
                            DataCallBack<T> dataCallBack,
                            DataCallBack<ResponseMessage<String>> DataError,
                            DataCallBack<Throwable> netWorkError,
                            DataCallBack<Disposable> start,
                            NullCallBack end,
                            String emptyString) {
        getData0(observable, (d) -> checkAndCallBack(d, dataCallBack, DataError, emptyString, false), netWorkError, start, end);
    }

    public <T> void getData(Observable<ResponseMessage<T>> observable,
                            DataCallBack<T> dataCallBack,
                            DataCallBack<ResponseMessage<String>> DataError,
                            DataCallBack<Throwable> netWorkError,
                            DataCallBack<Disposable> start,
                            NullCallBack end) {
        getData0(observable,
                data -> checkAndCallBack(data, dataCallBack,
                        DataError,
                        null,
                        false),
                netWorkError,
                start,
                end);
    }

//    public <T> void getDataTest(String url,
//                                DataCallBack<T> dataCallBack,
//                                DataCallBack<String> DataError,
//                                DataCallBack<Throwable> netWorkError,
//                                DataCallBack<Disposable> start,
//                                NullCallBack end) {
//        getData0(ServiceFactory.getExhibitionService().getTestUrl(url), (d) -> {
//                    CommonUtils.log(d);
//                    ResponseMessage ans = null;
//                    try {
//                        ans = new ObjectMapper().readValue(d, ResponseMessage.class);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    checkAndCallBack(
//                            ans, dataCallBack, DataError, null, false);
//                },
//                netWorkError,
//                start,
//                end);
//    }

    /**
     * 状态码错误返回错误   data可以为空
     *
     * @param dataCallBack 数据回调
     * @param DataError    数据为空
     * @param netWorkError 网络错误
     * @param end          无论成功或者失败都会回调
     */
    public <T> void getDataIgnoreDataNull(Observable<ResponseMessage<T>> observable,
                                          DataCallBack<T> dataCallBack,
                                          DataCallBack<ResponseMessage<String>> DataError,
                                          DataCallBack<Throwable> netWorkError,
                                          DataCallBack<Disposable> start,
                                          NullCallBack end) {
        getData0(observable, (d) -> checkAndCallBack(d, dataCallBack, DataError, "", true), netWorkError, start, end);
    }

    /**
     * @param dataCallBack 数据回调
     * @param netWorkError 网络错误
     * @param end          无论成功或者失败都会回调
     */
    public <T> void getData0(Observable<T> observable,
                             DataCallBack<T> dataCallBack,
                             DataCallBack<Throwable> netWorkError,
                             DataCallBack<Disposable> start,
                             NullCallBack end) {
        if (observable == null) {
            Log.e("ParamBuilder", "observable is null");
            return;
        }
        RxUtil.execute(observable, new Observer<T>() {
            @Override
            public void onComplete() {
                if (end != null) {
                    end.callBack();
                }
            }

            @Override
            public void onSubscribe(Disposable disposable) {
                addSubscription(disposable);
                if (start != null) {
                    start.onSuccess(disposable);
                }
            }

            @Override
            public void onError(Throwable e) {
                CommonUtils.log(e.getMessage());
                e.printStackTrace();
                if (netWorkError != null) {
                    netWorkError.onSuccess(e);
                }
                onComplete();
            }

            @Override
            public void onNext(T objectResponseMessage) {
                if (objectResponseMessage instanceof ResponseMessage) {
                    ResponseMessage responseMessage = (ResponseMessage) objectResponseMessage;
                    if (responseMessage.getData() instanceof ResponseMessage.NeedResponseTime) {
                        ((ResponseMessage.NeedResponseTime) responseMessage.getData()).setResponseTime(responseMessage.getResponseTime());
                    }
                }
                if (dataCallBack != null) {
                    dataCallBack.onSuccess(objectResponseMessage);
                }
            }
        });
    }

}
