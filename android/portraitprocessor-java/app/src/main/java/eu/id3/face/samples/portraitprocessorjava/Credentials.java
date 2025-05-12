package eu.id3.face.samples.portraitprocessorjava;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import eu.id3.face.FaceException;
import eu.id3.face.FaceLibrary;
import eu.id3.face.FaceLicense;
import eu.id3.face.LicenseHardwareCodeType;

/**
 * This class handles the credentials to use the SDK.
 * Before anything else, update either your license activation key
 * and package reference!
 */
class Credentials {
    /**
     * Activation key is...
     */
    private static String getLicenseActivationKey() { return "00000000-0000-0000-0000-000000000000"; }

    /**
     * Any id3 SDK needs a valid license to work.
     * An id3 license file is only valid for ONE application. Each application must retrieve its own
     * license file.
     * This SDK provides a specific API to directly download license files from the developed
     * applications.
     * This function tries to load a license file from internal storage and register it.
     * If the license file do not exist it tries to download a new license using the specified
     * activation key.
     * This API needs the login/password and product package of the SDK.
     */
    public static boolean registerSdkLicense(String licenseFilePath) {
        File licenseFile = new File(licenseFilePath);
        String LOG_TAG = "Credentials Class";
        String licenseActivationKey = getLicenseActivationKey();

        if (!licenseFile.exists()) {
            Log.v(LOG_TAG, "License file not found on file system.");

            byte[] lic = null;
            /*
             * If the license file does not exist, try to recover it from internet using the
             * specified activation key.
             */
            if (licenseActivationKey.equals("00000000-0000-0000-0000-000000000000")) {
                Log.e(
                        LOG_TAG,
                        "Please update the sample with a correct activation key."
                );
                return false;
            }

            try {
                String hardwareCode = FaceLicense.getHostHardwareCode(LicenseHardwareCodeType.ANDROID);
                lic = FaceLicense.activateActivationKeyBuffer(
                        hardwareCode,
                        licenseActivationKey, "Activated from portrait processor sample"
                );
            } catch (FaceException e1) {
                e1.printStackTrace();
                Log.e(
                        LOG_TAG,
                        "Error during online license retrieval using activation key: " + e1.getMessage()
                );
                return false;
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
            FaceLicense.checkLicense(licenseFilePath);
        } catch (FaceException e) {
            Log.e(LOG_TAG, "License check failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
