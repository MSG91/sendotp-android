package com.msg91.sendotp.sample;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.msg91.sendotpandroid.library.SendOTPConfig;
import com.msg91.sendotpandroid.library.SendOtpVerification;
import com.msg91.sendotpandroid.library.Verification;
import com.msg91.sendotpandroid.library.VerificationListener;


public class VerificationActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, VerificationListener {

    private static final String TAG = Verification.class.getSimpleName();
    private static final int OTP_LNGTH = 4;
    TextView resend_timer;
    private boolean isDirect = true;
    private Verification mVerification;
    private OtpEditText mOtpEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        resend_timer = (TextView) findViewById(R.id.resend_timer);
        resend_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResendCode();
            }
        });
        startTimer();
        mOtpEditText = findViewById(R.id.inputCode);
        mOtpEditText.setMaxLength(OTP_LNGTH);
        enableInputField(true);
        initiateVerification();
    }

    void createVerification(String phoneNumber, String countryCode) {
        boolean withoutOtp = false;
        if (NetworkConnectivity.isConnectedMobileNetwork(getApplicationContext())) {
            withoutOtp = true;
        }

        SendOTPConfig otpConfig = SendOtpVerification
                .config(countryCode + phoneNumber)
                .context(this)
                //////////////////direct verification while connect with mobile network/////////////////////////
                .setIp(getIp(withoutOtp))
                .verifyWithoutOtp(withoutOtp)
                //////////////////////////////////////////////////////////////////////////////////////////////////
                .expiry("5")//value in minutes
                .senderId("ABCDEF") //where ABCDEF is any string
                .otplength(String.valueOf(OTP_LNGTH)) //length of your otp max length up to 9 digits
                .build();
        mVerification = SendOtpVerification.createSmsVerification
                (otpConfig, this);
        mVerification.initiate();

    }


    private String getIp(boolean mobileNetwork) {
        if (mobileNetwork) {
            try {
                return IPConverter.getIPAddress(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            return Formatter.formatIpAddress(ip);
        }
        return "";
    }


    void initiateVerification() {
        Intent intent = getIntent();
        if (intent != null) {
            DataManager.getInstance().showProgressMessage(this, "");
            String phoneNumber = intent.getStringExtra(MainActivity.INTENT_PHONENUMBER);
            String countryCode = intent.getStringExtra(MainActivity.INTENT_COUNTRY_CODE);
            TextView phoneText = (TextView) findViewById(R.id.numberText);
            phoneText.setText("+" + countryCode + phoneNumber);
            createVerification(phoneNumber, countryCode);
        }
    }

    public void ResendCode() {
        startTimer();
        mVerification.resend("voice");
    }

    public void onSubmitClicked(View view) {
        String code = mOtpEditText.getText().toString();
        if (!code.isEmpty()) {
            hideKeypad();
            if (mVerification != null) {
                mVerification.verify(code);
                DataManager.getInstance().showProgressMessage(this, "");
                TextView messageText = (TextView) findViewById(R.id.textView);
                messageText.setText("Verification in progress");
                enableInputField(false);
            }
        }
    }

    void enableInputField(boolean enable) {
        View container = findViewById(R.id.inputContainer);
        if (enable) {
            container.setVisibility(View.VISIBLE);
            mOtpEditText.requestFocus();
        } else {
            container.setVisibility(View.GONE);
        }
        TextView resend_timer = (TextView) findViewById(R.id.resend_timer);
        resend_timer.setClickable(false);
    }

    void hideProgressBarAndShowMessage(int message) {
        hideProgressBar();
        TextView messageText = (TextView) findViewById(R.id.textView);
        messageText.setText(message);
    }

    void hideProgressBar() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressIndicator);
        progressBar.setVisibility(View.INVISIBLE);
        TextView progressText = (TextView) findViewById(R.id.progressText);
        progressText.setVisibility(View.INVISIBLE);
    }

    void showProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressIndicator);
        progressBar.setVisibility(View.VISIBLE);
    }

    void showCompleted() {
        ImageView checkMark = (ImageView) findViewById(R.id.checkmarkImage);
        if (isDirect) {
            checkMark.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_magic));
        } else {
            checkMark.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_checkmark));
        }
        checkMark.setVisibility(View.VISIBLE);
    }

    @Override
    public void onInitiated(String response) {
        Log.d(TAG, "Initialized!" + response);
        isDirect = false;
        DataManager.getInstance().hideProgressMessage();
    }

    @Override
    public void onInitiationFailed(Exception exception) {
        DataManager.getInstance().hideProgressMessage();
        Log.e(TAG, "Verification initialization failed: " + exception.getMessage());
        hideProgressBarAndShowMessage(R.string.failed);
    }

    @Override
    public void onVerified(String response) {
        DataManager.getInstance().hideProgressMessage();
        enableInputField(false);
        Log.d(TAG, "Verified!\n" + response);
        hideKeypad();
        TextView textView2 = (TextView) findViewById(R.id.textView2);
        TextView textView1 = (TextView) findViewById(R.id.textView1);
        TextView messageText = (TextView) findViewById(R.id.textView);
        ImageView topImg = (ImageView) findViewById(R.id.topImg);
        TextView phoneText = (TextView) findViewById(R.id.numberText);
        RelativeLayout topLayout = findViewById(R.id.topLayout);
        topLayout.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.gradient_bg_white));
        messageText.setVisibility(View.GONE);
        phoneText.setVisibility(View.GONE);
        topImg.setVisibility(View.INVISIBLE);
        textView1.setVisibility(View.VISIBLE);
        textView2.setVisibility(View.VISIBLE);
        if (isDirect)
            textView2.setText("Mobile verified using Invisible OTP.");
        else textView2.setText("Your Mobile number has been successfully verified.");

        hideProgressBarAndShowMessage(R.string.verified);
        showCompleted();
    }

    @Override
    public void onVerificationFailed(Exception exception) {
        DataManager.getInstance().hideProgressMessage();
        Log.e(TAG, "Verification failed: " + exception.getMessage());
        hideKeypad();
        hideProgressBarAndShowMessage(R.string.failed);
        enableInputField(true);
    }

    private void startTimer() {
        resend_timer.setClickable(false);
        resend_timer.setTextColor(ContextCompat.getColor(VerificationActivity.this, R.color.white));
        new CountDownTimer(30000, 1000) {
            int secondsLeft = 0;

            public void onTick(long ms) {
                if (Math.round((float) ms / 1000.0f) != secondsLeft) {
                    secondsLeft = Math.round((float) ms / 1000.0f);
                    resend_timer.setText("Resend via call ( " + secondsLeft + " )");
                }
            }

            public void onFinish() {
                resend_timer.setClickable(true);
                resend_timer.setText("Resend via call");
                resend_timer.setTextColor(ContextCompat.getColor(VerificationActivity.this, R.color.white));
            }
        }.start();
    }

    private void hideKeypad() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
