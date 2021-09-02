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
package com.atr.tedit.mainstate;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.dialog.ErrorMessage;
import com.atr.tedit.file.descriptor.AndFile;
import com.atr.tedit.settings.Settings;
import com.atr.tedit.settings.TxtSettings;
import com.atr.tedit.util.FontUtil;
import com.atr.tedit.util.SettingsApplicable;
import com.atr.tedit.util.TextSearch;
import com.atr.tedit.util.TEditDB;
import com.atr.tedit.utilitybar.UtilityBar;
import com.atr.tedit.utilitybar.state.TextSearchState;

/**
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */

public class Editor extends Fragment implements SettingsApplicable {
    private TEditActivity ctx;
    private long key;

    private EditText editText;
    private TextView docName;
    private TextWatcher editorChangeListener;

    private TextSearch searchString;
    private TextSearchState barSearch;

    private TxtSettings settings;

    private AndFile file;

    public static Editor newInstance(long key) {
        Bundle bundle = new Bundle();
        bundle.putLong("Editor.key", key);

        Editor editor = new Editor();
        editor.setArguments(bundle);

        return editor;
    }

    public long getKey() {
        return key;
    }

    public Editable getText() {
        return editText.getText();
    }

    public TxtSettings getSettings() {
        return settings;
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
        editText = (EditText)getView().findViewById(R.id.editorText);
        searchString = new TextSearch();

        docName = view.findViewById(R.id.documentname);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putLong("Editor.key", key);
    }

