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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.provider.DocumentFile;

import com.atr.tedit.file.descriptor.AndFile;
import com.atr.tedit.file.descriptor.DocumentDescriptor;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.util.LinkedList;

/**
 * Manages paths to {@link DocumentDescriptor}s on disk.
 *
 * @author Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */
public class DocumentPath extends AndPath<DocumentDescriptor> {
    private final LinkedList<DocumentDescriptor> path = new LinkedList<>();

    public DocumentPath(DocumentDescriptor root) {
        super(root);
    }

    private DocumentPath(DocumentDescriptor root, DocumentDescriptor current,
                         LinkedList<DocumentDescriptor> path) {
        super(root);
        this.current = current;
        this.path.addAll(path);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected DocumentPath(DocumentDescriptor root, Context ctx, JSONArray jArr) throws JSONException, FileNotFoundException {
        super(root);

        if (!root.exists())
            throw new FileNotFoundException("Root directory not found: " + root.toString());

        if (jArr.length() <= 3)
            return;

        DocumentFile currentDF = root.getFile();
        pathLoop: for (int i = jArr.length() - 2; i >= 3; i--) {
            String strNext = jArr.getString(i);
            DocumentFile[] files = currentDF.listFiles();
            for (DocumentFile df : files) {
                if (df.isDirectory() && df.getName().equals(strNext)) {
                    path.addFirst(AndFile.createDescriptor(df));
                    currentDF = df;
                    continue pathLoop;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.insert(0, strNext);
            sb.insert(0, "/");
            for (AndFile af : path) {
                sb.insert(0, af.getName());
                sb.insert(0, "/");
            }
            sb.insert(0, root.getName());
            sb.insert(0, "/");
            throw new FileNotFoundException("Directory not found: " + sb.toString());
        }

        String strCurrent = jArr.getString(2);
        DocumentFile[] files = currentDF.listFiles();
        current = null;
        for (DocumentFile df : files) {
            if (df.isDirectory() && df.getName().equals(strCurrent))
                current = AndFile.createDescriptor(df);
        }

        if (current == null)
            throw new FileNotFoundException("Directory not found: /" + root.getName() + "/" + strCurrent);
        path.add(root);
    }

    @Override
    public DocumentDescriptor getParent() {
        return path.isEmpty() ? null : path.getFirst();
    }

    @Override
    public DocumentDescriptor moveToParent() {
        if (path.isEmpty())
            return null;

        current = path.removeFirst();
        return current;
    }

    @Override
    public boolean moveToChild(DocumentDescriptor child) {
        path.addFirst(current);
        current = child;

        return true;
    }

    @Override
    public void moveToRoot() {
        current = root;
        path.clear();
    }

    @Override
    public String getPath() {
        StringBuilder sb = new StringBuilder();
        for (DocumentDescriptor file : path) {
            sb.insert(0, file.getName());
            sb.insert(0, '/');
        }
        sb.append('/');
        sb.append(current.getName());

        return sb.toString();
    }

    @Override
    public String toJson() {
        JSONArray jArr = new JSONArray();
        jArr.put(root.getType());
        jArr.put(root.toString());
        jArr.put(current.getName());
        for (DocumentDescriptor dd : path) {
            jArr.put(dd.getName());
        }

        return jArr.toString();
    }

    @Override
    public DocumentPath clone() {
        return new DocumentPath(root, current, path);
    }
}
