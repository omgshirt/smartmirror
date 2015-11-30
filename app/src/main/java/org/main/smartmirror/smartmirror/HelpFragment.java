package org.main.smartmirror.smartmirror;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Dialog that handles all the help commands
 */
public class HelpFragment extends DialogFragment {
    private AlertDialog.Builder builder;

    //used as the constructor to get the new instance of Helper dialogue
    public static HelpFragment newInstance(String name) {
        HelpFragment frag = new HelpFragment();
        Bundle args = new Bundle();
        args.putString("name",name);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        String name = getArguments().getString("name");
        Resources res = getResources();

        //needed for the help string dialogue
        String[] news_settings=res.getStringArray(R.array.news_settings);
        String[] news_settings2=res.getStringArray(R.array.news_settings2);
        String news_string =stringSpace(news_settings);
        String news_string2=stringSpace(news_settings2);

        //needed for the settings string dialogue
        String[] help_settings=res.getStringArray(R.array.help_settings);
        String settings_string =stringSpace(help_settings);
        String[] help_settings2=res.getStringArray(R.array.help_settings2);
        String settings_string2=stringSpace(help_settings2);



        //needed for the main string dialogue
        String[] help_array = res.getStringArray(R.array.General_help_array);
        String[] help_array2=res.getStringArray(R.array.General_help_array2);
        String main_string=stringSpace(help_array);
        String main_string2=stringSpace(help_array2);



        builder=new AlertDialog.Builder(getActivity(),R.style.MyDialog);

        switch (name) {


            case "news":
                builder.setTitle("News");
                View layout_news=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_news));
                TextView view=(TextView)layout_news.findViewById(R.id.General_Help_Content);
                TextView view2=(TextView)layout_news.findViewById(R.id.General_Help_content2);
                view.setText(news_string);
                view2.setText(news_string2);

                break;

            case "calendar":
                builder.setTitle("Calendar");
                View layout_calendar=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_calendar));
                TextView view_calendar=(TextView)layout_calendar.findViewById(R.id.General_Help_Content);
                TextView view_calendar2=(TextView)layout_calendar.findViewById(R.id.General_Help_content2);
                view_calendar.setText(main_string);
                view_calendar2.setText(main_string2);
                break;
            case "camera":
                builder.setTitle("Camera");
                View layout_camera=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_camera));
                TextView view_camera=(TextView)layout_camera.findViewById(R.id.General_Help_Content);
                TextView view_camera2=(TextView)layout_camera.findViewById(R.id.General_Help_content2);
                view_camera.setText(main_string);
                view_camera2.setText(main_string2);
                break;

            case "light":
                builder.setTitle("Light");
                View layout_light=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_light));
                TextView view_light=(TextView)layout_light.findViewById(R.id.General_Help_Content);
                TextView view_light2=(TextView)layout_light.findViewById(R.id.General_Help_content2);
                view_light.setText(main_string);
                view_light2.setText(main_string2);
                break;

            case "weather":
                builder.setTitle("Weather");
                View layout_weather=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_weather));
                TextView view_weather=(TextView)layout_weather.findViewById(R.id.General_Help_Content);
                TextView view_weather2=(TextView)layout_weather.findViewById(R.id.General_Help_content2);
                view_weather.setText(main_string);
                view_weather2.setText(main_string2);
                break;

            case "settings":
                builder.setTitle("Settings");
                View layout_settings=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_settings));
                TextView view_settings=(TextView)layout_settings.findViewById(R.id.General_Help_Content);
                TextView view_settings2=(TextView)layout_settings.findViewById(R.id.General_Help_content2);
                view_settings.setText(settings_string);
                view_settings2.setText(settings_string2);
                break;

            case "twitter":
                builder.setTitle("Twitter");
                View layout_twitter=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_twitter));
                TextView view_twitter=(TextView)layout_twitter.findViewById(R.id.General_Help_Content);
                TextView view_twitter2=(TextView)layout_twitter.findViewById(R.id.General_Help_content2);
                view_twitter.setText(main_string);
                view_twitter2.setText(main_string2);
                break;

            case "facebook":
                builder.setTitle("Facebook");
                View layout_facebook=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_facebook));
                TextView view_facebook=(TextView)layout_facebook.findViewById(R.id.General_Help_Content);
                TextView view_facebook2=(TextView)layout_facebook.findViewById(R.id.General_Help_content2);
                view_facebook.setText(main_string);
                view_facebook2.setText(main_string2);
                break;

            case "gallery":
                builder.setTitle("Gallery");
                View layout_gallery=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_gallery));
                TextView view_gallery=(TextView)layout_gallery.findViewById(R.id.General_Help_Content);
                TextView view_gallery2=(TextView)layout_gallery.findViewById(R.id.General_Help_content2);
                view_gallery.setText(main_string);
                view_gallery2.setText(main_string2);
                break;

            case "quotes":
                builder.setTitle("Quotes");
                View layout_quotes=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_quotes));
                TextView view_quotes=(TextView)layout_quotes.findViewById(R.id.General_Help_Content);
                TextView view_quotes2=(TextView)layout_quotes.findViewById(R.id.General_Help_content2);
                view_quotes.setText(main_string);
                view_quotes2.setText(main_string2);
                break;

            case "makeup":
                builder.setTitle("Makeup");
                View layout_makeup=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_makeup));
                TextView view_makeup=(TextView)layout_makeup.findViewById(R.id.General_Help_Content);
                TextView view_makeup2=(TextView)layout_makeup.findViewById(R.id.General_Help_content2);
                view_makeup.setText(main_string);
                view_makeup2.setText(main_string2);
        }
        return builder.create();

    }

    public void change_message(String s) {
        builder=new AlertDialog.Builder(getActivity());
        builder.setMessage(s);
    }

    public String stringSpace(String[] string){
        String str="";
        for(int i=0;i<string.length;i++){
            str+=string[i]+"\n";
        }
        return str;
    }
}
