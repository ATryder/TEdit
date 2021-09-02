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

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.atr.tedit.dialog.TDialog;
import com.atr.tedit.file.descriptor.AndFile;
import com.atr.tedit.mainstate.Browser;
import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.dialog.HelpDialog;
import com.atr.tedit.settings.dialog.DirectoryPicker;
import com.atr.tedit.utilitybar.UtilityBar;

public class BrowserState extends UtilityState {
    public BrowserState(UtilityBar bar) {
        super(bar, UtilityBar.STATE_BROWSE);

        Button dir_parent = new Button(BAR.ctx);
        dir_parent.setBackgroundResource(R.drawable.button_dir_parent);
        dir_parent.setId(R.id.zero);
        dir_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Browser)BAR.ctx.getFrag()).upDir();
            }
        });
        dir_parent.setEnabled(false);

        Button doc = new Button(BAR.ctx);
        doc.setBackgroundResource(R.drawable.button_doc);
        doc.setId(R.id.one);
        doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BAR.ctx.newDocument(TEditActivity.DEFAULTPATH, "");
            }
        });

        Button newdir = new Button(BAR.ctx);
        newdir.setBackgroundResource(R.drawable.button_dir_new);
        newdir.setId(R.id.two);
        newdir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(BAR.ctx.getFrag() instanceof Browser))
                    return;

                Browser browser = (Browser)BAR.ctx.getFrag();
                if (browser.isAnimating() || browser.isLoading())
                    return;

                if (browser.isBrowsingPermittedDirs()) {
                    BAR.ctx.launchDirPermissionIntent();
                    return;
                }
                String bDir = ((Browser)BAR.ctx.getFrag()).getCurrentPath().toJson();
                Browser.NewDirectory newDir = Browser.NewDirectory.newInstance(bDir);
                newDir.show(BAR.ctx.getSupportFragmentManager(), "NewDirectory");
            }
        });

        Button tabs = new Button(BAR.ctx);
        tabs.setBackgroundResource(R.drawable.button_tabs);
        tabs.setId(R.id.three);
        tabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BAR.ctx.tabs();
            }
        });

        Button help = new Button(BAR.ctx);
        help.setBackgroundResource(R.drawable.button_help);
        help.setId(R.id.four);

        Button[] l;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            help.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HelpDialog hd = HelpDialog.newInstance(R.layout.help_browser_prepie, BAR.ctx.getString(R.string.browser));
                    hd.show(BAR.ctx.getSupportFragmentManager(), "HelpDialog");
                }
            });

            Button drives = new Button(BAR.ctx);
            drives.setBackgroundResource(R.drawable.button_drives);
            drives.setId(R.id.five);
            drives.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Browser browser = (Browser)BAR.ctx.getFrag();
                    if (browser.isAnimating() || browser.isLoading())
                        return;
                    launchVolumePicker();
                }
            });

            l = new Button[]{dir_parent, doc, newdir, drives, tabs, help};
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                help.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HelpDialog hd = HelpDialog.newInstance(R.layout.help_browser, BAR.ctx.getString(R.string.browser));
                        hd.show(BAR.ctx.getSupportFragmentManager(), "HelpDialog");
                    }
                });
            } else {
                help.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HelpDialog hd = HelpDialog.newInstance(R.layout.help_browser_prelollipop, BAR.ctx.getString(R.string.browser));
                        hd.show(BAR.ctx.getSupportFragmentManager(), "HelpDialog");
                    }
                });
            }

            l = new Button[]{dir_parent, doc, newdir, tabs, help};
        }

        int count = 0;
        for (Button v : l) {
            if (count == l.length - 1) {
                v.setTranslationX(BAR.barWidth - BAR.bWidth - BAR.padding_w);
                v.setNextFocusRightId(l[0].getId());
                v.setNextFocusLeftId(l[count - 1].getId());
            } else {
                v.setTranslationX(BAR.padding_w + (count * (BAR.margin + bar.bWidth)));
                v.setNextFocusRightId(l[count + 1].getId());
                if (count == 0)
                    v.setNextFocusLeftId(l[l.length - 1].getId());
            }
            v.setTranslationY(BAR.padding_h);

            v.setFocusable(true);
            v.setWidth(BAR.bWidth);
            v.setHeight(BAR.bHeight);
            v.setScaleX(1);
            v.setScaleY(1);
            v.setAlpha(1);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(BAR.bar.getLayoutParams());
            lp.width = BAR.bWidth;
            lp.height = BAR.bHeight;
            v.setLayoutParams(lp);

            count++;
        }

        LAYERS = new View[1][];
        LAYERS[0] = l;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void launchVolumePicker() {
        boolean cardPresent = ContextCompat.getExternalFilesDirs(BAR.ctx, "external").length > 1;

        Uri[] volumes = BAR.ctx.getPermittedUris();
        for (int i = 0; i < volumes.length; i++) {
            if (!AndFile.createDescriptor(DocumentFile.fromTreeUri(BAR.ctx, volumes[i]), volumes[i]).exists()) {
                volumes = new Uri[0];
                break;
            }
        }
        if (volumes.length > 0 || !cardPresent) {
            ((Browser)BAR.ctx.getFrag()).launchVolumePicker();
            return;
        }

        LaunchSDCardIntent lsd = new LaunchSDCardIntent();
        lsd.show(BAR.ctx.getSupportFragmentManager(), "SDCardIntentDialog");
    }

    public static class LaunchSDCardIntent extends TDialog {
        private TEditActivity ctx;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.ctx = (TEditActivity)context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            setTitle(R.string.launch_sdpicker_title);
            setIcon(R.drawable.tedit_logo_brown);
            setMessage(R.string.launch_sdpicker);
            setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    Fragment dp = ctx.getSupportFragmentManager().findFragmentByTag(DirectoryPicker.TAG);
                    if (dp == null) {
                        ((Browser) ctx.getFrag()).launchVolumePicker();
                    } else
                        ((DirectoryPicker)dp).launchVolumePicker(false);
                }
            });
            setPositiveButton(getString(R.string.okay), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    ctx.launchSDcardIntent();
                }
            });

            return super.onCreateDialog(savedInstanceState);
        }
    }

}
