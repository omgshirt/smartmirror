package org.main.smartmirror.smartmirror;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;



public class FacebookFragment extends Fragment {

    LoginButton btnLoginButton;
    CallbackManager mCBManager;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCBManager = CallbackManager.Factory.create();
        View view = inflater.inflate(R.layout.facebook_fragment, container, false);

        btnLoginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
        btnLoginButton.setReadPermissions("user_friends");

        btnLoginButton.setFragment(this);

        btnLoginButton.registerCallback(mCBManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("status: ", "SUCCESS!");
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
