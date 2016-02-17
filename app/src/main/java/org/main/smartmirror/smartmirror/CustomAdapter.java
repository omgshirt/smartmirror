package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by harout on 2/17/16.
 */
public class CustomAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<CustomObject> objects;

    private class ViewHolder {
        TextView header;
        TextView snippet;
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
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_row_layout, null);
            holder.header = (TextView) convertView.findViewById(R.id.header);
            holder.snippet = (TextView) convertView.findViewById(R.id.snippet);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.header.setText(objects.get(position).getProp1());
        holder.snippet.setText(objects.get(position).getProp2());
        return convertView;
    }
}
