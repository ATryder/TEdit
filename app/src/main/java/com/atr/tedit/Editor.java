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

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.atr.tedit.util.TEditDB;

import java.io.File;

/**
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */

public class Editor extends Fragment {
    private TEditActivity ctx;
    private long key;

    protected static Editor newInstance(long key) {
        Bundle bundle = new Bundle();
        bundle.putLong("Editor.key", key);

        Editor editor = new Editor();
        editor.setArguments(bundle);

        return editor;
    }

    public long getKey() {
        return key;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ctx = (TEditActivity)context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            key = getArguments().getLong("Editor.key", -1);
            return;
        }

        key = savedInstanceState.getLong("Editor.key", -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.editor, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (!ctx.dbIsOpen()) {
            ((TextView)view.findViewById(R.id.documentname)).setText(TEditActivity.DEFAULTPATH);
            return;
        }

        Cursor cursor = ctx.getDB().fetchText(key);
        if (cursor == null) {
            ((TextView)view.findViewById(R.id.documentname)).setText(TEditActivity.DEFAULTPATH);
            return;
        }

        if (cursor.getColumnIndex(TEditDB.KEY_PATH) == -1) {
            ((TextView)view.findViewById(R.id.documentname)).setText(TEditActivity.DEFAULTPATH);
            cursor.close();
            return;
        }

        String path = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_PATH));
        cursor.close();
        if (path.equals(TEditActivity.DEFAULTPATH)) {
            ((TextView)view.findViewById(R.id.documentname)).setText(TEditActivity.DEFAULTPATH);
        } else {
            ((TextView) view.findViewById(R.id.documentname)).setText(new File(path).getName());

            String mediaState = Environment.getExternalStorageState();
            if (!(Environment.MEDIA_MOUNTED.equals(mediaState)
                    || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState))
                    || !path.startsWith(Environment.getExternalStorageDirectory().getPath())) {
                Toast.makeText(ctx, R.string.readonlymode, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        EditText et = (EditText)getView().findViewById(R.id.editorText);
        
        outState.putLong("Editor.key", key);
    }

    @Override
    public void onResume() {
        super.onResume();

        final EditText et = (EditText)getView().findViewById(R.id.editorText);
        et.setEnabled(true);
        et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });

        if (!ctx.dbIsOpen() || key < 0)
            return;

        Cursor cursor = ctx.getDB().fetchText(key);
        if (cursor == null)
            return;

        if (cursor.getColumnIndex(TEditDB.KEY_BODY) == -1) {
            et.setText("");
            cursor.close();
            return;
        }

        et.setText(cursor.getString(cursor.getColumnIndex(TEditDB.KEY_BODY)));
        
        final int scrollPos = cursor.getInt(cursor.getColumnIndex(TEditDB.KEY_SCROLLPOS));
        et.post(new Runnable() {
            @Override
            public void run() {
                et.clearFocus();
                et.scrollTo(0, scrollPos);
            }
        });
        cursor.close();
    }

    @Override
    public void onPause() {
        super.onPause();

        EditText et = (EditText)getView().findViewById(R.id.editorText);
        et.setEnabled(false);
        
        saveToDB();
    }

    protected void saveToDB() {
        EditText et = (EditText)getView().findViewById(R.id.editorText);

        if (!ctx.dbIsOpen() || key < 0)
            return;

        Cursor cursor = ctx.getDB().fetchText(key);
        if (cursor == null)
            return;

        String path = TEditActivity.DEFAULTPATH;
        if (cursor.getColumnIndex(TEditDB.KEY_PATH) !=  -1)
            path = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_PATH));
        cursor.close();

        ctx.getDB().updateText(key, path, et.getText().toString());
        ctx.getDB().updateTextState(key, et.getScrollY());
    }
}
