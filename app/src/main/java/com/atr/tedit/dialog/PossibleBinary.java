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
import com.atr.tedit.TEditActivity;
import com.atr.tedit.file.descriptor.AndFile;
import com.atr.tedit.mainstate.Browser;

import java.io.File;

public class PossibleBinary extends TDialog {
    String filePath;

    public static PossibleBinary getInstance(String filePath) {
        Bundle bundle = new Bundle();
        bundle.putString("pBinary.filePath", filePath);

        PossibleBinary pBin = new PossibleBinary();
        pBin.setArguments(bundle);

        return pBin;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            filePath = getArguments().getString("pBinary.filePath", "");
        } else {
            filePath = savedInstanceState.getString("pBinary.filePath", "");
        }

        setTitle(R.string.alert);
        setMessage(R.string.alert_binaryfile);
        setNegativeButton(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setPositiveButton(R.string.confirm, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                AndFile file = AndFile.createDescriptor(filePath, getActivity());
                ((Browser)((TEditActivity)getActivity()).getFrag())
                        .openFile(file, true);
            }
        });

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("pBinary.filePath", filePath);
    }
}
