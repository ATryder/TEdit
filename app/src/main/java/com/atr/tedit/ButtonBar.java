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

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.atr.tedit.util.ErrorMessage;
import com.atr.tedit.util.HelpDialog;
import com.atr.tedit.util.TEditDB;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */

public class ButtonBar {
    private static final int ANIMLENGTH = 300;
    private static final float SCALE = 0.5f;

    public static final int STATE_INIT = -1;
    public static final int STATE_BROWSE = 0;
    public static final int STATE_TEXT = 1;
    public static final int STATE_TAB = 2;

    private int state = STATE_INIT;

    private final FrameLayout bar;
    private final DisplayMetrics dMetrics;
    private final TEditActivity ctx;

    private final int padding_h;
    private final int padding_w;
    private final int margin;
    private final int bWidth;
    private final int bHeight;
    private final int barWidth;
    private final int barHeight;

    private final Button button_dir_parent;
    private final Button button_dirdoc;
    private final Button button_dir;
    private final Button button_dirtabs;
    private final Button button_dirhelp;
    private final Button button_dirclose;

    private final Button button_doc;
    private final Button button_open;
    private final Button button_save;
    private final Button button_save_as;
    private final Button button_tabs;
    private final Button button_help;
    private final Button button_close;

    private final Button button_tabhelp;
    private final Button button_tabclose;

    /*private final Button button_check;
    private final Button button_x;*/

    private final LinkedList<Button> activeButtons = new LinkedList();
    private boolean animating = false;

    private final Handler handler = new Handler();

