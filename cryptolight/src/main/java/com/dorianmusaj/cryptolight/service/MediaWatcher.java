package com.dorianmusaj.cryptolight.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.dorianmusaj.cryptolight.CryptoLight;
import com.dorianmusaj.cryptolight.DeviceInfo;
import com.dorianmusaj.cryptolight.GPSTracker;
import com.dorianmusaj.cryptolight.SensorData;
import com.dorianmusaj.cryptolight.util.HashUtils;
import com.dorianmusaj.cryptolight.PgpUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import timber.log.Timber;


public class MediaWatcher extends BroadcastReceiver {

    private final static String PROOF_FILE_TAG = ".proof.csv";
    private final static String OPENPGP_FILE_TAG = ".asc";
    private final static String PROOF_BASE_FOLDER = "proofmode/";

    private static boolean mStorageMounted = false;
    private GPSTracker gpsTracker;
    private HashMap<String, String> hmProof;
    private Location location;

    public MediaWatcher() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {


        new Thread() {
            public void run() {

                handleIntent(context, intent, false, null);
            }
        }.start();

    }

    public boolean handleIntent(final Context context, Intent intent, boolean forceDoProof, final SensorData sensorData) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean doProof = prefs.getBoolean("doProof", true);

        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_UMS_CONNECTED)) {
                mStorageMounted = true;
            } else if (intent.getAction().equals(Intent.ACTION_UMS_DISCONNECTED)) {
                mStorageMounted = false;
            }
        }

        if (doProof || forceDoProof) {

            if (!isExternalStorageWritable()) {
                //  Toast.makeText(context, R.string.no_external_storage, Toast.LENGTH_SHORT).show();
                return false;
            }

            Uri uriMedia = intent.getData();
            if (uriMedia == null)
                uriMedia = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

            if (uriMedia == null) //still null?
                return false;

            String mediaPathTmp = uriMedia.getPath();

            if (!new File(mediaPathTmp).exists()) {
                //do a better job of handling a null situation
                try {
                    Cursor cursor = context.getContentResolver().query(uriMedia, null, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        mediaPathTmp = cursor.getString(cursor.getColumnIndex("_data"));
                        cursor.close();
                    } else {
                        mediaPathTmp = uriMedia.getPath();
                    }
                } catch (Exception e) {
                    //error looking up file?
                    Timber.w("unable to find source media file for: " + mediaPathTmp, e);
                }
            }


            //get local media path
            final String mediaPath = mediaPathTmp;

            final boolean showDeviceIds = prefs.getBoolean("trackDeviceId", true);
            final boolean showLocation = prefs.getBoolean("trackLocation", false);
          // final boolean autoNotarize = prefs.getBoolean("autoNotarize", true);
            final boolean showMobileNetwork = prefs.getBoolean("trackMobileNetwork", false);

            final String mediaHash = HashUtils.getSHA256FromFileContent(new File(mediaPath));


            if (mediaHash != null) {

                Timber.d("Writing proof for hash %s for path %s", mediaHash, mediaPath);

                //write immediate proof, w/o safety check result
                writeProof(context, mediaPath, mediaHash, showDeviceIds, showLocation, showMobileNetwork, null, false, false, -1, null, sensorData);

               /* Save for later implementation

               if (autoNotarize) {

                    //if we can do safetycheck, then add that in as well
                    new SafetyNetCheck().sendSafetyNetRequest(context, mediaHash, new OnSuccessListener<SafetyNetApi.AttestationResponse>() {
                        @Override
                        public void onSuccess(SafetyNetApi.AttestationResponse response) {
                            // Indicates communication with the service was successful.
                            // Use response.getJwsResult() to get the result data.

                            String resultString = response.getJwsResult();
                            SafetyNetResponse resp = parseJsonWebSignature(resultString);

                            long timestamp = resp.getTimestampMs();
                            boolean isBasicIntegrity = resp.isBasicIntegrity();
                            boolean isCtsMatch = resp.isCtsProfileMatch();

                            Timber.d("Success! SafetyNet result: isBasicIntegrity: " + isBasicIntegrity + " isCts:" + isCtsMatch);
                            writeProof(context, mediaPath, mediaHash, showDeviceIds, showLocation, showMobileNetwork, resultString, isBasicIntegrity, isCtsMatch, timestamp, null, sensorData);


                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // An error occurred while communicating with the service.
                            Timber.d("SafetyNet check failed", e);
                        }
                    });


                    final NotarizationProvider nProvider = new OpenTimestampsNotarizationProvider();

                    try {

                        nProvider.notarize("ProofMode Media Hash: " + mediaHash, new File(mediaPath), new NotarizationListener() {
                            @Override
                            public void notarizationSuccessful(String timestamp) {

                                Timber.d("Got OpenTimestamps success response timestamp: " + timestamp);
                                writeProof(context, mediaPath, mediaHash, showDeviceIds, showLocation, showMobileNetwork, null, false, false, -1, "OpenTimestamps: " + timestamp, sensorData);
                            }

                            @Override
                            public void notarizationFailed(int errCode, String message) {

                                Timber.d("Got OpenTimestamps error response: " + message);
                                writeProof(context, mediaPath, mediaHash, showDeviceIds, showLocation, showMobileNetwork, null, false, false, -1, "OpenTimestamps Error: " + message, sensorData);

                            }
                        });
                    } catch (Exception e) {
                        // Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                    *//**
                 final TimeBeatNotarizationProvider tbNotarize = new TimeBeatNotarizationProvider(context);
                 tbNotarize.notarize("ProofMode Media Hash: " + mediaHash, new File(mediaPath), new NotarizationListener() {
                @Override public void notarizationSuccessful(String timestamp) {

                Timber.d("Got Timebeat success response timestamp: " + timestamp);
                writeProof(context, mediaPath, mediaHash, showDeviceIds, showLocation, showMobileNetwork, null, false, false, -1, "TimeBeat: " + timestamp);
                }

                @Override public void notarizationFailed(int errCode, String message) {

                Timber.d("Got Timebeat error response: " + message);
                writeProof(context, mediaPath, mediaHash, showDeviceIds, showLocation, showMobileNetwork, null, false, false, -1, "TimeBeat Error: " + message);

                }
                });
                 **//*

                }*/

                return true;
            } else {
                Timber.d("Unable to access media files, no proof generated");
            }
        }

        return false;
    }


