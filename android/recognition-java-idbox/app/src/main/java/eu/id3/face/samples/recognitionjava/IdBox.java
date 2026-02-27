package eu.id3.face.samples.recognitionjava;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import eu.id3.face.FaceLicense;

/**
 * Helper class for managing an ELYCTIS ID-BOX USB connection on Android.
 * Handles device discovery, USB permission requests, and connection lifecycle.
 *
 * <p>Usage:</p>
 * <pre>
 *   IdBox idBox = new IdBox(activity);
 *   idBox.open(new IdBox.Listener() {
 *       public void onReady() {
 *           // ID-BOX is ready. Call License.getHostHardwareCode(LicenseHardwareCodeType.ID_BOX), etc.
 *       }
 *       public void onError(String message) { }
 *       public void onDetached() { }
 *   });
 *
 *   // When done:
 *   idBox.close();
 *
 *   // In Activity.onDestroy():
 *   idBox.release();
 * </pre>
 */
public class IdBox {

    /** Callback interface for ID-BOX events. All methods are called on the main thread. */
    public interface Listener {
        /** Called when the ID-BOX is open and ready. License.setIdBoxUsbFd() has already been called. */
        void onReady();
        /** Called when an error occurs. */
        void onError(String message);
        /** Called when the ID-BOX is physically detached. */
        void onDetached();
    }

    private static final int IDBOX_VENDOR_ID = 0x2B78;
    private static final String ACTION_USB_PERMISSION = "eu.id3.license.USB_PERMISSION";

    private final Activity activity;
    private final UsbManager usbManager;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Listener listener;
    private UsbDeviceConnection connection;
    private boolean permissionRequested;
    private boolean open;

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                if (granted) {
                    handler.postDelayed(() -> doOpen(), 500);
                } else {
                    permissionRequested = false;
                    notifyError("USB permission denied");
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && device.getVendorId() == IDBOX_VENDOR_ID && open) {
                    close();
                    if (listener != null) {
                        listener.onDetached();
                    }
                }
            }
        }
    };

    public IdBox(Activity activity) {
        this.activity = activity;
        this.usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
    }

    /**
     * Find the ID-BOX, request USB permission if needed, open the device
     * and call {@link FaceLicense#setIdBoxUsbFd(int)}.
     * The result is delivered asynchronously via the {@link Listener}.
     */
    public void open(Listener listener) {
        this.listener = listener;
        this.permissionRequested = false;
        this.open = false;

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(usbReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            activity.registerReceiver(usbReceiver, filter);
        }

        handler.postDelayed(() -> doOpen(), 200);
    }

    /** Close the current USB connection. Safe to call multiple times. */
    public void close() {
        open = false;
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    /** Unregister receivers and clean up. Must be called in Activity.onDestroy(). */
    public void release() {
        close();
        handler.removeCallbacksAndMessages(null);
        try {
            activity.unregisterReceiver(usbReceiver);
        } catch (IllegalArgumentException ignored) {
            // receiver was not registered
        }
        listener = null;
    }

    /** Returns true if the ID-BOX connection is open and ready. */
    public boolean isOpen() {
        return open;
    }

    /** Returns the USB file descriptor, or -1 if not connected. */
    public int getFileDescriptor() {
        return (connection != null) ? connection.getFileDescriptor() : -1;
    }

    private void doOpen() {
        if (open) return;
        try {
            doOpenImpl();
        } catch (Exception e) {
            notifyError(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void doOpenImpl() {
        // Find ID-BOX
        UsbDevice idbox = null;
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            if (device.getVendorId() == IDBOX_VENDOR_ID) {
                idbox = device;
                break;
            }
        }
        if (idbox == null) {
            notifyError("No ID-BOX found");
            return;
        }

        // Request permission if needed
        if (!usbManager.hasPermission(idbox)) {
            if (!permissionRequested) {
                permissionRequested = true;
                Intent usbIntent = new Intent(ACTION_USB_PERMISSION);
                usbIntent.setPackage(activity.getPackageName());
                PendingIntent pi = PendingIntent.getBroadcast(activity, 0,
                        usbIntent, PendingIntent.FLAG_MUTABLE);
                usbManager.requestPermission(idbox, pi);
            }
            return;
        }

        // Open device
        connection = usbManager.openDevice(idbox);
        if (connection == null) {
            notifyError("Failed to open USB device");
            return;
        }

        int fd = connection.getFileDescriptor();
        try {
            FaceLicense.setIdBoxUsbFd(fd);
        } catch (Exception e) {
            connection.close();
            connection = null;
            notifyError("setIdBoxUsbFd failed: " + e.getMessage());
            return;
        }

        open = true;
        if (listener != null) {
            listener.onReady();
        }
    }

    private void notifyError(String message) {
        if (listener != null) {
            listener.onError(message);
        }
    }
}
