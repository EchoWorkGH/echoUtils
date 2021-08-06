package com.echo.library.network;

import androidx.annotation.NonNull;

import com.echo.library.util.CommonUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/8/25
 * change   :
 * describe :
 */
public class RxBus {
    private static RxBus instance;

    public static synchronized RxBus $() {
        if (null == instance) {
            instance = new RxBus();
        }
        return instance;
    }

    private RxBus() {
    }

    //    @SuppressWarnings("rawtypes")
//    private ConcurrentHashMap<Object, List<Subject>> subjectMapper = new ConcurrentHashMap();
    @SuppressWarnings("rawtypes")
    private final ConcurrentHashMap<Object, List<WeakReference<Subject>>> subjectMapper = new ConcurrentHashMap();


    /**
     * 注册事件源
     *
     * @param tag
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    public <T> Observable<T> register(@NonNull Object tag) {
        List<WeakReference<Subject>> subjectList = subjectMapper.get(tag);
        if (null == subjectList) {
            subjectList = new ArrayList();
            subjectMapper.put(tag, subjectList);
        }

        Subject<T> subject = PublishSubject.create();
        subjectList.add(new WeakReference<>(subject));
        return subject;
    }

    public <T> Observable<T> registerLogin(@NonNull Object tag) {
        return register("login");
    }

    @SuppressWarnings("rawtypes")
    public void unregister(@NonNull Object tag) {
        List<WeakReference<Subject>> subjects = subjectMapper.get(tag);
        if (null != subjects) {
            subjectMapper.remove(tag);
        }
    }

    public void unregisterDelay(@NonNull Object tag, long delayTime) {
        List<WeakReference<Subject>> subjects = subjectMapper.get(tag);
        if (null != subjects) {
            try {
                Thread.sleep(delayTime);
                subjectMapper.remove(tag);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 取消监听
     *
     * @param tag
     * @param observable
     * @return
     */
    @SuppressWarnings("rawtypes")
    public RxBus unregister(@NonNull Object tag,
                            @NonNull Observable<?> observable) {
        List<WeakReference<Subject>> subjects = subjectMapper.get(tag);
        if (subjects == null) {
            return instance;
        }
        Iterator<WeakReference<Subject>> iterator = subjects.iterator();
        while (iterator.hasNext()) {
            WeakReference<Subject> weakReference = iterator.next();
            if (weakReference.get() == null) {
                iterator.remove();
            }
        }
        if (isEmpty(subjects)) {
            subjectMapper.remove(tag);
        }
        return instance;
    }

    public void post(@NonNull Object content) {
        post(content, content);
    }

    /**
     * 触发事件
     *
     * @param content
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized void post(@NonNull Object tag, @NonNull Object content) {
        List<WeakReference<Subject>> subjectList = subjectMapper.get(tag);
        if (isEmpty(subjectList)) {
            CommonUtils.log("post isEmpty", tag, "  null");
            return;
        }
        Iterator<WeakReference<Subject>> iteratorWeakReference = subjectList.iterator();
        while (iteratorWeakReference.hasNext()) {
            WeakReference<Subject> subject = iteratorWeakReference.next();
//            Subject tmp = subject.get();
            CommonUtils.log("post subjectList ", tag, subject);
            if (subject == null) {
                CommonUtils.log("subject == null");
                continue;
            }
            if (subject.get() == null) {
                CommonUtils.log("subject.get() == null");
                iteratorWeakReference.remove();
                continue;
            }
            subject.get().onNext(content);
        }
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Collection collection) {
        return null == collection || collection.isEmpty();
    }

    public static class Event {
        /**
         * 登录成功
         */
        public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
        /**
         * 公告显示完成
         */
        public static final String NOTICES_OVER = "NOTICES_OVER";
        /**
         * 切换登录成功
         */
        public static final String CHANGE_LOGIN_SUCCESS = "CHANGE_LOGIN_SUCCESS";
        /**
         * 绑定账号成功
         */
        public static final String BIND_SUCCESS = "BIND_SUCCESS";
        /**
         * GH创建成功
         */
        public static final String GH_CREATE_SUCCEED = "GH_CREATE_SUCCEED";
        /**
         * 客服发送问题成功
         */
        public static final String CS_SEND_SUCCEED = "CS_SEND_SUCCEED";
        /**
         * 客服追问问题成功
         */
        public static final String CS_ASK_SEND_SUCCEED = "CS_ASK_SEND_SUCCEED";
        /**
         * 引继账号设置密码完成
         */
        public static final String GUEST_SET_PASS_WORD = "GUEST_SET_PASS_WORD";
        /**
         * 被人挤下去了
         */
        public static final String LOGIN_LOSE = "LOGIN_LOSE";
        /**
         * 保存外部完成
         */
        public static final String SAVE_OUT_OVER = "SAVE_OUT_OVER";
        /**
         * log
         */
        public static final String LOG = "LOG";
        /**
         * 删除资料
         */
        public static final String DEL_ACCOUNT_DATA = "DEL_ACCOUNT_DATA";
    }
}