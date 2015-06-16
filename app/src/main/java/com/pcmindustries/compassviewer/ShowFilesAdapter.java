package com.pcmindustries.compassviewer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Paul C. Murdock on 6/15/2015.
 */
public class ShowFilesAdapter extends ArrayAdapter<FileList> {

    Context context;
    int layoutResourceId;
    FileList data[] = null;

    public ShowFilesAdapter(Context context, int layoutResourceId, FileList[] data) {
        super(context, layoutResourceId, data);

        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FileListHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId,parent, false);

            holder = new FileListHolder();
            holder.imgThumbnail = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtFileName = (TextView)row.findViewById(R.id.txtTitle);

            row.setTag(holder);
        }
        else {
            holder = (FileListHolder) row.getTag();
        }

        FileList fileList = data[position];
        holder.txtFileName.setText(fileList.filename);
        //holder.imgThumbnail.setImageResource(fileList.thumbnail);
        holder.imgThumbnail.setImageResource(0);

        return row;

    }

    static class FileListHolder {
        ImageView imgThumbnail;
        TextView txtFileName;
    }
}
