package com.dorianmusaj.cryptolight.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

/**
 * Created by dorian musaj on 20/06/2019.
 */

public class HashUtils {

    public static String getSHA256FromFileContent(File filename)
    {

        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[65536]; //created at start.
            InputStream fis = new FileInputStream(filename);
            int n = 0;
            while (n != -1)
            {
                n = fis.read(buffer);
                if (n > 0)
                {
                    digest.update(buffer, 0, n);
                }
            }
            byte[] digestResult = digest.digest();
            return asHex(digestResult);
        }
        catch (FileNotFoundException e)
        {
            Timber.e("Could not find the file to generate hash %s",filename.getAbsolutePath());
            return null;
        }
        catch (IOException e)
        {
            Timber.e(e,"Error generating hash; IOError");
            return null;
        }
        catch (NoSuchAlgorithmException e)
        {
            Timber.e(e,"Error generating hash; No such algorithm");
            return null;
        }
    }


    public static String getMd5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();


            String secondHex="";

            // Create Hex String
            StringBuilder hexString = new StringBuilder();


            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }

            secondHex =hexString.toString();

            return secondHex;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String asHex(byte[] arrayBytes) {
        StringBuilder stringBuffer = new StringBuilder();
        for (byte arrayByte : arrayBytes) {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
}

