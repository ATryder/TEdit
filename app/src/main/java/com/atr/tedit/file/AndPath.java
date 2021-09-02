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
package com.atr.tedit.file;


import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;

import com.atr.tedit.TEditActivity;
import com.atr.tedit.file.descriptor.AndFile;
import com.atr.tedit.file.descriptor.DocumentDescriptor;
import com.atr.tedit.file.descriptor.FileDescriptor;
import com.atr.tedit.util.AndFileFilter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Unifies disk path management for {@link java.io.File}s and
 * {@link android.support.v4.provider.DocumentFile}s. Java's
 * {@link java.io.File} provides its own means to keep track
 * of the path to a file on disk, but Android's
 * {@link android.support.v4.provider.DocumentFile} does not
 * so this class is used to provide a common means to provide
 * path tracking to both.
 * <br>
 * <br>
 * for {@link java.io.File}s this simply exposes pre-existing
 * methods, but for
 * {@link android.support.v4.provider.DocumentFile} an array
 * of {@link com.atr.tedit.file.descriptor.AndFile} is
 * maintained to keep track of the path to a particular
 * document. Methods are provided here to convert these
 * paths to strings for writing to disk and reading that
 * string back into a new AndPath.
 *
 * @param <T> Either
 * {@link com.atr.tedit.file.descriptor.FileDescriptor} or
 * {@link com.atr.tedit.file.descriptor.DocumentDescriptor}
 *
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */
public abstract class AndPath<T extends AndFile> implements Cloneable {
    protected final T root;
    protected T current;

    protected AndPath(T root) {
        this.root = root;
        this.current = root;
    }

    /**
     * The current file or folder.
     *
     * @return The current {@link com.atr.tedit.file.descriptor.AndFile}.
     */
    public T getCurrent() {
        return current;
    }

    /**
     * The root directory of this path.
     *
     * @return The root directory of this path.
     */
    public T getRoot() {
        return root;
    }

    /**
     * The parent of this file or folder.
     *
     * @return The parent of this file or folder. Null if
     * {@link #getCurrent()} == {@link #getRoot()}.
     */
    public abstract T getParent();

    /**
     * Sets the current directory to {@link #getParent()} if
     * {@link #getCurrent()} != {@link #getParent()}.
     *
     * @return The new current directory or null if no change
     * was made.
     */
    public abstract T moveToParent();

    /**
     * Moves {@link #getCurrent()} to a child of {@link #getCurrent()}.
     *
     * @param child The child of {@linke #getCurrent()} to move to.
     * @return True if the operation was successful.
     */
    public abstract boolean moveToChild(T child);

    /**
     * Moves {@link #getCurrent()} to {@link #getRoot()} and clears
     * the path.
     */
    public abstract void moveToRoot();

    /**
     * Convenience method to list the files from the current
     * directory.
     *
     * @return An array of {@link com.atr.tedit.file.descriptor.AndFile}
     * representing the files in this directory. If the current
     * {@link com.atr.tedit.file.descriptor.AndFile} is not a directory
     * this will return an empty array.
     *
     * @see com.atr.tedit.file.descriptor.AndFile#listFiles()
     */
    public AndFile[] listFiles() {
        if (current.isFile())
            return new AndFile[0];

        return current.listFiles();
    }

    /**
     * Returns a matrix of {@link com.atr.tedit.file.descriptor.AndFile}
     * where index [0] is and array of directories and index [1] an array of files.
     *
     * @return A matrix of {@link com.atr.tedit.file.descriptor.AndFile}
     * where index [0] is an array of directories and index [1] an array of files.
     *
     * @see java.io.File#listFiles()
     * @see android.support.v4.provider.DocumentFile#listFiles()
     */
    public AndFile[][] listFilesAndDirs() {
        if (current.isFile())
            return new AndFile[0][0];

        return current.listFilesAndDirs();
    }

    /**
     * Convenience method to list the files from the current
     * directory.
     *
     * @param filter A {@link com.atr.tedit.util.AndFileFilter} to filter
     * the results.
     * @return An array of {@link com.atr.tedit.file.descriptor.AndFile}
     * representing the files in this directory. If the current
     * {@link com.atr.tedit.file.descriptor.AndFile} is not a directory
     * this will return an empty array.
     *
     * @see com.atr.tedit.file.descriptor.AndFile#listFiles()
     */
    public AndFile[] listFiles(AndFileFilter filter) {
        if (current.isFile())
            return new AndFile[0];

        return current.listFiles(filter);
    }

    public abstract String getPath();

    public static AndPath fromAndFile(AndFile file) {
        if (file.getType() == AndFile.TYPE_FILE)
            return new FilePath((FileDescriptor)file);

        return new DocumentPath((DocumentDescriptor)file);
    }

    /**
     * Convert this <code>AndPath</code> to a String suitable for saving
     * to disk or otherwise transferring between <code>Intents</code>.
     *
     * @return A JSON encoded String representing this <code>AndPath</code>.
     */
    public abstract String toJson();

    /**
     * Parses a JSON econded String created with the {@link #toJson()} method
     * and returns a new <code>AndPath</code> that matches the <code>AndPath</code>
     * in which the JSON String was created.
     *
     * @param ctx Appliation {@link android.content.Context}.
     * @param json JSON encoded String created with {@link #toJson()}.
     * @return A new <code>AndPath</code> or null if the String could
     * not be parsed.
     * @throws JSONException
     * @throws FileNotFoundException Thrown if the root directory or any
     * subdirectory along the path does not exist.
     */
    public static AndPath fromJson(TEditActivity ctx, String json) throws JSONException, FileNotFoundException {
        JSONArray jArr = new JSONArray(json);
        if (jArr.length() < 3)
            return null;

        int type = jArr.getInt(0);
        String strRoot = jArr.getString(1);
        if (type == AndFile.TYPE_FILE) {
            File rootFile = new File(strRoot);
            if (!rootFile.exists())
                throw new FileNotFoundException("Root directory not found: " + strRoot);

            if (jArr.length() > 2) {
                FileDescriptor fdCurrent = AndFile.createDescriptor(new File(jArr.getString(2)));
                if (!fdCurrent.exists())
                    throw new FileNotFoundException("Directory not found: " + fdCurrent.getPath());
                FilePath filePath = new FilePath(AndFile.createDescriptor(rootFile), fdCurrent);
                return filePath;
            }

            return new FilePath(AndFile.createDescriptor(rootFile));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            DocumentFile dfRoot = DocumentFile.fromTreeUri(ctx, Uri.parse(strRoot));
            DocumentPath dp = new DocumentPath(AndFile.createDescriptor(dfRoot), ctx, jArr);
            return dp;
        }

        return null;
    }

    @Override
    public abstract AndPath<T> clone();
}
