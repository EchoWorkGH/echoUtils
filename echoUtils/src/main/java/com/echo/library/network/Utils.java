package com.echo.library.network;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2021/7/20
 * change   :
 * describe :  这里主要是避免Response的输出流{@link ResponseBody#string()}调用一次后buffer就关闭的问题
 */
public class Utils {

    public static String justGetString(Response response) {
        Buffer buffer;
        try {
            buffer = getBufferClone(response);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        if (buffer == null) {
            return "";
        }
        Charset charset = UTF8;
        MediaType contentType = response.body().contentType();
        if (contentType != null) {
            try {
                charset = contentType.charset(UTF8);
            } catch (UnsupportedCharsetException e) {
                return "";
            }
        }
        return buffer.readString(charset);
    }

    public static Response clone(Response response) {
        try {
            response = response.newBuilder()
                    .body(ResponseBody.create(justGetString(response), response.body().contentType()))
                    .build();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return response;
    }

    static private Buffer getBufferClone(Response response) throws IOException {
        if (response == null) {
            return null;
        }
        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        if (!bodyEncoded(response.headers())) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.getBuffer();
            if (!isPlaintext(buffer)) {
                return null;
            }
            if (contentLength != 0) {
                return buffer.clone();
            }
        }
        return null;
    }


    private static final Charset UTF8 = StandardCharsets.UTF_8;

    static private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }

    static boolean isPlaintext(Buffer buffer) throws EOFException {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }
}
