package com.atr.tedit.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.view.ContextThemeWrapper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.dialog.ErrorMessage;
import com.atr.tedit.dialog.HelpDialog;
import com.atr.tedit.file.AndPath;
import com.atr.tedit.file.descriptor.AndFile;
import com.atr.tedit.mainstate.Editor;
import com.atr.tedit.settings.dialog.DirectoryPicker;
import com.atr.tedit.settings.dialog.TypefacePicker;
import com.atr.tedit.util.FontUtil;
import com.atr.tedit.util.SettingsApplicable;

import java.util.Arrays;

public class SettingsWindow {
    private static final int ANIMLENGTH = 240;

    private final long panelAnimLength;

    private final TEditActivity ctx;
    private final Handler handler = new Handler();

    private final ViewGroup settingsView;
    private final ViewAnimator settingsAnimator;

    private boolean animating = false;
    private boolean open = false;

    private TempSettings tempSettings;
    private TempSettings localTempSettings;

    public SettingsWindow(TEditActivity context) {
        ctx = context;

        final int fontSize = 16;
        final int colorCream = ctx.getThemeColor(R.color.cream);

        ViewGroup rootVG = ctx.findViewById(R.id.activity_tedit);
        LayoutInflater inflater = ctx.getLayoutInflater().cloneInContext(new ContextThemeWrapper(ctx, R.style.Dark_Roast));
        settingsView = inflater.inflate(R.layout.settings_global, rootVG, true).findViewById(R.id.settingsRoot);

        settingsAnimator = (ViewAnimator)settingsView.getChildAt(0);
        settingsAnimator.setInAnimation(AnimationUtils.loadAnimation(ctx, R.anim.settings_in));
        settingsAnimator.setOutAnimation(AnimationUtils.loadAnimation(ctx, R.anim.settings_out));
        panelAnimLength = settingsAnimator.getInAnimation().getDuration();

        settingsView.setVisibility(View.INVISIBLE);

        int width = (ctx.getMetrics().widthPixels > ctx.getMetrics().heightPixels)
                    ? ctx.getMetrics().heightPixels : ctx.getMetrics().widthPixels;
        ViewGroup.LayoutParams lp = settingsAnimator.getLayoutParams();
        lp.width = Math.round(width * 0.8f);
        settingsAnimator.setLayoutParams(lp);

        int colorLightRust = ctx.getThemeColor(R.color.lightRust);

        Button saveButton = settingsView.findViewById(R.id.saveButton);
        Button cancelButton = settingsView.findViewById(R.id.cancelButton);

        ConstraintLayout csLayout = (ConstraintLayout)saveButton.getParent();
        ConstraintSet cset = new ConstraintSet();
        cset.clone(csLayout);

        csLayout.removeView(saveButton);
        saveButton = new Button(new ContextThemeWrapper(ctx, R.style.buttonFlatDarkRust));
        saveButton.setId(R.id.saveButton);
        saveButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        saveButton.setText(ctx.getText(R.string.save));
        saveButton.setTextColor(colorLightRust);
        csLayout.addView(saveButton);

        csLayout.removeView(cancelButton);
        cancelButton = new Button(new ContextThemeWrapper(ctx, R.style.buttonFlatDarkRust));
        cancelButton.setId(R.id.cancelButton);
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        cancelButton.setText(ctx.getText(R.string.cancel));
        cancelButton.setTextColor(colorLightRust);
        csLayout.addView(cancelButton);

        cset.applyTo(csLayout);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applySettings();
                close(true);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close(true);
            }
        });

        saveButton = settingsView.findViewById(R.id.localSaveButton);
        cancelButton = settingsView.findViewById(R.id.localCancelButton);

        csLayout = (ConstraintLayout)saveButton.getParent();
        cset = new ConstraintSet();
        cset.clone(csLayout);

        csLayout.removeView(saveButton);
        saveButton = new Button(new ContextThemeWrapper(ctx, R.style.buttonFlatDarkRust));
        saveButton.setId(R.id.localSaveButton);
        saveButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        saveButton.setText(ctx.getText(R.string.save));
        saveButton.setTextColor(colorLightRust);
        csLayout.addView(saveButton);

        csLayout.removeView(cancelButton);
        cancelButton = new Button(new ContextThemeWrapper(ctx, R.style.buttonFlatDarkRust));
        cancelButton.setId(R.id.localCancelButton);
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        cancelButton.setText(ctx.getText(R.string.cancel));
        cancelButton.setTextColor(colorLightRust);
        csLayout.addView(cancelButton);

        cset.applyTo(csLayout);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applySettings();
                close(true);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close(true);
            }
        });

        Button localButton = settingsView.findViewById(R.id.localButton);
        Button globalHelpButton = settingsView.findViewById(R.id.helpButton);

        csLayout = (ConstraintLayout)localButton.getParent();
        cset = new ConstraintSet();
        cset.clone(csLayout);

        csLayout.removeView(localButton);
        localButton = new Button(new ContextThemeWrapper(ctx, R.style.buttonFlatDarkRust));
        localButton.setId(R.id.localButton);
        localButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        localButton.setText(ctx.getText(R.string.settings_local_button));
        localButton.setTextColor(colorLightRust);
        csLayout.addView(localButton);

        csLayout.removeView(globalHelpButton);
        globalHelpButton = new Button(new ContextThemeWrapper(ctx, R.style.buttonFlatDarkRust));
        globalHelpButton.setId(R.id.helpButton);
        globalHelpButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        globalHelpButton.setText(ctx.getText(R.string.help));
        globalHelpButton.setTextColor(colorLightRust);
        csLayout.addView(globalHelpButton);

        cset.applyTo(csLayout);

        Button globalButton = settingsView.findViewById(R.id.globalButton);
        Button localHelpButton = settingsView.findViewById(R.id.localHelpButton);

        csLayout = (ConstraintLayout)globalButton.getParent();
        cset = new ConstraintSet();
        cset.clone(csLayout);

        csLayout.removeView(globalButton);
        globalButton = new Button(new ContextThemeWrapper(ctx, R.style.buttonFlatDarkRust));
        globalButton.setId(R.id.globalButton);
        globalButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        globalButton.setText(ctx.getText(R.string.settings_global_button));
        globalButton.setTextColor(colorLightRust);
        csLayout.addView(globalButton);

        csLayout.removeView(localHelpButton);
        localHelpButton = new Button(new ContextThemeWrapper(ctx, R.style.buttonFlatDarkRust));
        localHelpButton.setId(R.id.localHelpButton);
        localHelpButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        localHelpButton.setText(ctx.getText(R.string.help));
        localHelpButton.setTextColor(colorLightRust);
        csLayout.addView(localHelpButton);

        cset.applyTo(csLayout);

        localButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsAnimator.getInAnimation().setDuration(panelAnimLength);
                settingsAnimator.getOutAnimation().setDuration(panelAnimLength);
                settingsAnimator.showNext();
            }
        });
        globalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsAnimator.getInAnimation().setDuration(panelAnimLength);
                settingsAnimator.getOutAnimation().setDuration(panelAnimLength);
                settingsAnimator.showPrevious();
            }
        });

        localHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int helpLayout = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        ? R.layout.help_settings_local : R.layout.help_settings_local_prelollipop;
                HelpDialog hd = HelpDialog.newInstance(helpLayout, ctx.getString(R.string.settings_local_button));
                hd.show(ctx.getSupportFragmentManager(), "HelpDialog");
            }
        });
        globalHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int helpLayout;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    helpLayout = R.layout.help_settings_global;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    helpLayout = R.layout.help_settings_global_prepie;
                } else
                    helpLayout = R.layout.help_settings_global_prelollipop;
                HelpDialog hd = HelpDialog.newInstance(helpLayout, ctx.getString(R.string.settings_global_button));
                hd.show(ctx.getSupportFragmentManager(), "HelpDialog");
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            TextView pathView = settingsView.findViewById(R.id.startupDir);
            pathView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            pathView.setTextDirection((Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                    View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);
        }

        Button startupDirButton = settingsView.findViewById(R.id.startupBrowse);
        startupDirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndPath currentPath = tempSettings.startupDir;
                if (currentPath != null && !currentPath.getCurrent().exists()) {
                    while(currentPath.moveToParent() != null && !currentPath.getCurrent().exists())
                        continue;

                    if (!currentPath.getCurrent().exists()) {
                        currentPath = null;
                    }
                }

                if (currentPath == null && (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
                    ErrorMessage em = ErrorMessage.getInstance(ctx.getString(R.string.alert),
                            ctx.getString(R.string.missing_dir));
                    em.show(ctx.getSupportFragmentManager(), "dialog");
                    return;
                }

                DirectoryPicker dialog = DirectoryPicker.newInstance(currentPath);
                dialog.show(ctx.getSupportFragmentManager(), DirectoryPicker.TAG);
            }
        });

        Button typefaceButton = settingsView.findViewById(R.id.fontBrowse);
        Button editorTypefaceButton = settingsView.findViewById(R.id.editorFontBrowse);
        Button localTypefaceButton = settingsView.findViewById(R.id.localFontBrowse);
        typefaceButton.setText(FontUtil.getTypefaceName(FontUtil.getSystemPath()));
        editorTypefaceButton.setText(FontUtil.getTypefaceName(FontUtil.getEditorPath()));

        FontUtil.applyFont(FontUtil.getSystemTypeface(), settingsView, editorTypefaceButton, localTypefaceButton);
        editorTypefaceButton.setTypeface(FontUtil.getEditorTypeface());

        typefaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypefacePicker dialog = TypefacePicker.newInstance(tempSettings.typefacePath, TypefacePicker.TYPE_SYSTEM);
                dialog.show(ctx.getSupportFragmentManager(), "TypefacePicker");
            }
        });

        editorTypefaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypefacePicker dialog = TypefacePicker.newInstance(tempSettings.editorTypefacePath, TypefacePicker.TYPE_EDITOR);
                dialog.show(ctx.getSupportFragmentManager(), "TypefacePicker");
            }
        });

        localTypefaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TypefacePicker dialog = TypefacePicker.newInstance(localTempSettings.editorTypefacePath, TypefacePicker.TYPE_LOCAL);
                dialog.show(ctx.getSupportFragmentManager(), "TypefacePicker");
            }
        });

        Spinner editorFontSize = settingsView.findViewById(R.id.editorFontSize);
        Spinner localFontSize = settingsView.findViewById(R.id.localFontSize);

        editorFontSize.setAdapter(new ArrayAdapter<String>(ctx, R.layout.spinner_item, FontUtil.getEditorPointSizes()));
        ((ArrayAdapter)editorFontSize.getAdapter()).setDropDownViewResource(R.layout.spinner_item);
        editorFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ((TextView)parentView.getChildAt(0)).setTypeface(FontUtil.getSystemTypeface());
                ((TextView)parentView.getChildAt(0)).setTextColor(colorCream);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        localFontSize.setAdapter(new ArrayAdapter<String>(ctx, R.layout.spinner_item, FontUtil.getLocalEditorPointSizes()));
        ((ArrayAdapter)editorFontSize.getAdapter()).setDropDownViewResource(R.layout.spinner_item);
        localFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ((TextView)parentView.getChildAt(0)).setTypeface(FontUtil.getSystemTypeface());
                ((TextView)parentView.getChildAt(0)).setTextColor(colorCream);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settingsView.findViewById(R.id.textDirLabel).setVisibility(View.GONE);
            settingsView.findViewById(R.id.textDirGroup).setVisibility(View.GONE);
            settingsView.findViewById(R.id.editorTextDirLabel).setVisibility(View.GONE);
            settingsView.findViewById(R.id.editorTextDirGroup).setVisibility(View.GONE);
            settingsView.findViewById(R.id.localTextDirLabel).setVisibility(View.GONE);
            settingsView.findViewById(R.id.localTextDirGroup).setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
            settingsView.findViewById(R.id.enableRoot).setVisibility(View.GONE);

            ((CheckBox) settingsView.findViewById(R.id.wordWrap)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.textDirLTR)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.textDirRTL)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.editorTextDirLTR)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.editorTextDirRTL)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.aobClose)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.aobParent)).setTextColor(colorCream);
            ((CheckBox) settingsView.findViewById(R.id.enableRoot)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.localWordWrapGlobal)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.localWordWrapOn)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.localWordWrapOff)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.localTextDirGlobal)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.localTextDirLTR)).setTextColor(colorCream);
            ((RadioButton) settingsView.findViewById(R.id.localTextDirRTL)).setTextColor(colorCream);
        }

        settingsView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return isOpen();
            }
        });

        resetGlobalSettings();
    }

    public boolean isAnimating() {
        return animating;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean canOpen() {
        return !isOpen() && !animating;
    }

    public boolean canClose() {
        return isOpen() && !animating;
    }

    public void open(boolean animate) {
        open = true;
        settingsView.setVisibility(View.VISIBLE);

        tempSettings = new TempSettings();
        resetGlobalSettings();

        if (ctx.getFrag() instanceof Editor) {
            settingsView.findViewById(R.id.localButton).setVisibility(View.VISIBLE);
            Editor editor = (Editor)ctx.getFrag();
            localTempSettings = new TempSettings(editor.getSettings());
            editor.clearFocus();
            editor.hideCursor(true);
            resetLocalSettings(editor.getSettings());

            if (settingsAnimator.getDisplayedChild() == 0) {
                settingsAnimator.getInAnimation().setDuration(0);
                settingsAnimator.getOutAnimation().setDuration(0);
                settingsAnimator.setDisplayedChild(1);
            }
        } else {
            settingsView.findViewById(R.id.localButton).setVisibility(View.INVISIBLE);
            if (settingsAnimator.getDisplayedChild() == 1) {
                settingsAnimator.getInAnimation().setDuration(0);
                settingsAnimator.getOutAnimation().setDuration(0);
                settingsAnimator.setDisplayedChild(0);
            }
        }

        ctx.closeContextMenu();

        if (!animate) {
            ((TransitionDrawable)settingsView.getBackground()).startTransition(0);
            return;
        }

        animating = true;
        ((TransitionDrawable)settingsView.getBackground()).startTransition(ANIMLENGTH);
        int width = settingsAnimator.getWidth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            animateGL(settingsAnimator,0, -width, new DecelerateInterpolator());
        } else
            animateSW(settingsAnimator,0, -width, new DecelerateInterpolator());

        handler.postDelayed(new Runnable() {
            public void run() {
                animating = false;
            }
        }, ANIMLENGTH);
    }

    public void close(boolean animate) {
        open = false;
        if (!animate) {
            settingsView.setVisibility(View.INVISIBLE);
            return;
        }

        animating = true;
        ((TransitionDrawable)settingsView.getBackground()).reverseTransition(ANIMLENGTH);
        int width = settingsAnimator.getWidth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            animateGL(settingsAnimator, -width, 0, new DecelerateInterpolator());
        } else
            animateSW(settingsAnimator, -width, 0, new DecelerateInterpolator());

        handler.postDelayed(new Runnable() {
            public void run() {
                animating = false;
                tempSettings = null;
                localTempSettings = null;
                if (ctx.getFrag() instanceof Editor)
                    ((Editor) ctx.getFrag()).hideCursor(false);
            }
        }, ANIMLENGTH);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void animateGL(View displayView, int to, int from, Interpolator interpolator) {
        displayView.setTranslationX(from);
        displayView.animate().translationX(to).setDuration(ANIMLENGTH).setInterpolator(interpolator).withLayer().start();
    }

    protected void animateSW(View displayView, int to, int from, Interpolator interpolator) {
        displayView.setTranslationX(from);
        displayView.animate().translationX(to).setDuration(ANIMLENGTH).setInterpolator(interpolator).start();
    }

    private void resetGlobalSettings() {
        settingsAnimator.findViewById(R.id.settingsScroll).scrollTo(0, 0);
        settingsAnimator.findViewById(R.id.localSettingsScroll).scrollTo(0, 0);

        final TextView pathView = settingsView.findViewById(R.id.startupDir);
        pathView.setText(Settings.getStartupPath() == null ? ctx.getText(R.string.permittedDirs) : Settings.getStartupPath().getPath());

        pathView.post(new Runnable() {
            public void run() {
                if (Settings.getSystemTextDirection() == Settings.TEXTDIR_RTL) {
                    ((HorizontalScrollView) pathView.getParent()).fullScroll(View.FOCUS_LEFT);
                } else
                    ((HorizontalScrollView) pathView.getParent()).fullScroll(View.FOCUS_RIGHT);
            }
        });

        Button fontBrowse = settingsView.findViewById(R.id.fontBrowse);
        fontBrowse.setTypeface(FontUtil.getSystemTypeface());
        fontBrowse.setText(FontUtil.getTypefaceName(FontUtil.getSystemPath()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) {
                ((RadioGroup) settingsView.findViewById(R.id.textDirGroup)).check(R.id.textDirLTR);
            } else
                ((RadioGroup) settingsView.findViewById(R.id.textDirGroup)).check(R.id.textDirRTL);
        }

        Button editorFontBrowse = settingsView.findViewById(R.id.editorFontBrowse);
        editorFontBrowse.setTypeface(FontUtil.getEditorTypeface());
        editorFontBrowse.setText(FontUtil.getTypefaceName(FontUtil.getEditorPath()));
        ((CheckBox)settingsView.findViewById(R.id.wordWrap)).setChecked(Settings.isWordWrap());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (Settings.getEditorTextDirection() == Settings.TEXTDIR_LTR) {
                ((RadioGroup) settingsView.findViewById(R.id.editorTextDirGroup)).check(R.id.editorTextDirLTR);
            } else
                ((RadioGroup) settingsView.findViewById(R.id.editorTextDirGroup)).check(R.id.editorTextDirRTL);
        }

        if (Settings.getActionOnBack() == Settings.AOB_PARENT) {
            ((RadioGroup) settingsView.findViewById(R.id.aobGroup)).check(R.id.aobParent);
        } else
            ((RadioGroup) settingsView.findViewById(R.id.aobGroup)).check(R.id.aobClose);

        ((CheckBox) settingsView.findViewById(R.id.enableRoot)).setChecked(Settings.isEnableRoot());

        Spinner editorFontSize = settingsView.findViewById(R.id.editorFontSize);
        editorFontSize.setSelection(FontUtil.getEditorSize() - 8);
    }

    private void resetLocalSettings(TxtSettings txtSettings) {
        Button fontBrowse = settingsView.findViewById(R.id.localFontBrowse);
        fontBrowse.setText(txtSettings.typeface.isEmpty() ? ctx.getText(R.string.useglobal)
                    : FontUtil.getTypefaceName(txtSettings.typeface));
        fontBrowse.setTypeface(FontUtil.getTypefaceFromPath(txtSettings.typeface, FontUtil.getEditorTypeface()));

        switch(txtSettings.wordWrap) {
            case 0:
                ((RadioGroup)settingsView.findViewById(R.id.localWordWrapGroup)).check(R.id.localWordWrapOff);
                break;
            case 1:
                ((RadioGroup)settingsView.findViewById(R.id.localWordWrapGroup)).check(R.id.localWordWrapOn);
                break;
            default:
                ((RadioGroup)settingsView.findViewById(R.id.localWordWrapGroup)).check(R.id.localWordWrapGlobal);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            switch (txtSettings.textDirection) {
                case Settings.TEXTDIR_LTR:
                    ((RadioGroup) settingsView.findViewById(R.id.localTextDirGroup)).check(R.id.localTextDirLTR);
                    break;
                case Settings.TEXTDIR_RTL:
                    ((RadioGroup) settingsView.findViewById(R.id.localTextDirGroup)).check(R.id.localTextDirRTL);
                    break;
                default:
                    ((RadioGroup) settingsView.findViewById(R.id.localTextDirGroup)).check(R.id.localTextDirGlobal);
            }
        }

        Spinner localFontSize = settingsView.findViewById(R.id.localFontSize);
        localFontSize.setSelection((txtSettings.pointSize < 0) ? 0 : txtSettings.pointSize - 7);
    }

    public void setStartupDirectory(AndPath newDirectory) {
        if ((Settings.getStartupPath() == null && newDirectory == null) || (Settings.getStartupPath() != null && Settings.getStartupPath().equals(newDirectory)))
            return;

        tempSettings.startupDir = newDirectory;
        final TextView pathView = settingsView.findViewById(R.id.startupDir);
        pathView.setText(newDirectory == null ? ctx.getText(R.string.permittedDirs) : newDirectory.getPath());

        pathView.post(new Runnable() {
            public void run() {
                if (Settings.getSystemTextDirection() == Settings.TEXTDIR_RTL) {
                    ((HorizontalScrollView)pathView.getParent()).fullScroll(View.FOCUS_LEFT);
                } else
                    ((HorizontalScrollView)pathView.getParent()).fullScroll(View.FOCUS_RIGHT);
            }
        });
    }

    public void setSystemTypeface(String typefacePath) {
        Typeface typeface = FontUtil.getTypefaceFromPath(typefacePath, FontUtil.getDefault());
        tempSettings.typefacePath = typefacePath;
        Button fontButton = settingsView.findViewById(R.id.fontBrowse);
        fontButton.setText(FontUtil.getTypefaceName(typefacePath));
        fontButton.setTypeface(typeface);
    }

    public void setEditorTypeface(String typefacePath) {
        Typeface typeface = FontUtil.getTypefaceFromPath(typefacePath, FontUtil.getEditorTypeface());
        tempSettings.editorTypefacePath = typefacePath;
        Button fontButton = settingsView.findViewById(R.id.editorFontBrowse);
        fontButton.setText(FontUtil.getTypefaceName(typefacePath));
        fontButton.setTypeface(typeface);
    }

    public void setLocalTypeface(String typefacePath) {
        Typeface typeface = FontUtil.getTypefaceFromPath(typefacePath, FontUtil.getEditorTypeface());
        localTempSettings.typefacePath = typefacePath;
        localTempSettings.editorTypefacePath = typefacePath;
        Button fontButton = settingsView.findViewById(R.id.localFontBrowse);
        fontButton.setText(typefacePath.isEmpty() ? ctx.getText(R.string.useglobal)
                : FontUtil.getTypefaceName(typefacePath));
        fontButton.setTypeface(typeface);
    }

    private void applySettings() {
        int textDirection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textDirection = ((RadioGroup) settingsView.findViewById(R.id.textDirGroup))
                    .getCheckedRadioButtonId() == R.id.textDirRTL ? Settings.TEXTDIR_RTL : Settings.TEXTDIR_LTR;
            if (Settings.getSystemTextDirection() != textDirection) {
                Settings.setSystemTextDirection(textDirection);
                TextView pathView = settingsView.findViewById(R.id.startupDir);
                pathView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                HorizontalScrollView.LayoutParams lp = (HorizontalScrollView.LayoutParams) pathView.getLayoutParams();
                if (Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) {
                    pathView.setTextDirection(View.TEXT_DIRECTION_LTR);
                    lp.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                } else {
                    pathView.setTextDirection(View.TEXT_DIRECTION_RTL);
                    lp.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                }
                pathView.setLayoutParams(lp);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textDirection = ((RadioGroup) settingsView.findViewById(R.id.editorTextDirGroup))
                    .getCheckedRadioButtonId() == R.id.editorTextDirRTL ? Settings.TEXTDIR_RTL : Settings.TEXTDIR_LTR;
            if (Settings.getEditorTextDirection() != textDirection)
                Settings.setEditorTextDirection(textDirection);
        }

        Settings.setActionOnBack(((RadioGroup) settingsView.findViewById(R.id.aobGroup))
                .getCheckedRadioButtonId() == R.id.aobParent ? Settings.AOB_PARENT : Settings.AOB_CLOSE);

        Settings.setEnableRoot(((CheckBox) settingsView.findViewById(R.id.enableRoot)).isChecked());

        Typeface tf = FontUtil.getTypefaceFromPath(tempSettings.typefacePath, FontUtil.getSystemTypeface());
        if (!FontUtil.getSystemTypeface().equals(tf)) {
            FontUtil.setSystemTypeface(tempSettings.typefacePath);
            FontUtil.applyFont(FontUtil.getSystemTypeface(), settingsView,
                    settingsView.findViewById(R.id.editorFontBrowse), settingsView.findViewById(R.id.fontBrowse));
        }

        tf = FontUtil.getTypefaceFromPath(tempSettings.editorTypefacePath, FontUtil.getEditorTypeface());
        if (!FontUtil.getEditorTypeface().equals(tf))
            FontUtil.setEditorTypeface(tempSettings.editorTypefacePath);

        FontUtil.setEditorSize(((Spinner)settingsView.findViewById(R.id.editorFontSize)).getSelectedItemPosition() + 8);

        if (tempSettings.startupDir == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Settings.setStartupPath(null);
        } else {
            Settings.setStartupPath(tempSettings.startupDir.getCurrent().exists()
                    ? tempSettings.startupDir : Settings.getStartupPath());
        }
        Settings.setWordWrap(((CheckBox)settingsView.findViewById(R.id.wordWrap)).isChecked());

        Settings.saveSettings(ctx);

        if (localTempSettings != null) {
            TxtSettings txtSettings = ((Editor)ctx.getFrag()).getSettings();
            txtSettings.typeface = localTempSettings.editorTypefacePath;

            int pointSize = ((Spinner)settingsView.findViewById(R.id.localFontSize)).getSelectedItemPosition();
            txtSettings.pointSize = (pointSize == 0) ? -1 : pointSize + 7;

            switch(((RadioGroup)settingsView.findViewById(R.id.localWordWrapGroup)).getCheckedRadioButtonId()) {
                case R.id.localWordWrapOn:
                    txtSettings.wordWrap = 1;
                    break;
                case R.id.localWordWrapOff:
                    txtSettings.wordWrap = 0;
                    break;
                default:
                    txtSettings.wordWrap = -1;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                switch (((RadioGroup) settingsView.findViewById(R.id.localTextDirGroup)).getCheckedRadioButtonId()) {
                    case R.id.localTextDirLTR:
                        txtSettings.textDirection = Settings.TEXTDIR_LTR;
                        break;
                    case R.id.localTextDirRTL:
                        txtSettings.textDirection = Settings.TEXTDIR_RTL;
                        break;
                    default:
                        txtSettings.textDirection = -1;
                }
            } else
                txtSettings.textDirection = Settings.TEXTDIR_LTR;
        }

        ((SettingsApplicable)ctx.getFrag()).applySettings();
    }

    public void setState(Bundle savedInstanceState) {
        open = true;
        settingsView.setVisibility(View.VISIBLE);

        tempSettings = new TempSettings();
        if (ctx.getState() == TEditActivity.STATE_TEXT)
            localTempSettings = new TempSettings();

        ((TransitionDrawable)settingsView.getBackground()).startTransition(0);

        int panel = savedInstanceState.getInt("TEdit.settingsWindow.panel", 0);
        panel = (panel > 1) ? 1 : (panel < 0) ? 0 : panel;
        if (ctx.getState() == TEditActivity.STATE_TEXT) {
            settingsView.findViewById(R.id.localButton).setVisibility(View.VISIBLE);
        } else {
            if (panel == 1)
                panel = 0;
            settingsView.findViewById(R.id.localButton).setVisibility(View.INVISIBLE);
        }
        settingsAnimator.getInAnimation().setDuration(0);
        settingsAnimator.getOutAnimation().setDuration(0);
        settingsAnimator.setDisplayedChild(panel);

        AndPath startupDir;
        try {
            startupDir = AndPath.fromJson(ctx,
                    savedInstanceState.getString("TEdit.settingsWindow.startupDir", ""));
        } catch (Exception e) {
            startupDir = null;
        }
        setStartupDirectory(startupDir);
        setSystemTypeface(savedInstanceState.getString("TEdit.settingsWindow.typeface", FontUtil.DEFAULT_PATH));

        Spinner editorFontSize = settingsView.findViewById(R.id.editorFontSize);
        editorFontSize.setSelection(savedInstanceState.getInt("TEdit.settingsWindow.editorFontSize", 15));

        int textDirection = savedInstanceState.getInt("TEdit.settingsWindow.textDirection", Settings.TEXTDIR_LTR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (textDirection == Settings.TEXTDIR_LTR) {
                ((RadioGroup) settingsView.findViewById(R.id.textDirGroup)).check(R.id.textDirLTR);
            } else
                ((RadioGroup) settingsView.findViewById(R.id.textDirGroup)).check(R.id.textDirRTL);
        }
        setEditorTypeface(savedInstanceState.getString("TEdit.settingsWindow.editorTypeface", FontUtil.DEFAULT_PATH));
        ((CheckBox)settingsView.findViewById(R.id.wordWrap))
                .setChecked(savedInstanceState.getBoolean("TEdit.settingsWindow.wordWrap", true));
        textDirection = savedInstanceState.getInt("TEdit.settingsWindow.editorTextDirection", Settings.TEXTDIR_LTR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (textDirection == Settings.TEXTDIR_LTR) {
                ((RadioGroup) settingsView.findViewById(R.id.editorTextDirGroup)).check(R.id.editorTextDirLTR);
            } else
                ((RadioGroup) settingsView.findViewById(R.id.editorTextDirGroup)).check(R.id.editorTextDirRTL);
        }

        if (localTempSettings == null)
            return;

        setLocalTypeface(savedInstanceState.getString("TEdit.settingsWindow.local.typeface", ""));

        Spinner localFontSize = settingsView.findViewById(R.id.localFontSize);
        localFontSize.setSelection(savedInstanceState.getInt("TEdit.settingsWindow.localFontSize", 15));

        switch(savedInstanceState.getInt("TEdit.settingsWindow.local.wordWrap", -1)) {
            case 0:
                ((RadioGroup)settingsView.findViewById(R.id.localWordWrapGroup)).check(R.id.localWordWrapOff);
                break;
            case 1:
                ((RadioGroup)settingsView.findViewById(R.id.localWordWrapGroup)).check(R.id.localWordWrapOn);
                break;
            default:
                ((RadioGroup)settingsView.findViewById(R.id.localWordWrapGroup)).check(R.id.localWordWrapGlobal);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            switch (savedInstanceState.getInt("TEdit.settingsWindow.local.textDirection", -1)) {
                case Settings.TEXTDIR_LTR:
                    ((RadioGroup) settingsView.findViewById(R.id.localTextDirGroup)).check(R.id.localTextDirLTR);
                    break;
                case Settings.TEXTDIR_RTL:
                    ((RadioGroup) settingsView.findViewById(R.id.localTextDirGroup)).check(R.id.localTextDirRTL);
                    break;
                default:
                    ((RadioGroup) settingsView.findViewById(R.id.localTextDirGroup)).check(R.id.localTextDirGlobal);
            }
        }
    }

    public void saveState(Bundle outState) {
        if (!isOpen())
            return;

        outState.putInt("TEdit.settingsWindow.panel", settingsAnimator.getDisplayedChild());

        if (tempSettings.startupDir != null)
            outState.putString("TEdit.settingsWindow.startupDir", tempSettings.startupDir.toJson());
        outState.putString("TEdit.settingsWindow.typeface", tempSettings.typefacePath);
        int textDirection = ((RadioGroup)settingsView.findViewById(R.id.textDirGroup))
                .getCheckedRadioButtonId() == R.id.textDirRTL ? Settings.TEXTDIR_RTL : Settings.TEXTDIR_LTR;
        outState.putInt("TEdit.settingsWindow.textDirection", textDirection);
        outState.putString("TEdit.settingsWindow.editorTypeface", tempSettings.editorTypefacePath);
        outState.putInt("TEdit.settingsWindow.editorFontSize", ((Spinner)settingsView.findViewById(R.id.editorFontSize)).getSelectedItemPosition() + 8);
        outState.putBoolean("TEdit.settingsWindow.wordWrap", ((CheckBox)settingsView.findViewById(R.id.wordWrap))
                .isChecked());
        textDirection = ((RadioGroup)settingsView.findViewById(R.id.editorTextDirGroup))
                .getCheckedRadioButtonId() == R.id.editorTextDirRTL ? Settings.TEXTDIR_RTL : Settings.TEXTDIR_LTR;
        outState.putInt("TEdit.settingsWindow.editorTextDirection", textDirection);

        if (localTempSettings == null)
            return;

        outState.putString("TEdit.settingsWindow.local.typeface", localTempSettings.editorTypefacePath);
        outState.putInt("TEdit.settingsWindow.localFontSize", ((Spinner)settingsView.findViewById(R.id.localFontSize)).getSelectedItemPosition() + 8);
        switch(((RadioGroup)settingsView.findViewById(R.id.localWordWrapGroup)).getCheckedRadioButtonId()) {
            case R.id.localWordWrapOn:
                outState.putInt("TEdit.settingsWindow.local.wordWrap", 1);
                break;
            case R.id.localWordWrapOff:
                outState.putInt("TEdit.settingsWindow.local.wordWrap", 0);
                break;
            default:
                outState.putInt("TEdit.settingsWindow.local.wordWrap", -1);
        }
        switch(((RadioGroup)settingsView.findViewById(R.id.localTextDirGroup)).getCheckedRadioButtonId()) {
            case R.id.localTextDirLTR:
                outState.putInt("TEdit.settingsWindow.local.textDirection", Settings.TEXTDIR_LTR);
                break;
            case R.id.localTextDirRTL:
                outState.putInt("TEdit.settingsWindow.local.textDirection", Settings.TEXTDIR_RTL);
                break;
            default:
                outState.putInt("TEdit.settingsWindow.local.textDirection", -1);
        }
    }

    private class TempSettings {
        public AndPath startupDir = Settings.getStartupPath();
        public String typefacePath = FontUtil.getSystemPath();

        public String editorTypefacePath = FontUtil.getEditorPath();

        private TempSettings() {
        }

        private TempSettings(TxtSettings txtSettings) {
            if (txtSettings == null)
                return;

            typefacePath = txtSettings.typeface;
            editorTypefacePath = txtSettings.typeface;
        }
    }
}
