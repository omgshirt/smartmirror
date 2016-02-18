package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by harout on 2/17/16.
 */
public class CustomAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<CustomObject> objects;
    Context context;

    private class ViewHolder {
        TextView header;
        TextView snippet;
        ImageView thumbnail;
    }

    public CustomAdapter(Context context, ArrayList<CustomObject> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public int getCount() {
        return objects.size();
    }

    public CustomObject getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_row_layout, null);
            holder.header = (TextView) convertView.findViewById(R.id.header);
            holder.snippet = (TextView) convertView.findViewById(R.id.snippet);
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.header.setText(objects.get(position).getProp1());
        holder.snippet.setText(objects.get(position).getProp2());
        //holder.thumbnail.setImageURI(objects.get(position).getProp3());
        Picasso.with(context).load(objects.get(position).getProp3()).fit().centerInside().into(holder.thumbnail);

        return convertView;
    }
}
