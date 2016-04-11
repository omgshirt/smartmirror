package org.main.smartmirror.smartmirror;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class CustomAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<CustomListViewObject> objects;
    Context context;
    private boolean showNumbers;

    private class ViewHolder {
        TextView header;
        TextView snippet;
        ImageView thumbnail;
    }

    public CustomAdapter(Context context, ArrayList<CustomListViewObject> objects, boolean showNumber) {
        inflater = LayoutInflater.from(context);
        showNumbers = showNumber;
        this.objects = objects;
    }

    public int getCount() {
        return objects.size();
    }

    public CustomListViewObject getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.twitter_news_list_row, null);
            holder.header = (TextView) convertView.findViewById(R.id.header);
            holder.snippet = (TextView) convertView.findViewById(R.id.snippet);
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            String number = "";
            if (showNumbers) {
                number = Integer.toString(position + 1) + " - ";
            }
            String articleTitle = number + objects.get(position).getProp1();
            holder.header.setText(articleTitle);
            holder.snippet.setText(objects.get(position).getProp2());
            Picasso.with(MainActivity.getContextForApplication()).load(objects.get(position).getProp3()).fit().centerInside().into(holder.thumbnail);
        } catch (Exception e) {}


        return convertView;
    }
}
