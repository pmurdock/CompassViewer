package com.pcmindustries.compassviewer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    private static final String DEBUG_TAG = "PCM";
    // TimerTask handler
    final Handler handler = new Handler();
    private ImageView imgView;
    private TextView txtLog;
    private TextView txtStatus;
    private EditText txtDrillSite, txtShot, txtDepth;
    private Button btnDownload;
    private Button btnSave;
    private Button btnFolders;
    private boolean bConnected = false;
    private boolean bDownloading = false;
    private boolean b_isViewImageFullScreen;
    private Timer timer;
    private TimerTask timerTask;
    Bitmap cleanbmp;
    Bitmap textbmp;

    public static String getCurrentTimeStamp() {
        //SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//dd/MM/yyyy
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss.SSS");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);

        imgView = (ImageView) findViewById(R.id.imageView);
        txtLog = (TextView) findViewById(R.id.txtLog);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnFolders = (Button) findViewById(R.id.btnFolders);
        txtDrillSite = (EditText) findViewById(R.id.txtDrillSite);
        txtShot = (EditText) findViewById(R.id.txtShot);
        txtDepth = (EditText) findViewById(R.id.txtDepth);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a temporary jpeg in the [temp] subdirectory under [CompassViewer]
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/CompassViewer/temp");
                path.mkdirs();
                File file = new File(path, "temp.jpg");



                if (imgView.getDrawable() != null) {
                    Log.d("PCM", "Starting to Write Temp to SD Card");
                    logTxt("start writing TEMP to SD card");

                    FileOutputStream fos = null;

                    try {
                        fos = new FileOutputStream(file, false);
                        textbmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                        fos.flush();
                        fos.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    logTxt("Wrote TEMP to SD card");
                }

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + path.getPath() + "/temp.jpg"), "image/jpeg");
                startActivity(intent);

            }
        });

        imgView.setOnTouchListener(
                new View.OnTouchListener() {

                    private Rect rect;

                    @Override
                    public boolean onTouch(View v, MotionEvent motionEvent) {


                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN: {
                                //overlay is black with transparency of 0x77 (119)
                                imgView.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                                break;
                            }
                            case MotionEvent.ACTION_UP: {
                                imgView.clearColorFilter();
                                // if we release while inside the control then make a click!
                                if (rect != null && rect.contains(v.getLeft() + (int) motionEvent.getX(),
                                        v.getTop() + (int) motionEvent.getY())) {
                                    if (imgView.getDrawable() != null) {
                                        v.performClick();
                                    }
                                }

                                break;
                            }
                            case MotionEvent.ACTION_MOVE: {
                                if (rect != null && !rect.contains(v.getLeft() + (int) motionEvent.getX(),
                                        v.getTop() + (int) motionEvent.getY())) {
                                    imgView.clearColorFilter();
                                } else {
                                    imgView.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                                }
                                break;
                            }
                            case MotionEvent.ACTION_CANCEL: {
                                //clear the overlay
                                imgView.clearColorFilter();
                                break;
                            }
                        }

                        return true;
                    }
                }

        );

        txtDrillSite.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (imgView.getDrawable() != null) {
                    UpdateBitmapText();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtShot.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (imgView.getDrawable() != null) {
                    UpdateBitmapText();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtDepth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (imgView.getDrawable() != null) {
                    UpdateBitmapText();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        logTxt("Start timer from onCreate");
        startTimer();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void btnDownload(View view) {
        imgView.setImageDrawable(null);
        // limit launches to 1 at a time
        if (bDownloading != true) {
            bDownloading = true;
            AsyncDownload runner = new AsyncDownload();
            runner.execute();
        } else {
            logTxt("THREAD already running..");
        }

    }

    public void btnClearLog(View view) {
        txtLog.setText("");
        imgView.setImageDrawable(null);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void btnConnect(View view) {
        // Check if WiFi is connected to PCM-CAMERA-MAC
        // if yes - then check ip address whether functional (responds)
        // if no - bring up Wifi dialog box to connect to device then check ip address

        // check if connected to correct WiFi SSID
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWiFiConn = networkInfo.isConnected();


        if (networkInfo != null && networkInfo.isConnected()) {
            // Log.d(DEBUG_TAG, "WiFi is connected : " + String.valueOf(isWiFiConn));
            // We know WiFi is connected - but to the PCM-CAMERA ssid?
            // Now print out to DEBUG which WiFi we are connected to (ssid)
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();

            if (info.getSSID().contains("pcm")) {
                Toast.makeText(getApplicationContext(), "Connected to PCM-CAMERA device",
                        Toast.LENGTH_LONG).show();
                // Change txtStatus to reflect that we are connected
                //bConnected = true;
                //txtStatus.setText("Connected");
                Log.d(DEBUG_TAG, "WiFi ssid is : " + info.getSSID());

            } else {
                // TODO
                // Open up the WiFi dialog box to allow them to connect to the PCM-CAMERA ssid
                Log.d(DEBUG_TAG, "Connected to wrong SSID : " + info.getSSID());

                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }

        } else {
            Log.d(DEBUG_TAG, "WiFi is not connected...");

            // If not connected to network - then help them bring up WiFi selection to
            // choose the Inventek Module - ssid: PCM-CAMERA
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }

        bConnected = !bConnected;
    }

    public void logTxt(String string) {
        String temp = txtLog.getText().toString();
        temp = getCurrentTimeStamp() + " >>> " + string + "\n" + temp;
        txtLog.setText(temp);
    }

    private void startTimer() {
        // set a new timer
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 1000, 2000);

    }

    private void stopTimerTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //logTxt("Timer Ran");
                        if (bConnected == true) {
                            txtStatus.setText("Connected");
                            txtStatus.setTextColor(Color.BLACK);
                            txtStatus.setBackgroundColor(Color.GREEN);
                            btnDownload.setEnabled(true);
                        } else {
                            txtStatus.setText("Not Connected");
                            txtStatus.setTextColor(Color.WHITE);
                            txtStatus.setBackgroundColor(Color.RED);
                            btnDownload.setEnabled(false);
                        }

                        if (imgView.getDrawable()==null){
                            btnSave.setEnabled(false);
                        } else {
                            btnSave.setEnabled(true);
                        }
                    }
                });
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimerTask();
        logTxt("Timer Paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer == null) {
            startTimer();
            //logTxt("Timer was null in onresume");
        }
        //logTxt("Timer Resumed");
    }

    public void btnSave(View view) {
        // Save the file to SD Card
        // TODO - create a folder for the program - then each drilling site get's a folder, etc
        // save the bitmap in the imageview (as I will have overlayed some text on the image)
        // filename format to use [drill site]-[shot#]-[depth]-[date].jpg
        // write to SD CARD

        String state = Environment.getExternalStorageState();
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES +
                "/CompassViewer/[" + txtDrillSite.getText() + "]");
        File file = new File(path, "[" + txtDrillSite.getText() + "]-[shot-" + txtShot.getText() +
            "]-[depth-" + txtDepth.getText() + "].jpg");


        if (imgView.getDrawable() != null) {
            Log.d("PCM", "Starting to Write to SD Card");
            logTxt("start writing to SD card");

            path.mkdirs();

            FileOutputStream fos = null;

            try {
                fos = new FileOutputStream(file, false);
                textbmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                fos.flush();
                fos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            logTxt("Wrote to SD card");
        }

        Toast.makeText(this, "File saved in " + path.getPath(), Toast.LENGTH_LONG).show();
    }

    public void btnFolder(View view) {
  /*      File mFile = new File("/storage/emulated/0/Pictures");
       File[] list=mFile.listFiles();

        for (File file : list) {
            Log.d(DEBUG_TAG,file.getPath().toString());
        }*/

        // Open up new Activity we created with intent
        //Intent intent = new Intent(this, ShowFilesActivity.class);
        //startActivity(intent);

        // This works for ASTRO file manager
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/"),
                "*/*");
        startActivity(intent);
    }

    private class AsyncDownload extends AsyncTask<Void, String, Boolean> {



        @Override
        protected Boolean doInBackground(Void... params) {

            // Use an http GET request to download the binary file from the system
            URL url = null;
            HttpURLConnection urlConnection = null;
            boolean bResult = false;

            try {
                url = new URL("http://192.168.0.110:8080/");
                publishProgress("Connecting . . .");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(2000);

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                publishProgress("SUCCESS");

                Log.d("PCM", "Finished reading in buffer...");
                publishProgress("http request completed");


                int nRead;
                int count = 0;
                int[] data = new int[480000];
                int width = 800;
                int height = 600;

                //IntBuffer intBuf = ByteBuffer.wrap(datab).asIntBuffer();
                while ((nRead = in.read()) != -1) {
                    data[count] = nRead;
                    count++;
                }
                publishProgress("buffer read into int array");


                // Create Bitmap from raw data that we received
                cleanbmp = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888);
                cleanbmp.eraseColor(Color.argb(255, 255, 0, 0));
                //bmp.setDensity(Bitmap.DENSITY_NONE);

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        data[y * width + x] = ((0xff000000) | ((data[y * width + x]) << 16) | ((data[y * width + x]) << 8) | ((data[y * width + x])));
                        //bmp.setPixel(x, y, (int)(0xff000000 | ((data[y * 600 + x])<<16) | ((data[y * 600 + x])<<8) | ((data[y * 600 + x]))));
                        //Log.d("PCM","x: " + String.valueOf(x) + "y: " + String.valueOf(y));
                    }

                }

                cleanbmp.setPixels(data, 0, 800, 0, 0, 800, 600);
