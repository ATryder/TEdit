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
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.util.FontUtil;

public abstract class TDialog extends DialogFragment {
    public int theme = R.style.Coffee_Cream;
    public Typeface typeFace = FontUtil.getSystemTypeface();
    public int padding = 7;
    public int contentMargin = 16;

    private ConstraintLayout layout;
    private ConstraintLayout titleLayout;
    private ImageView icon;
    private TextView title;
    private TextView message;
    private View view;
    private ConstraintLayout buttonLayout;
    private Button negativeButton;
    private Button neutralButton;
    private Button positiveButton;

    protected boolean fillParent = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        float density = ((TEditActivity)getActivity()).getUtilityBar().dMetrics.density;

        layout = new ConstraintLayout(new ContextThemeWrapper(getActivity(), theme));
        layout.setPadding(Math.round(padding * density), Math.round(padding * density),
                Math.round(padding * density), Math.round(padding * density));
        layout.setId(R.id.dialog_layout);

        ConstraintSet mainSet = new ConstraintSet();
        ConstraintSet titleSet = new ConstraintSet();
        ConstraintSet buttonSet = new ConstraintSet();

        if (icon != null || title != null) {
            titleLayout = new ConstraintLayout(new ContextThemeWrapper(getActivity(), theme));
            titleLayout.setId(R.id.dialog_layout_title);

            layout.addView(titleLayout);
            mainSet.constrainWidth(titleLayout.getId(), ConstraintSet.WRAP_CONTENT);
            mainSet.constrainHeight(titleLayout.getId(), ConstraintSet.WRAP_CONTENT);
            mainSet.setHorizontalBias(titleLayout.getId(), 0);

            mainSet.connect(titleLayout.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP);
            mainSet.connect(titleLayout.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT);
            mainSet.connect(titleLayout.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT);

            if (message == null) {
                if (view == null) {
                    if (negativeButton == null || neutralButton == null || positiveButton == null) {
                        mainSet.connect(titleLayout.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID,
                                ConstraintSet.BOTTOM);
                    } else
                        mainSet.connect(titleLayout.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(),
                                ConstraintSet.TOP, Math.round(contentMargin * density));
                } else
                    mainSet.connect(titleLayout.getId(), ConstraintSet.BOTTOM, view.getId(),
                            ConstraintSet.TOP);
            } else
                mainSet.connect(titleLayout.getId(), ConstraintSet.BOTTOM, message.getId(),
                        ConstraintSet.TOP);
        }

        if (negativeButton != null || neutralButton != null || positiveButton != null) {
            buttonLayout = new ConstraintLayout(new ContextThemeWrapper(getActivity(), theme));
            buttonLayout.setId(R.id.dialog_layout_button);

            layout.addView(buttonLayout);
            mainSet.constrainWidth(buttonLayout.getId(), ConstraintSet.WRAP_CONTENT);
            mainSet.constrainHeight(buttonLayout.getId(), ConstraintSet.WRAP_CONTENT);
            mainSet.setHorizontalBias(buttonLayout.getId(), 1);

            if (view == null) {
                if (message == null) {
                    if (titleLayout == null) {
                        mainSet.connect(buttonLayout.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                                ConstraintSet.TOP);
                    } else
                        mainSet.connect(buttonLayout.getId(), ConstraintSet.TOP, titleLayout.getId(),
                                ConstraintSet.BOTTOM);
                } else
                    mainSet.connect(buttonLayout.getId(), ConstraintSet.TOP, message.getId(),
                            ConstraintSet.BOTTOM);
            } else
                mainSet.connect(buttonLayout.getId(), ConstraintSet.TOP, view.getId(),
                        ConstraintSet.BOTTOM);

            mainSet.connect(buttonLayout.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT);
            mainSet.connect(buttonLayout.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT);
            mainSet.connect(buttonLayout.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM);
        }

        if (icon != null) {
            titleLayout.addView(icon);
            titleSet.constrainWidth(icon.getId(), ConstraintSet.WRAP_CONTENT);
            titleSet.constrainHeight(icon.getId(), ConstraintSet.WRAP_CONTENT);

            titleSet.connect(icon.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP);
            titleSet.connect(icon.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT);

            if (title != null) {
                titleSet.connect(icon.getId(), ConstraintSet.RIGHT,
                        title.getId(), ConstraintSet.LEFT, Math.round(7 * density));
            } else
                titleSet.connect(icon.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID,
                        ConstraintSet.RIGHT);

            titleSet.connect(icon.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM);
        }

        if (title != null) {
            titleLayout.addView(title);
            titleSet.constrainWidth(title.getId(), ConstraintSet.WRAP_CONTENT);
            titleSet.constrainHeight(title.getId(), ConstraintSet.WRAP_CONTENT);

            titleSet.connect(title.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP);

            if (icon != null) {
                titleSet.connect(title.getId(), ConstraintSet.LEFT,
                        icon.getId(), ConstraintSet.RIGHT);
            } else
                titleSet.connect(title.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID,
                        ConstraintSet.LEFT);

            titleSet.connect(title.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT);
            titleSet.connect(title.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM);
        }

