package com.example.ajn.wifimanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class QRGeneratorActivity extends AppCompatActivity {
    public final static int QRcodeWidth = 500 ;
    ImageView qr;
    ProgressDialog progress;
    RadioButton wifi;
    RadioButton hotspot;
    LinearLayout linearLayout;
    EditText password;
    EditText wifiName;
    WifiManager wifiManager;
    WifiInfo wifiInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator_qr);
        qr = findViewById(R.id.imageView_QR);
        Button generateQr = findViewById(R.id.button_generate);
        wifiName = findViewById(R.id.editText_wifi_ssid);
        password = findViewById(R.id.editText_wifi_password);
        wifi = findViewById(R.id.radioButton_wifi);
        hotspot = findViewById(R.id.radioButton_hotspot);
        linearLayout =  findViewById(R.id.layout_dynamic);
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(QRGeneratorActivity.this);
        if(!mSettings.getString("wifiname", "").equals(""))
        {
            wifiName.setText(mSettings.getString("wifiname", ""));
            password.setText(mSettings.getString("pass", ""));
        }
        wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifi.isChecked()) {
                    linearLayout.setVisibility(View.VISIBLE);
                }
                else {
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });
        hotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hotspot.isChecked()) {
                    linearLayout.setVisibility(View.VISIBLE);
                }
                else {
                    linearLayout.setVisibility(View.GONE);
                }
            }
        });


        generateQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new ProgressDialog(QRGeneratorActivity.this);
                //progress.setTitle("Loading");
                progress.setMessage("Generating QR");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

                if(wifi.isChecked()){

                    final String wifiSSID = wifiName.getText().toString();
                    final String pass = password.getText().toString();
                    SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(QRGeneratorActivity.this);
                    final SharedPreferences.Editor editor = mSettings.edit();
                    if(!mSettings.getString("wifiname","").equals(wifiSSID)|| !mSettings.getString("pass","").equals(pass))
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(QRGeneratorActivity.this).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                editor.putString("wifiname",wifiSSID);
                                editor.putString("pass",pass);
                                editor.apply();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setMessage("Do want to remember the wifi credentials?");
                        builder.show();
                    }


                    progress.show();
                    new ConvertToImage().execute(wifiSSID+","+pass);
                    wifiName.setText("");
                    password.setText("");

                } else if (hotspot.isChecked()) {
                    progress.show();
                    wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    wifiInfo = wifiManager.getConnectionInfo();
                    WifiConfiguration wifiConfig;
                    Method getConfigMethod = null;
                    try {
                        getConfigMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
                        wifiConfig = (WifiConfiguration) getConfigMethod.invoke(wifiManager);
//                        getConfigMethod = wifiManager.getClass().getMethod("getWifiApState");
//                        int wifiConfig = (int) getConfigMethod.invoke(wifiManager);
                        Log.d("wifissid", wifiConfig.preSharedKey+" hello");
                        new ConvertToImage().execute(wifiConfig.SSID+","+wifiConfig.preSharedKey);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }


//                    Log.d("wifiInfo", wifiInfo.toString());
//                    Log.d("SSID",wifiInfo.getSSID());

                } else {
                    Toast.makeText(QRGeneratorActivity.this, "Please select any one of the option", Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }


            }
        });

    }


    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor):getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }


    private class ConvertToImage extends AsyncTask <String,Void,Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                return TextToImageEncode(strings[0]);
            } catch (WriterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            qr.setImageBitmap(bitmap);
            progress.dismiss();

        }
    }

//    private void changeStateWifiAp(boolean activated) {
//        Method method;
//        try {
//            method = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
//            method.invoke(wifiManager, wifiConfiguration, activated);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
