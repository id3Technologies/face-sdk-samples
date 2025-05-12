package eu.id3.face.samples.recognition_camerax

import android.util.Log
import eu.id3.face.FaceException
import eu.id3.face.FaceLicense
import eu.id3.face.LicenseHardwareCodeType
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val LOG_TAG = "Credentials"

/**
 * This class handles the credentials to use the SDK.
 * Before anything else, update either your license serial key OR your id3 account login/password
 * and package reference!
 */
internal object Credentials {
    /** Activation key is... */
    private fun getLicenseActivationKey() = "00000000-0000-0000-0000-000000000000"

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
    fun registerSdkLicense(licenseFilePath: String): Boolean {
        val licenseFile = File(licenseFilePath)
        val licenseActivationKey = getLicenseActivationKey()
        if (!licenseFile.exists()) {
            Log.v(LOG_TAG, "License file not found on file system.")

            var lic: ByteArray? = null
            /**
             * If the license file does not exist, try to recover it from internet using the
             * specified serial key OR the specified login/password and package reference.
             */
            if (licenseActivationKey == "00000000-0000-0000-0000-000000000000") {
                Log.e(
                    LOG_TAG,
                    "Please update the sample with a correct activation key."
                )
                return false
            }

            try {
                val hardwareCode = FaceLicense.getHostHardwareCode(LicenseHardwareCodeType.ANDROID)
                lic = FaceLicense.activateActivationKeyBuffer(
                    hardwareCode,
                    licenseActivationKey, "Activated from recognition camera x sample"
                )
            } catch (e1: FaceException) {
                e1.printStackTrace()
                Log.e(
                    LOG_TAG,
                    "Error during online license retrieval using activation key: " + e1.message
                )
                return false
            }
            /** Save the license file on storage. */
            val bos: BufferedOutputStream?
            try {
                bos = BufferedOutputStream(FileOutputStream(licenseFile))
                bos.write(lic)
                bos.flush()
                bos.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }

        /** Once the license is downloaded inside the app's folder, try to check it to allow usage
         * of the SDK functions. */
        try {
            FaceLicense.checkLicense(licenseFilePath)
        } catch (e: FaceException) {
            Log.e(LOG_TAG, "License check failed: " + e.message)
            e.printStackTrace()
            return false
        }

        return true
    }
}