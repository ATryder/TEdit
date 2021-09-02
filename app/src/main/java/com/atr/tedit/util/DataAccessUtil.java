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
package com.atr.tedit.util;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.atr.tedit.file.descriptor.AndFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;

public class DataAccessUtil {
    private static final String[] EXTENSIONS = new String[] {
            "txt",
            "text",
            "html",
            "htm",
            "php",
            "xml",
            "java",
            "c",
            "cpp",
            "h",
            "py",
            "ini",
            "cfg",
            "config",
            "bat",
            "map",
            "conf",
            "opt",
            "rmp",
            "nfo",
            "profile",
            "asp",
            "pl",
            "ph",
            "pm",
            "rb",
            "rbw",
            "rhtml",
            "rjs",
            "bsh",
            "sh",
            "css",
            "cs",
            "csv",
            "log",
            "lua",
            "md",
            "gd",
            "js",
            "torrent"};

    private static final String[] MIME = new String[] {
            "text/plain",
            "text/plain",
            "text/html",
            "text/html",
            "text/x-php",
            "text/xml",
            "text/x-java",
            "text/x-c",
            "text/x-c",
            "text/x-h",
            "text/x-python",
            "text/plain",
            "text/plain",
            "text/plain",
            "text/plain",
            "text/plain",
            "text/plain",
            "text/plain",
            "text/plain",
            "text/plain",
            "text/plain",
            "text/asp",
            "application/x-perl",
            "application/x-perl",
            "application/x-perl",
            "application/x-ruby",
            "application/x-ruby",
            "application/x-ruby",
            "application/x-ruby",
            "application/x-bsh",
            "application/x-sh",
            "text/css",
            "text/x-csharp",
            "text/csv",
            "text/plain",
            "text/x-lua",
            "text/plain",
            "text/plain",
            "application/x-javascript",
            "application/x-bittorrent"};

