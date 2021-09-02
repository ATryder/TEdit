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
package com.atr.tedit.file.descriptor;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.atr.tedit.TEditActivity;
import com.atr.tedit.util.AndFileFilter;
import com.atr.tedit.util.DataAccessUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Unifies the interfaces for {@link java.io.File} and
 * {@link android.support.v4.provider.DocumentFile}.
 *
 * @param <T> Either {@link java.io.File} or {@link android.support.v4.provider.DocumentFile}.
 *
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */
public abstract class AndFile<T> {
    public static final int TYPE_FILE = 0;
    public static final int TYPE_DOCFILE = 1;

    protected final T file;

    protected AndFile(T file) {
        this.file = file;
    }

    /**
     * Creates a new instance of {@lnk FileDescriptor}.
     *
     * @param file The {@link java.io.File} to encapsulate.
     * @return A new {@link FileDescriptor} representing the
     * supplied {@link java.io.File}.
     */
    public static FileDescriptor createDescriptor(File file) {
        return new FileDescriptor(file);
    }

    /**
     * Creates a new instance of {@link DocumentDescriptor}.
     *
     * @param file The {@link android.support.v4.provider.DocumentFile}
     * to encapsulate.
     * @return A new {@link DocumentDescriptor} representing the
     * supplied {@link android.support.v4.provider.DocumentFile}.
     */
    public static DocumentDescriptor createDescriptor(DocumentFile file) {
        return new DocumentDescriptor(file);
    }

    /**
     * Creates a new instance of {@link DocumentDescriptor}.
     *
     * @param file The {@link android.support.v4.provider.DocumentFile}
     * to encapsulate.
     * @param treeUri The Uri returned from {@link TEditActivity#getPermittedUris()}
     * @return A new {@link DocumentDescriptor} representing the
     * supplied {@link android.support.v4.provider.DocumentFile}.
     */
    public static DocumentDescriptor createDescriptor(DocumentFile file, Uri treeUri) {
        return new DocumentDescriptor(file, treeUri);
    }

    /**
     * Pass a string created from {@link #getPathIdentifier()} to create a new
     * <code>AndFile</code> with the same {@link android.support.v4.provider.DocumentFile} or
     * {@link java.io.File} as the one in which {@link #getPathIdentifier()}
     * was called.
     * <br>
     * <br>
     * {@link #createDescriptorFromTree(String, Context)} or
     * {@link #createDescriptor(String, Context)} can be called on
     * <code>AndFile</code>s of type <code>AndFile.TYPE_FILE</code>,
     * but this must be called for <code>AndFile.TYPE_DOCFILE</code>
     * where the underlying {@link android.support.v4.provider.DocumentFile}
     * describes a single Uri.
     *
     * @param path A String created with {@link #getPathIdentifier()}.
     * @param ctx The appliaction {@link android.content.Context}.
     * @return A new <code>AndFile</code> that is a duplicate of the
     * <code>AndFile</code> in which {@link #getPathIdentifier()} was called.
     *
     * @see #getPathIdentifier()
     * @see #createDescriptorFromTree(String, Context)
     */
    public static AndFile createDescriptor(String path, Context ctx) {
        int type = -1;
        try {
            type = Integer.parseInt(Character.toString(path.charAt(0)));
        } catch (NumberFormatException e) {
            type = -1;
        }

        AndFile file;
        switch(type) {
            case AndFile.TYPE_FILE:
                file = AndFile.createDescriptor(new File(path.substring(1)));
                break;
            case AndFile.TYPE_DOCFILE:
                file = AndFile.createDescriptor(DocumentFile.fromSingleUri(ctx, Uri.parse(path.substring(1))));
                break;
            default:
                file = null;
        }

        return file;
    }

