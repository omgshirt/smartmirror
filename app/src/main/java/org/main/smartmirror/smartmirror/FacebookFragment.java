package org.main.smartmirror.smartmirror;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.util.Arrays;


public class FacebookFragment extends Fragment {

    LoginButton btnLoginButton;
    CallbackManager mCBManager;
    AccessToken mAccessToken;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCBManager = CallbackManager.Factory.create();
        View view = inflater.inflate(R.layout.facebook_fragment, container, false);

        btnLoginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
        btnLoginButton.setReadPermissions("user_friends, user_posts");

        btnLoginButton.setFragment(this);

        btnLoginButton.registerCallback(mCBManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("status: ", "SUCCESS!");
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/feed",
                        //"/{user_id}/notifications",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Log.i("resonse ", response.toString());
                            }
                        }
                ).executeAsync();

                mAccessToken = AccessToken.getCurrentAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(
                        mAccessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.i("graph response ", response.toString());
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();
                //https://graph.facebook.com/page-username/posts?access_token=sometoken


            }

            @Override
            public void onCancel() {
                Log.i("status: ", "CANCEL!");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i("status: ", "ERROR!");
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCBManager.onActivityResult(requestCode, resultCode, data);
    }

}
