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
package com.atr.tedit.utilitybar.state;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.mainstate.Editor;
import com.atr.tedit.dialog.HelpDialog;
import com.atr.tedit.utilitybar.UtilityBar;

public class TextState extends UtilityState {
    public TextState(UtilityBar bar) {
        super(bar, UtilityBar.STATE_TEXT);

        boolean twoLayer = (bar.padding_w * 2 + bar.bWidth * 7 + bar.margin * 6) > bar.barWidth;

        Button newdoc = new Button(BAR.ctx);
        newdoc.setBackgroundResource(R.drawable.button_doc);
        newdoc.setId(R.id.zero);
        newdoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BAR.ctx.newDocument(TEditActivity.DEFAULTPATH, "");
            }
        });

        Button opendoc = new Button(BAR.ctx);
        opendoc.setBackgroundResource(R.drawable.button_dir);
        opendoc.setId(R.id.one);
        opendoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BAR.ctx.requestOpenBrowser();
            }
        });

        Button savedoc = new Button(BAR.ctx);
        savedoc.setBackgroundResource(R.drawable.button_save);
        savedoc.setId(R.id.two);
        savedoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BAR.ctx.saveDocument(false);
            }
        });

        Button savedocas = new Button(BAR.ctx);
        savedocas.setBackgroundResource(R.drawable.button_save_as);
        savedocas.setId(R.id.three);
        savedocas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BAR.ctx.saveAsDocument(false);
            }
        });

        Button tabs = new Button(BAR.ctx);
        tabs.setBackgroundResource(R.drawable.button_tabs);
        tabs.setId(R.id.four);
        tabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BAR.ctx.tabs();
            }
        });

        Button search = new Button(BAR.ctx);
        search.setBackgroundResource(R.drawable.button_search);
        search.setId(R.id.five);
        search.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               ((Editor)BAR.ctx.getFrag()).activateSearch();
           }
        });

        Button help = new Button(BAR.ctx);
        help.setBackgroundResource(R.drawable.button_help);
        help.setId(R.id.six);

        if (!twoLayer) {
            help.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HelpDialog hd = HelpDialog.newInstance(R.layout.help_editor, BAR.ctx.getString(R.string.editor));
                    hd.show(BAR.ctx.getSupportFragmentManager(), "HelpDialog");
                }
            });

            Button[] l = {newdoc, opendoc, savedoc, savedocas, search, tabs, help};
            LAYERS = new View[1][];
            LAYERS[0] = l;
        } else {
            help.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HelpDialog hd = HelpDialog.newInstance(R.layout.help_editor_layer1, BAR.ctx.getString(R.string.editor));
                    hd.show(BAR.ctx.getSupportFragmentManager(), "HelpDialog");
                }
            });

            Button up = new Button(BAR.ctx);
            up.setBackgroundResource(R.drawable.button_arrow_up);
            up.setId(R.id.seven);
            up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BAR.getState().transToLayer(0);
                }
            });

            Button down = new Button(BAR.ctx);
            down.setBackgroundResource(R.drawable.button_arrow_down);
            down.setId(R.id.eight);
            down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BAR.getState().transToLayer(1);
                }
            });

            Button helpL2 = new Button(BAR.ctx);
            helpL2.setBackgroundResource(R.drawable.button_help);
            helpL2.setId(R.id.six);
            helpL2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HelpDialog hd = HelpDialog.newInstance(R.layout.help_editor_layer2, BAR.ctx.getString(R.string.editor));
                    hd.show(BAR.ctx.getSupportFragmentManager(), "HelpDialog");
                }
            });

            Button[] l1 = {savedoc, search, tabs, down, help};
            Button[] l2 = {newdoc, opendoc, savedocas, up, helpL2};
            LAYERS = new View[2][];
            LAYERS[0] = l1;
            LAYERS[1] = l2;
        }
        for (int i = 0; i < (twoLayer ? 2 : 1); i++) {
            int count = 0;
            for (View v : LAYERS[i]) {
                if (count == LAYERS[i].length - 1) {
                    v.setTranslationX(BAR.barWidth - BAR.bWidth - BAR.padding_w);
                    v.setNextFocusRightId(LAYERS[i][0].getId());
                    v.setNextFocusLeftId(LAYERS[i][count - 1].getId());
                } else {
                    v.setTranslationX(BAR.padding_w + (count * (BAR.margin + bar.bWidth)));
                    v.setNextFocusRightId(LAYERS[i][count + 1].getId());
                    if (count == 0)
                        v.setNextFocusLeftId(LAYERS[i][LAYERS[i].length - 1].getId());
                }
                v.setTranslationY(BAR.padding_h);

                v.setFocusable(true);
                v.setScaleX(1);
                v.setScaleY(1);
                v.setAlpha(1);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(BAR.bar.getLayoutParams());
                lp.width = BAR.bWidth;
                lp.height = BAR.bHeight;
                v.setLayoutParams(lp);

                count++;
            }
        }
    }
}