    /**
     * Pass a string created from {@link #getPathIdentifier()} to create a new
     * <code>AndFile</code> with the same {@link android.support.v4.provider.DocumentFile} or
     * {@link java.io.File} as the one in which {@link #getPathIdentifier()}
     * was called.
     * <br>
     * <br>
     * {@link #createDescriptorFromTree(String, Context)} or
     * {@link #createDescriptor(String, Context)} can be called on
     * <code>AndFile</code>s of type <code>AndFile.TYPE_FILE</code>,
     * but this must be called for <code>AndFile.TYPE_DOCFILE</code>
     * where the underlying {@link android.support.v4.provider.DocumentFile}
     * describes a tree Uri.
     *
     * @param path A String created with {@link #getPathIdentifier()}.
     * @param ctx The appliaction {@link android.content.Context}.
     * @return A new <code>AndFile</code> that is a duplicate of the
     * <code>AndFile</code> in which {@link #getPathIdentifier()} was called.
     *
     * @see #getPathIdentifier()
     * @see #createDescriptor(String, Context)
     */
    public static AndFile createDescriptorFromTree(String path, Context ctx) {
        int type = -1;
        try {
            type = Integer.parseInt(Character.toString(path.charAt(0)));
        } catch (NumberFormatException e) {
            type = -1;
        }

        AndFile file;
        switch(type) {
            case AndFile.TYPE_FILE:
                file = AndFile.createDescriptor(new File(path.substring(1)));
                break;
            case AndFile.TYPE_DOCFILE:
                file = AndFile.createDescriptor(DocumentFile.fromTreeUri(ctx, Uri.parse(path.substring(1))));
                break;
            default:
                file = null;
        }

        return file;
    }

    /**
     * Returns the underlying {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File
     * @see android.support.v4.provider.DocumentFile
     */
    public T getFile() {
        return file;
    }

    /**
     * Exposes the getName() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#getName();
     * @see android.support.v4.provider.DocumentFile#getName()
     */
    public abstract String getName();

    /**
     * Exposes the getParentFile() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#getParentFile()
     * @see android.support.v4.provider.DocumentFile#getParentFile()
     */
    public abstract AndFile<T> getParent();

    /**
     * Exposes the getPath() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#getPath()
     * @see android.support.v4.provider.DocumentFile#getUri()#getPath()
     */
    public abstract String getPath();

    /**
     * Exposes the isDirectory() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#isDirectory()
     * @see android.support.v4.provider.DocumentFile#isDirectory()
     */
    public abstract boolean isDirectory();

    /**
     * Exposes the isFile() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#isFile()
     * @see android.support.v4.provider.DocumentFile#isFile()
     */
    public abstract boolean isFile();

    /**
     * Exposes the lastModified() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#lastModified()
     * @see android.support.v4.provider.DocumentFile#lastModified()
     */
    public abstract long lastModified();

    /**
     * Exposes the length() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#length()
     * @see android.support.v4.provider.DocumentFile#length()
     */
    public abstract long length();

    /**
     * Exposes the listFiles() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#listFiles()
     * @see android.support.v4.provider.DocumentFile#listFiles()
     */
    public abstract AndFile<T>[] listFiles();

    /**
     * Exposes the listFiles() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @param filter A {@link com.atr.tedit.util.AndFileFilter} to filter
     * the results.
     * @return
     *
     * @see java.io.File#listFiles()
     * @see android.support.v4.provider.DocumentFile#listFiles()
     */
    public AndFile[] listFiles(AndFileFilter filter) {
        AndFile[] files = listFiles();
        LinkedList<AndFile<T>> list = new LinkedList<>();
        for (AndFile file : files) {
            if (filter.accept(file))
                list.add(file);
        }

        return list.toArray(new AndFile[list.size()]);
    }

