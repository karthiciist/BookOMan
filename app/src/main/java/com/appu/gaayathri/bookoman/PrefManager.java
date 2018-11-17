package com.appu.gaayathri.bookoman;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by SolanD on 27.01.17.
 */

public class PrefManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "splash-welcome";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String BARCODE_IS_FIRST_TIME_LAUNCH = "BarcodeIsFirstTimeLaunch";
    private static final String CHAT_IS_FIRST_TIME_LAUNCH = "BarcodeIsFirstTimeLaunch";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public void barcodeSetFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(BARCODE_IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public void chatSetFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(CHAT_IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public boolean isBarcodeFirstTimeLaunch() {
        return pref.getBoolean(BARCODE_IS_FIRST_TIME_LAUNCH, true);
    }

    public boolean isChatFirstTimeLaunch() {
        return pref.getBoolean(CHAT_IS_FIRST_TIME_LAUNCH, true);
    }
}
