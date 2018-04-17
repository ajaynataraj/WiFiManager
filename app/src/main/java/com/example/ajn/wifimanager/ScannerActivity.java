package com.example.ajn.wifimanager;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        // Log.v("tag", rawResult.getText()); // Prints scan results
        // Log.v("tag", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        Toast toast = Toast.makeText(this,rawResult.getText(),Toast.LENGTH_SHORT);
        toast.show();;
        //MainActivity.tvresult.setText(rawResult.getText());
        //onBackPressed();
        String message = rawResult.getText();
        message = "Moto M,maddymaddy";
        String[] separated = message.split(",");

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", separated[0]);
        wifiConfig.preSharedKey = String.format("\"%s\"", separated[1]);

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
//remember id
        wifiManager.setWifiEnabled(true);
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }
}