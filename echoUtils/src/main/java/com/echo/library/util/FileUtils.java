package com.echo.library.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by mike.chen on 2017/7/7.
 */
public class FileUtils {

    public static String getPicturesDir(Activity activity) throws Exception {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (!SystemUtils.checkPermissions(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    1004)) {
                throw new Exception();
            }
            return getExternalPicturesDir(activity);
        }
        return activity.getFilesDir().getPath();
    }

    public static String getExternalPicturesDir(Context context) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), context.getPackageName());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getPath();
    }

    public static String readContent(File file) {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "";

        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getNameWithoutExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index != -1) {
            return name.substring(0, index);
        }
        return name;
    }

    public static String getExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index != -1) {
            return name.substring(index + 1);
        }
        return "";
    }
}
