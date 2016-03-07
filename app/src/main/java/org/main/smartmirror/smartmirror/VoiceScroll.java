package org.main.smartmirror.smartmirror;

import android.widget.ListView;
import android.widget.ScrollView;


public class VoiceScroll {

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
