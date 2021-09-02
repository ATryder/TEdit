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

import android.app.Activity;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.view.ContextThemeWrapper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atr.tedit.R;
import com.atr.tedit.mainstate.Editor;
import com.atr.tedit.dialog.HelpDialog;
import com.atr.tedit.settings.Settings;
import com.atr.tedit.util.FontUtil;
import com.atr.tedit.utilitybar.UtilityBar;

public class TextSearchState extends UtilityState {
    private final TextView searchtv;
    private final TextView replacetv;
    private final EditText searchField;
    private final EditText replaceField;
    private final CheckBox wholeWord;
    private final CheckBox matchCase;

    private int barHeight;

    public TextSearchState(UtilityBar bar) {
        super(bar, UtilityBar.STATE_TEXT_SEARCH);
        int fontSize = 16;

        searchField = new EditText(new ContextThemeWrapper(bar.ctx, R.style.Dark_Roast));
        searchField.setSingleLine(true);
        searchField.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        searchField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        searchField.setId(R.id.six);
        searchField.setNextFocusRightId(R.id.seven);
        searchField.setNextFocusLeftId(R.id.five);
        searchField.setFocusable(true);
        searchField.setTypeface(FontUtil.getSystemTypeface());
        searchField.measure(bar.barWidth - bar.padding_w, LayoutParams.WRAP_CONTENT);
        int searchHeight = searchField.getMeasuredHeight() + bar.padding_h * 2;

        searchtv = new TextView(new ContextThemeWrapper(bar.ctx, R.style.Dark_Roast));
        searchtv.setText(R.string.search);
        searchtv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        searchtv.setId(R.id.ten);
        searchtv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        searchtv.setTypeface(FontUtil.getSystemTypeface());
        searchtv.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int tvSearchWidth = searchtv.getMeasuredWidth();

        replaceField = new EditText(new ContextThemeWrapper(bar.ctx, R.style.Dark_Roast));
        replaceField.setSingleLine(true);
        replaceField.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        replaceField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        replaceField.setId(R.id.seven);
        replaceField.setNextFocusRightId(R.id.eight);
        replaceField.setNextFocusLeftId(R.id.six);
        replaceField.setFocusable(true);
        replaceField.setTypeface(FontUtil.getSystemTypeface());
        replaceField.measure(bar.barWidth - bar.padding_w, LayoutParams.WRAP_CONTENT);
        int replaceHeight = replaceField.getMeasuredHeight() + bar.padding_h * 2;

        replacetv = new TextView(new ContextThemeWrapper(bar.ctx, R.style.Dark_Roast));
        replacetv.setText(R.string.replace);
        replacetv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        replacetv.setId(R.id.eleven);
        replacetv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        replacetv.setTypeface(FontUtil.getSystemTypeface());
        replacetv.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int tvReplaceWidth = replacetv.getMeasuredWidth();

        ConstraintLayout searchLayout = new ConstraintLayout(bar.ctx);
        LayoutParams lp = new LayoutParams(bar.barWidth - bar.padding_w, LayoutParams.WRAP_CONTENT);
        searchLayout.setLayoutParams(lp);
        searchLayout.setTranslationX(bar.padding_w);
        searchLayout.setTranslationY(bar.barHeight + bar.padding_h);
        searchLayout.addView(searchtv);
        searchLayout.addView(searchField);
        ConstraintSet cset = new ConstraintSet();
        cset.constrainWidth(searchtv.getId(), tvSearchWidth > tvReplaceWidth ? tvSearchWidth : tvReplaceWidth);
        cset.constrainHeight(searchtv.getId(), ConstraintSet.MATCH_CONSTRAINT);
        cset.connect(searchtv.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
        cset.connect(searchtv.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        cset.connect(searchtv.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        cset.connect(searchtv.getId(), ConstraintSet.RIGHT, searchField.getId(), ConstraintSet.LEFT, 0);
        cset.constrainWidth(searchField.getId(), ConstraintSet.MATCH_CONSTRAINT);
        cset.constrainHeight(searchField.getId(), ConstraintSet.WRAP_CONTENT);
        cset.connect(searchField.getId(), ConstraintSet.LEFT, searchtv.getId(), ConstraintSet.RIGHT, bar.margin);
        cset.connect(searchField.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        cset.connect(searchField.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
        cset.connect(searchField.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        cset.applyTo(searchLayout);

        ConstraintLayout replaceLayout = new ConstraintLayout(bar.ctx);
        lp = new LayoutParams(bar.barWidth - bar.padding_w, LayoutParams.WRAP_CONTENT);
        replaceLayout.setLayoutParams(lp);
        replaceLayout.setTranslationX(bar.padding_w);
        replaceLayout.setTranslationY(bar.barHeight + searchHeight + bar.padding_h);
        replaceLayout.addView(replacetv);
        replaceLayout.addView(replaceField);
        cset = new ConstraintSet();
        cset.constrainWidth(replacetv.getId(), tvSearchWidth > tvReplaceWidth ? tvSearchWidth : tvReplaceWidth);
        cset.constrainHeight(replacetv.getId(), ConstraintSet.MATCH_CONSTRAINT);
        cset.connect(replacetv.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
        cset.connect(replacetv.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        cset.connect(replacetv.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        cset.connect(replacetv.getId(), ConstraintSet.RIGHT, replaceField.getId(), ConstraintSet.LEFT, 0);
        cset.constrainWidth(replaceField.getId(), ConstraintSet.MATCH_CONSTRAINT);
        cset.constrainHeight(replaceField.getId(), ConstraintSet.WRAP_CONTENT);
        cset.connect(replaceField.getId(), ConstraintSet.LEFT, replacetv.getId(), ConstraintSet.RIGHT, bar.margin);
        cset.connect(replaceField.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
        cset.connect(replaceField.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
        cset.connect(replaceField.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        cset.applyTo(replaceLayout);

        wholeWord = new CheckBox(new ContextThemeWrapper(bar.ctx, R.style.Dark_Roast));
        wholeWord.setText(R.string.whole_word);
        wholeWord.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        wholeWord.setId(R.id.eight);
        wholeWord.setNextFocusRightId(R.id.nine);
        wholeWord.setNextFocusLeftId(R.id.seven);
        wholeWord.setFocusable(true);
        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mlp.setMargins(0, 0, bar.margin, 0);
        wholeWord.setLayoutParams(mlp);
        wholeWord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((Editor)BAR.ctx.getFrag()).getSearchString().setWholeWord(b);
            }
        });

        matchCase = new CheckBox(new ContextThemeWrapper(bar.ctx, R.style.Dark_Roast));
        matchCase.setText(R.string.match_case);
        matchCase.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        matchCase.setId(R.id.nine);
        matchCase.setNextFocusRightId(R.id.zero);
        matchCase.setNextFocusLeftId(R.id.eight);
        matchCase.setFocusable(true);
        mlp = new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mlp.setMargins(bar.margin, 0, 0, 0);
        matchCase.setLayoutParams(mlp);
        matchCase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ((Editor)BAR.ctx.getFrag()).getSearchString().setMatchCase(b);
            }
        });

        LinearLayout checkLayout = new LinearLayout(bar.ctx);
        checkLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lllp.gravity = Gravity.CENTER;
        checkLayout.setLayoutParams(lllp);
        checkLayout.addView(wholeWord);
        checkLayout.addView(matchCase);
        FontUtil.applyFont(FontUtil.getSystemTypeface(), checkLayout);
        checkLayout.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        checkLayout.setTranslationX(Math.round(bar.barWidth * 0.5) - Math.round(checkLayout.getMeasuredWidth() * 0.5));
        checkLayout.setTranslationY(bar.barHeight + searchHeight + replaceHeight + bar.padding_h);
        int checkHeight = checkLayout.getMeasuredHeight() + bar.padding_h * 2;

        Button replace = new Button(bar.ctx);
        replace.setBackgroundResource(R.drawable.button_replace);
        replace.setId(R.id.zero);
        replace.setNextFocusRightId(R.id.one);
        replace.setNextFocusLeftId(R.id.nine);
        replace.setLayoutParams(new LayoutParams(bar.bWidth, bar.bHeight));
        replace.setFocusable(true);
        replace.setTranslationX(bar.padding_w);
        replace.setTranslationY(bar.padding_h);
        replace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Editor)BAR.ctx.getFrag()).replace(searchField.getText().toString(),
                        replaceField.getText().toString());
            }
        });

        Button replaceAll = new Button(bar.ctx);
        replaceAll.setBackgroundResource(R.drawable.button_replace_all);
        replaceAll.setId(R.id.one);
        replaceAll.setNextFocusRightId(R.id.two);
        replaceAll.setNextFocusLeftId(R.id.zero);
        replaceAll.setLayoutParams(new LayoutParams(bar.bWidth, bar.bHeight));
        replaceAll.setFocusable(true);
        replaceAll.setTranslationX(bar.padding_w + bar.margin + bar.bWidth);
        replaceAll.setTranslationY(bar.padding_h);
        replaceAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Editor)BAR.ctx.getFrag()).replaceAll(searchField.getText().toString(),
                        replaceField.getText().toString());
            }
        });

        Button left = new Button(bar.ctx);
        left.setBackgroundResource(R.drawable.button_arrow_left);
        left.setId(R.id.two);
        left.setNextFocusRightId(R.id.three);
        left.setNextFocusLeftId(R.id.one);
        left.setLayoutParams(new LayoutParams(bar.bWidth, bar.bHeight));
        left.setFocusable(true);
        left.setTranslationX(bar.padding_w + bar.bWidth * 2 + bar.margin * 2);
        left.setTranslationY(bar.padding_h);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Editor)BAR.ctx.getFrag()).findPrevious(searchField.getText().toString());
            }
        });

        Button right = new Button(bar.ctx);
        right.setBackgroundResource(R.drawable.button_arrow_right);
        right.setId(R.id.three);
        right.setNextFocusRightId(R.id.four);
        right.setNextFocusLeftId(R.id.two);
        right.setLayoutParams(new LayoutParams(bar.bWidth, bar.bHeight));
        right.setFocusable(true);
        right.setTranslationX(bar.padding_w + bar.bWidth * 3 + bar.margin * 3);
        right.setTranslationY(bar.padding_h);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Editor)BAR.ctx.getFrag()).findNext(searchField.getText().toString());
            }
        });

        Button help = new Button(BAR.ctx);
        help.setBackgroundResource(R.drawable.button_help);
        help.setId(R.id.five);
        help.setNextFocusRightId(R.id.six);
        help.setNextFocusLeftId(R.id.four);
        help.setLayoutParams(new LayoutParams(bar.bWidth, bar.bHeight));
        help.setFocusable(true);
        help.setTranslationX(bar.barWidth - bar.padding_w - bar.bWidth);
        help.setTranslationY(bar.padding_h);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpDialog hd = HelpDialog.newInstance(R.layout.help_textsearch, BAR.ctx.getString(R.string.editor));
                hd.show(BAR.ctx.getSupportFragmentManager(), "HelpDialog");
            }
        });


        View[] l = {replace, replaceAll, left, right, help, searchLayout, replaceLayout, checkLayout};
        LAYERS = new View[1][];
        LAYERS[0] = l;

        barHeight = searchHeight + replaceHeight + checkHeight + bar.barHeight;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            searchField.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            searchField.setTextDirection((Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                    View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);

            replaceField.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            replaceField.setTextDirection((Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                    View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);
        }
    }

    public TextSearchState(UtilityBar bar, String searchTerm, String replaceTerm,
                           boolean wholeWord, boolean matchCase) {
        this(bar);

        searchField.setText(searchTerm);
        replaceField.setText(replaceTerm);
        this.wholeWord.setChecked(wholeWord);
        this.matchCase.setChecked(matchCase);
    }

    @Override
    public void setToState(int layer) {
        LayoutParams lp = BAR.bar.getLayoutParams();
        lp.height = barHeight;
        BAR.bar.setLayoutParams(lp);

        super.setToState(layer);
    }

    @Override
    protected void transOut() {
        animating = true;
        View[] l = LAYERS[getLayer()];
        int count = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            for (int i = 0; i < 5; i++) {
                View v = l[i];
                v.setEnabled(false);
                animateGL(v, 0, SCALE, new AnticipateInterpolator(), 3 * animDelay + animDelay * i);
            }

            for (int i = l.length - 1; i >= 5; i--) {
                View v = l[i];
                v.setEnabled(false);
                animateGL(v, 0, SCALE, new AnticipateInterpolator(), animDelay * count);
                count++;
            }
        } else {
            for (int i = 0; i < 5; i++) {
                View v = l[i];
                v.setEnabled(false);
                animateSW(v, 0, SCALE, new AnticipateInterpolator(), 3 * animDelay + animDelay * i);
            }

            for (int i = l.length - 1; i >= 5; i--) {
                View v = l[i];
                v.setEnabled(false);
                animateSW(v, 0, SCALE, new AnticipateInterpolator(), animDelay * count);
                count++;
            }
        }

        animateBarHeight(barHeight, BAR.barHeight, new AccelerateDecelerateInterpolator(), ANIMLENGTH);
    }

    @Override
    protected void transIn() {
        super.transIn();
        animateBarHeight(BAR.barHeight, barHeight, new AccelerateDecelerateInterpolator(), 0);
    }

    public void setFields(String searchPhrase, String replacePhrase, boolean wholeWord, boolean matchCase) {
        searchField.setText(searchPhrase);
        replaceField.setText(replacePhrase);
        this.wholeWord.setChecked(wholeWord);
        this.matchCase.setChecked(matchCase);
    }

    public String getSearchPhrase() {
        return searchField.getText().toString();
    }

    public String getReplacePhrase() {
        return replaceField.getText().toString();
    }

    public boolean isWholeWord() {
        return wholeWord.isChecked();
    }

    public boolean isMatchCase() {
        return matchCase.isChecked();
    }

    public void clearFocus() {
        if (searchField.hasFocus()) {
            InputMethodManager imm = (InputMethodManager)BAR.ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
            } catch (Exception e) {

            }
            searchField.clearFocus();
        } else if (replaceField.hasFocus()) {
            InputMethodManager imm = (InputMethodManager)BAR.ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(replaceField.getWindowToken(), 0);
            } catch (Exception e) {

            }
            replaceField.clearFocus();
        }
    }

    public void hideCursor(boolean hide) {
        searchField.setCursorVisible(!hide);
        replaceField.setCursorVisible(!hide);
    }

    @Override
    public void applySettings() {
        for (int i = 0; i < LAYERS.length; i++) {
            for (View v : LAYERS[i])
                FontUtil.applyFont(FontUtil.getSystemTypeface(), v);
        }

        searchtv.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        replacetv.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int tvSearchWidth = searchtv.getMeasuredWidth();
        int searchHeight = searchField.getMeasuredHeight() + BAR.padding_h * 2;
        int tvReplaceWidth = replacetv.getMeasuredWidth();
        int replaceHeight = replaceField.getMeasuredHeight() + BAR.padding_h * 2;

        ConstraintLayout cLayout = (ConstraintLayout)searchtv.getParent();
        ConstraintSet cset = new ConstraintSet();
        cset.clone(cLayout);
        cset.constrainWidth(searchtv.getId(), tvSearchWidth > tvReplaceWidth ? tvSearchWidth : tvReplaceWidth);
        cset.applyTo(cLayout);

        cLayout = (ConstraintLayout)replacetv.getParent();
        cLayout.setTranslationY(BAR.barHeight + searchHeight + BAR.padding_h);
        cset.clone(cLayout);
        cset.constrainWidth(replacetv.getId(), tvSearchWidth > tvReplaceWidth ? tvSearchWidth : tvReplaceWidth);
        cset.applyTo(cLayout);

        LinearLayout checkLayout = (LinearLayout)matchCase.getParent();
        checkLayout.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        checkLayout.setTranslationX(Math.round(BAR.barWidth * 0.5) - Math.round(checkLayout.getMeasuredWidth() * 0.5));
        checkLayout.setTranslationY(BAR.barHeight + searchHeight + replaceHeight + BAR.padding_h);
        int checkHeight = checkLayout.getMeasuredHeight() + BAR.padding_h * 2;

        barHeight = searchHeight + replaceHeight + checkHeight + BAR.barHeight;

        if (BAR.getState().STATE == UtilityBar.STATE_TEXT_SEARCH) {
            ViewGroup.LayoutParams lp = BAR.bar.getLayoutParams();
            lp.height = barHeight;
            BAR.bar.setLayoutParams(lp);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            searchField.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            searchField.setTextDirection((Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                    View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);

            replaceField.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            replaceField.setTextDirection((Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                    View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);
        }
    }
}