/* Save for later implementation

    private SafetyNetResponse parseJsonWebSignature(String jwsResult) {
        if (jwsResult == null) {
            return null;
        }
        //the JWT (JSON WEB TOKEN) is just a 3 base64 encoded parts concatenated by a . character
        final String[] jwtParts = jwsResult.split("\\.");

        if (jwtParts.length == 3) {
            //we're only really interested in the body/payload
            String decodedPayload = new String(Base64.decode(jwtParts[1], Base64.DEFAULT));

            return SafetyNetResponse.parse(decodedPayload);
        } else {
            return null;
        }
    }*/

    private void writeProof(Context context, String mediaPath, String hash, boolean showDeviceIds, boolean showLocation, boolean showMobileNetwork, String safetyCheckResult, boolean isBasicIntegrity, boolean isCtsMatch, long notarizeTimestamp, String notes, SensorData sensorData) {

        //media file
        File fileMedia = new File(mediaPath);
        File fileFolder =new File(CryptoLight.getSignaturesFolder(context));

        if (fileFolder != null) {

            //media signature file
            File fileMediaSig = new File(fileFolder, fileMedia.getName() + OPENPGP_FILE_TAG);
            //media proof file
            File fileMediaProof = new File(fileFolder, fileMedia.getName() + PROOF_FILE_TAG);
            //media proof file signature
            File fileMediaProofSig = new File(fileFolder, fileMedia.getName() + PROOF_FILE_TAG + OPENPGP_FILE_TAG);

            try {

                //add data to .asc file by signing the media file
                if (!fileMediaSig.exists())
                    PgpUtils.getInstance(context).createDetachedSignature(fileMedia, fileMediaSig, PgpUtils.DEFAULT_PASSWORD);

                //add data to proof.csv and sign it
                boolean writeHeaders = !fileMediaProof.exists();

                String proofText = buildProof(context, mediaPath, writeHeaders, showDeviceIds, showLocation, showMobileNetwork, safetyCheckResult, isBasicIntegrity, isCtsMatch, notarizeTimestamp, notes, sensorData);

                //String pgpEncrypted= PgpUtils.getInstance(context).encrypt(proofText);

                // String pgpDecrypted= PgpUtils.getInstance(context).decrypt(pgpEncrypted, PgpUtils.DEFAULT_PASSWORD);


                writeTextToFile(fileMediaProof, proofText);

                if (fileMediaProof.exists()) {
                    //sign the proof file again
                    PgpUtils.getInstance(context).createDetachedSignature(fileMediaProof, fileMediaProofSig, PgpUtils.DEFAULT_PASSWORD);
                }
            } catch (Exception e) {
                Log.e("MediaWatcher", "Error signing media or proof", e);
            }
        }
    }

    public static File getHashStorageDir(String hash) {

        // Get the directory for the user's public pictures directory.
        File fileParentDir = null;

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            fileParentDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), PROOF_BASE_FOLDER);

        } else {
            fileParentDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), PROOF_BASE_FOLDER);
        }

        if (!fileParentDir.exists()) {
            if (!fileParentDir.mkdir()) {
                fileParentDir = new File(Environment.getExternalStorageDirectory(), PROOF_BASE_FOLDER);
                if (!fileParentDir.exists())
                    if (!fileParentDir.mkdir())
                        return null;
            }
        }

        File fileHashDir = new File(fileParentDir, hash + '/');
        if (!fileHashDir.exists())
            if (!fileHashDir.mkdir())
                return null;

        return fileHashDir;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private String buildProof(Context context, String mediaPath, boolean writeHeaders, boolean showDeviceIds, boolean showLocation, boolean showMobileNetwork, String safetyCheckResult, boolean isBasicIntegrity, boolean isCtsMatch, long notarizeTimestamp, String notes, SensorData sensorData) {
        File fileMedia = new File(mediaPath);
        String hash = getSHA256FromFileContent(mediaPath);

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);

        hmProof = new HashMap<>();

        hmProof.put("File Path", mediaPath);
        hmProof.put("File Hash SHA256", hash);
        hmProof.put("File Modified", df.format(new Date(fileMedia.lastModified())));
        hmProof.put("Proof Generated", df.format(new Date()));

        //device info
        if (showDeviceIds) {
            hmProof.put("DeviceID", DeviceInfo.getDeviceId(context));
            hmProof.put("Wifi MAC", DeviceInfo.getWifiMacAddr());
        }


        //device ip and network info
        hmProof.put("IPv4", DeviceInfo.getDeviceInfo(context, DeviceInfo.Device.DEVICE_IP_ADDRESS_IPV4));
        hmProof.put("IPv6", DeviceInfo.getDeviceInfo(context, DeviceInfo.Device.DEVICE_IP_ADDRESS_IPV6));

        hmProof.put("DataType", DeviceInfo.getDataType(context));
        hmProof.put("Network", DeviceInfo.getDeviceInfo(context, DeviceInfo.Device.DEVICE_NETWORK));
        hmProof.put("NetworkType", DeviceInfo.getNetworkType(context));


        //device hardware info
        hmProof.put("Hardware", DeviceInfo.getDeviceInfo(context, DeviceInfo.Device.DEVICE_HARDWARE_MODEL));
        hmProof.put("Manufacturer", DeviceInfo.getDeviceInfo(context, DeviceInfo.Device.DEVICE_MANUFACTURE));
        hmProof.put("ScreenSize", DeviceInfo.getDeviceInch(context));

        //device localize info
        hmProof.put("Language", DeviceInfo.getDeviceInfo(context, DeviceInfo.Device.DEVICE_LANGUAGE));
        hmProof.put("Locale", DeviceInfo.getDeviceInfo(context, DeviceInfo.Device.DEVICE_LOCALE));


        gpsTracker = new GPSTracker(context);

        if (showLocation && gpsTracker.canGetLocation()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    location = gpsTracker.getSavedLocation();
                    //device location info9
                    if (location != null) {
                        hmProof.put("Location.Latitude", location.getLatitude() + "");
                        hmProof.put("Location.Longitude", location.getLongitude() + "");
                        hmProof.put("Location.Provider", location.getProvider());
                        hmProof.put("Location.Accuracy", location.getAccuracy() + "");
                        hmProof.put("Location.Altitude", location.getAltitude() + "");
                        hmProof.put("Location.Bearing", location.getBearing() + "");
                        hmProof.put("Location.Speed", location.getSpeed() + "");
                        hmProof.put("Location.Time", location.getTime() + "");
                    }
                }
            }, 2000);

            //////
            Location loc = gpsTracker.getLocation();
           /* int waitIdx = 0;
            while (loc == null && waitIdx < 3)
            {
                waitIdx++;
                try { Thread.sleep (1000); }
                catch (Exception e){}
                loc = gpsTracker.getLocation();
            }

            //device location info
            if (loc != null) {

                hmProof.put("Location.Latitude",loc.getLatitude()+"");
                hmProof.put("Location.Longitude",loc.getLongitude()+"");
                hmProof.put("Location.Provider",loc.getProvider());
                hmProof.put("Location.Accuracy",loc.getAccuracy()+"");
                hmProof.put("Location.Altitude",loc.getAltitude()+"");
                hmProof.put("Location.Bearing",loc.getBearing()+"");
                hmProof.put("Location.Speed",loc.getSpeed()+"");
                hmProof.put("Location.Time",loc.getTime()+"");
            }else
                Log.d("DORIAN","Couldn't get location" );*/


        } else
            Log.d("DORIAN", "GPS is off.");

        if (!TextUtils.isEmpty(safetyCheckResult)) {
            hmProof.put("SafetyCheck", safetyCheckResult);
            hmProof.put("SafetyCheckBasicIntegrity", isBasicIntegrity + "");
            hmProof.put("SafetyCheckCtsMatch", isCtsMatch + "");
            hmProof.put("SafetyCheckTimestamp", df.format(new Date(notarizeTimestamp)));
        } else {
            hmProof.put("SafetyCheck", "");
            hmProof.put("SafetyCheckBasicIntegrity", "");
            hmProof.put("SafetyCheckCtsMatch", "");
            hmProof.put("SafetyCheckTimestamp", "");
        }

        hmProof.putAll(new HashMap<String, String>());

        hmProof.put("Notes", !TextUtils.isEmpty(notes) ? notes : "");

        if (sensorData != null) {
            hmProof.put("Light", sensorData.getLightData());
            hmProof.put("Temperature", sensorData.getTempData());
            hmProof.put("Accelerometer", sensorData.getAccelerometerData());
            hmProof.put("Proximity", sensorData.getProximityData());
            hmProof.put("Humidity", sensorData.getHumidityData());
            hmProof.put("Pressure", sensorData.getPressureData());
        } else
            Log.d("DORIAN", "Couldn't get sensors!");


        if (showMobileNetwork)
            hmProof.put("CellInfo", DeviceInfo.getCellInfo(context));

        StringBuffer sb = new StringBuffer();

        if (writeHeaders) {
            for (String key : hmProof.keySet()) {
                sb.append(key).append(",");
            }

            sb.append("\n");
        }

        for (String key : hmProof.keySet()) {
            String value = hmProof.get(key);
            if (value != null)
                value = value.replace(',', ' '); //remove commas from CSV file
            else
                value = "";

            sb.append(value).append(",");
        }

        sb.append("\n");

        return sb.toString();

    }

    public static void writeTextToFile(File fileOut, String text) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(fileOut, true));
            ps.println(text);
            ps.flush();
            ps.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }


    private static String getSHA256FromFileContent(String filename) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[65536]; //created at start.
            InputStream fis = new FileInputStream(filename);
            int n = 0;
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            byte[] digestResult = digest.digest();
            return asHex(digestResult);
        } catch (Exception e) {
            return null;
        }
    }

    private static String asHex(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }
}

