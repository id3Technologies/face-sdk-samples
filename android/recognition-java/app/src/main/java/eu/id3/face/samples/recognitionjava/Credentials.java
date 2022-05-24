package eu.id3.face.samples.recognitionjava;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import eu.id3.face.FaceException;
import eu.id3.face.FaceLibrary;
import eu.id3.face.License;
import eu.id3.face.LicenseHardwareCodeType;

/**
 * This class handles the credentials to use the SDK.
 * Before anything else, update either your license serial key OR your id3 account login/password
 * and package reference!
 */
class Credentials {
    /**
     * Serial key is...
     */
    private final static String licenseSerialKey = "0000-0000-0000-0000";
    /**
     * id3 account login is ...
     */
    private final static String accountLogin = "login";
    /**
     * id3 account password is ...
     */
    private final static String accountPassword = "password";
    /**
     * Package reference is ...
     */
    private final static String packageReference = "86FM2780";

    /**
     * Any id3 SDK needs a valid license to work.
     * An id3 license file is only valid for ONE application. Each application must retrieve its own
     * license file.
     * This SDK provides a specific API to directly download license files from the developed
     * applications.
     * This function tries to load a license file from internal storage and register it.
     * If the license file do not exist it tries to download a new license using the specified
     * serial key.
     * For deployment purposes there is also an API to use your id3 account to retrieve the license
     * file.
     * This API needs the login/password and product package of the SDK.
     */
    public static boolean registerSdkLicense(String licenseFilePath) {
        File licenseFile = new File(licenseFilePath);
        String LOG_TAG = "Credentials Class";
        if (!licenseFile.exists()) {
            Log.v(LOG_TAG, "License file not found on file system.");

            byte[] lic = null;
            /*
             * If the license file does not exist, try to recover it from internet using the
             * specified serial key OR the specified login/password and package reference.
             */
            if (licenseSerialKey.equals("0000-0000-0000-0000") &&
                    (accountLogin.equals("login") ||
                            accountPassword.equals("password") ||
                            packageReference.equals("00000000"))
            ) {
                Log.e(
                        LOG_TAG,
                        "Please update the sample with a correct serial key OR a correct id3 account login/password and package reference."
                );
                return false;
            }

            String hardwareCode = License.getHostHardwareCode(LicenseHardwareCodeType.ANDROID);
            if (!licenseSerialKey.equals("0000-0000-0000-0000")) {
                try {
                    lic = License.activateSerialKeyBuffer(
                            hardwareCode,
                            licenseSerialKey, "Activated from recognition sample"
                    );
                } catch (FaceException e1) {
                    e1.printStackTrace();
                    Log.e(
                            LOG_TAG,
                            "Error during online license retrieval using serial key: " + e1.getMessage()
                    );
                    return false;
                }
            }
            if (!accountLogin.equals("login") &&
                    !accountPassword.equals("password") &&
                    !packageReference.equals("00000000")
            ) {
                try {
                    lic = License.activateBuffer(
                            hardwareCode,
                            accountLogin,
                            accountPassword,
                            packageReference,
                            "Activated from Kotlin Credentials sample class."
                    );
                } catch (FaceException e1) {
                    e1.printStackTrace();
                    Log.e(
                            LOG_TAG,
                            "Error during online license retrieval using id3 account and package reference: " + e1.getMessage()
                    );
                    return false;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            /* Save the license file on storage. */
            BufferedOutputStream bos;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(licenseFile));
                bos.write(lic);
                bos.flush();
                bos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /* Once the license is downloaded inside the app's folder, try to check it to allow usage
         * of the SDK functions. */
        try {
            FaceLibrary.checkLicense(licenseFilePath);
        } catch (FaceException e) {
            Log.e(LOG_TAG, "License check failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