        if (message != null) {
            layout.addView(message);
            mainSet.constrainWidth(message.getId(), ConstraintSet.WRAP_CONTENT);
            mainSet.constrainHeight(message.getId(), ConstraintSet.WRAP_CONTENT);
            mainSet.setHorizontalBias(message.getId(), 0);

            if (titleLayout == null) {
                mainSet.connect(message.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            } else
                mainSet.connect(message.getId(), ConstraintSet.TOP, titleLayout.getId(),
                        ConstraintSet.BOTTOM, Math.round(contentMargin * density));

            mainSet.connect(message.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            mainSet.connect(message.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);

            if (view == null) {
                if (buttonLayout == null) {
                    mainSet.connect(message.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                } else
                    mainSet.connect(message.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(),
                            ConstraintSet.TOP, Math.round(contentMargin * density));
            } else
                mainSet.connect(message.getId(), ConstraintSet.BOTTOM, view.getId(),
                            ConstraintSet.TOP, Math.round(10 * density));
        }

        if (view != null) {
            layout.addView(view);
            mainSet.constrainWidth(view.getId(), ConstraintSet.MATCH_CONSTRAINT);
            mainSet.constrainHeight(view.getId(), ConstraintSet.MATCH_CONSTRAINT);
            mainSet.constrainDefaultHeight(view.getId(),
                    fillParent ? ConstraintSet.MATCH_CONSTRAINT_SPREAD : ConstraintSet.MATCH_CONSTRAINT_WRAP);

            if (message == null) {
                if (titleLayout == null) {
                    mainSet.connect(view.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                } else
                    mainSet.connect(view.getId(), ConstraintSet.TOP, titleLayout.getId(),
                            ConstraintSet.BOTTOM, Math.round(contentMargin * density));
            } else
                mainSet.connect(view.getId(), ConstraintSet.TOP, message.getId(),
                        ConstraintSet.BOTTOM, Math.round(contentMargin * density));

            mainSet.connect(view.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            mainSet.connect(view.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);

            if (buttonLayout == null) {
                mainSet.connect(view.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            } else
                mainSet.connect(view.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(),
                        ConstraintSet.TOP, Math.round(contentMargin * density));
        }

        if (negativeButton != null) {
            buttonLayout.addView(negativeButton);
            buttonSet.constrainWidth(negativeButton.getId(), ConstraintSet.WRAP_CONTENT);
            buttonSet.constrainHeight(negativeButton.getId(), ConstraintSet.WRAP_CONTENT);

            buttonSet.connect(negativeButton.getId(), ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            buttonSet.connect(negativeButton.getId(), ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID, ConstraintSet.LEFT);

            if (neutralButton != null) {
                buttonSet.connect(negativeButton.getId(), ConstraintSet.RIGHT,
                        neutralButton.getId(), ConstraintSet.LEFT, Math.round(7 * density));
            } else if (positiveButton != null) {
                buttonSet.connect(negativeButton.getId(), ConstraintSet.RIGHT,
                        positiveButton.getId(), ConstraintSet.LEFT, Math.round(7 * density));
            } else
                buttonSet.connect(negativeButton.getId(), ConstraintSet.RIGHT,
                        ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);

            buttonSet.connect(negativeButton.getId(), ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        }

        if (neutralButton != null) {
            buttonLayout.addView(neutralButton);
            buttonSet.constrainWidth(neutralButton.getId(), ConstraintSet.WRAP_CONTENT);
            buttonSet.constrainHeight(neutralButton.getId(), ConstraintSet.WRAP_CONTENT);

            buttonSet.connect(neutralButton.getId(), ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID, ConstraintSet.TOP);

            if (negativeButton == null) {
                buttonSet.connect(neutralButton.getId(), ConstraintSet.LEFT,
                        ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            } else
                buttonSet.connect(neutralButton.getId(), ConstraintSet.LEFT,
                        negativeButton.getId(), ConstraintSet.RIGHT);

            if (positiveButton == null) {
                buttonSet.connect(neutralButton.getId(), ConstraintSet.RIGHT,
                        ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            } else
                buttonSet.connect(neutralButton.getId(), ConstraintSet.RIGHT,
                        positiveButton.getId(), ConstraintSet.LEFT, Math.round(7 * density));

            buttonSet.connect(neutralButton.getId(), ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        }

        if (positiveButton != null) {
            buttonLayout.addView(positiveButton);
            buttonSet.constrainWidth(positiveButton.getId(), ConstraintSet.WRAP_CONTENT);
            buttonSet.constrainHeight(positiveButton.getId(), ConstraintSet.WRAP_CONTENT);

            buttonSet.connect(positiveButton.getId(), ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID, ConstraintSet.TOP);

            if (neutralButton == null) {
                if (negativeButton == null) {
                    buttonSet.connect(positiveButton.getId(), ConstraintSet.LEFT,
                            ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                } else
                    buttonSet.connect(positiveButton.getId(), ConstraintSet.LEFT,
                            negativeButton.getId(), ConstraintSet.RIGHT);
            } else
                buttonSet.connect(positiveButton.getId(), ConstraintSet.LEFT,
                        neutralButton.getId(), ConstraintSet.RIGHT);

            buttonSet.connect(positiveButton.getId(), ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            buttonSet.connect(positiveButton.getId(), ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        }

        if (titleLayout != null)
            titleSet.applyTo(titleLayout);

        if (buttonLayout != null)
            buttonSet.applyTo(buttonLayout);

        mainSet.applyTo(layout);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (fillParent) {
            layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            RelativeLayout rl = new RelativeLayout(new ContextThemeWrapper(getActivity(), theme));
            rl.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            rl.addView(layout);

            builder.setView(rl);
        } else {
            layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            builder.setView(layout);
        }

        return builder.create();
    }

    public ConstraintLayout getLayout() {
        return layout;
    }

    public void setIcon(int resourceId) {
        if (icon == null) {
            icon = new ImageView(new ContextThemeWrapper(getActivity(), theme));
            icon.setId(R.id.dialog_icon);
        }
        icon.setImageResource(resourceId);
    }

    public void setIcon(Drawable drawable) {
        if (icon == null) {
            icon = new ImageView(new ContextThemeWrapper(getActivity(), theme));
            icon.setId(R.id.dialog_icon);
        }
        icon.setImageDrawable(drawable);
    }

    public void setIcon(Bitmap bitmap) {
        if (icon == null) {
            icon = new ImageView(new ContextThemeWrapper(getActivity(), theme));
            icon.setId(R.id.dialog_icon);
        }
        icon.setImageBitmap(bitmap);
    }

    public void setTitle(int stringResourceId) {
        setTitle(getString(stringResourceId));
    }

    public void setTitle(String text) {
        if (title == null) {
            title = new TextView(new ContextThemeWrapper(getActivity(), theme));
            title.setTypeface(typeFace);
            title.setTextColor(((TEditActivity)getContext()).getThemeColor(R.color.coffeeNcream));
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            title.setId(R.id.dialog_title);
        }

        title.setText(text);
    }

    public void setMessage(int stringResourceId) {
        setMessage(getString(stringResourceId));
    }

    public void setMessage(String text) {
        if (message == null) {
            message = new TextView(new ContextThemeWrapper(getActivity(), theme));
            message.setTypeface(typeFace);
            message.setId(R.id.dialog_message);
        }

        message.setText(text);
    }

    public void setView(View view) {
        this.view = view;
        view.setId(R.id.dialog_view);
    }

    public void setNegativeButton(int stringResourceId, View.OnClickListener onClickListener) {
        setNegativeButton(getString(stringResourceId), onClickListener);
    }

    public void setNegativeButton(String text, View.OnClickListener onClickListener) {
        if (negativeButton == null) {
            negativeButton = new Button(new ContextThemeWrapper(getActivity(), theme));
            negativeButton.setTypeface(typeFace);
            negativeButton.setTextColor(((TEditActivity)getContext()).getThemeColor(R.color.coffeeNcream));
            negativeButton.setId(R.id.dialog_negative_button);
        }

        negativeButton.setText(text);
        if (onClickListener != null)
            negativeButton.setOnClickListener(onClickListener);
    }

    public void setNeutralButton(int stringResourceId, View.OnClickListener onClickListener) {
        setNeutralButton(getString(stringResourceId), onClickListener);
    }

    public void setNeutralButton(String text, View.OnClickListener onClickListener) {
        if (neutralButton == null) {
            neutralButton = new Button(new ContextThemeWrapper(getActivity(), theme));
            neutralButton.setTypeface(typeFace);
            neutralButton.setTextColor(((TEditActivity)getContext()).getThemeColor(R.color.coffeeNcream));
            neutralButton.setId(R.id.dialog_neutral_button);
        }

        neutralButton.setText(text);
        if (onClickListener != null)
            neutralButton.setOnClickListener(onClickListener);
    }

    public void setPositiveButton(int stringResourceId, View.OnClickListener onClickListener) {
        setPositiveButton(getString(stringResourceId), onClickListener);
    }

    public void setPositiveButton(String text, View.OnClickListener onClickListener) {
        if (positiveButton == null) {
            positiveButton = new Button(new ContextThemeWrapper(getActivity(), theme));
            positiveButton.setTypeface(typeFace);
            positiveButton.setTextColor(((TEditActivity)getContext()).getThemeColor(R.color.coffeeNcream));
            positiveButton.setId(R.id.dialog_positive_button);
        }

        positiveButton.setText(text);
        if (onClickListener != null)
            positiveButton.setOnClickListener(onClickListener);
    }
}
