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
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.dialog.ConfirmCloseText;
import com.atr.tedit.dialog.ErrorMessage;
import com.atr.tedit.file.descriptor.AndFile;
import com.atr.tedit.settings.Settings;
import com.atr.tedit.settings.TxtSettings;
import com.atr.tedit.util.FontUtil;
import com.atr.tedit.util.SettingsApplicable;
import com.atr.tedit.util.TEditDB;
import com.atr.tedit.utilitybar.UtilityBar;

import java.io.File;

/**
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */

public class Tabs extends ListFragment implements SettingsApplicable {
    private TEditActivity ctx;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ctx = (TEditActivity)context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tabs, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ctx.getUtilityBar().getState().STATE == UtilityBar.STATE_TAB) {
            ctx.getUtilityBar().getState().setEnabled(false);
            ctx.getUtilityBar().handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ctx.getUtilityBar().getState().setEnabled(true);
                }
            }, TEditActivity.SWAP_ANIM_LENGTH);
        } else
            ctx.getUtilityBar().setToTab();

        populateTabs();
        getListView().setEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        getListView().setEnabled(false);
    }

    private void populateTabs() {
        Cursor cursor = ctx.getDB().fetchAllTexts();
        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null)
                cursor.close();
            closeTabsView();
            return;
        }

        DBItem[] items = new DBItem[cursor.getCount()];
        int index = 0;
        while (cursor.moveToNext()) {
            String name = null;
            long key = -1;
            String desc = "";
            if (cursor.getColumnIndex(TEditDB.KEY_PATH) != -1) {
                String strName = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_PATH));
                if (strName.equalsIgnoreCase(TEditActivity.DEFAULTPATH)) {
                    name = TEditActivity.DEFAULTPATH;
                } else {
                    AndFile file = AndFile.createDescriptor(cursor.getString(cursor.getColumnIndex(TEditDB.KEY_PATH)), ctx);
                    name = file == null ? null : file.getName();
                }
                key = cursor.getLong(cursor.getColumnIndex(TEditDB.KEY_ROWID));
                desc = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_BODY));
            }

            if (key == -1 || name == null)
                continue;

            items[index++] = new DBItem(name, key, desc);
        }
        cursor.close();

        DBAdapter adapter = new DBAdapter(ctx, (Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                R.layout.tab_row : R.layout.tab_row_rtl, items, this);
        if (getListView().getAdapter() == null) {
            setListAdapter(adapter);
        } else {
            Parcelable state = getListView().onSaveInstanceState();
            setListAdapter(adapter);
            getListView().onRestoreInstanceState(state);
        }
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        if (!ctx.dbIsOpen()) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dbclosed));
            em.show(ctx.getSupportFragmentManager(), "dialog");
            return;
        }

        DBItem item = (DBItem)listView.getItemAtPosition(position);
        Cursor cursor = ctx.getDB().fetchText(item.key);
        if (cursor == null) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_dberror));
            em.show(ctx.getSupportFragmentManager(), "dialog");
            populateTabs();
            return;
        }
        cursor.close();

        ctx.openDocument(item.key);
    }

    private void closeTab(long key) {
        if (!ctx.dbIsOpen())
            return;

        Cursor cursor = ctx.getDB().fetchText(key);
        if (cursor.getColumnIndex(TEditDB.KEY_DATA) != -1) {
            TxtSettings settings = new TxtSettings(cursor.getBlob(cursor.getColumnIndex(TEditDB.KEY_DATA)));
            cursor.close();
            if (!settings.saved) {
                ConfirmCloseText cct = ConfirmCloseText.getInstance(key);
                cct.show(ctx.getSupportFragmentManager(), "Confirm Close Text");
                return;
            }
        } else
            cursor.close();

        ctx.getDB().deleteText(key);
        cursor = ctx.getDB().fetchAllTexts();
        if (cursor == null) {
            closeTabsView();
            return;
        }
        if (cursor.getCount() <= 0) {
            cursor.close();
            closeTabsView();
            return;
        }
        cursor.close();
        populateTabs();
    }

    private void closeTabsView() {
        if (!ctx.dbIsOpen()) {
            ctx.setLastTxt(-1);
            ctx.openBrowser(ctx.getCurrentPath());
            return;
        }

        if (ctx.getLastTxt() != -1) {
            Cursor cursor = ctx.getDB().fetchText(ctx.getLastTxt());
            if (cursor != null) {
                cursor.close();
                ctx.openDocument(ctx.getLastTxt());
            }
        }

        Cursor cursor = ctx.getDB().fetchAllTexts();
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null)
                cursor.close();
            ctx.setLastTxt(-1);
            ctx.openBrowser(ctx.getCurrentPath());
            return;
        }

        cursor.moveToFirst();
        if (cursor.getColumnIndex(TEditDB.KEY_ROWID) == -1) {
            cursor.close();
            ctx.setLastTxt(-1);
            ctx.openBrowser(ctx.getCurrentPath());
            return;
        }
        long id = cursor.getLong(cursor.getColumnIndex(TEditDB.KEY_ROWID));
        cursor.close();
        ctx.openDocument(id);
    }

    public void reset() {
        Cursor cursor = ctx.getDB().fetchAllTexts();
        if (cursor == null) {
            closeTabsView();
            return;
        }
        if (cursor.getCount() <= 0) {
            cursor.close();
            closeTabsView();
            return;
        }
        cursor.close();
        populateTabs();
    }

    public void applySettings() {
        populateTabs();
    }

    private class DBItem {
        public final String name;
        public final long key;
        public final String desc;

        private DBItem(String name, long key, String description) {
            this.name = name;
            this.key = key;
            if (description.length() > 100) {
                desc = description.substring(0, 100);
            } else
                desc = description;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class DBAdapter extends ArrayAdapter<DBItem> {
        private final int layoutResID;
        private final int threshold;
        private final Tabs tabs;
        private final GestureDetector gesture;

        public DBAdapter(Context context, int layoutResID, DBItem[] items,
                         Tabs tabs) {
            super(context, layoutResID, items);
            gesture = new GestureDetector(context, new SingleTapConfirm());
            this.tabs = tabs;
            this.layoutResID = layoutResID;
            threshold = Math.round(((TEditActivity)context).getMetrics().density * 24);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ItemHolder holder = null;
            if (row == null) {
                LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
                row = inflater.inflate(layoutResID, parent, false);
                holder = new ItemHolder();

                holder.ll = (LinearLayout)row.findViewById(R.id.tablayout);
                holder.filename = (TextView)row.findViewById(R.id.tabfilename);
                holder.filename.setTypeface(FontUtil.getDefault());
                holder.description = (TextView)row.findViewById(R.id.tabdescription);
                holder.description.setTypeface(FontUtil.getEditorTypeface());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    holder.filename.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    holder.filename.setTextDirection((Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                            View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);
                    holder.description.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    holder.description.setTextDirection((Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                            View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);
                }

                row.setTag(holder);
                row.setOnTouchListener(touch);
            } else {
                holder = (ItemHolder)convertView.getTag();
            }

            DBItem item = getItem(position);
            holder.filename.setText(item.name);
            holder.description.setText(item.desc);

            return row;
        }

        private View.OnTouchListener touch = new View.OnTouchListener() {
            private int initX = 0;
            private final float slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            private boolean deleted = false;
            private long startTme;

            @Override
            public boolean onTouch(final View view, MotionEvent event) {
                if (gesture.onTouchEvent(event)) {
                    int pos = tabs.getListView().getPositionForView(view);
                    tabs.onListItemClick(tabs.getListView(), view, pos,
                            tabs.getListView().getItemIdAtPosition(pos));
                    return true;
                }

                ItemHolder holder = (ItemHolder)view.getTag();

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (deleted)
                        return true;

                    initX = (int)event.getX();
                    startTme = System.currentTimeMillis();
                    holder.ll.setTranslationX(0);
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (deleted)
                        return true;

                    int offset = (int)(event.getX() - initX);
                    if (Math.abs(offset) > slop) {
                        holder.ll.setTranslationX(offset);
                        if (offset > threshold || offset < -threshold) {
                            deleted = true;
                            int pos = tabs.getListView().getPositionForView(view);
                            final long key = ((DBItem)tabs.getListView().getItemAtPosition(pos)).key;

                            long tmeDiff = System.currentTimeMillis() - startTme;
                            int remaining = view.getWidth() - Math.abs(offset);
                            tmeDiff = Math.round(tmeDiff * (remaining / (float)Math.abs(offset)));
                            if (tmeDiff <= 0) {
                                tabs.closeTab(key);
                                return true;
                            }
                            tmeDiff = tmeDiff > 300 ? 300 : tmeDiff;

                            holder.ll.animate().setInterpolator(new AccelerateInterpolator())
                                    .translationX(offset >= 0 ? view.getWidth() : -view.getWidth())
                                    .setDuration(tmeDiff);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!tabs.isResumed())
                                        return;
                                    tabs.closeTab(key);
                                }
                            }, tmeDiff);
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (!deleted) {
                        holder.ll.animate().setInterpolator(new AnticipateInterpolator()).translationX(0)
                            .setDuration(300);
                    }

                    if (event.getAction() == MotionEvent.ACTION_CANCEL)
                        return false;
                }

                return true;
            }
        };

        private static class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                return true;
            }
        }

        private static class ItemHolder {
            public LinearLayout ll;
            public TextView filename;
            public TextView description;
        }
    }
}
