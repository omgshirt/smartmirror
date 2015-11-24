package org.main.smartmirror.smartmirror;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Jesus on 11/8/2015.
 */
public class Helper_Fragment extends DialogFragment



{

    private AlertDialog.Builder builder;



    //used as the constructor to get the new instance of Helper dialogue
    public static Helper_Fragment newInstance(String name) {
        Helper_Fragment frag = new Helper_Fragment();
        Bundle args = new Bundle();
        args.putString("name",name);
        frag.setArguments(args);
        return frag;
    }




    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();


        String name = getArguments().getString("name");


        Resources res = getResources();
        String[] help_array = res.getStringArray(R.array.help_array);
        String[] help_settings=res.getStringArray(R.array.help_settings);
        String[] news_settings=res.getStringArray(R.array.news_settings);

        String helper_string = stringSpace(help_array);
        String news_string =stringSpace(news_settings);

        String settings_string =stringSpace(help_settings);
        //sets the array for the different help dialogs
        String[] Help_Array=getResources().getStringArray(R.array.smart_help_array);


       builder=new AlertDialog.Builder(getActivity(),R.style.MyDialog);


        switch (name)

        {
            case "Help":
                   builder.setMessage(helper_string);
                break;

            case "News":
                builder.setTitle("News Help");
                View layout_news=inflater.inflate(R.layout.main_help_fragment,null);
                builder.setView((layout_news));
                TextView view=(TextView)layout_news.findViewById(R.id.main_settings_content);
                view.setText(news_string);
                break;

            case "Calendar":
                builder.setTitle("Calendar Help");
                View layout_calendar=inflater.inflate(R.layout.main_help_fragment,null);
                builder.setView((layout_calendar));
                TextView view_calendar=(TextView)layout_calendar.findViewById(R.id.main_settings_content);
                view_calendar.setText(helper_string);
                break;
            case "Camera":
                builder.setTitle("Camera Help");
                View layout_camera=inflater.inflate(R.layout.main_help_fragment,null);
                builder.setView((layout_camera));
                TextView view_camera=(TextView)layout_camera.findViewById(R.id.main_settings_content);
                view_camera.setText(helper_string);
               break;

            case "Light":
                builder.setTitle("Light Help");
                builder.setMessage(Help_Array[2]);
                break;

            case "Weather":
                builder.setTitle("Weather Help");
                View layout_weather=inflater.inflate(R.layout.main_help_fragment,null);
                builder.setView((layout_weather));
                TextView view_weather=(TextView)layout_weather.findViewById(R.id.main_settings_content);
                view_weather.setText(helper_string);


                break;
            case "Settings":
                builder.setTitle("Settings Help");
                View layout_settings=inflater.inflate(R.layout.main_help_fragment,null);
                builder.setView((layout_settings));
                TextView view_settings=(TextView)layout_settings.findViewById(R.id.main_settings_content);
                view_settings.setText(settings_string);
                break;

            case "Off":
                builder.setMessage(Help_Array[5]);
                break;
        }
        return builder.create();

    }
    public void change_message(String s)
    {

        builder=new AlertDialog.Builder(getActivity());
        builder.setMessage(s);

    }

    public String stringSpace(String[] string)
    {
        String str="";
        for(int i=0;i<string.length;i++)
        {
            str+=string[i]+"\n";
        }
        return str;
    }
    }

