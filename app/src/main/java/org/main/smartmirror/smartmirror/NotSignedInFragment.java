package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Handles the error messages that will be displayed when user is not
 * logged into Facebook or GMail
 */
public class NotSignedInFragment extends Fragment {

    public static NotSignedInFragment newInstance(String fragmentName) {
        Bundle args = new Bundle();
        args.putString("fragment", fragmentName);
        NotSignedInFragment fragment = new NotSignedInFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notsignedin_fragment, container, false);
        TextView txtErrorMessage = (TextView) view.findViewById(R.id.error_message);

        String fragmentName = (getArguments().getString("fragment") == null) ? "this fragment" : getArguments().getString("fragment");
        String message = getResources().getText(R.string.not_logged_in_err).toString();
        try {
            if (fragmentName.length() > 0) {
                fragmentName = fragmentName.substring(0, 1).toUpperCase() + fragmentName.substring(1);
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        message = String.format(message, fragmentName);
        txtErrorMessage.setText(message);

        return view;
    }
}
