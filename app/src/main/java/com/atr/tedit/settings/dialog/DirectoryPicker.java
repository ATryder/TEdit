package com.atr.tedit.settings.dialog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;
import com.atr.tedit.dialog.ErrorMessage;
import com.atr.tedit.dialog.VolumePicker;
import com.atr.tedit.file.AndPath;

import com.atr.tedit.dialog.TDialog;
import com.atr.tedit.file.FilePath;
import com.atr.tedit.file.descriptor.AndFile;
import com.atr.tedit.settings.Settings;
import com.atr.tedit.util.AndFileFilter;
import com.atr.tedit.util.FontUtil;
import com.atr.tedit.utilitybar.state.BrowserState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DirectoryPicker extends TDialog {
    public static final String TAG = "DirectoryPicker";

    private AndPath currentPath;

    private TextView pathView;
    private ListView listView;

    public static DirectoryPicker newInstance(AndPath currentDirectory) {
        Bundle bundle = new Bundle();
        if (currentDirectory != null)
            bundle.putString("TEdit.directoryPicker.currentPath", currentDirectory.toJson());

        DirectoryPicker dp = new DirectoryPicker();
        dp.setArguments(bundle);

        return dp;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        fillParent = true;

        String json = savedInstanceState == null ? getArguments().getString("TEdit.directoryPicker.currentPath", "")
                : savedInstanceState.getString("TEdit.directoryPicker.currentPath", "");
        if (json.isEmpty()) {
            currentPath = Settings.getStartupPath();
        } else {
            try {
                currentPath = AndPath.fromJson((TEditActivity)getContext(), json);
            } catch (Exception e) {
                currentPath = Settings.getStartupPath();
                Log.w("TEdit Directory Picker", "Unable to parse current directory: " + e.getMessage());
            }
        }

        pathView = new TextView(new ContextThemeWrapper(getContext(), theme));
        pathView.setSingleLine(true);
        HorizontalScrollView hsv = new HorizontalScrollView(new ContextThemeWrapper(getContext(), theme));
        hsv.addView(pathView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            pathView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            HorizontalScrollView.LayoutParams hlp = new HorizontalScrollView.LayoutParams(HorizontalScrollView.LayoutParams.WRAP_CONTENT,
                    HorizontalScrollView.LayoutParams.WRAP_CONTENT);
            if (Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) {
                pathView.setTextDirection(View.TEXT_DIRECTION_LTR);
                hlp.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            } else {
                pathView.setTextDirection(View.TEXT_DIRECTION_RTL);
                hlp.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            }
            pathView.setLayoutParams(hlp);
        }

        pathView.setTypeface(FontUtil.getSystemTypeface());
        listView = new ListView(new ContextThemeWrapper(getContext(), theme));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    if (i == 0) {
                        launchVolumePicker();
                        return;
                    }

                    if (i == 1) {
                        if (currentPath.moveToParent() == null)
                            return;
                        populateBrowser();
                        return;
                    }
                } else if (i == 0) {
                    if (currentPath == null) {
                        ((TEditActivity)getContext()).launchDirPermissionIntent();
                    } else {
                        if (currentPath.moveToParent() == null) {
                            populatePermittedDirs();
                            return;
                        }
                        populateBrowser();
                    }
                    return;
                }

                AndFile file = (AndFile)listView.getAdapter().getItem(i);
                if (!file.exists()) {
                    ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                            getString(R.string.missing_dir));
                    em.show(((TEditActivity)getContext()).getSupportFragmentManager(), "dialog");

                    return;
                }

                if (currentPath == null) {
                    currentPath = AndPath.fromAndFile(file);
                } else if (!currentPath.moveToChild(file)) {
                    ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                            getString(R.string.missing_dir));
                    em.show(((TEditActivity)getContext()).getSupportFragmentManager(), "dialog");
                    return;
                }
                populateBrowser();
            }
        });

        LinearLayout viewLayout = new LinearLayout(new ContextThemeWrapper(getContext(), theme));
        viewLayout.setOrientation(LinearLayout.VERTICAL);
        viewLayout.addView(hsv);
        viewLayout.addView(listView);

        setIcon(R.drawable.tedit_logo_brown);
        setTitle(R.string.directoryPicker);
        setView(viewLayout);
        setNegativeButton(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setPositiveButton(R.string.okay, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TEditActivity)getContext()).getSettingsWindow().setStartupDirectory(currentPath);
                dismiss();
            }
        });

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentPath == null) {
            populatePermittedDirs();
        } else
            populateBrowser();
    }

    private void populateBrowser() {
        if (currentPath == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                populatePermittedDirs();
                return;
            } else
                currentPath = new FilePath(AndFile.createDescriptor(Environment.getExternalStorageDirectory()));
        }

        if (!currentPath.getCurrent().exists()) {
            while(currentPath.moveToParent() != null && !currentPath.getCurrent().exists())
                continue;

            if (!currentPath.getCurrent().exists()) {
                ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                        getString(R.string.missing_dir));
                em.show(((TEditActivity)getContext()).getSupportFragmentManager(), "dialog");
                return;
            }
        }

        pathView.setText(currentPath.getPath());
        pathView.post(new Runnable() {
            public void run() {
                if (Settings.getSystemTextDirection() == Settings.TEXTDIR_RTL) {
                    ((HorizontalScrollView)pathView.getParent()).fullScroll(View.FOCUS_LEFT);
                } else
                    ((HorizontalScrollView)pathView.getParent()).fullScroll(View.FOCUS_RIGHT);
            }
        });

        AndFile[] contents = currentPath.listFiles(new AndFileFilter() {
            public boolean accept(AndFile file) {
                return file.isDirectory();
            }
        });
        Arrays.sort(contents, new Comparator<AndFile>() {
            @Override
            public int compare(final AndFile o1, final AndFile o2) {
                return o1.getPath().compareToIgnoreCase(o2.getPath());
            }
        });

        List<AndFile> listContents = new ArrayList<>(contents.length
                + ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) ? 2 : 1));
        listContents.add(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
            listContents.add(null);
        listContents.addAll(Arrays.asList(contents));

        listView.setAdapter(new ArrayAdapter<AndFile>(getContext(),
                (Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                R.layout.browser_row : R.layout.browser_row_rtl, listContents) {

            @Override
            public int getCount() { return super.getCount(); }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                View row = view;
                ImageView iv;
                TextView tv;
                if (row == null) {
                    row = ((Activity) getContext()).getLayoutInflater().inflate((Settings.getSystemTextDirection()
                                    == Settings.TEXTDIR_LTR) ? R.layout.browser_row : R.layout.browser_row_rtl,
                            parent, false);
                    iv = row.findViewById(R.id.dirIcon);
                    tv = row.findViewById(R.id.dirText);

                    tv.setTypeface(FontUtil.getSystemTypeface());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        tv.setTextDirection((Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                                View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);
                    }
                } else {
                    iv = row.findViewById(R.id.dirIcon);
                    tv = row.findViewById(R.id.dirText);
                }
                AndFile item = getItem(position);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    switch (position) {
                        case 0:
                            iv.setImageResource(R.drawable.drives_focused);
                            tv.setText(getString(R.string.changevolume));
                            break;
                        case 1:
                            iv.setImageResource(R.drawable.dir_parent_focused);
                            tv.setText("..");
                            break;
                        default:
                            iv.setImageResource(R.drawable.dir_focused);
                            tv.setText(item.getName());
                    }
                } else if (position == 0) {
                    iv.setImageResource(R.drawable.dir_parent_focused);
                    tv.setText("..");
                } else {
                    iv.setImageResource(R.drawable.dir_focused);
                    tv.setText(item.getName());
                }

                return row;
            }
        });
    }

    public void populatePermittedDirs() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            if (currentPath == null) {
                currentPath = new FilePath(AndFile.createDescriptor(Environment.getExternalStorageDirectory()));
            }
            populateBrowser();
            return;
        }

        currentPath = null;
        pathView.setText(getString(R.string.permittedDirs));
        pathView.post(new Runnable() {
            public void run() {
                if (Settings.getSystemTextDirection() == Settings.TEXTDIR_RTL) {
                    ((HorizontalScrollView)pathView.getParent()).fullScroll(View.FOCUS_LEFT);
                } else
                    ((HorizontalScrollView)pathView.getParent()).fullScroll(View.FOCUS_RIGHT);
            }
        });

        Uri[] uris = ((TEditActivity)getContext()).getPermittedUris();
        AndFile[] dirs = new AndFile[uris.length + 1];
        for (int i = 1; i < dirs.length; i++) {
            dirs[i] = AndFile.createDescriptor(DocumentFile.fromTreeUri(getContext(), uris[i - 1]), uris[i - 1]);
        }

        listView.setAdapter(new ArrayAdapter<AndFile>(getContext(),
                (Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                        R.layout.browser_row : R.layout.browser_row_rtl, dirs) {

            @Override
            public int getCount() { return super.getCount(); }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                View row = view;
                ImageView iv;
                TextView tv;
                if (row == null) {
                    row = ((Activity) getContext()).getLayoutInflater().inflate((Settings.getSystemTextDirection()
                                    == Settings.TEXTDIR_LTR) ? R.layout.browser_row : R.layout.browser_row_rtl,
                            parent, false);
                    iv = row.findViewById(R.id.dirIcon);
                    tv = row.findViewById(R.id.dirText);

                    tv.setTypeface(FontUtil.getSystemTypeface());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        tv.setTextDirection((Settings.getSystemTextDirection() == Settings.TEXTDIR_LTR) ?
                                View.TEXT_DIRECTION_LTR : View.TEXT_DIRECTION_RTL);
                    }
                } else {
                    iv = row.findViewById(R.id.dirIcon);
                    tv = row.findViewById(R.id.dirText);
                }

                if (position == 0) {
                    iv.setImageResource(R.drawable.dir_new_focused);
                    tv.setText(getContext().getText(R.string.newPermittedDir));
                } else {
                    iv.setImageResource(R.drawable.dir_focused);
                    tv.setText(getItem(position).getName());
                }

                return row;
            }
        });
    }

    public void launchVolumePicker() {
        launchVolumePicker(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void launchVolumePicker(boolean promptSD) {
        TEditActivity ctx = (TEditActivity)getContext();
        if (!promptSD) {
            VolumePicker vp = VolumePicker.newInstance(currentPath.getRoot().getPathIdentifier(), TAG);
            vp.show(ctx.getSupportFragmentManager(), "VolumePicker");
            return;
        }

        boolean cardPresent = ContextCompat.getExternalFilesDirs(getContext(), "external").length > 1;
        Uri[] volumes = ctx.getPermittedUris();
        for (int i = 0; i < volumes.length; i++) {
            if (!AndFile.createDescriptor(DocumentFile.fromTreeUri(ctx, volumes[i]), volumes[i]).exists()) {
                volumes = new Uri[0];
                break;
            }
        }
        if (volumes.length > 0 || !cardPresent) {
            VolumePicker vp = VolumePicker.newInstance(currentPath.getRoot().getPathIdentifier(), TAG);
            vp.show(ctx.getSupportFragmentManager(), "VolumePicker");
            return;
        }

        BrowserState.LaunchSDCardIntent lsd = new BrowserState.LaunchSDCardIntent();
        lsd.show(ctx.getSupportFragmentManager(), "SDCardIntentDialog");
    }

    public void setVolume(AndFile volume) {
        if (currentPath != null && currentPath.getRoot().getPathIdentifier().equals(volume.getPathIdentifier()))
            return;

        if (!volume.exists()) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.missing_dir));
            em.show(((TEditActivity)getContext()).getSupportFragmentManager(), "dialog");
            return;
        }

        currentPath = AndPath.fromAndFile(volume);
        populateBrowser();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (currentPath != null)
            outState.putString("TEdit.directoryPicker.currentPath", currentPath.toJson());
    }
}
