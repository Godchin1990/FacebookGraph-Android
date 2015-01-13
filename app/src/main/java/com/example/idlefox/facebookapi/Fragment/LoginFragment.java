package com.example.idlefox.facebookapi.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.idlefox.facebookapi.R;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private UiLifecycleHelper uiHelper;
    private TextView userInfoTextView;
    private Button batchRequestButton;
    private TextView textViewResults;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("user_location", "user_birthday", "user_likes"));
        userInfoTextView = (TextView) view.findViewById(R.id.userInfoTextView);

        batchRequestButton = (Button) view.findViewById(R.id.batchRequestButton);
        batchRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBatchRequest();
            }
        });

        textViewResults = (TextView) view.findViewById(R.id.textViewResults);
        textViewResults.setText("");

        return view;
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logined...");
            userInfoTextView.setVisibility(View.VISIBLE);
            Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        // Display the parsed user info
                        userInfoTextView.setText(buildUserInfoDisplay(user));
                    }
                }
            });
            batchRequestButton.setVisibility(View.VISIBLE);
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            userInfoTextView.setVisibility(View.GONE);
            batchRequestButton.setVisibility(View.GONE);
        }
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, uiHelper.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed()) ) {
            batchRequestButton.setVisibility(View.VISIBLE);
            userInfoTextView.setVisibility(View.VISIBLE);
            Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        // Display the parsed user info
                        userInfoTextView.setText(buildUserInfoDisplay(user));
                    }
                }
            });
        }else{
            batchRequestButton.setVisibility(View.GONE);
            userInfoTextView.setVisibility(View.GONE);
        }
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private String buildUserInfoDisplay(GraphUser user) {
        StringBuilder userInfo = new StringBuilder("");

        // Below data
        // - no special permissions required
        userInfo.append(String.format("Name: %s\n\n",
                user.getName()));

        userInfo.append(String.format("Birthday: %s\n\n",
                user.getBirthday()));

        userInfo.append(String.format("Location: %s\n\n",
                user.getLocation().getProperty("name")));

        userInfo.append(String.format("Locale: %s\n\n",
                user.getProperty("locale")));

        // Example: access via key for array (languages)
        // - requires user_likes permission
        JSONArray languages = (JSONArray)user.getProperty("languages");
        if (languages.length() > 0) {
            ArrayList<String> languageNames = new ArrayList<String> ();
            for (int i=0; i < languages.length(); i++) {
                JSONObject language = languages.optJSONObject(i);
                // Add the language name to a list. Use JSON
                // methods to get access to the name field.
                languageNames.add(language.optString("name"));
            }
            userInfo.append(String.format("Languages: %s\n\n",
                    languageNames.toString()));
        }

        return userInfo.toString();
    }

    private void doBatchRequest() {
        String[] requestIds = {"me", "4"};

        RequestBatch requestBatch = new RequestBatch();
        for (final String requestId : requestIds) {
            requestBatch.add(new Request(Session.getActiveSession(), requestId, null, null, new Request.Callback() {
                public void onCompleted(Response response) {
                    GraphObject graphObject = response.getGraphObject();
                    String s = textViewResults.getText().toString();
                    if (graphObject != null) {
                        if (graphObject.getProperty("id") != null) {
                            s = s + String.format("%s: %s\n", graphObject.getProperty("id"), graphObject.getProperty("name"));
                        }
                    }
                    textViewResults.setText(s);
                }
            }));
        }
        requestBatch.executeAsync();
    }

}
