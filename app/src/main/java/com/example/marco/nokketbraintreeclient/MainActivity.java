package com.example.marco.nokketbraintreeclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.Card;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.CardBuilder;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements PaymentMethodNonceCreatedListener,
        BraintreeCancelListener, BraintreeErrorListener {

    private String mAuthorization;
    private BraintreeFragment mBraintreeFragment;
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
                    onAuthorizationFetched();
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

    public void onAuthorizationFetched() {
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, mAuthorization);
            mDonateButton.setEnabled(true);
        } catch (InvalidArgumentException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onDonate(View v) {
        Toast.makeText(MainActivity.this, "Donating...", Toast.LENGTH_LONG).show();

        CardBuilder cardBuilder = new CardBuilder()
                .cardNumber("4111111111111111")
                //.cardNumber("4837419008371760")
                .expirationMonth("10")
                .expirationYear("2019");

        Card.tokenize(mBraintreeFragment, cardBuilder);
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        Toast.makeText(MainActivity.this, "Payment Method Nonce Created", Toast.LENGTH_LONG).show();
        String nonce = paymentMethodNonce.getNonce();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("nonce", nonce);
        client.post("http://www.nokket.com/api/nonce/transaction", params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);

                    try {
                        String message = response.getString("message");

                        if (message != null && message.startsWith("created")) {

                            Toast.makeText(MainActivity.this,
                                    "Payment Success: " + message, Toast.LENGTH_LONG).show();
                        } else {
                            if (TextUtils.isEmpty(message)) {

                                Toast.makeText(MainActivity.this,
                                        "Payment Failure: Server response was empty or malformed", Toast.LENGTH_LONG).show();
                            } else {

                                Toast.makeText(MainActivity.this,
                                        "Payment Failure: " + message, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);

                    Toast.makeText(MainActivity.this, "Payment Failure", Toast.LENGTH_LONG).show();
                }
            }
        );
    }

    @Override
    public void onCancel(int requestCode) {
        Toast.makeText(MainActivity.this, "Payment Cancelled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(Exception error) {
        Toast.makeText(MainActivity.this, "Payment Error", Toast.LENGTH_LONG).show();
    }
}