    @Override
    public void onResume() {
        super.onResume();

        editText.setEnabled(true);
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });

        boolean clear = !ctx.dbIsOpen() || key < 0;
        Cursor cursor = null;
        if (!clear) {
            cursor = ctx.getDB().fetchText(key);
            if (cursor == null) {
                cursor = ctx.getDB().fetchAllTexts();
                if (cursor == null) {
                    clear = true;
                } else if (cursor.getCount() <= 0) {
                    cursor.close();
                    clear = true;
                } else {
                    cursor.moveToFirst();
                    if (cursor.getColumnIndex(TEditDB.KEY_ROWID) == -1) {
                        cursor.close();
                        clear = true;
                    } else {
                        key = cursor.getLong(cursor.getColumnIndex(TEditDB.KEY_ROWID));
                        ctx.setLastTxt(key);
                    }
                }
            }
        }

        if (!clear) {
            if (cursor.getColumnIndex(TEditDB.KEY_BODY) == -1) {
                editText.setText("");
            } else
                editText.setText(cursor.getString(cursor.getColumnIndex(TEditDB.KEY_BODY)));

            if (cursor.getColumnIndex(TEditDB.KEY_DATA) == -1) {
                settings = new TxtSettings();
            } else
                settings = new TxtSettings(cursor.getBlob(cursor.getColumnIndex(TEditDB.KEY_DATA)));

            if (cursor.getColumnIndex(TEditDB.KEY_PATH) == -1) {
                docName.setText(TEditActivity.DEFAULTPATH);
            } else {
                String path = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_PATH));
                if (path.equals(TEditActivity.DEFAULTPATH)) {
                    docName.setText(TEditActivity.DEFAULTPATH);
                } else {
                    file = AndFile.createDescriptor(path, ctx);

                    if (file == null || file.getName() ==  null || file.getName().isEmpty() || !file.exists()) {
                        docName.setText(TEditActivity.DEFAULTPATH);
                    } else
                        docName.setText(file.getName());
                }
            }

            editText.post(new Runnable() {
                @Override
                public void run() {
                    editText.setPressed(true);
                    int scrollX = 0;
                    if (settings.wordWrap < 0) {
                        scrollX = (Settings.isWordWrap()) ? 0 : settings.scrollPosX;
                    } else if (settings.wordWrap == 0)
                        scrollX = settings.scrollPosX;
                    editText.setSelection(settings.selectionStart, settings.selectionEnd);
                    editText.scrollTo(scrollX, settings.scrollPosY);
                }
            });

            if (!settings.searchActive) {
                if (ctx.getUtilityBar().getState().STATE == UtilityBar.STATE_TEXT
                        && ctx.getUtilityBar().getState().getLayer() == settings.utilityBarLayer) {
                    ctx.getUtilityBar().getState().setEnabled(false);
                    ctx.getUtilityBar().handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ctx.getUtilityBar().getState().setEnabled(true);
                        }
                    }, TEditActivity.SWAP_ANIM_LENGTH);
                } else
                    ctx.getUtilityBar().setState(ctx.getUtilityBar().UTILITY_STATE_TEXT, settings.utilityBarLayer);

                barSearch = new TextSearchState(ctx.getUtilityBar(), settings.searchPhrase,
                        settings.searchReplacePhrase, settings.searchWholeWord, settings.searchMatchCase);
            } else if (ctx.getUtilityBar().getState().STATE == UtilityBar.STATE_TEXT_SEARCH){
                barSearch = (TextSearchState)ctx.getUtilityBar().getState();
                barSearch.setFields(settings.searchPhrase, settings.searchReplacePhrase, settings.searchWholeWord,
                        settings.searchMatchCase);
            } else {
                barSearch = new TextSearchState(ctx.getUtilityBar(), settings.searchPhrase,
                        settings.searchReplacePhrase, settings.searchWholeWord, settings.searchMatchCase);
                activateSearch();
            }
            cursor.close();
        } else {
            editText.setText("");
            settings = new TxtSettings();
            docName.setText(TEditActivity.DEFAULTPATH);
            if (ctx.dbIsOpen()) {
                key = ctx.getDB().createText(TEditActivity.DEFAULTPATH, "");
                ctx.setLastTxt(key);
            }
        }

        applySettings();

        editorChangeListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchString.clearSearchCache();
                settings.saved = false;
            }
        };
        editText.addTextChangedListener(editorChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        editText.setEnabled(false);
        
        saveToDB();
    }

    public void saveToDB() {
        if (!ctx.dbIsOpen() || key < 0)
            return;

        Cursor cursor = ctx.getDB().fetchText(key);
        if (cursor == null)
            return;

        String path = TEditActivity.DEFAULTPATH;
        if (cursor.getColumnIndex(TEditDB.KEY_PATH) !=  -1)
            path = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_PATH));
        cursor.close();

        ctx.getDB().updateText(key, path, editText.getText().toString());
        if (settings == null)
            settings = new TxtSettings();
        settings.scrollPosX = editText.getScrollX();
        settings.scrollPosY = editText.getScrollY();
        settings.selectionStart = editText.getSelectionStart();
        settings.selectionEnd = editText.getSelectionEnd();
        settings.utilityBarLayer = ctx.getUtilityBar().getState().getLayer();
        settings.searchActive = ctx.getUtilityBar().getState().STATE == UtilityBar.STATE_TEXT_SEARCH;
        settings.searchPhrase = barSearch.getSearchPhrase();
        settings.searchReplacePhrase = barSearch.getReplacePhrase();
        settings.searchWholeWord = barSearch.isWholeWord();
        settings.searchMatchCase = barSearch.isMatchCase();
        ctx.getDB().updateTextState(key, settings);
    }

    public void activateSearch() {
        ctx.getUtilityBar().setState(barSearch);
    }

    public void findNext(String phrase) {
        if (phrase.isEmpty())
            return;

        searchString.setSearchPhrase(phrase);
        TextSearch.SearchResult sr;

        try {
            sr = searchString.nextSearchResult(editText.getText(), editText.getSelectionEnd());
        } catch (OutOfMemoryError e) {
            sr = null;
            Log.e("TEdit.Editor", "Out of memory while attempting a Find Next text search: "
                    + e.getMessage());
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.error),
                    getString(R.string.error_oom));
            em.show(ctx.getSupportFragmentManager(), "dialog");
        }

        if (sr == null) {
            editText.setSelection(editText.getSelectionStart());
            return;
        }

        barSearch.clearFocus();
        editText.setSelection(sr.start, sr.end);
        editText.requestFocus();
    }

    public void findPrevious(String phrase) {
        if (phrase.isEmpty())
            return;

        searchString.setSearchPhrase(phrase);
        TextSearch.SearchResult sr;
        try {
            sr = searchString.previousSearchResult(editText.getText(), editText.getSelectionStart());
        } catch (OutOfMemoryError e) {
            sr = null;
            Log.e("TEdit.Editor", "Out of memory while attempting a Find Previous text search: "
                    + e.getMessage());
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.error),
                    getString(R.string.error_oom));
            em.show(ctx.getSupportFragmentManager(), "dialog");
        }

        if (sr == null) {
            editText.setSelection(editText.getSelectionStart());
            return;
        }

        barSearch.clearFocus();
        editText.setSelection(sr.start, sr.end);
        editText.requestFocus();
    }

    public void replace(String phrase, String replaceWith) {
        if (phrase.isEmpty())
            return;

        searchString.setSearchPhrase(phrase);
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        TextSearch.SearchResult sr;
        try {
            sr = searchString.getSelectedResult(editText.getText(), start, end);
        } catch (OutOfMemoryError e) {
            sr = null;
            Log.e("TEdit.Editor", "Out of memory while attempting to get Selected Search Result: "
                    + e.getMessage());
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.error),
                    getString(R.string.error_oom));
            em.show(ctx.getSupportFragmentManager(), "dialog");
        }

        if (sr == null)
            return;

        Editable text;
        try {
            text = searchString.replace(editText.getText(), replaceWith, start, end);
        } catch (OutOfMemoryError e) {
            text = null;
            Log.e("TEdit.Editor", "Out of memory while attempting Replace action: "
                    + e.getMessage());
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.error),
                    getString(R.string.error_oom));
            em.show(ctx.getSupportFragmentManager(), "dialog");
        }

        if (text == null)
            return;

        barSearch.clearFocus();
        editText.removeTextChangedListener(editorChangeListener);
        final int scrollPos = editText.getScrollY();
        final int cstart = sr.start;
        final int cend = sr.start + replaceWith.length();
        editText.setText(text);
        editText.addTextChangedListener(editorChangeListener);

        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.scrollTo(0, scrollPos);
                editText.setSelection(cstart, cend);
                editText.requestFocus();
            }
        });
    }

    public void replaceAll(String phrase, String replaceWith) {
        if (phrase.isEmpty())
            return;

        searchString.setSearchPhrase(phrase);
        Editable text = editText.getText();
        int total = searchString.getCache(text).length;

        if (total == 0) {
            Toast.makeText(ctx, "0 " + ctx.getString(R.string.items_replaced),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            text = searchString.replaceAll(editText.getText(), replaceWith);
        } catch (OutOfMemoryError e) {
            text = null;
            Log.e("TEdit.Editor", "Out of memory while attempting Replace All action: "
                    + e.getMessage());
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.error),
                    getString(R.string.error_oom));
            em.show(ctx.getSupportFragmentManager(), "dialog");
        }

        if (text == null)
            return;

        barSearch.clearFocus();
        editText.removeTextChangedListener(editorChangeListener);
        final int scrollPos = editText.getScrollY();
        final int cursorPos = editText.getSelectionStart() <= text.length() ? editText.getSelectionStart() : text.length();
        editText.setText(text);
        editText.addTextChangedListener(editorChangeListener);

        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.setSelection(cursorPos);
                editText.scrollTo(0, scrollPos);
                editText.requestFocus();
            }
        });

        Toast.makeText(ctx, Integer.toString(total) + " " + ctx.getString(R.string.items_replaced),
                Toast.LENGTH_SHORT).show();
    }

    public TextSearch getSearchString() {
        return searchString;
    }

    public void clearFocus() {
        if (editText.hasFocus()) {
            InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            } catch (Exception e) {

            }
            editText.clearFocus();

            return;
        }

        barSearch.clearFocus();
    }

    public void hideCursor(boolean hide) {
        editText.setCursorVisible(!hide);
        barSearch.hideCursor(hide);
    }

    @Override
    public void applySettings() {
        barSearch.applySettings();

        editText.setTypeface(FontUtil.getTypefaceFromPath(settings.typeface, FontUtil.getEditorTypeface()));
        if (settings.pointSize < 0) {
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontUtil.getEditorSize());
        } else
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, settings.pointSize);

        boolean wrap = (settings.wordWrap < 0) ? Settings.isWordWrap() : settings.wordWrap == 1;
        editText.setHorizontallyScrolling(!wrap);
        if (wrap && editText.getScrollX() > 0)
            editText.post(new Runnable() {
                public void run() {
                    editText.scrollTo(0, editText.getScrollY());
                }
            });

        docName.setTypeface(FontUtil.getSystemTypeface());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            editText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            int textDirection = settings.textDirection < 0 ? Settings.getEditorTextDirection() : settings.textDirection;
            editText.setTextDirection((textDirection == Settings.TEXTDIR_LTR) ?
                    View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);

            docName.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            textDirection = Settings.getSystemTextDirection();
            docName.setTextDirection((textDirection == Settings.TEXTDIR_LTR) ?
                    View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);
        }
    }
}
