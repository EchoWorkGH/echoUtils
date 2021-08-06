package com.echo.library;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/8/5
 * change   :
 * describe : 包含2个数据的类型
 */
public class Data2<A, B> {
    public A a;
    public B b;

    public Data2() {
    }

    public Data2(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public Data2<A, B> setA(A a) {
        this.a = a;
        return this;
    }

    public Data2<A, B> setB(B b) {
        this.b = b;
        return this;
    }
}
