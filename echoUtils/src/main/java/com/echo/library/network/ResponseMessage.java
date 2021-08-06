package com.echo.library.network;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2021/7/20
 * change   :
 * describe :
 */
public interface ResponseMessage<T> {
    int getCode();

    int getStatus();

    String getMsg();

    T getData();

    long getResponseTime();

    boolean isSuccess();

    public static ResponseMessage<String> error(int errorCode, String error) {
        return new ResponseMessage<String>() {
            @Override
            public int getCode() {
                return errorCode;
            }

            @Override
            public int getStatus() {
                return 0;
            }

            @Override
            public String getMsg() {
                return error;
            }

            @Override
            public String getData() {
                return null;
            }

            @Override
            public long getResponseTime() {
                return 0;
            }

            @Override
            public boolean isSuccess() {
                return false;
            }
        };
    }

    static ResponseMessage<String> error(String error) {
        return error(-1, error);
    }

    public static ResponseMessage<String> error(ResponseMessage<?> rmg, String emptyString) {
        return error(rmg.getCode(), emptyString);
    }


    interface NeedResponseTime {
        void setResponseTime(long time);
    }
}
