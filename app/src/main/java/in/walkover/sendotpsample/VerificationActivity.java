package in.walkover.sendotpsample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.msg91.sendotpandroid.library.SendOtpVerification;
import com.msg91.sendotpandroid.library.Verification;
import com.msg91.sendotpandroid.library.VerificationListener;


public class VerificationActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, VerificationListener {

    private static final String TAG = Verification.class.getSimpleName();
    TextView resend_timer;
    private Verification mVerification;

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
        enableInputField(true);
        initiateVerification();
    }

    void createVerification(String phoneNumber, boolean skipPermissionCheck, String countryCode) {
        if (!skipPermissionCheck && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 0);
            hideProgressBar();
        } else {
            mVerification = SendOtpVerification.createSmsVerification
                    (SendOtpVerification
                            .config(countryCode + phoneNumber)
                            .context(this)
                            .autoVerification(false)
                            .httpsConnection(false)//connection to be use in network calls
                            .expiry("5")//value in minutes
                            .senderId("SMSIND") //where XXXX is any string
//                            .otp("1234")// Default Otp code if want to add yours
                            .otplength("4") //length of your otp
                            .message("##OTP## is Your verification digits.")
                            .build(), this);
            mVerification.initiate();
        }
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
        String code = ((EditText) findViewById(R.id.inputCode)).getText().toString();
        if (!code.isEmpty()) {
            hideKeypad();
            if (mVerification != null) {
                mVerification.verify(code);
                showProgress();
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
            EditText input = (EditText) findViewById(R.id.inputCode);
            input.requestFocus();
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
        checkMark.setVisibility(View.VISIBLE);
    }

    @Override
    public void onInitiated(String response) {
        Log.d(TAG, "Initialized!" + response);
    }

    @Override
    public void onInitiationFailed(Exception exception) {
        Log.e(TAG, "Verification initialization failed: " + exception.getMessage());
        hideProgressBarAndShowMessage(R.string.failed);
    }

    @Override
    public void onVerified(String response) {
        enableInputField(false);
        Log.d(TAG, "Verified!\n" + response);
        hideKeypad();
        hideProgressBarAndShowMessage(R.string.verified);
        showCompleted();
    }

    @Override
    public void onVerificationFailed(Exception exception) {
        Log.e(TAG, "Verification failed: " + exception.getMessage());
        hideKeypad();
        hideProgressBarAndShowMessage(R.string.failed);
        enableInputField(true);
    }

    private void startTimer() {
        resend_timer.setClickable(false);
        resend_timer.setTextColor(ContextCompat.getColor(VerificationActivity.this, R.color.sendotp_grey));
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