    /**
     * Retrieves the contents from data specified via a {@link android.net.Uri}.
     * The contents of the {@link android.net.Uri} must be text.
     *
     * @param context The application {@link android.content.Context}.
     * @param uri The {@link android.net.Uri} to retrieve data from.
     * @return A String containing the data read from the{@link android.net.Uri}
     * or null if a problem was encountered.
     */
    public static String getData(final Context context, final Uri uri) {
        String contents = null;
        AssetFileDescriptor afd;
        InputStreamReader isr = null;
        BufferedReader bReader = null;
        try {
            afd =  context.getContentResolver().openAssetFileDescriptor(uri, "r");
            isr = new InputStreamReader(afd.createInputStream());
            bReader = new BufferedReader(isr);
            contents = readStream(bReader);
        } catch (Exception e) {
            Log.w("TEdit Data Access", "Unable to access contents of Uri. " + e.getMessage());
            contents = null;
        } finally {
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException ioe) {

                }
            } else if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ioe) {

                }
            }

            return contents;
        }
    }

    /**
     * Attempts to find the file system path to the data specified
     * by the {@link android.net.Uri} and returns a
     * {@link java.io.File} pointing to the file at said location.
     * The returned file may not exist.
     *
     * @param context The application {@link android.content.Context}.
     * @param uri The {@link android.net.Uri}.
     * @return A {@link java.io.File} describing the file on disk the
     * {@link android.net.Uri} points to or null if the location on disk
     * could not be determined.
     *
     * @see #getData(Context, Uri)
     */
    public static File getDataFile(final Context context, final Uri uri) {
        File file = null;
        String dataPath = getDataPath(context, uri);

        if (dataPath == null) {
            if (uri.getLastPathSegment() != null) {
                String[] segs = uri.getLastPathSegment().split(":");
                dataPath = (segs.length > 1) ? segs[1] : segs[0];
                file = new File(dataPath);
            }
        } else
            file = new File(dataPath);

        return file;
    }

    /**
     * Attempts to find the file system path to the data specified
     * by the {@link android.net.Uri}.
     *
     * @param context The application {@link android.content.Context}.
     * @param uri The {@link android.net.Uri} in which to determine a
     * file system path.
     * @return A {@link java.lang.String} containing the file system
     * path to the data specified by the {@link android.net.Uri} or
     * null if the path could not be determined.
     */
    public static String getDataPath(final Context context, final Uri uri) {
        String path = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            path = getPostKitKatPath(uri, context);

        if (path == null) {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                path = getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme()))
                path = uri.getPath();
        }

        return path;
    }

    /**
     * Get the value of the data column for a {@link android.net.Uri}.
     *
     * @param context The application {@link android.content.Context}.
     * @param uri The {@link android.net.Uri} to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     *
     * @see #getDataPath(Context, Uri)
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.w("TEdit Data Access", "Unable to access data column from external application: " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Attempts to retrieve the file system path to the data specified
     * by the {@link android.net.Uri}.
     *
     * @param context The application {@link android.content.Context}.
     * @param uri The {@link android.net.Uri}
     * @return A {@link java.lang.String} containing the file system
     * path to the data specified by the {@link android.net.Uri} or
     * null if the path could not be determined.
     *
     * @see #getDataPath(Context, Uri)
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getPostKitKatPath(Uri uri, Context context) {
        if (!DocumentsContract.isDocumentUri(context, uri)) {
            return null;
        }

        if (isExternalStorageDocument(uri)) {
            //ExternalStorageProvider
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type) && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (split.length > 1) {
                    return Environment.getExternalStorageDirectory().getPath() + "/" + split[1];
                } else
                    return Environment.getExternalStorageDirectory().getPath();
            }
        } else if (isDownloadsDocument(uri)) {
            //DownloadsProvider
            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            return getDataColumn(context, contentUri, null, null);
        } else if (isMediaDocument(uri)) {
            //MediaProvider
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("image".equals(type) || "video".equals(type) || "audio".equals(type))
                return null;

            Uri contentUri = MediaStore.Files.getContentUri("external");

            final String selection = "_id=?";
            final String[] selectionArgs = new String[] {
                    split[1]
            };

            String path = getDataColumn(context, contentUri, selection, selectionArgs);
            if (path == null) {
                contentUri = MediaStore.Files.getContentUri("internal");
                path = getDataColumn(context, contentUri, selection, selectionArgs);
            }

            return path;
        }

        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String checkExt(String ext, String defaultExt) {
        for (int i = 0; i < EXTENSIONS.length; i++) {
            if (EXTENSIONS[i].equalsIgnoreCase(ext))
                return ext;
        }

        return defaultExt;
    }

    /**
     * Attempts to determine the supported MIME type of a {@link java.io.File}.
     *
     * @param file The {@link java.io.File} to determine a MIME type for.
     * @return A {@link java.lang.String} containing the MIME type in the format
     * such as "text/plain" or "text/html". If a MIME type could not be
     * determined an empty {@link java.lang.String} is returned.
     */
    public static String getFileMime(File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String mime = getFileMimePostO(file);

            if (mime != null && !mime.isEmpty())
                return mime;
        }

        return getFileNameMime(file.getName());
    }

    public static String getFileNameMime(String filename) {
        int p = filename.indexOf('.');
        if (p < 0 || p > filename.length() - 2)
            return "";

        String ext = filename.substring(p + 1);
        for (int i = 0; i < EXTENSIONS.length; i++) {
            if (EXTENSIONS[i].equalsIgnoreCase(ext))
                if (i >= MIME.length)
                    return "text/plain";
                return MIME[i];
        }

        return "";
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static String getFileMimePostO(File file) {
        String mime = "";
        try {
            mime = Files.probeContentType(file.toPath());
        } catch (Exception e) {
            mime = "";
            Log.w("TEdit MIME", "Error while determining MIME information for file " + file.getPath() + ": " + e.getMessage());
        } finally {
            return mime;
        }
    }

    /**
     * Tests if a filename contains a file extension.
     *
     * @param file The file to test for an extension.
     * @return True if the filename has an extension otherwise false.
     */
    public static boolean hasExtension(File file) {
        return hasExtension(file.getName());
    }

    /**
     * Tests if a filename contains a file extension.
     *
     * @param filename The filename to test for an extension.
     * @return True if the filename has an extension otherwise false.
     */
    public static boolean hasExtension(String filename) {
        return filename.indexOf(".") >= 0;
    }

    /**
     * Checks if a supplied mime type is supported.
     *
     * @param mime The MIME type to check such as text/plain.
     * @return True if the type is supported otherwise false.
     */
    public static boolean mimeSupported(String mime) {
        if (mime.startsWith("text/"))
            return true;

        for (String m : MIME) {
            if (mime.equalsIgnoreCase(m))
                return true;
        }

        return false;
    }

    public static boolean probablyBinaryFile(AndFile file, Context ctx) {
        InputStreamReader isr = null;
        InputStream is = null;
        boolean probBin = false;
        try {
            is = file.openInputStream(ctx);
            isr = new InputStreamReader(is);
            int i;
            int count = 0;

            while ((i = isr.read()) != -1 && count < 1024) {
                char c = (char)i;
                count ++;
                if ((c > (char)0 && c < (char)8) || (c > (char)13 && c < (char)26)) {
                    probBin = true;
                    break;
                }
            }
        } catch (Exception e) {

        } finally {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ioe) {

                }
            } else if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {

                }
            }

            return probBin;
        }
    }

    /**
     * Reads the text contents of a file. Returns null if an error ocurred.
     *
     * @param file the {@link java.io.File} to read.
     * @return The contents of the {@link java.io.File} as a {@link java.lang.String}.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readFile(File file) throws FileNotFoundException, IOException {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new FileInputStream(file));
        } catch (FileNotFoundException fnfe) {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ioe) {

                }
            }
            Log.e("Tedit Read Operation", "Error opening input stream " + file.getPath() + ": " + fnfe.getMessage());
            throw fnfe;
        }

        BufferedReader bReader = new BufferedReader(isr);
        String contents = null;
        try {
           contents = readStream(bReader);
        } catch (IOException e) {
            try {
                bReader.close();
            } catch (IOException ioe) {

            }
            Log.e("Tedit Read Operation", "Error reading file " + file.getPath() + ": " + e.getMessage());
            throw e;
        }

        try {
            bReader.close();
        } catch (IOException ioe) {

        }

        return contents;
    }

    /**
     * Reads the text contents of a file. Returns null if an error ocurred.
     *
     * @param file the {@link com.atr.tedit.file.descriptor.AndFile} to read.
     * @return The contents of the file as a {@link java.lang.String}.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readFile(AndFile file, Context ctx) throws FileNotFoundException, IOException {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(file.openInputStream(ctx));
        } catch (FileNotFoundException fnfe) {
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ioe) {

                }
            }
            Log.e("Tedit Read Operation", "Error opening input stream " + file.toString() + ": " + fnfe.getMessage());
            throw fnfe;
        }

        BufferedReader bReader = new BufferedReader(isr);
        String contents = null;
        try {
            contents = readStream(bReader);
        } catch (IOException e) {
            try {
                bReader.close();
            } catch (IOException ioe) {

            }
            Log.e("Tedit Read Operation", "Error reading file " + file.getPath() + ": " + e.getMessage());
            throw e;
        }

        try {
            bReader.close();
        } catch (IOException ioe) {

        }

        return contents;
    }

    /**
     * Reads from a {@link java.io.BufferedReader} and returns the contents as a {@link java.lang.String}.
     *
     * @param bReader The {@link java.io.BufferedReader} to read from.
     * @return A {@link java.lang.String} with the contents of the stream.
     * @throws IOException
     */
    public static String readStream(BufferedReader bReader) throws IOException {
        String contents = "";
        String newLine;

        while ((newLine = bReader.readLine()) != null) {
            contents += newLine;
            contents += "\n";
        }

        return contents;
    }

    /**
     * Writes the contents of <code>body</code> to <code>file</code>.
     *
     * @param file The {@link java.io.File} to write to.
     * @param body The {@link java.lang.String} to be written.
     * @throws IOException
     */
    public static void writeFile(File file, String body) throws IOException {
        Log.i("TEdit Write Operation", "Writing to file: " + file.getPath());

        FileOutputStream fileOut = null;
        PrintStream pStream = null;
        IOException exc = null;
        try{
            fileOut = new FileOutputStream(file);
            pStream = new PrintStream(fileOut);
            pStream.print(body);
        } catch (IOException ioe) {
            exc = ioe;
            Log.e("Tedit Write Operation", "Error writing to file: " + ioe.getMessage());
        } finally {
            if (pStream != null) {
                pStream.close();
            } else if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (Exception e) {

                }
            }

            if (exc != null)
                throw exc;
        }
    }

    /**
     * Writes the contents of <code>body</code> to <code>file</code>.
     *
     * @param file The {@link com.atr.tedit.file.descriptor.AndFile} to write to.
     * @param body The {@link java.lang.String} to be written.
     * @throws IOException
     */
    public static void writeFile(AndFile file, Context ctx, String body) throws IOException {
        Log.i("TEdit Write Operation", "Writing to file: " + file.getPath());

        OutputStream fileOut = null;
        PrintStream pStream = null;
        IOException exc = null;
        try{
            fileOut = file.openOutputStream(ctx);
            pStream = new PrintStream(fileOut);
            pStream.print(body);
        } catch (IOException ioe) {
            exc = ioe;
            Log.e("Tedit Write Operation", "Error writing to file: " + ioe.getMessage());
        } finally {
            if (pStream != null) {
                pStream.close();
            } else if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (Exception e) {

                }
            }

            if (exc != null)
                throw exc;
        }
    }
}
