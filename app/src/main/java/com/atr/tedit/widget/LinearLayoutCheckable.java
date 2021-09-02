package com.atr.tedit.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;

import java.util.LinkedList;
import java.util.List;

public class LinearLayoutCheckable extends LinearLayoutCompat implements Checkable {
    private boolean isChecked = false;
    private List<Checkable> checkableViews = new LinkedList<>();

    public LinearLayoutCheckable(Context context) {
        super(context);
    }

    public LinearLayoutCheckable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutCheckable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        for (Checkable c : checkableViews) {
            c.setChecked(isChecked);
        }
    }

    @Override
    public void toggle() {
        isChecked = !isChecked;
        for (Checkable c : checkableViews) {
            c.toggle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            findCheckableChildren(getChildAt(i));
        }
    }

    private void findCheckableChildren(View v) {
        if (v instanceof Checkable)
            checkableViews.add((Checkable)v);

        if (!(v instanceof ViewGroup))
            return;

        final ViewGroup vg = (ViewGroup)v;
        final int childCount = vg.getChildCount();
        for (int i = 0; i < childCount; i++) {
            findCheckableChildren(vg.getChildAt(i));
        }
    }
}