    /**
     * Returns a matrix of AndFiles where index [0] is and array of
     * directories and index [1] an array of files.
     *
     * @return A matrix of AndFiles where index [0] is an array of
     * directories and index [1] an array of files.
     *
     * @see java.io.File#listFiles()
     * @see android.support.v4.provider.DocumentFile#listFiles()
     */
    public AndFile[][] listFilesAndDirs() {
        int numDirs = 0;
        int numFiles = 0;
        AndFile[] files = listFiles();
        AndFile[][] fad = new AndFile[2][files.length];
        for (AndFile file : files) {
            if (file.isDirectory()) {
                fad[0][numDirs++] = file;
            } else if (DataAccessUtil.mimeSupported(file.getMIME())) {
                fad[1][numFiles++] = file;
            } else {
                String filename = file.getName();
                if (!DataAccessUtil.getFileNameMime(filename).isEmpty() || !DataAccessUtil.hasExtension(filename)) {
                    fad[1][numFiles++] = file;
                }
            }
        }

        fad[0] = Arrays.copyOf(fad[0], numDirs);
        fad[1] = Arrays.copyOf(fad[1], numFiles);

        return fad;
    }

    /**
     * Exposes the renameTo() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#renameTo(File)
     * @see android.support.v4.provider.DocumentFile#renameTo(String)
     */
    public abstract boolean rename(String name);

    /**
     * Exposes the delete() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#delete()
     * @see android.support.v4.provider.DocumentFile#delete()
     */
    public abstract boolean delete();

    /**
     * Exposes the exists) method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#exists()
     * @see android.support.v4.provider.DocumentFile#exists()
     */
    public abstract boolean exists();

    /**
     * Exposes the canRead() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#canRead()
     * @see android.support.v4.provider.DocumentFile#canRead()
     */
    public abstract boolean canRead();

    /**
     * Exposes the canWrite() method of the encapsulated
     * {@link java.io.File} or
     * {@link android.support.v4.provider.DocumentFile}.
     *
     * @return
     *
     * @see java.io.File#canWrite()
     * @see android.support.v4.provider.DocumentFile#canWrite()
     */
    public abstract boolean canWrite();

    /**
     * Opens an {@link java.io.OutputStream} to the file.
     *
     * @param ctx The application {@link android.content.Context}
     * @return An {@link java.io.OutputStream} to the file.
     * @throws IOException
     */
    public abstract OutputStream openOutputStream(Context ctx) throws IOException;

    /**
     * Opens an {@link java.io.InputStream} to the file.
     *
     * @param ctx The application {@link android.content.Context}
     * @return An {@link java.io.InputStream} to the file.
     * @throws FileNotFoundException
     */
    public abstract InputStream openInputStream(Context ctx) throws FileNotFoundException;

    /**
     * The type of <code>AndFile</code>.
     *
     * @return {@link AndFile#TYPE_FILE} if this <code>AndFile</code>
     * encapsulates a {java.io.File} or {@link AndFile#TYPE_DOCFILE}
     * if this <code>AndFile</code> encapsulates a
     * {@link android.support.v4.provider.DocumentFile}.
     */
    public abstract int getType();

    /**
     * Gets the mime type of the file.
     *
     * @return The mime type of the file, such as text/plain,
     * or an empty string if no MIME type could be determined.
     */
    public abstract String getMIME();

    /**
     * A <code>String</code> that can be used to identify this
     * <code>AndFile</code> as being type <code>AndFile.TYPE_FILE</code>
     * or <code>AndFile.TYPE_DOCFILE</code>. Can be used with
     * {@link #createDescriptor(String, Context)} and
     * {@link #createDescriptorFromTree(String, Context)}
     *
     * @return A String that can be used to create a
     * new <code>AndFile</code> from this one.
     *
     * @see #createDescriptor(String, Context)
     * @see #createDescriptorFromTree(String, Context)
     */
    public abstract String getPathIdentifier();

    /**
     *
     * @return
     *
     * @see java.io.File#toString()
     * @see android.support.v4.provider.DocumentFile#getUri()#toString()
     */
    @Override
    public String toString() {
        return file.toString();
    }
}
