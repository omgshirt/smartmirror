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
        String title = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase() + " - Help";
        builder.setTitle(title);
        switch (name) {

            case "news":
                View layout_news=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_news));
                TextView view=(TextView)layout_news.findViewById(R.id.General_Help_Content);
                TextView view2=(TextView)layout_news.findViewById(R.id.General_Help_content2);
                view.setText(news_string);
                view2.setText(news_string2);
                break;

            case "settings":
                View layout_settings=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layout_settings));
                TextView view_settings=(TextView)layout_settings.findViewById(R.id.General_Help_Content);
                TextView view_settings2=(TextView)layout_settings.findViewById(R.id.General_Help_content2);
                view_settings.setText(settings_string);
                view_settings2.setText(settings_string2);
                break;

            default:
                View layGeneralHelp=inflater.inflate(R.layout.help_fragment,null);
                builder.setView((layGeneralHelp));
                TextView view_makeup=(TextView)layGeneralHelp.findViewById(R.id.General_Help_Content);
                TextView view_makeup2=(TextView)layGeneralHelp.findViewById(R.id.General_Help_content2);
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
