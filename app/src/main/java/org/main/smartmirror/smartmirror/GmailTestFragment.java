package org.main.smartmirror.smartmirror;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gm.contentprovider.GmailContract;

/**
 * Created by Luis on 3/15/2016.
 */
public class GmailTestFragment extends Fragment {

    private Preferences mPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = Preferences.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gmail_test_fragment, container, false);
        Cursor labelsCursor = getActivity().getContentResolver().query(GmailContract.Labels.getLabelsUri(mPreference.getGmailAccount()), null, null, null, null);
        // loop through the cursor and find the Inbox
        if (labelsCursor != null) {
            final String inboxCanonicalName = GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_ALL_MAIL;
            final int canonicalNameIndex = labelsCursor.getColumnIndexOrThrow(GmailContract.Labels.NUM_UNREAD_CONVERSATIONS);
            while (labelsCursor.moveToNext()) {
                if (inboxCanonicalName.equals(labelsCursor.getString(canonicalNameIndex))) {
                    Log.i("GMAIL", inboxCanonicalName);
                    TextView txt = (TextView) view.findViewById(R.id.unread_count);
                    txt.setText(inboxCanonicalName);
                    txt.setTextSize(24.0f);
                }
            }
        }
        return view;
    }
}
