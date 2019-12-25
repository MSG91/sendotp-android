package com.msg91.sendotp.sample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.msg91.sendotpandroid.library.SendOTPConfig;
import com.msg91.sendotpandroid.library.SendOtpVerification;
import com.msg91.sendotpandroid.library.Verification;
import com.msg91.sendotpandroid.library.VerificationListener;


public class VerificationActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, VerificationListener {

    private static final String TAG = Verification.class.getSimpleName();
    TextView resend_timer;
    private boolean isDirect = true;
    private Verification mVerification;
private  OtpEditText mOtpEditText;
private static final int OTP_LNGTH = 4;
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
        mOtpEditText =  findViewById(R.id.inputCode);
        mOtpEditText.setMaxLength(OTP_LNGTH);
        enableInputField(true);
        initiateVerification();
    }

    void createVerification(String phoneNumber, boolean skipPermissionCheck, String countryCode) {
        if (!skipPermissionCheck && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 0);
            DataManaer.getInstance().hideProgressMessage();
        } else {
            boolean withoutOtp = false;
            if (NetworkConnectivity.isConnectedMobileNetwork(getApplicationContext())) {
                withoutOtp = true;
            } else {

            }


            SendOTPConfig otpConfig = SendOtpVerification
                    .config(countryCode + phoneNumber)
                    .context(this)
                    .httpsConnection(false)//use false currently https is under maintenance
                    //////////////////direct verification while connect with mobile network/////////////////////////
                    .autoVerification(false)
                    .setIp(getIp(withoutOtp))
                    .verifyWithoutOtp(withoutOtp)
                    //////////////////////////////////////////////////////////////////////////////////////////////////
                    .unicode(false) // set true if you want to use unicode (or other language) in sms
                    .expiry("5")//value in minutes
                    .senderId("ABCDEF") //where ABCDEF is any string
                    .otplength(String.valueOf(OTP_LNGTH)) //length of your otp max length up to 9 digits
                    //--------case 1-------------------
//                            .message("##OTP## is Your verification digits.")//##OTP## use for default generated OTP
                    //--------case 2-------------------
                    /*  .otp("1234")// Custom Otp code, if want to add yours
                      .message("1234 is Your verification digits.")//Here 1234 same as above Custom otp.*/
                    //-------------------------------------
                    //use single case at a time either 1 or 2
                    .build();
            mVerification = SendOtpVerification.createSmsVerification
                    (otpConfig, this);
            mVerification.initiate();
        }
    }


    /**
     * This work is done  by me rajendra verma
     * if moiblenetwork is true than device is in mobile network
     */
    private String getIp(boolean moibleNetwork) {
        if (moibleNetwork) {
            try {

                return IPConverter.getIPAddress(true);
            } catch (Exception ex) {
            }

        } else {
            WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            return Formatter.formatIpAddress(ip);
        }
        return "";
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "This application needs permission to read your SMS to automatically verify your "
                        + "phone, you may disable the permission once you have been verified.", Toast.LENGTH_LONG)
                        .show();
            }
            enableInputField(true);
        }
        initiateVerificationAndSuppressPermissionCheck();
    }

    void initiateVerification() {
        initiateVerification(false);
    }

    void initiateVerificationAndSuppressPermissionCheck() {
        initiateVerification(true);
    }

    void initiateVerification(boolean skipPermissionCheck) {
        Intent intent = getIntent();
        if (intent != null) {
            DataManaer.getInstance().showProgressMessage(this,"");
            String phoneNumber = intent.getStringExtra(MainActivity.INTENT_PHONENUMBER);
            String countryCode = intent.getStringExtra(MainActivity.INTENT_COUNTRY_CODE);
            TextView phoneText = (TextView) findViewById(R.id.numberText);
            phoneText.setText("+" + countryCode + phoneNumber);
            createVerification(phoneNumber, skipPermissionCheck, countryCode);
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
                DataManaer.getInstance().showProgressMessage(this,"");
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
        if(isDirect){
            checkMark.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_magic));
        }else {
            checkMark.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_checkmark));
        }
        checkMark.setVisibility(View.VISIBLE);
    }

    @Override
    public void onInitiated(String response) {
        Log.d(TAG, "Initialized!" + response);
        isDirect = false;
        DataManaer.getInstance().hideProgressMessage();
    }

    @Override
    public void onInitiationFailed(Exception exception) {
        DataManaer.getInstance().hideProgressMessage();
        Log.e(TAG, "Verification initialization failed: " + exception.getMessage());
        hideProgressBarAndShowMessage(R.string.failed);
    }

    @Override
    public void onVerified(String response) {
        DataManaer.getInstance().hideProgressMessage();
        enableInputField(false);
        Log.d(TAG, "Verified!\n" + response);
        hideKeypad();
        TextView textView2 = (TextView) findViewById(R.id.textView2);
        TextView textView1 = (TextView) findViewById(R.id.textView1);
        TextView messageText = (TextView) findViewById(R.id.textView);
        ImageView topImg = (ImageView) findViewById(R.id.topImg);
        TextView phoneText = (TextView) findViewById(R.id.numberText);
        RelativeLayout topLayout  = findViewById(R.id.topLayout);
        topLayout.setBackgroundDrawable(ContextCompat.getDrawable(this,R.drawable.gradient_bg_white));
        messageText.setVisibility(View.GONE);
        phoneText.setVisibility(View.GONE);
        topImg.setVisibility(View.INVISIBLE);
        textView1.setVisibility(View.VISIBLE);
        textView2.setVisibility(View.VISIBLE);
        if(isDirect)
        textView2.setText("Mobile verified using Invisible OTP.");
       else textView2.setText("Your Mobile number has been successfully verified.");

        hideProgressBarAndShowMessage(R.string.verified);
        showCompleted();
    }

    @Override
    public void onVerificationFailed(Exception exception) {
        DataManaer.getInstance().hideProgressMessage();
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
                resend_timer.setTextColor(ContextCompat.getColor(VerificationActivity.this, R.color.send_otp_blue));
            }
        }.start();
    }

    private void hideKeypad() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
/*        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0)*/
    }
}