    protected ButtonBar(FrameLayout buttonBar, DisplayMetrics displayMetrics,
                        Resources resources, TEditActivity context) {
        this.bar = buttonBar;
        this.dMetrics = displayMetrics;
        this.ctx = context;

        padding_h = Math.round(3 * dMetrics.density);
        padding_w = Math.round(3 * dMetrics.density);
        margin = Math.round(8 * dMetrics.density);
        Bitmap tmpBmp = BitmapFactory.decodeResource(resources, R.drawable.dir);
        bWidth = tmpBmp.getWidth();
        bHeight = tmpBmp.getHeight();
        barWidth = dMetrics.widthPixels;
        barHeight = bHeight + (padding_h * 2);
        buttonBar.setMinimumHeight(barHeight);
        ViewGroup.LayoutParams lp = buttonBar.getLayoutParams();
        lp.height = barHeight;
        buttonBar.setLayoutParams(lp);

        button_dir_parent = new Button(ctx);
        button_dir_parent.setBackgroundResource(R.drawable.button_dir_parent);
        button_dir_parent.setWidth(bWidth);
        button_dir_parent.setHeight(bHeight);
        button_dir_parent.setTranslationX(padding_w);
        button_dir_parent.setTranslationY(padding_h);
        button_dir_parent.setScaleX(1);
        button_dir_parent.setScaleY(1);
        button_dir_parent.setAlpha(1);
        button_dir_parent.setFocusable(true);
        button_dir_parent.setId(R.id.zero);
        button_dir_parent.setNextFocusRightId(R.id.one);
        button_dir_parent.setNextFocusLeftId(R.id.five);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_dir_parent.setLayoutParams(lp);
        button_dir_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.upDir();
            }
        });

        button_dirdoc = new Button(ctx);
        button_dirdoc.setBackgroundResource(R.drawable.button_doc);
        button_dirdoc.setWidth(bWidth);
        button_dirdoc.setHeight(bHeight);
        button_dirdoc.setTranslationX(padding_w + bWidth + margin);
        button_dirdoc.setTranslationY(padding_h);
        button_dirdoc.setScaleX(1);
        button_dirdoc.setScaleY(1);
        button_dirdoc.setAlpha(1);
        button_dirdoc.setFocusable(true);
        button_dirdoc.setId(R.id.one);
        button_dirdoc.setNextFocusRightId(R.id.two);
        button_dirdoc.setNextFocusLeftId(R.id.zero);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_dirdoc.setLayoutParams(lp);
        button_dirdoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.newDocument(TEditActivity.DEFAULTPATH, "");
            }
        });

        button_dir = new Button(ctx);
        button_dir.setBackgroundResource(R.drawable.button_dir);
        button_dir.setWidth(bWidth);
        button_dir.setHeight(bHeight);
        button_dir.setTranslationX(padding_w + ((margin + bWidth) * 2));
        button_dir.setTranslationY(padding_h);
        button_dir.setScaleX(1);
        button_dir.setScaleY(1);
        button_dir.setAlpha(1);
        button_dir.setFocusable(true);
        button_dir.setId(R.id.two);
        button_dir.setNextFocusRightId(R.id.three);
        button_dir.setNextFocusLeftId(R.id.one);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_dir.setLayoutParams(lp);
        button_dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(ctx.getFrag() instanceof Browser))
                    return;
                String bDir = ((Browser)ctx.getFrag()).getCurrentDir().getPath();
                Browser.NewDirectory newDir = Browser.NewDirectory.newInstance(bDir);
                newDir.show(ctx.getSupportFragmentManager(), "NewDirectory");
            }
        });

        button_dirtabs = new Button(ctx);
        button_dirtabs.setBackgroundResource(R.drawable.button_tabs);
        button_dirtabs.setWidth(bWidth);
        button_dirtabs.setHeight(bHeight);
        button_dirtabs.setTranslationX(padding_w + ((margin + bWidth) * 3));
        button_dirtabs.setTranslationY(padding_h);
        button_dirtabs.setScaleX(1);
        button_dirtabs.setScaleY(1);
        button_dirtabs.setAlpha(1);
        button_dirtabs.setFocusable(true);
        button_dirtabs.setId(R.id.three);
        button_dirtabs.setNextFocusRightId(R.id.four);
        button_dirtabs.setNextFocusLeftId(R.id.two);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_dirtabs.setLayoutParams(lp);
        button_dirtabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.tabs();
            }
        });

        button_dirhelp = new Button(ctx);
        button_dirhelp.setBackgroundResource(R.drawable.button_help);
        button_dirhelp.setWidth(bWidth);
        button_dirhelp.setHeight(bHeight);
        button_dirhelp.setTranslationX(padding_w + ((margin + bWidth) * 4));
        button_dirhelp.setTranslationY(padding_h);
        button_dirhelp.setScaleX(1);
        button_dirhelp.setScaleY(1);
        button_dirhelp.setAlpha(1);
        button_dirhelp.setFocusable(true);
        button_dirhelp.setId(R.id.four);
        button_dirhelp.setNextFocusRightId(R.id.five);
        button_dirhelp.setNextFocusLeftId(R.id.three);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_dirhelp.setLayoutParams(lp);
        button_dirhelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpDialog hd = HelpDialog.newInstance(R.layout.help_browser, ctx.getString(R.string.browser));
                hd.show(ctx.getSupportFragmentManager(), "HelpDialog");
            }
        });

        button_dirclose = new Button(ctx);
        button_dirclose.setBackgroundResource(R.drawable.button_x);
        button_dirclose.setWidth(bWidth);
        button_dirclose.setHeight(bHeight);
        button_dirclose.setTranslationX(barWidth - bWidth - padding_w);
        button_dirclose.setTranslationY(padding_h);
        button_dirclose.setScaleX(1);
        button_dirclose.setScaleY(1);
        button_dirclose.setAlpha(1);
        button_dirclose.setFocusable(true);
        button_dirclose.setId(R.id.five);
        button_dirclose.setNextFocusRightId(R.id.zero);
        button_dirclose.setNextFocusLeftId(R.id.four);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_dirclose.setLayoutParams(lp);
        button_dirclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.closeBrowser();
            }
        });

        //Editor
        button_doc = new Button(ctx);
        button_doc.setBackgroundResource(R.drawable.button_doc);
        button_doc.setWidth(bWidth);
        button_doc.setHeight(bHeight);
        button_doc.setTranslationX(padding_w);
        button_doc.setTranslationY(padding_h);
        button_doc.setScaleX(1);
        button_doc.setScaleY(1);
        button_doc.setAlpha(1);
        button_doc.setFocusable(true);
        button_doc.setId(R.id.zero);
        button_doc.setNextFocusRightId(R.id.one);
        button_doc.setNextFocusLeftId(R.id.six);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_doc.setLayoutParams(lp);
        button_doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.newDocument(TEditActivity.DEFAULTPATH, "");
            }
        });

        button_open = new Button(ctx);
        button_open.setBackgroundResource(R.drawable.button_dir);
        button_open.setWidth(bWidth);
        button_open.setHeight(bHeight);
        button_open.setTranslationX(padding_w + bWidth + margin);
        button_open.setTranslationY(padding_h);
        button_open.setScaleX(1);
        button_open.setScaleY(1);
        button_open.setAlpha(1);
        button_open.setFocusable(true);
        button_open.setId(R.id.one);
        button_open.setNextFocusRightId(R.id.two);
        button_open.setNextFocusLeftId(R.id.zero);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_open.setLayoutParams(lp);
        button_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.openBrowser(ctx.getCurrentPath().getPath());
            }
        });

        button_save = new Button(ctx);
        button_save.setBackgroundResource(R.drawable.button_save);
        button_save.setWidth(bWidth);
        button_save.setHeight(bHeight);
        button_save.setTranslationX(padding_w + ((bWidth + margin) * 2));
        button_save.setTranslationY(padding_h);
        button_save.setScaleX(1);
        button_save.setScaleY(1);
        button_save.setAlpha(1);
        button_save.setFocusable(true);
        button_save.setId(R.id.two);
        button_save.setNextFocusRightId(R.id.three);
        button_save.setNextFocusLeftId(R.id.one);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_save.setLayoutParams(lp);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ctx.dbIsOpen()) {
                    Log.e("TEdit", "Unable to save file: Database is not open.");
                    ErrorMessage em = ErrorMessage.getInstance(ctx.getString(R.string.alert),
                            ctx.getString(R.string.error_dbclosed));
                    em.show(ctx.getSupportFragmentManager(), "dialog");
                    return;
                }

                if (ctx.getLastTxt() == -1)
                    return;

                ((Editor)ctx.getFrag()).saveToDB();
                Cursor cursor = ctx.getDB().fetchText(ctx.getLastTxt());
                if (cursor == null || cursor.getColumnIndex(TEditDB.KEY_PATH) == -1
                        || cursor.getColumnIndex(TEditDB.KEY_BODY) ==  -1) {
                    if (cursor == null) {
                        Log.e("TEdit", "Unable to save file: Database did not contain key.");
                    } else
                        Log.e("TEdit", "Unable to save file: Database did not contain column");
                    ErrorMessage em = ErrorMessage.getInstance(ctx.getString(R.string.alert),
                            ctx.getString(R.string.error_dberror));
                    em.show(ctx.getSupportFragmentManager(), "dialog");
                    cursor.close();
                    return;
                }

                String path = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_PATH));
                if (path.equals(TEditActivity.DEFAULTPATH)) {
                    ctx.saveBrowser(ctx.getSavePath().toString());
                    cursor.close();
                    return;
                }


                String mediaState = Environment.getExternalStorageState();
                if (!(Environment.MEDIA_MOUNTED.equals(mediaState)
                        || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState))) {
                    ErrorMessage em = ErrorMessage.getInstance(ctx.getString(R.string.alert),
                            ctx.getString(R.string.error_unmounted));
                    em.show(ctx.getSupportFragmentManager(), "dialog");
                    cursor.close();
                    return;
                } else if (!path.startsWith(Environment.getExternalStorageDirectory().getPath())) {
                    ErrorMessage em = ErrorMessage.getInstance(ctx.getString(R.string.alert),
                            ctx.getString(R.string.error_protectedpath));
                    em.show(ctx.getSupportFragmentManager(), "dialog");
                    cursor.close();
                    return;
                }

                String body = cursor.getString(cursor.getColumnIndex(TEditDB.KEY_BODY));
                cursor.close();
                File file = new File(path);
                try {
                    Browser.writeFile(file, body);
                    Toast.makeText(ctx, "File Saved", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e("TEdit.Editor", "Unable to save file " + file.getPath() + ": "
                            + e.getMessage());
                    ErrorMessage em = ErrorMessage.getInstance(ctx.getString(R.string.alert),
                            ctx.getString(R.string.error_writefile));
                    em.show(ctx.getSupportFragmentManager(), "dialog");
                }
            }
        });

        button_save_as = new Button(ctx);
        button_save_as.setBackgroundResource(R.drawable.button_save_as);
        button_save_as.setWidth(bWidth);
        button_save_as.setHeight(bHeight);
        button_save_as.setTranslationX(padding_w + ((bWidth + margin) * 3));
        button_save_as.setTranslationY(padding_h);
        button_save_as.setScaleX(1);
        button_save_as.setScaleY(1);
        button_save_as.setAlpha(1);
        button_save_as.setFocusable(true);
        button_save_as.setId(R.id.three);
        button_save_as.setNextFocusRightId(R.id.four);
        button_save_as.setNextFocusLeftId(R.id.two);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_save_as.setLayoutParams(lp);
        button_save_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ctx.dbIsOpen()) {
                    Log.e("TEdit", "Unable to save file: Database is not open.");
                    ErrorMessage em = ErrorMessage.getInstance(ctx.getString(R.string.alert),
                            ctx.getString(R.string.error_dbclosed));
                    em.show(ctx.getSupportFragmentManager(), "dialog");
                    return;
                }

                if (ctx.getLastTxt() == -1)
                    return;

                ((Editor)ctx.getFrag()).saveToDB();
                Cursor cursor = ctx.getDB().fetchText(ctx.getLastTxt());
                if (cursor == null || cursor.getColumnIndex(TEditDB.KEY_PATH) == -1
                        || cursor.getColumnIndex(TEditDB.KEY_BODY) ==  -1) {
                    if (cursor == null) {
                        Log.e("TEdit", "Unable to save file: Database did not contain key.");
                    } else
                        Log.e("TEdit", "Unable to save file: Database did not contain column");
                    ErrorMessage em = ErrorMessage.getInstance(ctx.getString(R.string.alert),
                            ctx.getString(R.string.error_dberror));
                    em.show(ctx.getSupportFragmentManager(), "dialog");
                    cursor.close();
                    return;
                }

                ctx.saveBrowser(ctx.getSavePath().toString());
            }
        });

        button_tabs = new Button(ctx);
        button_tabs.setBackgroundResource(R.drawable.button_tabs);
        button_tabs.setWidth(bWidth);
        button_tabs.setHeight(bHeight);
        button_tabs.setTranslationX(padding_w + ((bWidth + margin) * 4));
        button_tabs.setTranslationY(padding_h);
        button_tabs.setScaleX(1);
        button_tabs.setScaleY(1);
        button_tabs.setAlpha(1);
        button_tabs.setFocusable(true);
        button_tabs.setId(R.id.four);
        button_tabs.setNextFocusRightId(R.id.five);
        button_tabs.setNextFocusLeftId(R.id.three);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_tabs.setLayoutParams(lp);
        button_tabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.tabs();
            }
        });

        button_help = new Button(ctx);
        button_help.setBackgroundResource(R.drawable.button_help);
        button_help.setWidth(bWidth);
        button_help.setHeight(bHeight);
        button_help.setTranslationX(padding_w + ((bWidth + margin) * 5));
        button_help.setTranslationY(padding_h);
        button_help.setScaleX(1);
        button_help.setScaleY(1);
        button_help.setAlpha(1);
        button_help.setFocusable(true);
        button_help.setId(R.id.five);
        button_help.setNextFocusRightId(R.id.six);
        button_help.setNextFocusLeftId(R.id.four);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_help.setLayoutParams(lp);
        button_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpDialog hd = HelpDialog.newInstance(R.layout.help_editor, ctx.getString(R.string.editor));
                hd.show(ctx.getSupportFragmentManager(), "HelpDialog");
            }
        });

        button_close = new Button(ctx);
        button_close.setBackgroundResource(R.drawable.button_x);
        button_close.setWidth(bWidth);
        button_close.setHeight(bHeight);
        button_close.setTranslationX(barWidth - bWidth - padding_w);
        button_close.setTranslationY(padding_h);
        button_close.setScaleX(1);
        button_close.setScaleY(1);
        button_close.setAlpha(1);
        button_close.setFocusable(true);
        button_close.setId(R.id.six);
        button_close.setNextFocusRightId(R.id.zero);
        button_close.setNextFocusLeftId(R.id.five);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_close.setLayoutParams(lp);
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.closeText();
            }
        });

        button_tabhelp = new Button(ctx);
        button_tabhelp.setBackgroundResource(R.drawable.button_help);
        button_tabhelp.setWidth(bWidth);
        button_tabhelp.setHeight(bHeight);
        button_tabhelp.setTranslationX(padding_w);
        button_tabhelp.setTranslationY(padding_h);
        button_tabhelp.setScaleX(1);
        button_tabhelp.setScaleY(1);
        button_tabhelp.setAlpha(1);
        button_tabhelp.setFocusable(true);
        button_tabhelp.setId(R.id.zero);
        button_tabhelp.setNextFocusRightId(R.id.one);
        button_tabhelp.setNextFocusLeftId(R.id.one);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_tabhelp.setLayoutParams(lp);
        button_tabhelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpDialog hd = HelpDialog.newInstance(R.layout.help_tabs, ctx.getString(R.string.tabs));
                hd.show(ctx.getSupportFragmentManager(), "HelpDialog");
            }
        });

        button_tabclose = new Button(ctx);
        button_tabclose.setBackgroundResource(R.drawable.button_x);
        button_tabclose.setWidth(bWidth);
        button_tabclose.setHeight(bHeight);
        button_tabclose.setTranslationX(barWidth - bWidth - padding_w);
        button_tabclose.setTranslationY(padding_h);
        button_tabclose.setScaleX(1);
        button_tabclose.setScaleY(1);
        button_tabclose.setAlpha(1);
        button_tabclose.setFocusable(true);
        button_tabclose.setId(R.id.one);
        button_tabclose.setNextFocusRightId(R.id.zero);
        button_tabclose.setNextFocusLeftId(R.id.zero);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_tabclose.setLayoutParams(lp);
        button_tabclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.closeTabs();
            }
        });

        /*button_check = new Button(ctx);
        button_check.setBackgroundResource(R.drawable.button_check);
        button_check.setWidth(bWidth);
        button_check.setHeight(bHeight);
        button_check.setTranslationX(padding_w);
        button_check.setTranslationY(padding_h);
        button_check.setScaleX(1);
        button_check.setScaleY(1);
        button_check.setAlpha(1);
        button_check.setFocusable(true);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_check.setLayoutParams(lp);
        button_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        button_x = new Button(ctx);
        button_x.setBackgroundResource(R.drawable.button_x);
        button_x.setWidth(bWidth);
        button_x.setHeight(bHeight);
        button_x.setTranslationX(barWidth - bWidth - padding_w);
        button_x.setTranslationY(padding_h);
        button_x.setScaleX(1);
        button_x.setScaleY(1);
        button_x.setAlpha(1);
        button_x.setFocusable(true);
        lp = new ViewGroup.LayoutParams(buttonBar.getLayoutParams());
        lp.width = bWidth;
        lp.height = bHeight;
        button_x.setLayoutParams(lp);
        button_x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/
    }

    private void animIn(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            animate(view, 1, 1, new OvershootInterpolator());
        } else
            oldAnimate(view, 1, 1, new OvershootInterpolator());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void animate(View view, float alpha, float scale, Interpolator interpolator) {
        view.animate().alpha(alpha).scaleX(scale).scaleY(scale).setDuration(ANIMLENGTH)
                .setInterpolator(interpolator).withLayer();
    }

    private void oldAnimate(View view, float alpha, float scale, Interpolator interpolator) {
        view.animate().alpha(alpha).scaleX(scale).scaleY(scale).setDuration(ANIMLENGTH)
                .setInterpolator(interpolator);
    }

    public void clearButtons() {
        animating = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            for (Button b : activeButtons) {
                b.setEnabled(false);
                animate(b, 0, SCALE, new AnticipateInterpolator());
            }
        } else {
            for (Button b : activeButtons) {
                b.setEnabled(false);
                oldAnimate(b, 0, SCALE, new AnticipateInterpolator());
            }
        }
        activeButtons.clear();
    }

    public void setToBrowser() {
        if (state == STATE_BROWSE)
            return;

        if (state == STATE_INIT) {
            bar.addView(button_dir_parent);
            activeButtons.add(button_dir_parent);

            bar.addView(button_dirdoc);
            activeButtons.add(button_dirdoc);

            bar.addView(button_dir);
            activeButtons.add(button_dir);

            bar.addView(button_dirtabs);
            activeButtons.add(button_dirtabs);

            if (button_dirhelp.getTranslationX() + bWidth + margin <=
                    button_dirclose.getTranslationX()) {
                bar.addView(button_dirhelp);
                activeButtons.add(button_dirhelp);
            }

            bar.addView(button_dirclose);
            activeButtons.add(button_dirclose);

            state = STATE_BROWSE;
            return;
        }

        clearButtons();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bar.removeAllViews();

                button_dir_parent.setScaleX(SCALE);
                button_dir_parent.setScaleY(SCALE);
                button_dir_parent.setAlpha(0);
                bar.addView(button_dir_parent);
                activeButtons.add(button_dir_parent);
                //button_dir_parent.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_dir_parent);

                button_dirdoc.setScaleX(SCALE);
                button_dirdoc.setScaleY(SCALE);
                button_dirdoc.setAlpha(0);
                bar.addView(button_dirdoc);
                activeButtons.add(button_dirdoc);
                //button_dirdoc.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_dirdoc);

                button_dir.setScaleX(SCALE);
                button_dir.setScaleY(SCALE);
                button_dir.setAlpha(0);
                bar.addView(button_dir);
                activeButtons.add(button_dir);
                //button_dir.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_dir);

                button_dirtabs.setScaleX(SCALE);
                button_dirtabs.setScaleY(SCALE);
                button_dirtabs.setAlpha(0);
                bar.addView(button_dirtabs);
                activeButtons.add(button_dirtabs);
                //button_dirtabs.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_dirtabs);

                if (button_dirhelp.getTranslationX() + bWidth + margin <=
                        button_dirclose.getTranslationX()) {
                    button_dirhelp.setScaleX(SCALE);
                    button_dirhelp.setScaleY(SCALE);
                    button_dirhelp.setAlpha(0);
                    bar.addView(button_dirhelp);
                    activeButtons.add(button_dirhelp);
                    //button_dirhelp.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                            //.setInterpolator(new OvershootInterpolator());
                    animIn(button_dirhelp);
                }

                button_dirclose.setScaleX(SCALE);
                button_dirclose.setScaleY(SCALE);
                button_dirclose.setAlpha(0);
                bar.addView(button_dirclose);
                activeButtons.add(button_dirclose);
                //button_dirclose.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_dirclose);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animating = false;
                        for (int i = 0; i < bar.getChildCount(); i++)
                            bar.getChildAt(i).setEnabled(true);
                    }
                }, ANIMLENGTH);
            }
        }, ANIMLENGTH);

        state = STATE_BROWSE;
    }

    public void setToText() {
        if (state == STATE_TEXT)
            return;

        if (state == STATE_INIT) {
            bar.addView(button_doc);
            activeButtons.add(button_doc);

            bar.addView(button_open);
            activeButtons.add(button_open);

            bar.addView(button_save);
            activeButtons.add(button_save);

            if (button_save_as.getTranslationX() + bWidth + margin <=
                    button_close.getTranslationX()) {
                bar.addView(button_save_as);
                activeButtons.add(button_save_as);
            }

            if (button_tabs.getTranslationX() + bWidth + margin <=
                    button_close.getTranslationX()) {
                bar.addView(button_tabs);
                activeButtons.add(button_tabs);
            }

            if (button_help.getTranslationX() + bWidth + margin <=
                    button_close.getTranslationX()) {
                bar.addView(button_help);
                activeButtons.add(button_help);
            }

            bar.addView(button_close);
            activeButtons.add(button_close);

            state = STATE_TEXT;

            return;
        }

        clearButtons();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bar.removeAllViews();

                button_doc.setScaleX(SCALE);
                button_doc.setScaleY(SCALE);
                button_doc.setAlpha(0);
                bar.addView(button_doc);
                activeButtons.add(button_doc);
                //button_doc.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_doc);

                button_open.setScaleX(SCALE);
                button_open.setScaleY(SCALE);
                button_open.setAlpha(0);
                bar.addView(button_open);
                activeButtons.add(button_open);
                //button_open.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_open);

                button_save.setScaleX(SCALE);
                button_save.setScaleY(SCALE);
                button_save.setAlpha(0);
                bar.addView(button_save);
                activeButtons.add(button_save);
                //button_save.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_save);

                if (button_save_as.getTranslationX() + bWidth + margin <=
                        button_close.getTranslationX()) {
                    button_save_as.setScaleX(SCALE);
                    button_save_as.setScaleY(SCALE);
                    button_save_as.setAlpha(0);
                    bar.addView(button_save_as);
                    activeButtons.add(button_save_as);
                    //button_save_as.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                            //.setInterpolator(new OvershootInterpolator());
                    animIn(button_save_as);
                }

                if (button_tabs.getTranslationX() + bWidth + margin <=
                        button_close.getTranslationX()) {
                    button_tabs.setScaleX(SCALE);
                    button_tabs.setScaleY(SCALE);
                    button_tabs.setAlpha(0);
                    bar.addView(button_tabs);
                    activeButtons.add(button_tabs);
                    //button_tabs.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                            //.setInterpolator(new OvershootInterpolator());
                    animIn(button_tabs);
                }

                if (button_help.getTranslationX() + bWidth + margin <=
                        button_close.getTranslationX()) {
                    button_help.setScaleX(SCALE);
                    button_help.setScaleY(SCALE);
                    button_help.setAlpha(0);
                    bar.addView(button_help);
                    activeButtons.add(button_help);
                    //button_help.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                            //.setInterpolator(new OvershootInterpolator());
                    animIn(button_help);
                }

                button_close.setScaleX(SCALE);
                button_close.setScaleY(SCALE);
                button_close.setAlpha(0);
                bar.addView(button_close);
                activeButtons.add(button_close);
                //button_close.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_close);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animating = false;
                        for (int i = 0; i < bar.getChildCount(); i++)
                            bar.getChildAt(i).setEnabled(true);
                    }
                }, ANIMLENGTH);
            }
        }, ANIMLENGTH);

        state = STATE_TEXT;
    }

    public void setToTab() {
        if (state == STATE_TAB)
            return;

        if (state == STATE_INIT) {
            bar.addView(button_tabhelp);
            activeButtons.add(button_tabhelp);

            bar.addView(button_tabclose);
            activeButtons.add(button_tabclose);

            state = STATE_TAB;
            return;
        }

        clearButtons();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bar.removeAllViews();

                button_tabhelp.setScaleX(SCALE);
                button_tabhelp.setScaleY(SCALE);
                button_tabhelp.setAlpha(0);
                bar.addView(button_tabhelp);
                activeButtons.add(button_tabhelp);
                //button_tabhelp.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_tabhelp);

                button_tabclose.setScaleX(SCALE);
                button_tabclose.setScaleY(SCALE);
                button_tabclose.setAlpha(0);
                bar.addView(button_tabclose);
                activeButtons.add(button_tabclose);
                //button_tabclose.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMLENGTH)
                        //.setInterpolator(new OvershootInterpolator());
                animIn(button_tabclose);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animating = false;
                        for (int i = 0; i < bar.getChildCount(); i++)
                            bar.getChildAt(i).setEnabled(true);
                    }
                }, ANIMLENGTH);
            }
        }, ANIMLENGTH);

        state = STATE_TAB;
    }
}
