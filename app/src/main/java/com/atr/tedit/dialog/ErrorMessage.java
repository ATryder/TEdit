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
import android.os.Bundle;
import android.view.View;

import com.atr.tedit.R;

/**
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */

public class ErrorMessage extends TDialog {
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
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            title = bundle.getString("Message.title", getActivity().getString(R.string.alert));
            message = bundle.getString("Message.message", "");
            setTitle(title);
            setMessage(message);
            setPositiveButton(getActivity().getString(R.string.okay), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        } else {
            title = savedInstanceState.getString("Message.title", getActivity().getString(R.string.alert));
            message = savedInstanceState.getString("Message.message", "");
            setTitle(title);
            setMessage(message);
            setPositiveButton(getActivity().getString(R.string.okay), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("Message.title", title);
        outState.putString("Message.message", message);
    }
}
