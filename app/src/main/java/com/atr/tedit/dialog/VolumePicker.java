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

import android.annotation.TargetApi;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.support.v7.view.ContextThemeWrapper;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.file.descriptor.AndFile;
import com.atr.tedit.mainstate.Browser;
import com.atr.tedit.settings.dialog.DirectoryPicker;
import com.atr.tedit.util.FontUtil;

import java.util.Arrays;

public class VolumePicker extends TDialog {
    private AndFile[] volumes;
    private AndFile currentChoice;
    private String fragmentTag;

    public static VolumePicker newInstance(String currentVolume) {
        return newInstance(currentVolume, null);
    }

    public static VolumePicker newInstance(String currentVolume, String fragmentTag) {
        Bundle bundle = new Bundle();
        bundle.putString("TEdit.volumePicker.currentChoice", currentVolume);
        if (fragmentTag != null)
            bundle.putString("TEdit.volumePicker.fragmentTag", fragmentTag);

        VolumePicker vp = new VolumePicker();
        vp.setArguments(bundle);

        return vp;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final TEditActivity ctx = (TEditActivity)getActivity();
        float density = ctx.getUtilityBar().dMetrics.density;

        int choice = -1;
        if (savedInstanceState == null) {
            String strChoice = getArguments().getString("TEdit.volumePicker.currentChoice", "");
            if (strChoice.isEmpty()) {
                currentChoice = AndFile.createDescriptor(Environment.getExternalStorageDirectory());
            } else
                currentChoice = AndFile.createDescriptorFromTree(strChoice, ctx);
            fragmentTag = getArguments().getString("TEdit.volumePicker.fragmentTag", null);
        } else {
            String strChoice = savedInstanceState.getString("TEdit.volumePicker.currentChoice", "");
            if (strChoice.isEmpty()) {
                currentChoice = AndFile.createDescriptor(Environment.getExternalStorageDirectory());
            } else
                currentChoice = AndFile.createDescriptorFromTree(strChoice, ctx);
            fragmentTag = savedInstanceState.getString("TEdit.volumePicker.fragmentTag", null);
        }

        Uri[] vols = ctx.getPermittedUris();
        volumes = new AndFile[vols.length + 2];
        int count = 0;
        for (int i = 0; i < vols.length; i++) {
            AndFile af = AndFile.createDescriptor(DocumentFile.fromTreeUri(ctx, vols[i]), vols[i]);
            if (af.exists())
                volumes[count++] = af;
        }
        if (count + 2 < volumes.length) {
            volumes = Arrays.copyOf(volumes, count + 2);
        }
        volumes[volumes.length - 2] = AndFile.createDescriptor(Environment.getExternalStorageDirectory());
        volumes[volumes.length - 1] = ctx.getRoot();

        String[] options = new String[volumes.length];
        for (int i = 0; i < options.length - 2; i++) {
            AndFile f = volumes[i];
            options[i] = f.getName();
            if (currentChoice.getPathIdentifier().equals(f.getPathIdentifier()))
                choice = i;
        }
        options[volumes.length - 2] = getString(R.string.internal);
        options[volumes.length - 1] = getString(R.string.root);

        if (choice < 0) {
            if (currentChoice.getPathIdentifier().equals(ctx.getRoot().getPathIdentifier())) {
                choice = volumes.length - 1;
            } else
                choice = volumes.length - 2;
        }

        RadioGroup radioGroup = new RadioGroup(new ContextThemeWrapper(getActivity(), theme));
        radioGroup.setPadding(Math.round(24 * density), 0, Math.round(24 * density), 0);
        final int[] ids = new int[options.length];
        for (int i = 0; i < options.length; i++) {
            RadioButton rb = new RadioButton(new ContextThemeWrapper(getActivity(), theme));
            rb.setId(i);
            ids[i] = rb.getId();
            rb.setText(options[i]);
            rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            rb.setTypeface(FontUtil.getDefault());
            RadioGroup.LayoutParams lp = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT);
            if (i < options.length - 1)
                lp.setMargins(0, 0, 0, Math.round(10 * density));
            radioGroup.addView(rb, lp);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                for (int i = 0; i < ids.length; i++) {
                    if (ids[i] == id) {
                        currentChoice = volumes[i];
                    }
                }
            }
        });
        radioGroup.check(choice);

        setIcon(R.drawable.tedit_logo_brown);
        setTitle(R.string.volumePicker);
        setView(radioGroup);
        setNegativeButton(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setPositiveButton(R.string.okay, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragmentTag == null) {
                    ((Browser)ctx.getFrag()).setVolume(currentChoice);
                } else {
                    DirectoryPicker dp = (DirectoryPicker)ctx.getSupportFragmentManager().findFragmentByTag(fragmentTag);
                    dp.setVolume(currentChoice);
                }
                dismiss();
            }
        });

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("TEdit.volumePicker.currentChoice", currentChoice.getPathIdentifier());
        if (fragmentTag != null)
            outState.putString("TEdit.volumePicker.fragmentTag", fragmentTag);
    }
}
