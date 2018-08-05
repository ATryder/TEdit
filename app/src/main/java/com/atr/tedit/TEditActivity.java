/*
 * Free Public License 1.0.0
 * Permission to use, copy, modify, and/or distribute this software
 * for any purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.atr.tedit;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.atr.tedit.util.ErrorMessage;
import com.atr.tedit.util.TEditDB;

import java.io.File;
import java.io.IOException;

/**
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */

public class TEditActivity extends AppCompatActivity {
    public static final String DEFAULTPATH = "Untitled";
    public static final int INIT_BROWSER_PERMISSION = 0;
    public static final int INIT_TEXT_PERMISSION = 1;
    public static final int SAVE_DOCUMENT_PERMISSION = 2;
    public static final int SAVEAS_DOCUMENT_PERMISSION = 3;
    public static final int OPEN_BROWSER_PERMISSION = 4;

    private static final int STATE_BROWSE = 0;
    private static final int STATE_TEXT = 1;
    private static final int STATE_TAB = 2;

    private int state = STATE_BROWSE;

    private DisplayMetrics dMetrics;
    private ButtonBar buttonBar;

    private TEditDB db;
    private boolean dbOpen = true;

    private File rootPath;
    private File currentPath;
    private File savePath;

    private long lastTxt = -1;

    private Fragment frag;

