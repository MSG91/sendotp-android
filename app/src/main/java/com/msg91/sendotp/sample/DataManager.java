package com.msg91.sendotp.sample;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class DataManager {
    private static final DataManager ourInstance = new DataManager();
    private boolean isProgressDialogRunning;
    private Dialog mDialog;

    private DataManager() {
    }

    public static DataManager getInstance() {
        return ourInstance;
    }

    public void hideProgressMessage() {
        isProgressDialogRunning = true;
        try {
            if (mDialog != null)
                mDialog.dismiss();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void showProgressMessage(Context dialogActivity, String msg) {
        try {
            if (isProgressDialogRunning) {
                hideProgressMessage();
            }
            isProgressDialogRunning = true;
            mDialog = new Dialog(dialogActivity, R.style.MyMaterialTheme);
            mDialog.setContentView(R.layout.custom_progress_bar);
//            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(dialogActivity,R.color.white_trance)));
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            TextView textView = mDialog.findViewById(R.id.loadingText);
            textView.setVisibility(View.VISIBLE);
            if (msg != null)
                textView.setText(Html.fromHtml(msg));
            else textView.setVisibility(View.GONE);
            WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
            lp.dimAmount = 0.8f;
            mDialog.getWindow().setAttributes(lp);
            mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
