package com.msg91.sendotp.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.msg91.sendotpandroid.library.internal.SendOTP;
import com.msg91.sendotpandroid.library.listners.VerificationListener;
import com.msg91.sendotpandroid.library.roots.RetryType;
import com.msg91.sendotpandroid.library.roots.SendOTPConfigBuilder;
import com.msg91.sendotpandroid.library.roots.SendOTPResponseCode;


public class VerificationActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, VerificationListener {
    private static final String TAG = "VerificationActivity";
    private static final int OTP_LNGTH = 4;
    TextView resend_timer;
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

    void createVerification(String phoneNumber, int countryCode) {
        new SendOTPConfigBuilder()
                .setCountryCode(countryCode)
                .setMobileNumber(phoneNumber)
                //////////////////direct verification while connect with mobile network/////////////////////////
                .setVerifyWithoutOtp(true)
                //////////////////Auto read otp from Sms And Verify///////////////////////////
                .setAutoVerification(VerificationActivity.this)
                .setOtpExpireInMinute(5)//default value is one day
                .setOtpHits(3) //number of otp request per number
                .setOtpHitsTimeOut(0L)//number of otp request time out reset in milliseconds default is 24 hours
                .setSenderId("ABCDEF")
                .setMessage("##OTP## is Your verification digits.")
                .setOtpLength(OTP_LNGTH)
                .setVerificationCallBack(this).build();

        SendOTP.getInstance().getTrigger().initiate();


    }


    void initiateVerification() {
        Intent intent = getIntent();
        if (intent != null) {
            DataManager.getInstance().showProgressMessage(this, "");
            String phoneNumber = intent.getStringExtra(MainActivity.INTENT_PHONENUMBER);
            int countryCode = intent.getIntExtra(MainActivity.INTENT_COUNTRY_CODE, 0);
            TextView phoneText = (TextView) findViewById(R.id.numberText);
            phoneText.setText("+" + countryCode + phoneNumber);
            createVerification(phoneNumber, countryCode);
        }
    }

    public void ResendCode() {
        startTimer();
        SendOTP.getInstance().getTrigger().resend(RetryType.VOICE);
    }

    public void onSubmitClicked(View view) {
        String code = mOtpEditText.getText().toString();
        if (!code.isEmpty()) {
            hideKeypad();
            verifyOtp(code);
            DataManager.getInstance().showProgressMessage(this, "");
            TextView messageText = (TextView) findViewById(R.id.textView);
            messageText.setText("Verification in progress");
            enableInputField(false);

        }

    }


    void enableInputField(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });

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

    void showCompleted(boolean isDirect) {
        ImageView checkMark = (ImageView) findViewById(R.id.checkmarkImage);
        if (isDirect) {
            checkMark.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_magic));
        } else {
            checkMark.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_checkmark));
        }
        checkMark.setVisibility(View.VISIBLE);
    }

    public void verifyOtp(String otp) {
        SendOTP.getInstance().getTrigger().verify(otp);
    }


    @Override
    public void onSendOtpResponse(final SendOTPResponseCode responseCode, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "onSendOtpResponse: " + responseCode.getCode() + "=======" + message);
                if (responseCode == SendOTPResponseCode.DIRECT_VERIFICATION_SUCCESSFUL_FOR_NUMBER || responseCode == SendOTPResponseCode.OTP_VERIFIED) {
                    DataManager.getInstance().hideProgressMessage();
                    enableInputField(false);
                    hideKeypad();
                    TextView textView2 = (TextView) findViewById(R.id.textView2);
                    TextView textView1 = (TextView) findViewById(R.id.textView1);
                    TextView messageText = (TextView) findViewById(R.id.textView);
                    ImageView topImg = (ImageView) findViewById(R.id.topImg);
                    TextView phoneText = (TextView) findViewById(R.id.numberText);
                    RelativeLayout topLayout = findViewById(R.id.topLayout);
                    if (android.os.Build.VERSION.SDK_INT > 16)
                        topLayout.setBackgroundDrawable(ContextCompat.getDrawable(VerificationActivity.this, R.drawable.gradient_bg_white));
                    else
                        topLayout.setBackgroundResource(R.drawable.gradient_bg_white);
                    messageText.setVisibility(View.GONE);
                    phoneText.setVisibility(View.GONE);
                    topImg.setVisibility(View.INVISIBLE);
                    textView1.setVisibility(View.VISIBLE);
                    textView2.setVisibility(View.VISIBLE);
                    if (responseCode == SendOTPResponseCode.DIRECT_VERIFICATION_SUCCESSFUL_FOR_NUMBER)
                        textView2.setText("Mobile verified using Invisible OTP.");
                    else textView2.setText("Your Mobile number has been successfully verified.");

                    hideProgressBarAndShowMessage(R.string.verified);
                    showCompleted(responseCode == SendOTPResponseCode.DIRECT_VERIFICATION_SUCCESSFUL_FOR_NUMBER);
                } else if (responseCode == SendOTPResponseCode.READ_OTP_SUCCESS) {
                    DataManager.getInstance().hideProgressMessage();
                    mOtpEditText.setText(message);

                } else if (responseCode == SendOTPResponseCode.SMS_SUCCESSFUL_SEND_TO_NUMBER || responseCode == SendOTPResponseCode.DIRECT_VERIFICATION_FAILED_SMS_SUCCESSFUL_SEND_TO_NUMBER) {
                    DataManager.getInstance().hideProgressMessage();
                } else if (responseCode == SendOTPResponseCode.NO_INTERNET_CONNECTED) {
                    DataManager.getInstance().hideProgressMessage();
                } else {
                    DataManager.getInstance().hideProgressMessage();
                    hideKeypad();
                    hideProgressBarAndShowMessage(R.string.failed);
                    enableInputField(true);
                }
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SendOTP.getInstance().getTrigger().stop();
    }
}
