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

import com.atr.tedit.util.DataAccessUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileDescriptor extends AndFile<File> {
    protected FileDescriptor(File file) {
        super(file);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public FileDescriptor getParent() {
        File parent = file.getParentFile();
        return parent == null ? null : AndFile.createDescriptor(parent);
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public AndFile<File>[] listFiles() {
        File[] files = file.listFiles();
        if (files == null)
            return new FileDescriptor[0];

        FileDescriptor[] fd = new FileDescriptor[files.length];
        for (int i = 0; i < files.length; i++) {
            fd[i] = AndFile.createDescriptor(files[i]);
        }

        return fd;
    }

    @Override
    public boolean rename(String name) {
        return file.renameTo(new File(name));
    }

    @Override
    public boolean delete() {
        return file.delete();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public boolean canRead() {
        return file.canRead();
    }

    @Override
    public boolean canWrite() {
        return file.canWrite();
    }

    @Override
    public OutputStream openOutputStream(Context ctx) throws IOException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
        } catch (IOException e) {
            if (os != null)
                os.close();
            throw(e);
        }

        return os;
    }

    @Override
    public InputStream openInputStream(Context ctx) throws FileNotFoundException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {

                }
            }
            throw(e);
        }

        return is;
    }

    @Override
    public int getType() {
        return AndFile.TYPE_FILE;
    }

    @Override
    public String getPathIdentifier() {
        return Integer.toString(getType()) + getPath();
    }

    @Override
    public String getMIME() {
        return DataAccessUtil.getFileMime(file);
    }
}
