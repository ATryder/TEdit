package com.atr.tedit.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.mainstate.Editor;
import com.atr.tedit.mainstate.Tabs;

public class ConfirmCloseText extends TDialog {
    private long key;

    public static ConfirmCloseText getInstance(long key) {
        Bundle bundle = new Bundle();
        bundle.putLong("key", key);

        ConfirmCloseText cct = new ConfirmCloseText();
        cct.setArguments(bundle);

        return cct;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final TEditActivity ctx = (TEditActivity)getContext();

        if (savedInstanceState == null) {
            key = getArguments().getLong("key", ctx.getLastTxt());
        } else
            key = savedInstanceState.getLong("key", ctx.getLastTxt());

        setTitle(R.string.confirmclose);
        setMessage(R.string.confirmclose_message);
        setPositiveButton(R.string.confirm, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ctx.getFrag() instanceof Editor && ((Editor)ctx.getFrag()).getKey() == key) {
                    ctx.closeText();
                } else {
                    if (ctx.dbIsOpen())
                        ctx.getDB().deleteText(key);

                    if (ctx.getFrag() instanceof Tabs)
                        ((Tabs)ctx.getFrag()).reset();
                }

                dismiss();
            }
        });
        setNegativeButton(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ctx.getFrag() instanceof Tabs)
                    ((Tabs)ctx.getFrag()).reset();
                dismiss();
            }
        });

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("key", key);
    }
}
