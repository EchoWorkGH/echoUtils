package com.echo.library.network;

import android.annotation.SuppressLint;

import com.echo.library.util.CommonUtils;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/6
 * change   :
 * describe :
 */
public class RxUtil {


    public static <T> void execute(Observable<T> t, Observer<T> t1) {
        t.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(t1);
    }

    @SuppressLint("CheckResult")
    public static void execute(final Runnable runnable) {
        Observable
                .create((emitter) -> {
                    runnable.run();
                    emitter.onComplete();
                    CommonUtils.log("CheckResult runnable.run()");
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe((o) -> CommonUtils.log("CheckResult runnable.run()"));
    }

    @SuppressLint("CheckResult")
    public static void runMainThread(final Runnable runnable) {
        Observable
                .create((emitter) -> {
                    runnable.run();
                    emitter.onComplete();
                    CommonUtils.log("CheckResult runnable.run()");
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((o) -> CommonUtils.log("CheckResult runnable.run()"));
    }

    public abstract static class JustObserver<T> implements Observer<T> {

        @Override
        public void onSubscribe(Disposable d) {
            CommonUtils.log("onSubscribe", d);
        }


        @Override
        public void onError(Throwable e) {
            CommonUtils.log("onError", e.getMessage());
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            CommonUtils.log("onComplete");
        }

        @Override
        public void onNext(T o) {
            CommonUtils.log("onNext");
        }
    }
}
