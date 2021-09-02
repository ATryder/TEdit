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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DocumentDescriptor extends AndFile<DocumentFile> {
    private Uri treeUri;

    protected DocumentDescriptor(DocumentFile file) {
        super(file);
    }

    protected DocumentDescriptor(DocumentFile file, Uri treeUri) {
        super(file);
        this.treeUri = treeUri;
    }

    public Uri getTreeUri() {
        return treeUri;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public DocumentDescriptor getParent() {
        DocumentFile parent = file.getParentFile();
        return parent == null ? null : AndFile.createDescriptor(parent);
    }

    @Override
    public String getPath() {
        return file.getUri().getPath();
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
    public AndFile<DocumentFile>[] listFiles() {
        DocumentFile[] files = file.listFiles();
        if (files == null)
            return new DocumentDescriptor[0];

        DocumentDescriptor[] fd = new DocumentDescriptor[files.length];
        for (int i = 0; i < files.length; i++) {
            fd[i] = AndFile.createDescriptor(files[i]);
        }

        return fd;
    }

    @Override
    public boolean rename(String name) {
        return file.renameTo(name);
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
        return ctx.getContentResolver().openOutputStream(file.getUri(), "rwt");
    }

    @Override
    public InputStream openInputStream(Context ctx) throws FileNotFoundException {
        return ctx.getContentResolver().openInputStream(file.getUri());
    }

    @Override
    public int getType() {
        return AndFile.TYPE_DOCFILE;
    }

    @Override
    public String getPathIdentifier() {
        return Integer.toString(getType()) + toString();
    }

    @Override
    public String getMIME() {
        return file.getType();
    }

    @Override
    public String toString() {
        return file.getUri().toString();
    }
}
