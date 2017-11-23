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

public class ErrorMessage extends DialogFragment {
    private String title;
    private String message;

    public static ErrorMessage getInstance(String title, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("Message.title", title);
        bundle.putString("Message.message", message);

        ErrorMessage em = new ErrorMessage();
        em.setArguments(bundle);

        return em;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            title = bundle.getString("Message.title", getActivity().getString(R.string.alert));
            message = bundle.getString("Message.message", "");
            builder.setTitle(title);
            builder.setMessage(message)
                .setPositiveButton(getActivity().getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        } else {
            title = savedInstanceState.getString("Message.title", getActivity().getString(R.string.alert));
            message = savedInstanceState.getString("Message.message", "");
            builder.setTitle(title);
            builder.setMessage(message)
                .setPositiveButton(getActivity().getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        }

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("Message.title", title);
        outState.putString("Message.message", message);
    }
}
