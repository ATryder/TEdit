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
package com.atr.tedit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.atr.tedit.util.ErrorMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */

public class Browser extends ListFragment {
    private static final String invalidChars = "\'/\\*?:|\"<>%\n";

    private static final String[] extensions = new String[] {
            ".txt",
            ".html",
            ".htm",
            ".php",
            ".xml",
            ".java",
            ".c",
            ".cpp",
            ".h",
            ".py"};

    public static final int TYPE_OPEN = 0;
    public static final int TYPE_SAVE = 1;

    private int type;
    private TEditActivity ctx;

    private File currentDir;

    private int numDirs;
    private int numFiles;

    private long keyToSave;

    protected static Browser newInstance(String directory, long key) {
        Bundle bundle = new Bundle();
        bundle.putInt("TEditBrowser.type", TYPE_SAVE);
        bundle.putString("TEditBrowser.currentDir", directory);
        bundle.putLong("TEditBrowser.keyToSave", key);

        Browser browser = new Browser();
        browser.setArguments(bundle);

        return browser;
    }

    protected static Browser newInstance(String directory) {
        Bundle bundle = new Bundle();
        bundle.putInt("TEditBrowser.type", TYPE_OPEN);
        bundle.putString("TEditBrowser.currentDir", directory);

        Browser browser = new Browser();
        browser.setArguments(bundle);

        return browser;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ctx = (TEditActivity)context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            type = bundle.getInt("TEditBrowser.type", TYPE_OPEN);
            currentDir = new File(bundle.getString("TEditBrowser.currentDir",
                    type == TYPE_OPEN ? ctx.getCurrentPath().getPath()
                    : ctx.getSavePath().getPath()));
            if (type == TYPE_SAVE)
                keyToSave = bundle.getLong("TEditBrowser.keyToSave", -1);

            return;
        }

