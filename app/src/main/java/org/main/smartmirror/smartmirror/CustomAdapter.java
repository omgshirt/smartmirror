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

    private class ViewHolder {
        TextView header;
        TextView snippet;
        ImageView thumbnail;
        TextView articleNumber;
    }

    public CustomAdapter(Context context, ArrayList<CustomListViewObject> objects) {
        inflater = LayoutInflater.from(context);
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
            holder.articleNumber = (TextView) convertView.findViewById(R.id.article_number);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            holder.header.setText(objects.get(position).getProp1());
            holder.snippet.setText(objects.get(position).getProp2());
            Picasso.with(MainActivity.getContextForApplication()).load(objects.get(position).getProp3()).fit().centerInside().into(holder.thumbnail);
            holder.articleNumber.setText(objects.get(position).getProp4());
        } catch (Exception e) {}


        return convertView;
    }
}
