package com.echo.library.util;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/8/5
 * change   :
 * describe : 测试用
 */
public class Test {

    static int testCount = 0;

    static String[] testString = {
            "WESDFSDFEWFWE",
            "a",
            "一汽丰田 · 汉兰达",
            "お問い合わせ",
            "先程認証コードを発行しましたので",
            "引き継ぎアカウントを連携すると，请等待经回复",
            "等待",
            "5000",
            "可以确认最近20次的购买记录"
    };

    public static String getTestName(int order) {
        return testString[order % testString.length];
    }

    public static String getTestName() {
        testCount++;
        return getTestName(testCount);
    }
}