        String mediaState = Environment.getExternalStorageState();
        type = savedInstanceState.getInt("TEditBrowser.type", TYPE_OPEN);
        if (Environment.MEDIA_MOUNTED.equals(mediaState)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState)) {
            currentDir = new File(savedInstanceState.getString("TEditBrowser.currentDir",
                    Environment.getExternalStorageDirectory().getPath()));
        } else {
            currentDir = new File(savedInstanceState.getString("TEditBrowser.currentDir",
                    "/"));
        }
        if (type == TYPE_SAVE)
            keyToSave = savedInstanceState.getLong("TEditBrowser.keyToSave", -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (type == TYPE_OPEN)
            return inflater.inflate(R.layout.browser, container, false);

        return inflater.inflate(R.layout.browser_save, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("TEditBrowser.currentDir", currentDir.getPath());
        outState.putInt("TEditBrowser.type", type);
        outState.putLong("TEditBrowser.keyToSave", keyToSave);
    }

    @Override
    public void onResume() {
        super.onResume();
        getListView().setEnabled(true);
        populateBrowser();
        if (type == TYPE_SAVE)
            getView().findViewById(R.id.filename).setEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (type == TYPE_SAVE) {
            ctx.setSavePath(currentDir);
            getView().findViewById(R.id.filename).setEnabled(false);
        }
        getListView().setEnabled(false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        if (info.position >= numDirs)
            menu.add(0, 0, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            String filename = ((TextView)((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).targetView
                    .findViewById(R.id.dirText)).getText().toString();
            File file = new File(currentDir, filename);
            if (file.isDirectory()) {
                Log.w("TEdit", "Attempt to delete directory: " + file.getPath());
                return true;
            }
            if (!file.delete()) {
                ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                        getString(R.string.error_delete));
                em.show(ctx.getSupportFragmentManager(), "dialog");
            } else {
                Parcelable state = getListView().onSaveInstanceState();
                populateBrowser();
                getListView().onRestoreInstanceState(state);
                Toast.makeText(ctx, getString(R.string.filedeleted), Toast.LENGTH_SHORT).show();
            }

            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        //String filename = ((TextView)((ViewGroup)view).getChildAt(1)).getText().toString();
        String filename = ((TextView)view.findViewById(R.id.dirText)).getText().toString();
        File file = new File(currentDir, filename);

        if (position < numDirs) {
            if (file.exists()) {
                currentDir = file;
                if (type != TYPE_SAVE)
                    ctx.setCurrentPath(currentDir);
                populateBrowser();
                return;
            }

            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.missing_dir));
            em.show(ctx.getSupportFragmentManager(), "dialog");

            return;
        }

        if (type == TYPE_SAVE) {
            //((EditText)((ViewGroup)((ViewGroup)this.getView()).getChildAt(0)).getChildAt(0)).setText(filename);
            ((EditText)getView().findViewById(R.id.savelayout).findViewById(R.id.filename)).setText(filename);
            return;
        }

        if (!file.exists()) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.missing_file));
            em.show(ctx.getSupportFragmentManager(), "dialog");

            return;
        }

        //open file
        String contents = null;
        try {
            contents = readFile(file);
        } catch (IOException e) {
            contents = null;
            Log.e("TEdit.Browser", "Unable to read file " + file.getPath() + ": "
                    + e.getMessage());
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_readfile));
            em.show(ctx.getSupportFragmentManager(), "dialog");

            return;
        } finally {
            if (contents != null)
                ctx.newDocument(file.getPath(), contents);
        }
    }

    protected void upDir() {
        File parent = currentDir.getParentFile();
        if (parent == null) {
            return;
        }

        currentDir = parent;
        if (type != TYPE_SAVE)
            ctx.setCurrentPath(currentDir);
        populateBrowser();
    }

    public String getEnteredFilename() {
        return ((EditText)getView().findViewById(R.id.savelayout).findViewById(R.id.filename)).getText().toString();
    }

    public File getCurrentDir() {
        return currentDir;
    }

    private void populateBrowser() {
        List<Map<String, Object>> items;
        String mediaState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(mediaState)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState)) {
            if (!currentDir.exists()) {
                do {
                    currentDir = currentDir.getParentFile();
                } while (currentDir != null && !currentDir.exists());

                if (currentDir == null)
                    currentDir = new File(ctx.getRootPath().getPath());

                ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                        getString(R.string.missing_dir));
                em.show(ctx.getSupportFragmentManager(), "dialog");

                if (!currentDir.exists())
                    return;
            }
            if (type == TYPE_OPEN) {
                ((TextView)getView().findViewById(R.id.browsepath)).setText(currentDir.getPath());
            } else
                ((TextView)getView().findViewById(R.id.savebrowsepath)).setText(currentDir.getPath());

            File[] dirList = currentDir.listFiles(new DirFilter());
            File[] fileList = currentDir.listFiles(new TxtFilter());
            if (dirList == null)
                dirList = new File[0];
            if (fileList == null)
                fileList = new File[0];
            numDirs = dirList.length;
            numFiles = fileList.length;
            Logger.getLogger(getClass().getName()).log(Level.INFO, currentDir.getPath());

            //sort directories
            if (numDirs > 1) {
                String[] tmpDirs = new String[dirList.length];
                for (int i = 0; i < tmpDirs.length; i++)
                    tmpDirs[i] = dirList[i].getPath();
                Arrays.sort(tmpDirs, String.CASE_INSENSITIVE_ORDER);

                for (int i = 0; i < dirList.length; i++)
                    dirList[i] = new File(tmpDirs[i]);
            }

            //sort files
            if (numFiles > 1) {
                String[] tmpFiles = new String[fileList.length];
                for (int i = 0; i < tmpFiles.length; i++)
                    tmpFiles[i] = fileList[i].getPath();
                Arrays.sort(tmpFiles, String.CASE_INSENSITIVE_ORDER);

                for (int i = 0; i < fileList.length; i++)
                    fileList[i] = new File(tmpFiles[i]);
            }

            items = new ArrayList<>(fileList.length + dirList.length);
            for (File f : dirList) {
                Map<String, Object> item = new HashMap<>(2);
                item.put("item", f.getName());
                item.put("icon", R.drawable.dir);
                items.add(item);
            }

            for (File f : fileList) {
                Map<String, Object> item = new HashMap<>(2);
                item.put("item", f.getName());
                item.put("icon", R.drawable.doc);
                items.add(item);
            }
        } else {
            items = new ArrayList<>(0);
        }

        SimpleAdapter adapter = new SimpleAdapter(ctx, items, R.layout.browser_row,
                new String[] {"item", "icon"}, new int[] {R.id.dirText, R.id.dirIcon});
        setListAdapter(adapter);
    }

    private static boolean isValidName (String name) {
        if (name.length() == 0) {
            return false;
        } else {
            for (int count = 0; count < invalidChars.length(); count++) {
                if (name.indexOf(invalidChars.charAt(count)) > -1) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean hasExtension(String name) {
        for (String ext : extensions) {
            if (name.endsWith(ext))
                return true;
        }

        return false;
    }

    protected boolean saveFile(String filename, final String body) {
        if (!currentDir.exists()) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.missing_dir));
            em.show(ctx.getSupportFragmentManager(), "dialog");

            return false;
        }

        if (!isValidName(filename)) {
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_invalidname));
            em.show(ctx.getSupportFragmentManager(), "dialog");

            return false;
        }

        if (!hasExtension(filename.toLowerCase())) {
            if (filename.endsWith(".")) {
                filename += "txt";
            } else
                filename += ".txt";
        }

        final File file = new File(currentDir, filename);

        String mediaState = Environment.getExternalStorageState();
        if (!(Environment.MEDIA_MOUNTED.equals(mediaState)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState))) {
            ErrorMessage em = ErrorMessage.getInstance(ctx.getString(R.string.alert),
                    ctx.getString(R.string.error_unmounted));
            em.show(ctx.getSupportFragmentManager(), "dialog");

            return false;
        } else if (!file.getPath().startsWith(Environment.getExternalStorageDirectory().getPath())) {
            ErrorMessage em = ErrorMessage.getInstance(ctx.getString(R.string.alert),
                    ctx.getString(R.string.error_protectedpath));
            em.show(ctx.getSupportFragmentManager(), "dialog");

            return false;
        }

        if (file.exists()) {
            //prompt overwrite
            Bundle bundle = new Bundle();
            bundle.putString("Overwrite.filePath", file.getPath());
            bundle.putString("Overwrite.body", body);

            OverwriteDialog od = new OverwriteDialog();
            od.setArguments(bundle);
            od.show(ctx.getSupportFragmentManager(), "Overwrite");

            return false;
        }

        try {
            writeFile(file, body);
        } catch (IOException e) {
            Log.e("TEdit.Browser", "Unable to save file " + file.getPath() + ": "
                    + e.getMessage());
            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                    getString(R.string.error_writefile));
            em.show(ctx.getSupportFragmentManager(), "dialog");

            return false;
        }

        return true;
    }

    protected static void writeFile(File file, String body) throws IOException {
        Log.i("TEdit", "Writing to file: " + file.getPath());

        FileOutputStream fileOut = new FileOutputStream(file);
        PrintStream pStream = new PrintStream(fileOut);

        pStream.print(body);
        pStream.close();
        fileOut.close();
    }

    public static String readFile(File file) throws IOException {
        FileReader fReader = new FileReader(file);
        BufferedReader bReader = new BufferedReader(fReader);
        String contents = "";
        String newLine;

        while ((newLine = bReader.readLine()) != null) {
            contents += newLine;
            contents += "\n";
        }
        bReader.close();
        fReader.close();

        return contents;
    }

    private class DirFilter implements FileFilter {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    }

    private class TxtFilter implements FileFilter {
        public boolean accept(File file) {
            for (String s : extensions) {
                if (file.getName().toLowerCase().endsWith(s))
                    return true;
            }

            return false;
        }
    }

    public static class OverwriteDialog extends DialogFragment {
        private String filePath;
        private String body;
        private TEditActivity ctx;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.ctx = (TEditActivity)context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            if (savedInstanceState == null) {
                Bundle bundle = getArguments();
                filePath = bundle.getString("Overwrite.filePath");
                body = bundle.getString("Overwrite.body", "");
                String filename = new File(filePath).getName();
                builder.setTitle(getString(R.string.overwrite));
                builder.setMessage(getString(R.string.overwrite_message).replace("%s", filename))
                    .setPositiveButton(getActivity().getString(R.string.okay), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                            try {
                                writeFile(new File(filePath), body);
                                if (ctx.dbIsOpen()) {
                                    ctx.getDB().updateText(ctx.getLastTxt(), filePath, body);
                                    ctx.openDocument(ctx.getLastTxt());
                                }
                                Toast.makeText(ctx, getString(R.string.filesaved), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e("TEdit.Browser", "Unable to save file " + filePath + ": "
                                    + e.getMessage());
                                ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                                        getString(R.string.error_writefile));
                                em.show(getActivity().getSupportFragmentManager(), "dialog");
                            }
                        }
                    })
                    .setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
            } else {
                filePath = savedInstanceState.getString("Overwrite.filePath");
                body = savedInstanceState.getString("Overwrite.body");
                String filename = new File(filePath).getName();
                builder.setTitle(getString(R.string.overwrite));
                builder.setMessage(getString(R.string.overwrite_message).replace("%s", filename))
                    .setPositiveButton(getActivity().getString(R.string.okay), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                            try {
                                writeFile(new File(filePath), body);
                                if (ctx.dbIsOpen()) {
                                    ctx.getDB().updateText(ctx.getLastTxt(), filePath, body);
                                    ctx.openDocument(ctx.getLastTxt());
                                }
                                Toast.makeText(ctx, getString(R.string.filesaved), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e("TEdit.Browser", "Unable to save file " + filePath + ": "
                                        + e.getMessage());
                                ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                                        getString(R.string.error_writefile));
                                em.show(getActivity().getSupportFragmentManager(), "dialog");
                            }
                        }
                    })
                    .setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
            }

            return builder.create();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("Overwrite.filePath", filePath);
            outState.putString("Overwrite.body", body);
        }
    }

    public static class NewDirectory extends DialogFragment {
        private TEditActivity ctx;
        private String inDir;

        public static NewDirectory newInstance(String createIn) {
            NewDirectory nd = new NewDirectory();
            Bundle bundle = new Bundle();
            bundle.putString("TEdit.newdirectory", createIn);
            nd.setArguments(bundle);
            return nd;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.ctx = (TEditActivity)context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (savedInstanceState == null) {
                inDir = getArguments().getString("TEdit.newdirectory", "");
            } else
                inDir = savedInstanceState.getString("TEdit.newdirectory", "");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText et = new EditText(ctx);
            builder.setTitle(getString(R.string.newdirectory));
            builder.setMessage(getString(R.string.newdirmessage)).setView(et)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                }).setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                        String dirName = et.getText().toString();
                        if (!isValidName(dirName)) {
                            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                                    getString(R.string.error_invalidname));
                            em.show(ctx.getSupportFragmentManager(), "dialog");

                            return;
                        }

                        File dir = new File(inDir, dirName);
                        if (dir.exists()) {
                            ErrorMessage em = ErrorMessage.getInstance(getString(R.string.alert),
                                    getString(R.string.error_direxists));
                            em.show(ctx.getSupportFragmentManager(), "dialog");

                            return;
                        }

                        dir.mkdirs();
                        Toast.makeText(ctx, getString(R.string.dircreated), Toast.LENGTH_SHORT).show();
                        ((Browser)ctx.getFrag()).populateBrowser();
                    }
                });

            return builder.create();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            outState.putString("TEdit.newdirectory", inDir);
        }
    }
}
