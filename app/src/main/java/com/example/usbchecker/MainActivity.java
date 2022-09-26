package com.example.usbchecker;

import androidx.appcompat.app.AppCompatActivity;

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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.brother.ptouch.sdk.Printer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    private final String ACTION_USB_PERMISSION = "com.example.usbchecker.USB_PERMISSION";
    private Activity activity;
    private TextView printerInfo;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "on intent received >" + intent.getAction());
            if (ACTION_USB_PERMISSION.equals(action)) {
                Log.i(TAG, "On receive for usb permission");
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.i(TAG, "Usb connection permission accepted");
                        //permission granted
                        if (device != null) {
                            if (printerInfo != null) {
                                printerInfo.setText("USB Permission Granted. Device Data: " + device.toString());
                            }
                            Log.i(TAG, "usb connection devices");
                        } else {
                            if (printerInfo != null) {
                                printerInfo.setText("USB Permission Granted. Device Data NOT FOUND");
                            }
                            Log.i(TAG, "No usb connection devices");
                        }
                    } else {
                        if (printerInfo != null) {
                            printerInfo.setText("USB Permission NOT Granted.");
                        }
                        Log.i(TAG, "Usb connection permission denied");
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        Button button = (Button) findViewById(R.id.scanPrinters);
        printerInfo = (TextView) findViewById(R.id.printerInfo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsbDevice usbDevice = null;
                Set<Map.Entry<String, UsbDevice>> deviceList = usbManager.getDeviceList().entrySet();
                for (Map.Entry<String, UsbDevice> stringUsbDeviceEntry : deviceList) {
                    if (stringUsbDeviceEntry.getValue().getVendorId() == 1273) {
                        usbDevice = stringUsbDeviceEntry.getValue();
                        break;
                    }
                }

                PendingIntent permissionIntent = PendingIntent.getBroadcast(activity, 0,
                        new Intent(ACTION_USB_PERMISSION), 0);
                activity.registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
                System.out.println("Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
                while (true) {
                    System.out.println(usbManager.hasPermission(usbDevice));
                    if (!usbManager.hasPermission(usbDevice)) {
                        usbManager.requestPermission(usbDevice, permissionIntent);
                    } else {
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, String.valueOf(usbDevice));
                System.out.println(usbManager.hasPermission(usbDevice));
            }
        });
    }
}