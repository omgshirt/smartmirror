package org.main.smartmirror.smartmirror;

import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;


public class VoiceScroll {

    int position = 0;

    public void scrollViewDown(View view) {
        if (view instanceof ListView) {
            position = position + 5;
            scrollListView(Constants.SCROLL_DOWN, (ListView) view, position);
        }
        else if (view instanceof ScrollView) {
            scrollScrollView(Constants.SCROLL_DOWN, (ScrollView) view);
        }
    }

    public void scrollViewUp(View view) {
        if (view instanceof ListView) {
            position = position - 5;
            if (position < 0) position = 0;
            scrollListView(Constants.SCROLL_UP, (ListView) view, position);
        }
        else if (view instanceof ScrollView) {
            scrollScrollView(Constants.SCROLL_UP, (ScrollView) view);
        }
    }

    public void scrollListView(String message, ListView lv, int position) {
        if(message.contains(Constants.SCROLL_DOWN))
            lv.smoothScrollToPosition(position);
        else if(!message.contains(Constants.SCROLL_DOWN) && message.contains(Constants.SCROLL_UP))
            lv.smoothScrollToPosition(position);
    }

    public void scrollScrollView(String message, ScrollView sv) {
        if(message.contains(Constants.SCROLL_DOWN))
            sv.smoothScrollBy(0, -(0-sv.getHeight()));
        else if(!message.contains(Constants.SCROLL_DOWN) && message.contains(Constants.SCROLL_UP))
            sv.smoothScrollBy(0, 0-sv.getHeight());
    }
}
