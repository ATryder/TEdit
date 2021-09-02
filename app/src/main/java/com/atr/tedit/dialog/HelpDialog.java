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
package com.atr.tedit.dialog;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atr.tedit.BuildConfig;
import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.util.FontUtil;

/**
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */

public class HelpDialog extends TDialog {
    private int layout;
    private String title;

    public static HelpDialog newInstance(int layout, String title) {
        Bundle bundle = new Bundle();
        bundle.putInt("TEdit.help.layout", layout);
        bundle.putString("TEdit.help.title", title);
        HelpDialog hd = new HelpDialog();
        hd.setArguments(bundle);

        return hd;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            layout = getArguments().getInt("TEdit.help.layout", R.layout.help_browser);
            title = getArguments().getString("TEdit.help.title", "Help");
        } else {
            layout = savedInstanceState.getInt("TEdit.help.layout", R.layout.help_browser);
            title = savedInstanceState.getString("TEdit.help.title", getString(R.string.help));
        }

        LayoutInflater inflater = ((TEditActivity)getContext()).getLayoutInflater()
                .cloneInContext(new ContextThemeWrapper(getContext(), theme));
        View viewLayout = inflater.inflate(R.layout.help_header, null);
        inflater.inflate(layout, (LinearLayout)viewLayout.findViewById(R.id.helpDisplay), true);

        TextView versionView = (TextView)viewLayout.findViewById(R.id.version);
        if (versionView != null) {
            try {
                versionView.setText(getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), PackageManager.GET_ACTIVITIES).versionName);
            } catch (Exception e) {
                Log.i("TEdit", "Unable to obtain version name from Package Manager: " + e.getMessage());
                versionView.setText("v" + BuildConfig.VERSION_NAME);
            }
        }
        FontUtil.applyFont(FontUtil.getDefault(), viewLayout);
        TextView titleView = (TextView)viewLayout.findViewById(R.id.apptitle);
        if (titleView != null)
            titleView.setTypeface(FontUtil.getTitleTypeface());

        setIcon(R.drawable.tedit_logo_brown);
        setTitle(title);
        setView(viewLayout);

        setNeutralButton(R.string.okay, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("TEdit.help.layout", layout);
        outState.putString("TEdit.help.title", title);
    }
}
