package com.igio90.crbot;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.chrisplus.rootmanager.RootManager;
import com.chrisplus.rootmanager.container.Result;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String CR_PACKAGE = "com.supercell.clashroyale";

    private String mToken = "";
    private int mAcctIdHigh = 0;
    private int mAcctIdLow = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("bot", Context.MODE_PRIVATE);

        boolean isCrInstalled = appInstalledOrNot(CR_PACKAGE);
        if (isCrInstalled) {
            // Get root
            RootManager rootManager = RootManager.getInstance();
            // What's yours, is also mine
            String cmd = "cp /data/data/" + CR_PACKAGE + "/shared_prefs/storage.xml /data/data/" + getPackageName() + "/shared_prefs/storage.xml";
            Result result = rootManager.runCommand(cmd);
            if (result.getMessage().isEmpty()) {
                rootManager.runCommand("chmod 644 /data/data/" + getPackageName() + "/shared_prefs/storage.xml");
                PrefsBreaker prefsBreaker = new PrefsBreaker(this, "storage", "4c3c4f0b854f0b3a");
                SharedPreferences.Editor editor = preferences.edit();
                for (Map.Entry<String, ?> entry : prefsBreaker.getMap().entrySet()) {
                    String key = entry.getKey();
                    String decodedKey = prefsBreaker.decodeKey(key);
                    String value = prefsBreaker.getString(key, true);

                    switch (decodedKey) {
                        case "Pass_PROD":
                            mToken = value;
                            editor.putString("token", value);
                            break;
                        case "Low_PROD":
                            mAcctIdLow = Integer.parseInt(value);
                            editor.putInt("low", mAcctIdLow);
                            break;
                        case "High_PROD":
                            mAcctIdHigh = Integer.parseInt(value);
                            editor.putInt("high", mAcctIdHigh);
                            break;
                    }
                }
                editor.apply();

                if (mAcctIdHigh == 0 && mAcctIdLow == 0 || mToken.isEmpty()) {
                    finishWithToast(getString(R.string.error_finding_account));
                } else {
                    TextView textView = (TextView) findViewById(R.id.hello_crack);
                    textView.setText("CRACKED:\n\nToken: " + mToken + "\nID: " + mAcctIdHigh + "/" + mAcctIdLow);
                }
            } else {
                finishWithToast(getString(R.string.error_copying_prefs));
            }
        } else {
            crIsNotInstalled();
        }
    }

    private void finishWithToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    finish();
                }
            }
        }, 500);
    }

    private void crIsNotInstalled() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.cr_not_installed))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }
}