//            bmp.eraseColor(Color.argb(255, 255, 0, 0));
                publishProgress("bitmap set");



                bResult = true;


            } catch (SocketTimeoutException e) {
                publishProgress("SocketTimeoutException");
                bResult = false;
            } catch (IOException e) {
                e.printStackTrace();
                bResult = false;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            publishProgress("EXIT background task");

            return bResult;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String temp = txtLog.getText().toString();
            temp = getCurrentTimeStamp() + " >>> " + values[0] + "\n" + temp;
            txtLog.setText(temp);
        }

        @Override
        protected void onPostExecute(Boolean bSuccess) {
            if (bSuccess) {
                UpdateBitmapText();

            } else {
                imgView.setImageDrawable(null);
            }
            bDownloading = false;
        }
    }

    private void UpdateBitmapText() {
        // write text into bitmap as well
        //textbmp = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888);
        //textbmp.eraseColor(Color.RED);
        textbmp = cleanbmp.copy(cleanbmp.getConfig(),true);

        Canvas canvas = new Canvas(textbmp);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(12);
        paint.setTextSize(30);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        //canvas.drawBitmap(bmp,0,0,paint);
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy HH:mm");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        canvas.drawText(strDate,10,30,paint);
        canvas.drawText(txtDrillSite.getText().toString(), 10,60,paint);
        canvas.drawText("Shot # " + txtShot.getText().toString(), 10,90,paint);
        canvas.drawText("Depth : " + txtDepth.getText().toString(), 10, 120, paint);

        imgView.setImageBitmap(textbmp);

        //logTxt("UPDATE bitmap text");

    }
}
