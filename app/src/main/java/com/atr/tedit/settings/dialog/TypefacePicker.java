package com.atr.tedit.settings.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.dialog.TDialog;
import com.atr.tedit.settings.Settings;
import com.atr.tedit.util.FontUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class TypefacePicker extends TDialog {
    public static final int TYPE_SYSTEM = 0;
    public static final int TYPE_EDITOR = 1;
    public static final int TYPE_LOCAL = 2;

    private int type;

    private String currentTypeface;
    private ListView listView;

    public static TypefacePicker newInstance(String currentTypefacePath, int type) {
        Bundle bundle = new Bundle();
        bundle.putInt("TEdit.typefacePicker.type", type);
        bundle.putString("TEdit.typefacePicker.currentTypefacePath", currentTypefacePath);

        TypefacePicker tp = new TypefacePicker();
        tp.setArguments(bundle);

        return tp;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            type = getArguments().getInt("TEdit.typefacePicker.type", TYPE_EDITOR);
            currentTypeface = getArguments().getString("TEdit.typefacePicker.currentTypefacePath",
                    (type == TYPE_LOCAL) ? "" : FontUtil.DEFAULT_PATH);
        } else {
            type = savedInstanceState.getInt("TEdit.typefacePicker.type", TYPE_EDITOR);
            currentTypeface = savedInstanceState.getString("TEdit.typefacePicker.currentTypefacePath",
                    (type == TYPE_LOCAL) ? "" : FontUtil.DEFAULT_PATH);
        }

        String[] systemFonts = FontUtil.getSystemFonts();
        Arrays.sort(systemFonts, String.CASE_INSENSITIVE_ORDER);
        ArrayList<String> fontList;
        if (type == TYPE_LOCAL) {
            fontList = new ArrayList<>(systemFonts.length + 4);
            fontList.add(getString(R.string.useglobal));
            fontList.addAll(FontUtil.getBuiltinPaths());
        } else {
            fontList = new ArrayList<>(systemFonts.length + 3);
            fontList.addAll(FontUtil.getBuiltinPaths());
        }

        StringBuilder sb = new StringBuilder();
        for (String ttf : systemFonts) {
            sb.append(FontUtil.SYSTEM_FONT_DIR);
            sb.append("/");
            sb.append(ttf);
            fontList.add(sb.toString());
            sb.delete(0, sb.length());
        }

        listView = new ListView(new ContextThemeWrapper(getContext(), theme));
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemsCanFocus(false);
        listView.setAdapter(new ArrayAdapter<String>(getContext(), (Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                R.layout.typeface_row : R.layout.typeface_row_rtl, fontList) {
            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                View row = view;
                TextView textView;

                if (row == null) {
                    row = ((Activity) getContext()).getLayoutInflater().inflate((Settings.getSystemTextDirection()
                                    == Settings.TEXTDIR_LTR) ? R.layout.typeface_row : R.layout.typeface_row_rtl,
                                    viewGroup, false);
                    textView = row.findViewById(R.id.typefaceName);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        textView.setTextDirection((Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                                View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);
                    }
                } else {
                    textView = row.findViewById(R.id.typefaceName);
                }

                String path = getItem(i);
                if (type == TYPE_LOCAL && i == 0) {
                    textView.setTypeface(FontUtil.getEditorTypeface());
                    textView.setText(path);
                } else {
                    textView.setTypeface(FontUtil.getTypefaceFromPath(path, FontUtil.getDefault()));
                    textView.setText(FontUtil.getTypefaceName(path));
                }

                return row;
            }
        });

        int selection = currentTypeface.isEmpty() ? 0 : fontList.indexOf(currentTypeface);
        listView.setItemChecked(selection >= 0 ? selection : 0, true);

        setIcon(R.drawable.tedit_logo_brown);
        setTitle(R.string.typefacePicker);
        setView(listView);
        setNegativeButton(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setPositiveButton(R.string.okay, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTypeface = getSelectedItem();
                switch(type) {
                    case TYPE_SYSTEM:
                        ((TEditActivity)getContext()).getSettingsWindow().setSystemTypeface(currentTypeface);
                        break;
                    case TYPE_LOCAL:
                        ((TEditActivity)getContext()).getSettingsWindow().setLocalTypeface(currentTypeface);
                        break;
                    default:
                        ((TEditActivity)getContext()).getSettingsWindow().setEditorTypeface(currentTypeface);
                        break;
                }
                dismiss();
            }
        });

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("TEdit.typefacePicker.type", type);
        currentTypeface = getSelectedItem();
        outState.putString("TEdit.typefacePicker.currentTypefacePath", currentTypeface);
    }

    private String getSelectedItem() {
        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
        for (int i = 0; i < checkedItems.size(); i++) {
            if (!checkedItems.valueAt(i))
                continue;

            int position = checkedItems.keyAt(i);
            return (type == TYPE_LOCAL && position == 0) ? "" : (String)listView.getAdapter().getItem(position);
        }

        return currentTypeface;
    }
}
