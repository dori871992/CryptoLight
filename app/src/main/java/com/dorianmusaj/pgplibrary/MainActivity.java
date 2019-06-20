package com.dorianmusaj.pgplibrary;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.dorianmusaj.cryptolight.CryptoLight;
import com.dorianmusaj.cryptolight.SensorData;

import java.io.File;

import static com.dorianmusaj.cryptolight.CryptoLight.OPENPGP_FILE_TAG;
import static com.dorianmusaj.cryptolight.PgpUtils.FILE_PUBLIC_KEY_RING;
import static com.dorianmusaj.pgplibrary.LibraryApp.DEBUG_TAG;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //generate proof


        //check if proof exists


    }

    @Override
    protected void onResume() {
        super.onResume();

        /* get publicKey, privateKey

        String publicKey = CryptoLight.getPublicKey(this);
        String privateKey = CryptoLight.getPrivateKey(this);*/

        /* encrypt/decrypt string

        String encryptedMessage = CryptoLight.encrypt(this, "Dorian Musaj is coming amigos!");
        String decryptedMessage = CryptoLight.decrypt(this, encryptedMessage); */


        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/IMG_20190603_164516.jpg");

        String filePath = file.getAbsolutePath();
        String signatureToVerify = "";
        String publicKeyFilePath = new File(getFilesDir(), FILE_PUBLIC_KEY_RING).getAbsolutePath();


        //create signature
        //  File signatureFile = CryptoLight.generateDigitalSignature(this, filePath);
        //File signatureFile = generateSignature(filePath);


        // check if signature exists
        // boolean signatureExists = CryptoLight.checkSignatureExistsForFile(this, filePath);

        // get signature
        // File signatureFileRetrieved = CryptoLight.getSignatureForFile(this, filePath);


        // verify signature
        // boolean verified = CryptoLight.verifySignature(this, filePath, signatureToVerify, publicKeyFilePath);


      /*  SensorData sensorData = new SensorData();

        sensorData.setAccelerometerData("49");
        sensorData.setHumidityData("043");
        sensorData.setTempData("33");

        Uri uri = Uri.fromFile(file);
        //generate proof
        CryptoLight.generateProof(this, uri, sensorData);

        Log.d(DEBUG_TAG, "Generating proof for uri: "+uri.toString());*/


      /*  // check if signature exists
        boolean signatureExists = CryptoLight.checkSignatureExistsForFile(this, filePath);

        //check if proof exists
        boolean proofExists = CryptoLight.checkProofExistsForFile(this, filePath);

        //check if proof exists
        boolean proofSignatureExists = CryptoLight.checkProofSignatureExistsForFile(this, filePath);

        Log.d(DEBUG_TAG, "\n\nSignature exits? " + signatureExists +
                "\nProof exists? " + proofExists +
                "\nProof signature exists? " + proofSignatureExists);

        // get signature
        File signatureFile = CryptoLight.getSignatureForFile(this, filePath);

        //get proof file
        File proofFile = CryptoLight.getProofForFile(this, filePath);

        //get proof signature file
        File proofSignatureFile = CryptoLight.getProofSignatureForFile(this, filePath);

        Log.d(DEBUG_TAG, "\n\nSignature file " + signatureFile +
                "\nProof file " + proofFile +
                "\nProof signature " + proofSignatureFile);

        String signatureContent = CryptoLight.getTextContent(this,signatureFile.getAbsolutePath());
        String proofContent = CryptoLight.getTextContent(this,proofFile.getAbsolutePath());

        String proofSignatureContent = CryptoLight.getTextContent(this,proofSignatureFile.getAbsolutePath());

        Log.d(DEBUG_TAG, "\n\nsignatureContent " + signatureContent +
                "\nproofContent " + proofContent +
                "\nproofSignatureContent " + proofSignatureContent);

                */


    }

    public boolean checkNecessaryPermissions(Context context) {

        int permissionCheck = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            permissionCheck = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            } else
                return true;

        } else
            return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    //we show permission dialog again
                    checkNecessaryPermissions(this);
                    return;
                }
                return;
            }

        }
    }

    public File generateSignature(String filePath) {

        boolean granted = checkNecessaryPermissions(this);
        if (granted)
            return CryptoLight.generateDigitalSignature(this, filePath);

        return null;


    }
}
