package com.dorianmusaj.cryptolight.util;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CommonUtils {

    public static String readFromFile(String filePath) {


        String data = "";

        if (!new File(filePath).exists())
            return data;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {

            InputStream inputStream = new FileInputStream(filePath);

            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            data = byteArrayOutputStream.toString();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
