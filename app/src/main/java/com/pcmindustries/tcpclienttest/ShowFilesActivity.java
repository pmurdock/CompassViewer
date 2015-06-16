package com.pcmindustries.tcpclienttest;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.File;


public class ShowFilesActivity extends ActionBarActivity {

    private static final String DEBUG_TAG = "PCM";
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_files);

        // this is the actual data we are working on - normally this will
        // come from a scan of the Pictures directory.
        File mFile = new File("/storage/emulated/0/Pictures");
        File[] list=mFile.listFiles();
        FileList[] fileList_data = new FileList[list.length];

        int count = 0;

        for (File file : list) {
            Log.d(DEBUG_TAG, file.getPath().toString());
            fileList_data[count] = new FileList(0,file.getPath().toString());

            count++;
        }




        /*FileList fileList_data[] = new FileList[]
                {
                        new FileList(0, "test1"),
                        new FileList(0, "test2")
                };
*/
        ShowFilesAdapter adapter = new ShowFilesAdapter(this,
                R.layout.listview_item_row, fileList_data);

        listView = (ListView)findViewById(R.id.listView);

        View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row,null);
        listView.addHeaderView(header);

        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_show_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
