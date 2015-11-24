package org.main.smartmirror.smartmirror;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.twitter.sdk.android.Twitter;

public class TwitterFragment extends Fragment {

    private Button mTwitterLogin;
    private Button mTwitterButton;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.twitter_fragment, container, false);
        mTwitterButton = (Button)view.findViewById(R.id.btn_twitter);
        mTwitterLogin = (Button)view.findViewById(R.id.btn_login);

        mTwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterAsync();
            }
        });

        mTwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterLogin();
            }
        });

        return view;
    }

    //to twitter login activity
    public void twitterLogin() {
        Intent intent = new Intent(getContext(), TwitterAct.class);
        startActivity(intent);
    }

    public void twitterAsync() {
        new TwitterASyncTask().execute();
    }

}
