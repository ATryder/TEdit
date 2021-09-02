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
package com.atr.tedit.utilitybar;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.util.Callback;
import com.atr.tedit.utilitybar.state.BrowserState;
import com.atr.tedit.utilitybar.state.InitState;
import com.atr.tedit.utilitybar.state.TabsState;
import com.atr.tedit.utilitybar.state.TextState;
import com.atr.tedit.utilitybar.state.UtilityState;

/**
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */

public class UtilityBar {
    public static final int STATE_INIT = -1;
    public static final int STATE_BROWSE = 0;
    public static final int STATE_TEXT = 1;
    public static final int STATE_TAB = 2;
    public static final int STATE_TEXT_SEARCH = 3;

    public final BrowserState UTILITY_STATE_BROWSER;
    public final TextState UTILITY_STATE_TEXT;
    public final TabsState UTILITY_STATE_TABS;

    private UtilityState state = new InitState(this);

    public final FrameLayout bar;
    public final DisplayMetrics dMetrics;
    public final TEditActivity ctx;

    public final int padding_h;
    public final int padding_w;
    public final int margin;
    public final int bWidth;
    public final int bHeight;
    public final int barWidth;
    public final int barHeight;

    public final Handler handler = new Handler(Looper.getMainLooper());

    public UtilityBar(FrameLayout buttonBar, DisplayMetrics displayMetrics,
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

        UTILITY_STATE_BROWSER = new BrowserState(this);
        UTILITY_STATE_TEXT = new TextState(this);
        UTILITY_STATE_TABS = new TabsState(this);
    }

    public boolean isAnimating() {
        return state.isAnimating();
    }

    public void setState(final UtilityState nextState) {
        this.setState(nextState, 0);
    }

    public void setState(final UtilityState nextState, int layer) {
        if (state.STATE == STATE_INIT) {
            nextState.setToState(layer);
            state = nextState;
            return;
        }

        state.transTo(nextState, layer, new Callback<UtilityState>() {
            public void call(UtilityState var) {
                if (var == null) {
                    state = nextState;
                } else
                    state = var;
            }
        });
    }

    public UtilityState getState() {
        return state;
    }

    public void setToBrowser() {
        setState(UTILITY_STATE_BROWSER);
    }

    public void setToText() {
        setState(UTILITY_STATE_TEXT);
    }

    public void setToTab() {
        setState(UTILITY_STATE_TABS);
    }
}
