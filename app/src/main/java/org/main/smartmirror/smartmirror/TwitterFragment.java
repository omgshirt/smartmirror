package org.main.smartmirror.smartmirror;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.identity.*;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;


import io.fabric.sdk.android.Fabric;

public class TwitterFragment extends Fragment {

    private static final String mTWITTER_KEY = "mQ51h9ZbAz9Xk2AZtsUBJAGlx";
    private static final String mTWITTER_SECRET = "uSRCxg6AqE9DyIiuKjVD2ZzKC7CsGmuUcEljx2yafBwYHW74Rt";

    public TwitterFragment() {}

    private TwitterLoginButton loginButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        TwitterAuthConfig authConfig = new TwitterAuthConfig(mTWITTER_KEY, mTWITTER_SECRET);
        Fabric.with(super.getActivity(), new Twitter(authConfig));

        View view = inflater.inflate(R.layout.twitter_login_fragment, container, false);

        loginButton = (TwitterLoginButton) view.findViewById(R.id.twitter_login_button);

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {

                TwitterSession session = result.data;

                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
        /*Fragment fragment = getFragmentManager().findFragmentById(R.id.twitter_login_button);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }*/

    }


}
