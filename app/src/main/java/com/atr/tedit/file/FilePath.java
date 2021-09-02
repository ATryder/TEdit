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

import com.atr.tedit.file.descriptor.FileDescriptor;

import org.json.JSONArray;

/**
 * Manages paths to {@link FileDescriptor}s on disk.
 *
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */
public class FilePath extends AndPath<FileDescriptor> {
    public FilePath(FileDescriptor root) {
        super(root);
    }

    protected FilePath(FileDescriptor root, FileDescriptor current) {
        super(root);
        this.current = current;
    }

    @Override
    public FileDescriptor getParent() {
        return current.getParent();
    }

    @Override
    public FileDescriptor moveToParent() {
        if (current.getPath().equals(root.getPath()))
            return null;

        FileDescriptor parent = current.getParent();
        if (parent == null)
            return null;

        current = parent;
        return current;
    }

    @Override
    public boolean moveToChild(FileDescriptor child) {
        if (!child.getPath().startsWith(current.getPath()))
            return false;

        current = child;
        return true;
    }

    @Override
    public void moveToRoot() {
        current = root;
    }

    @Override
    public String getPath() {
        return current.getPath();
    }

    @Override
    public String toJson() {
        JSONArray jArr = new JSONArray();
        jArr.put(root.getType());
        jArr.put(root.getPath());
        jArr.put(current.getPath());

        return jArr.toString();
    }

    @Override
    public FilePath clone() {
        return  new FilePath(root, current);
    }
}