    private File tmpFileToOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tedit);

        dMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dMetrics);

        buttonBar = new ButtonBar((FrameLayout)findViewById(R.id.buttonbar), dMetrics,
                getResources(), this);

        db = new TEditDB(this);
        dbOpen = true;
        try {
            db.open();
        } catch (SQLException e) {
            dbOpen = false;
            Log.e("TEdit", "Unable to open database: " + e.getMessage());
        }
        if (dbOpen && savedInstanceState == null)
            db.deleteAll();

        String mediaState = Environment.getExternalStorageState();
        if (savedInstanceState == null) {
            if (Environment.MEDIA_MOUNTED.equals(mediaState)
                    || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState)) {
                rootPath = Environment.getExternalStorageDirectory();
                currentPath = new File(rootPath.getPath());
                savePath = new File(rootPath.getPath());
            } else {
                rootPath = Environment.getExternalStorageDirectory();
                if (rootPath == null)
                    rootPath = new File("/");
                currentPath = new File(rootPath.getPath());
                savePath = new File(rootPath.getPath());
            }

            if (!dbOpen) {
                initializeToBrowser();
                return;
            }

            Uri data = getIntent().getData();
            if (data == null) {
                if (getIntent().getExtras() == null) {
                    initializeToBrowser();
                    return;
                }
                Object obj = getIntent().getExtras().get(Intent.EXTRA_STREAM);
                if (obj == null || !(obj instanceof Uri)) {
                    initializeToBrowser();
                    return;
                }
                data = (Uri)obj;
            }

            String[] dataPath = data.getPath().split(":");
            File file;
            if (dataPath.length > 1) {
                file = new File(dataPath[1]);
            } else
                file = new File(dataPath[0]);
            if (!file.exists() || file.isDirectory()) {
                initializeToBrowser();
                return;
            }

            initializeToText(file);

            return;
        }

        if (Environment.MEDIA_MOUNTED.equals(mediaState)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState)) {
            rootPath = Environment.getExternalStorageDirectory();
            currentPath = new File(savedInstanceState.getString("TEdit.currentPath", rootPath.getPath()));
            savePath = new File(savedInstanceState.getString("TEdit.savePath", currentPath.getPath()));
            if (!currentPath.exists())
                currentPath = new File(rootPath.getPath());
            if (!savePath.exists())
                savePath = new File(currentPath.getPath());
        } else {
            rootPath = Environment.getExternalStorageDirectory();
            if (rootPath == null)
                rootPath = new File("/");
            currentPath = new File(rootPath.getPath());
            savePath = new File(rootPath.getPath());
        }

        lastTxt = (!dbOpen) ? -1 : savedInstanceState.getLong("TEdit.lastTxt", -1);
        int lastState = savedInstanceState.getInt("TEdit.state", -1);
        frag = getSupportFragmentManager().findFragmentById(R.id.activitycontent);
        switch (lastState) {
            case STATE_BROWSE:
                buttonBar.setToBrowser();
                state = STATE_BROWSE;
                break;
            case STATE_TEXT:
                if (lastTxt != -1) {
                    buttonBar.setToText();
                    state = STATE_TEXT;
                    break;
                }

                buttonBar.setToBrowser();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.remove(frag);
                frag = Browser.newInstance(currentPath.getPath());
                ft1.add(R.id.activitycontent, frag);
                ft1.commit();
                state = STATE_BROWSE;
                break;
            case STATE_TAB:
                buttonBar.setToTab();
                state = STATE_TAB;
                break;
            default:
                state = STATE_BROWSE;
                buttonBar.setToBrowser();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.remove(frag);
                frag = Browser.newInstance(currentPath.getPath());
                ft.add(R.id.activitycontent, frag);
                ft.commit();

        }
    }

    public DisplayMetrics getMetrics() {
        return dMetrics;
    }

    private void initializeToBrowser() {
        initializeToBrowser(false);
    }

    private void initializeToBrowser(boolean skipPermissionCheck) {
        if (!skipPermissionCheck
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkWritePermission()) {
                requestPermission(INIT_BROWSER_PERMISSION);
                return;
            }
        }

        state = STATE_BROWSE;
        buttonBar.setToBrowser();
        frag = Browser.newInstance(currentPath.getPath());

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.activitycontent, frag);
        ft.commit();
    }

    private void initializeToText(File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkWritePermission()) {
                tmpFileToOpen = file;
                requestPermission(INIT_TEXT_PERMISSION);
                return;
            }
        }
        String content = null;
        try {
            content = Browser.readFile(file);
        } catch (IOException e) {
            Log.e("TEdit", "Unable to initialize on file " + file.getPath() + ":\n" + e.getMessage());
            content = null;
        } finally {
            if (content == null) {
                lastTxt = db.createText(DEFAULTPATH, getString(R.string.error_fileassoc));
            } else
                lastTxt = db.createText(file.getPath(), content);

            state = STATE_TEXT;
            buttonBar.setToText();
            frag = Editor.newInstance(lastTxt);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.activitycontent, frag);
            ft.commit();
        }
    }

    public long getLastTxt() {
        return lastTxt;
    }

    protected void setLastTxt(long key) {
        lastTxt = key;
    }

    public void newDocument(String path, String body) {
        if (!dbOpen) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dbclosed));
            em.show(getSupportFragmentManager(), "dialog");
            return;
        }
        openDocument(db.createText(path, body));
    }

    public void openDocument(long key) {
        if (!dbOpen) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dbclosed));
            em.show(getSupportFragmentManager(), "dialog");
            return;
        }
        state = STATE_TEXT;
        lastTxt = key;
        buttonBar.setToText();
        swapFragment(Editor.newInstance(lastTxt));
    }

    public void openBrowser(String path) {
        state = STATE_BROWSE;
        File file = new File(path);
        if (file.isFile())
            file = file.getParentFile();
        if (!file.exists())
            file = new File(rootPath.getPath());
        currentPath = file;

        buttonBar.setToBrowser();
        swapFragment(Browser.newInstance(currentPath.getPath()));
    }

    public void saveBrowser(String path) {
        state = STATE_BROWSE;
        File file = new File(path);
        if (file.isFile())
            file = file.getParentFile();
        if (!file.exists())
            file = new File(rootPath.getPath());
        savePath = file;

        buttonBar.setToBrowser();
        swapFragment(Browser.newInstance(savePath.getPath(), lastTxt));
    }

    public void tabs() {
        Cursor cursor = db.fetchAllTexts();
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null)
                cursor.close();
            Toast.makeText(this, getString(R.string.noopendocuments), Toast.LENGTH_SHORT).show();
            return;
        }

        cursor.close();
        state = STATE_TAB;
        buttonBar.setToTab();
        swapFragment(new Tabs());
    }

    public void upDir() {
        if (!(frag instanceof Browser))
            return;
        ((Browser)frag).upDir();
    }

    public void saveFile(View view) {
        if (lastTxt == -1 || !(frag instanceof Browser))
            return;

        if (!dbOpen) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dbclosed));
            em.show(getSupportFragmentManager(), "dialog");

            return;
        }

        Cursor cursor = db.fetchText(lastTxt);
        if (cursor == null) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dberror));
            em.show(getSupportFragmentManager(), "dialog");
            Log.e("TEdit", "File not found in database.");

            return;
        }

        Browser browser = (Browser)frag;
        File file = new File(browser.getCurrentDir(), browser.getEnteredFilename());
        String body = null;
        if (cursor.getColumnIndex(TEditDB.KEY_BODY) != -1) {
            body = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_BODY));
        }
        cursor.close();

        if (body == null) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dberror));
            em.show(getSupportFragmentManager(), "dialog");
            Log.e("TEdit", "Body not found in database.");
            return;
        }

        setSavePath(file);
        if (!browser.saveFile(file.getName(), body)) {
            return;
        }

        db.updateText(lastTxt, file.getPath(), body);
        openDocument(lastTxt);
        Toast.makeText(this, getString(R.string.filesaved), Toast.LENGTH_SHORT).show();
    }

    private void swapFragment(Fragment newFrag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.frag_in, R.anim.frag_out);

        ft.remove(frag);
        ft.add(R.id.activitycontent, newFrag);
        frag = newFrag;

        ft.commit();
    }

    protected Fragment getFrag() {
        return frag;
    }

    protected void setCurrentPath(String path) {
        setCurrentPath(new File(path));
    }

    protected void setCurrentPath(File file) {
        if (file.isFile())
            file = file.getParentFile();
        if (!file.exists())
            return;

        currentPath = file;
    }

    protected void setSavePath(String path) { setSavePath(new File(path)); }

    protected void setSavePath(File file) {
        if (file.isFile())
            file = file.getParentFile();
        if (!file.exists())
            return;

        savePath = file;
    }

    protected File getRootPath() {
        return rootPath;
    }

    protected File getCurrentPath() {
        return currentPath;
    }

    protected File getSavePath() {
        return savePath;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("TEdit.currentPath", currentPath.getPath());
        outState.putString("TEdit.savePath", savePath.getPath());
        outState.putLong("TEdit.lastTxt", lastTxt);
        outState.putInt("TEdit.state", state);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbOpen) {
            if (isFinishing())
                db.deleteAll();
            db.close();
        }
    }

    public TEditDB getDB() {
        return db;
    }

    public boolean dbIsOpen() {
        return dbOpen;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch(state) {
                case STATE_BROWSE:
                    closeBrowser();
                    break;
                case STATE_TEXT:
                    closeText();
                    break;
                case STATE_TAB:
                    closeTabs();
                    break;
                default:
                    finish();
            }
            return true;
        } else
            return super.onKeyUp(keyCode, event);
    }

    protected void closeBrowser() {
        if (!dbIsOpen()) {
            finish();
            return;
        }

        if (getLastTxt() != -1) {
            Cursor cursor = getDB().fetchText(getLastTxt());
            if (cursor != null) {
                cursor.close();
                openDocument(getLastTxt());
                return;
            }
        }

        Cursor cursor = getDB().fetchAllTexts();
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null)
                cursor.close();
            finish();
            return;
        }

        cursor.moveToFirst();
        if (cursor.getColumnIndex(TEditDB.KEY_ROWID) == -1) {
            cursor.close();
            finish();
            return;
        }
        long id = cursor.getLong(cursor.getColumnIndex(TEditDB.KEY_ROWID));
        cursor.close();
        openDocument(id);
    }

    protected void closeText() {
        if (!dbIsOpen()) {
            setLastTxt(-1);
            openBrowser(getCurrentPath().getPath());
            return;
        }

        getDB().deleteText(getLastTxt());
        Cursor cursor = getDB().fetchAllTexts();
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null)
                cursor.close();
            setLastTxt(-1);
            openBrowser(getCurrentPath().getPath());
            return;
        }

        cursor.moveToFirst();
        if (cursor.getColumnIndex(TEditDB.KEY_ROWID) == -1) {
            cursor.close();
            setLastTxt(-1);
            openBrowser(getCurrentPath().getPath());
            return;
        }
        long id = cursor.getLong(cursor.getColumnIndex(TEditDB.KEY_ROWID));
        cursor.close();
        openDocument(id);
    }

    protected void closeTabs() {
        if (!dbIsOpen()) {
            setLastTxt(-1);
            openBrowser(getCurrentPath().getPath());
            return;
        }

        if (getLastTxt() != -1) {
            Cursor cursor = getDB().fetchText(getLastTxt());
            if (cursor != null) {
                cursor.close();
                openDocument(getLastTxt());
                return;
            }
        }

        Cursor cursor = getDB().fetchAllTexts();
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null)
                cursor.close();
            setLastTxt(-1);
            openBrowser(getCurrentPath().getPath());
            return;
        }

        cursor.moveToFirst();
        if (cursor.getColumnIndex(TEditDB.KEY_ROWID) == -1) {
            cursor.close();
            setLastTxt(-1);
            openBrowser(getCurrentPath().getPath());
            return;
        }
        long id = cursor.getLong(cursor.getColumnIndex(TEditDB.KEY_ROWID));
        cursor.close();
        openDocument(id);
    }

    protected void saveDocument(boolean skipPermissionCheck) {
        if (!skipPermissionCheck
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !checkWritePermission()) {
            requestPermission(SAVE_DOCUMENT_PERMISSION);
            return;
        }

        if (!dbIsOpen()) {
            Log.e("TEdit", "Unable to save file: Database is not open.");
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dbclosed));
            em.show(getSupportFragmentManager(), "dialog");
            return;
        }

        if (getLastTxt() == -1)
            return;

        ((Editor)getFrag()).saveToDB();
        Cursor cursor = getDB().fetchText(getLastTxt());
        if (cursor == null || cursor.getColumnIndex(TEditDB.KEY_PATH) == -1
                || cursor.getColumnIndex(TEditDB.KEY_BODY) ==  -1) {
            if (cursor == null) {
                Log.e("TEdit", "Unable to save file: Database did not contain key.");
            } else
                Log.e("TEdit", "Unable to save file: Database did not contain column");
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dberror));
            em.show(getSupportFragmentManager(), "dialog");
            cursor.close();
            return;
        }

        String path = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_PATH));
        if (path.equals(TEditActivity.DEFAULTPATH)) {
            saveBrowser(getSavePath().toString());
            cursor.close();
            return;
        }


        String mediaState = Environment.getExternalStorageState();
        if (!(Environment.MEDIA_MOUNTED.equals(mediaState)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState))) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_unmounted));
            em.show(getSupportFragmentManager(), "dialog");
            cursor.close();
            return;
        } else if (!path.startsWith(Environment.getExternalStorageDirectory().getPath())) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_protectedpath));
            em.show(getSupportFragmentManager(), "dialog");
            cursor.close();
            return;
        }

        String body = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_BODY));
        cursor.close();
        File file = new File(path);
        try {
            Browser.writeFile(file, body);
            Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !checkWritePermission()) {
                Log.e("TEdit.Browser", "Unable to save file " + file.getPath() + ". Permission denied: "
                        + e.getMessage());
                ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                        getString(R.string.error_nowritepermission));
                em.show(getSupportFragmentManager(), "dialog");
            } else {
                Log.e("TEdit.Editor", "Unable to save file " + file.getPath() + ": "
                        + e.getMessage());
                ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                        getString(R.string.error_writefile));
                em.show(getSupportFragmentManager(), "dialog");
            }
        }
    }

    protected void saveAsDocument(boolean skipPermissionCheck) {
        if (!skipPermissionCheck
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !checkWritePermission()) {
            requestPermission(SAVEAS_DOCUMENT_PERMISSION);
            return;
        }

        if (!dbIsOpen()) {
            Log.e("TEdit", "Unable to save file: Database is not open.");
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dbclosed));
            em.show(getSupportFragmentManager(), "dialog");
            return;
        }

        if (getLastTxt() == -1)
            return;

        ((Editor)getFrag()).saveToDB();
        Cursor cursor = getDB().fetchText(getLastTxt());
        if (cursor == null || cursor.getColumnIndex(TEditDB.KEY_PATH) == -1
                || cursor.getColumnIndex(TEditDB.KEY_BODY) ==  -1) {
            if (cursor == null) {
                Log.e("TEdit", "Unable to save file: Database did not contain key.");
            } else
                Log.e("TEdit", "Unable to save file: Database did not contain column");
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dberror));
            em.show(getSupportFragmentManager(), "dialog");
            cursor.close();
            return;
        }

        saveBrowser(getSavePath().toString());
    }

    protected void requestOpenBrowser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !checkWritePermission()) {
            requestPermission(SAVEAS_DOCUMENT_PERMISSION);
            return;
        }

        openBrowser(getCurrentPath().getPath());
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected boolean checkWritePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission(int requestState) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestState);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[]results) {
        switch(requestCode) {
            case SAVE_DOCUMENT_PERMISSION:
                if (results.length > 0
                        && results[0] == PackageManager.PERMISSION_GRANTED) {
                    saveDocument(true);
                } else {
                    ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                            getString(R.string.error_nowritepermission));
                    em.show(getSupportFragmentManager(), "dialog");
                }
                break;
            case SAVEAS_DOCUMENT_PERMISSION:
                if (results.length > 0
                        && results[0] == PackageManager.PERMISSION_GRANTED) {
                    saveAsDocument(true);
                } else {
                    ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                            getString(R.string.error_nowritepermission));
                    em.show(getSupportFragmentManager(), "dialog");
                }
                break;
            case OPEN_BROWSER_PERMISSION:
                if (results.length > 0
                        && results[0] == PackageManager.PERMISSION_GRANTED) {
                    openBrowser(getCurrentPath().getPath());
                } else {
                    ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                            getString(R.string.error_nowritepermission));
                    em.show(getSupportFragmentManager(), "dialog");
                }
                break;
            case INIT_BROWSER_PERMISSION:
                initializeToBrowser(true);
                break;
            case INIT_TEXT_PERMISSION:
                if (results.length > 0
                        && results[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeToText(tmpFileToOpen);
                    tmpFileToOpen = null;
                } else if (!dbIsOpen()) {
                    finish();
                } else {
                    if (getLastTxt() != -1) {
                        Cursor cursor = getDB().fetchText(getLastTxt());
                        if (cursor != null) {
                            cursor.close();
                            state = STATE_TEXT;
                            buttonBar.setToText();
                            frag = Editor.newInstance(getLastTxt());

                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.add(R.id.activitycontent, frag);
                            ft.commit();
                            return;
                        }
                    }

                    Cursor cursor = getDB().fetchAllTexts();
                    if (cursor == null || cursor.getCount() <= 0) {
                        if (cursor != null)
                            cursor.close();
                        finish();
                        return;
                    }

                    cursor.moveToFirst();
                    if (cursor.getColumnIndex(TEditDB.KEY_ROWID) == -1) {
                        cursor.close();
                        finish();
                        return;
                    }
                    long id = cursor.getLong(cursor.getColumnIndex(TEditDB.KEY_ROWID));
                    cursor.close();
                    lastTxt = id;
                    state = STATE_TEXT;
                    buttonBar.setToText();
                    frag = Editor.newInstance(getLastTxt());

                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.add(R.id.activitycontent, frag);
                    ft.commit();
                }
                break;
        }
    }
}
