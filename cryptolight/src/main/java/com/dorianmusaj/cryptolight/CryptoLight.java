package com.dorianmusaj.cryptolight;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.dorianmusaj.cryptolight.service.MediaWatcher;

import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPPublicKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;

import static com.dorianmusaj.cryptolight.PgpUtils.DEFAULT_PASSWORD;
import static com.dorianmusaj.cryptolight.PgpUtils.FILE_PUBLIC_KEY_RING;
import static com.dorianmusaj.cryptolight.PgpUtils.FILE_SECRET_KEY_RING;
import static com.dorianmusaj.cryptolight.PgpUtils.createQueryStringForParameters;
import static com.dorianmusaj.cryptolight.util.CommonUtils.readFromFile;

public class CryptoLight {

    public final static String PROOF_FILE_TAG = ".proof.csv";
    public final static String OPENPGP_FILE_TAG = ".asc";


    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static PgpUtils mInstance;

    private static boolean mInit = false;


    public synchronized static void init(Context context) {
        if (mInit)
            return;

        mInit = true;

        initialize(context); //initialize keypair generation

    }

    private static void initialize(Context context) {
        mInstance = PgpUtils.getInstance(context);
    }

    public static File generateDigitalSignature(Context context, String mediaFilePath /*e, File mediaSignature, String password*/) {

        File mediaFile =  new File(mediaFilePath);

        if(!mediaFile.exists())
            return null;

        try {

            File mediaSignature = new File(getSignaturesFolder(context), mediaFile.getName() + OPENPGP_FILE_TAG);
            PgpUtils.getInstance(context).createDetachedSignature(mediaFile, mediaSignature, DEFAULT_PASSWORD);
            return mediaSignature;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean generateDigitalSignature(Context context, InputStream mediaFile, OutputStream mediaSignature, String password) {
        try {
            PgpUtils.getInstance(context).createDetachedSignature(mediaFile, mediaSignature, password);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean verifySignature(Context context, String filePath, String signatureFilePath, String publicKeyFilePath) {
        return PgpUtils.getInstance(context).verifySignature(filePath, signatureFilePath, publicKeyFilePath);
    }

    public static boolean verifySignatureFromInputStream(Context context, String filePath, String signaureFilePath, String publicKeyFilePath) {
        return PgpUtils.getInstance(context).verifySignatureFromIs(context, filePath, signaureFilePath, publicKeyFilePath);
    }

    public static String getPublicKey(Context context) {
        return readFromFile(new File(context.getFilesDir(), FILE_PUBLIC_KEY_RING).getAbsolutePath());

    }

    public static String getPrivateKey(Context context) {
        return readFromFile(new File(context.getFilesDir(), FILE_SECRET_KEY_RING).getAbsolutePath());

    }

    //uses public key for encryption
    public static String encrypt(Context context, String message) {
        if (mInstance == null)
            mInstance = PgpUtils.getInstance(context);

        try {
            return mInstance.encrypt(message);
        } catch (IOException e) {

            return "Error during encryption: " + e.getMessage();
        } catch (PGPException e) {
            return "Error during encryption: " + e.getMessage();
        }

    }

    //uses private key for decrypting
    public static String decrypt(Context context, String encryptedMessage/*, String password*/) {

        if (mInstance == null)
            mInstance = PgpUtils.getInstance(context);


        try {
            return mInstance.decrypt(encryptedMessage, PgpUtils.DEFAULT_PASSWORD);
        } catch (Exception e) {

            return "Error during decryption: " + e.getMessage();
        }

    }

    public static void generateProof(Context context, Uri uri, SensorData sensorData) {
        Intent intent = new Intent();
        intent.setData(uri);
        new MediaWatcher().handleIntent(context, intent, true, sensorData);
    }

    public static boolean checkSignatureExistsForFile(Context context, String filePath) {

        File mediaFile = new File(filePath);
        //File fileFolder = mediaFile.getParentFile();

        if (mediaFile.exists()) {
            File fileMediaSig = new File(getSignaturesFolder(context), mediaFile.getName() + OPENPGP_FILE_TAG);
            return fileMediaSig.exists();
        } else
            return false;
    }

    public static File getSignatureForFile(Context context, String filePath) {

        File mediaFile = new File(filePath);
        // File fileFolder = mediaFile.getParentFile();

        if (mediaFile.exists()) {
            File fileMediaSig = new File(getSignaturesFolder(context), mediaFile.getName() + OPENPGP_FILE_TAG);

            if (fileMediaSig.exists())
                return fileMediaSig;
            else
                return null;
        } else
            return null;

    }


    public static boolean checkProofExistsForFile(Context context, String filePath) {

        File mediaFile = new File(filePath);
        File fileFolder = new File (getSignaturesFolder(context));

        if (mediaFile.exists()) {
            File fileMediaProof = new File(fileFolder, mediaFile.getName() + PROOF_FILE_TAG);
            return fileMediaProof.exists();
        } else
            return false;

    }

    public static File getProofForFile(Context context, String filePath) {

        File mediaFile = new File(filePath);
        File fileFolder = new File(getSignaturesFolder(context));

        if (mediaFile.exists()) {
            File fileMediaProof = new File(fileFolder, mediaFile.getName() + PROOF_FILE_TAG);

            if (fileMediaProof.exists())
                return fileMediaProof;
            else
                return null;
        } else
            return null;

    }


    public static boolean checkProofSignatureExistsForFile(Context context, String filePath) {

        File mediaFile = new File(filePath);
        File fileFolder = new File (getSignaturesFolder(context));

        if (mediaFile.exists()) {
            File fileMediaProofSig = new File(fileFolder, mediaFile.getName() + PROOF_FILE_TAG + OPENPGP_FILE_TAG);

            return fileMediaProofSig.exists();
        } else
            return false;

    }


    public static File getProofSignatureForFile(Context context, String filePath) {

        File mediaFile = new File(filePath);
        File fileFolder = new File(getSignaturesFolder(context));

        if (mediaFile.exists()) {
            File fileMediaProof = new File(fileFolder, mediaFile.getName() + PROOF_FILE_TAG + OPENPGP_FILE_TAG);

            if (fileMediaProof.exists())
                return fileMediaProof;
            else
                return null;
        } else
            return null;

    }

    public static String getTextContent(Context context, String path){
      return readFromFile(path);

    }



   private static String SIGNATURES_FOLDER_NAME = "Signatures";

   public static String getSignaturesFolder(Context context){

       String fullFolderPath = context.getFilesDir()+"/"+SIGNATURES_FOLDER_NAME;
       File folder=  new File(fullFolderPath);
       folder.mkdirs();

       return fullFolderPath;
   }
}



