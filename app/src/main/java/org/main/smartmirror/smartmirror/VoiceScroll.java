package org.main.smartmirror.smartmirror;

import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * Created by harout on 2/21/16.
 */
public class VoiceScroll {

    public static void scrollViewDown(View view){
        if (view instanceof ListView) {
            voiceListView(Constants.SCROLL_DOWN, (ListView)view, 1);
        }
    }

    public static void voiceListView(String message, ListView lv, int position) {
        if(message.contains(Constants.SCROLL_DOWN))
            lv.smoothScrollToPosition(position);
        else if(!message.contains(Constants.SCROLL_DOWN) && message.contains(Constants.SCROLL_UP))
            lv.smoothScrollToPosition(position);
    }

    public static void voiceScrollView(String message, ScrollView sv) {
        if(message.contains(Constants.SCROLL_DOWN))
            sv.scrollBy(0, -(0-sv.getHeight()));
        else if(!message.contains(Constants.SCROLL_DOWN) && message.contains(Constants.SCROLL_UP))
            sv.scrollBy(0, 0-sv.getHeight());
    }
}
