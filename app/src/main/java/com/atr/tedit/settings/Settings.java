package com.atr.tedit.settings;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.atr.tedit.TEditActivity;
import com.atr.tedit.file.AndPath;
import com.atr.tedit.util.FontUtil;

public class Settings {
    public static final int TEXTDIR_LTR = 0;
    public static final int TEXTDIR_RTL = 1;

    public static final int AOB_CLOSE = 0;
    public static final int AOB_PARENT = 1;

    private static AndPath startupPath;

    private static int actionOnBack = AOB_PARENT;
    private static boolean enableRoot = false;

    private static boolean wordWrap = true;
    private static int systemTextDirection = TEXTDIR_LTR;
    private static int editorTextDirection = TEXTDIR_LTR;

    private static boolean showPermitHelp = true;
    private static boolean firstRun = true;

    public static AndPath getStartupPath() { return startupPath == null ? null : startupPath.clone(); }

    public static void setStartupPath(AndPath path) {
        startupPath = path;
    }

    public static boolean isWordWrap() {
        return wordWrap;
    }

    protected static void setWordWrap(boolean wrap) {
        wordWrap = wrap;
    }

    public static int getSystemTextDirection() {
        return systemTextDirection;
    }

    protected static void setSystemTextDirection(int direction) {
        systemTextDirection = (direction > TEXTDIR_RTL) ? TEXTDIR_RTL : (direction < TEXTDIR_LTR) ? TEXTDIR_LTR : direction;
    }

    public static int getActionOnBack() {
        return actionOnBack;
    }

    protected static void setActionOnBack(int aob) {
        actionOnBack = (aob > AOB_PARENT) ? AOB_PARENT : (aob < AOB_CLOSE) ? AOB_CLOSE : aob;
    }

    public static boolean isEnableRoot() { return enableRoot; }

    public static void setEnableRoot(boolean enable) { enableRoot = enable; }

    public static boolean isShowPermitHelp() {
        return showPermitHelp;
    }

    public static void setShowPermitHelp(boolean show) {
        showPermitHelp = show;
    }

    public static int getEditorTextDirection() {
        return editorTextDirection;
    }

    protected static void setEditorTextDirection(int direction) {
        editorTextDirection = (direction > TEXTDIR_RTL) ? TEXTDIR_RTL : (direction < TEXTDIR_LTR) ? TEXTDIR_LTR : direction;
    }

    public static void loadSettings(final TEditActivity ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getPackageName(), ctx.MODE_PRIVATE);

        String startupJson = prefs.getString("startupPath", "");
        if (startupJson.equals("null") || startupJson.isEmpty()) {
            startupPath = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ? null : ctx.getCurrentPath().clone();
        } else {
            try {
                startupPath = AndPath.fromJson(ctx, prefs.getString("startupPath", ""));
            } catch (Exception e) {
                startupPath = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ? null : ctx.getCurrentPath().clone();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && startupPath != null) {
                Uri[] uris = ctx.getPermittedUris();
                boolean found = false;
                for (int i = 0; i < uris.length; i++) {
                    if (((DocumentFile) startupPath.getRoot().getFile()).getUri().toString().startsWith(uris[i].toString())) {
                        found = true;
                        break;
                    }
                }

                if (!found)
                    startupPath = null;
            }
        }

        wordWrap = prefs.getBoolean("wordWrap", true);
        systemTextDirection = prefs.getInt("systemTextDirection", TEXTDIR_LTR);
        editorTextDirection = prefs.getInt("editorTextDirection", TEXTDIR_LTR);

        FontUtil.setSystemTypeface(prefs.getString("systemTypeface", "montserratalternates_regular"));
        FontUtil.setEditorTypeface(prefs.getString("editorTypeface", "metropolis_regular"));
        FontUtil.setEditorSize(prefs.getInt("editorFontSize", 15));

        actionOnBack = prefs.getInt("actionOnBack", AOB_PARENT);
        enableRoot = prefs.getBoolean("enableRoot", false);

        showPermitHelp = prefs.getBoolean("showPermitHelp", true);
    }

    public static void saveSettings(final TEditActivity ctx) {
        SharedPreferences.Editor prefs = ctx.getSharedPreferences(ctx.getPackageName(), ctx.MODE_PRIVATE).edit();

        prefs.putString("startupPath", startupPath == null ? "null" : startupPath.toJson());
        prefs.putBoolean("wordWrap", wordWrap);
        prefs.putInt("systemTextDirection", systemTextDirection);
        prefs.putInt("editorTextDirection", editorTextDirection);
        prefs.putString("systemTypeface", FontUtil.getSystemPath());
        prefs.putString("editorTypeface", FontUtil.getEditorPath());
        prefs.putInt("editorFontSize", FontUtil.getEditorSize());
        prefs.putInt("editorFontSize", FontUtil.getEditorSize());
        prefs.putInt("actionOnBack", actionOnBack);
        prefs.putBoolean("enableRoot", enableRoot);

        prefs.putBoolean("showPermitHelp", showPermitHelp);

        prefs.commit();
    }

    public static boolean isFirstRun(final TEditActivity ctx) {
        if (!firstRun)
            return false;

        firstRun = false;
        SharedPreferences prefs = ctx.getSharedPreferences(ctx.getPackageName(), ctx.MODE_PRIVATE);
        long lastVer = prefs.getLong("longVersion", -1);
        if (lastVer < 0)
            lastVer = prefs.getInt("version", 0);
        long currentVer = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                currentVer = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES).getLongVersionCode();
            } else
                currentVer = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES).versionCode;
        } catch (Exception e) {
            Log.w("TEdit Settings:", "Unable to assertain current version: " + e.getMessage());
            currentVer = 19;
        }
        return lastVer < currentVer;
    }

    public static void saveVer(final TEditActivity ctx) {
        SharedPreferences.Editor prefs = ctx.getSharedPreferences(ctx.getPackageName(), ctx.MODE_PRIVATE).edit();
        long currentVer = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                currentVer = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES).getLongVersionCode();
            } else
                currentVer = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES).versionCode;
        } catch (Exception e) {
            Log.w("TEdit Settings:", "Unable to assertain current version: " + e.getMessage());
            currentVer = 19;
        }
        prefs.putLong("longVersion", currentVer);
        prefs.commit();
        firstRun = false;
    }
}
