package com.echo.library.util;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.Request;
import okio.Buffer;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/6
 * change   :
 * describe :
 */
public class StringUtils {
    public static final SimpleDateFormat FILE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    public static final SimpleDateFormat TIMER_FORMAT = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
    static final String USERNAME_RULE = "^[a-zA-Z0-9]{6,20}$";
    static final String PASSWORD_RULE = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20}$";
    static final String EMAIL_RULE = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

    public static String queryString(Map<String, String> map) {
        if (map.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public static boolean validAccount(String account) {
        return account.matches(USERNAME_RULE);
    }

    public static boolean validPassword(String password) {
        return password.matches(PASSWORD_RULE);
    }

    public static boolean validEmail(String email) {
        return email.matches(EMAIL_RULE);
    }

    // https://stackoverflow.com/questions/415953
    public static String md5(String value) {
        if (value == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(value.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String sha1(String value) {
        if (value == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] array = md.digest(value.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        }
        return Html.fromHtml(html);
    }

    public static String bodyToString(Request request) {
        if (request.body() == null) {
            return "";
        }
        try {
            if (request.body() instanceof MultipartBody) {
                return "MultipartBody";
            }
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTodayDate() {
        return DATE_FORMAT.format(System.currentTimeMillis());
    }

    /**
     * 时间戳转换成字符窜
     * Created by Vito on 2019/8/28.
     *
     * @param milSecond 時間戳
     * @param pattern   匹配格式
     * @return 日期
     */
    public static String getDateToString(long milSecond, String pattern) {
        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.format(date);
    }

    /**
     * 将字符串转为时间戳
     * Created by Vito on 2019/8/28.
     *
     * @param dateString 時間字符串
     * @param pattern    匹配格式
     * @return 時間戳
     */
    public static long getStringToDate(String dateString, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
}
