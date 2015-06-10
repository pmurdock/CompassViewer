package com.pcmindustries.tcpclienttest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.apache.http.client.HttpClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;


public class MainActivity extends ActionBarActivity {


    private Button btnDownload;
    private EditText textIP;
    private EditText textPort;
    private Socket socket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        btnDownload = (Button) findViewById(R.id.btnDownload);
        textIP = (EditText) findViewById(R.id.textIP);
        textPort = (EditText) findViewById(R.id.textPort);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void btnConnect(View view) {
        // Connect to Windows C# test server
        new Thread(new Runnable() {
            @Override
            public void run() {
                String addr = textIP.getText().toString();
                try {
                    InetAddress serverAddr = InetAddress.getByName(addr);
                    int value = Integer.parseInt(textPort.getText().toString());
                    socket = new Socket(serverAddr, value);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();

    }

    public void btnDownload(View view) {
        // Use an http GET request to download the binary file from the system
        URL url = null;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL("http://192.168.0.110:8080/");
            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            Log.d("PCM", "Finished reading in buffer...");


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

            // Now write out to SD Card

            // Create Bitmap from raw data that we received
            Bitmap bmp = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888);
            bmp.eraseColor(Color.argb(255,255,0,0));
            //bmp.setDensity(Bitmap.DENSITY_NONE);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    data[y*width+x] = ((0xff000000) | ((data[y * width + x])<<16) | ((data[y * width + x])<<8) | ((data[y * width + x])));
                    //bmp.setPixel(x, y, (int)(0xff000000 | ((data[y * 600 + x])<<16) | ((data[y * 600 + x])<<8) | ((data[y * 600 + x]))));
                    //Log.d("PCM","x: " + String.valueOf(x) + "y: " + String.valueOf(y));
                }

            }

            bmp.setPixels(data,0,800,0,0,800,600);
//            bmp.eraseColor(Color.argb(255, 255, 0, 0));

            ImageView imgView = (ImageView) findViewById(R.id.imageView);
            imgView.setImageBitmap(bmp);

            // write to SD CARD
            boolean mExternalStorageAvailable = false;
            boolean mExternalStorageWritable = false;

            String state = Environment.getExternalStorageState();
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(path, "compass.jpg");

            Log.d("PCM", "Starting to Write to SD Card");

            path.mkdirs();

            FileOutputStream fos = new FileOutputStream(file,false);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }

        Log.d("PCM", "Completed to Write to SD Card");

    }
}
