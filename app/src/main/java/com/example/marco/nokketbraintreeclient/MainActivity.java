package com.example.marco.nokketbraintreeclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private String mAuthorization;
    private Button mDonateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDonateButton = (Button) findViewById(R.id.donate);

        fetchAuthorization();
    }

    protected void fetchAuthorization() {
        mAuthorization = null;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://www.nokket.com/api/client_token", new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Toast.makeText(MainActivity.this, "Client Token: Success", Toast.LENGTH_LONG).show();

                try {
                    mAuthorization = response.getString("client_token");
                    mDonateButton.setEnabled(true);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Toast.makeText(MainActivity.this, "Client Token: Failure", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onDonate(View v) {
        Toast.makeText(MainActivity.this, "Donating...", Toast.LENGTH_LONG).show();
    }
}
