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
package com.atr.tedit.util;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.atr.tedit.R;

/**
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */

public class HelpDialog extends DialogFragment {
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
        super.onCreateDialog(savedInstanceState);

        if (savedInstanceState == null) {
            layout = getArguments().getInt("TEdit.help.layout", R.layout.help_browser);
            title = getArguments().getString("TEdit.help.title", "Help");
        } else {
            layout = savedInstanceState.getInt("TEdit.help.layout", R.layout.help_browser);
            title = savedInstanceState.getString("TEdit.help.title", getString(R.string.help));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title).setView(getActivity().getLayoutInflater().inflate(layout, null))
                .setIcon(R.drawable.help_focused)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("TEdit.help.layout", layout);
        outState.putString("TEdit.help.title", title);
    }
}
