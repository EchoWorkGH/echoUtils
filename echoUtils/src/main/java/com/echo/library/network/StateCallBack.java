package com.echo.library.network;


import com.echo.library.util.CommonUtils;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/16
 * change   :
 * describe :
 */
public interface StateCallBack<T> {
    /**
     * 错误时
     * {@link ResponseMessage#getCode()}  错误码
     * {@link ResponseMessage#getMsg()} 错误信息
     */
    void onError(ResponseMessage<String> error);

    void onSuccess(T data);

    StateCallBack<String> defaultStateCallBack = new StateCallBack<String>() {
        @Override
        public void onError(ResponseMessage<String> error) {
            CommonUtils.log("defaultStateCallBack", error);
        }

        @Override
        public void onSuccess(String data) {
            CommonUtils.log("defaultStateCallBack", data);
        }
    };

}