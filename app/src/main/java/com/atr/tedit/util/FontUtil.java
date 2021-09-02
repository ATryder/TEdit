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

import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.atr.tedit.R;
import com.atr.tedit.TEditActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class FontUtil {
    public static final String DEFAULT_PATH = "montserratalternates_regular";
    public static final String SYSTEM_FONT_DIR = "/system/fonts";

    private static HashMap<String, SoftReference<Typeface>> cache = new HashMap<String, SoftReference<Typeface>>();
    private static final LinkedHashMap<String, Integer> builtin = new LinkedHashMap<String, Integer>();
    private static final String[] builtinNames = {
                                                "Android 7",
                                                "Ballpoint Print",
                                                "Bebedera",
                                                "Cantarell Bold",
                                                "Cantarell Bold Oblique",
                                                "Cantarell Oblique",
                                                "Cantarell Regular",
                                                "Chomsky",
                                                "Dancing Script Bold",
                                                "Dancing Script Regular",
                                                "Feltpen",
                                                "Gidole Regular",
                                                "Jellee",
                                                "Metropolis Regular",
                                                "Montserrat Alternates Regular",
                                                "Oxanium Bold",
                                                "Oxanium Extra Bold",
                                                "Oxanium Extra Light",
                                                "Oxanium Light",
                                                "Oxanium Medium",
                                                "Oxanium Regular",
                                                "Oxanium Semi-Bold",
                                                "Precious",
                                                "Rawline Bold",
                                                "Rawline Bold Italic",
                                                "Rawline Light",
                                                "Rawline Light Italic",
                                                "Rawline Regular",
                                                "Rawline Regular Italic",
                                                "Unique",
                                                "Xolonium Bold",
                                                "Xolonium Regular"
                                                };
    private static Set<String> builtinKeySet;

    private static TEditActivity ctx;

    private static Typeface system;
    private static Typeface editor;
    private static String systemPath;
    private static String editorPath;

    private static int editorSize = 15;

    public static void init(TEditActivity context) {
        ctx = context;

        builtin.put("android_7", R.font.android_7);
        builtin.put("ballpointprint", R.font.ballpointprint);
        builtin.put("bebedera", R.font.bebedera);
        builtin.put("cantarell_bold", R.font.cantarell_bold);
        builtin.put("cantarell_boldoblique", R.font.cantarell_boldoblique);
        builtin.put("cantarell_oblique", R.font.cantarell_oblique);
        builtin.put("cantarell_regular", R.font.cantarell_regular);
        builtin.put("chomsky", R.font.chomsky);
        builtin.put("dancingscript_bold", R.font.dancingscript_bold);
        builtin.put("dancingscript_regular", R.font.dancingscript_regular);
        builtin.put("feltpen", R.font.feltpen);
        builtin.put("gidole_regular", R.font.gidole_regular);
        builtin.put("jellee_roman", R.font.jellee_roman);
        builtin.put("metropolis_regular", R.font.metropolis_regular);
        builtin.put("montserratalternates_regular", R.font.montserratalternates_regular);
        builtin.put("oxanium_bold", R.font.oxanium_bold);
        builtin.put("oxanium_extrabold", R.font.oxanium_extrabold);
        builtin.put("oxanium_extralight", R.font.oxanium_extralight);
        builtin.put("oxanium_light", R.font.oxanium_light);
        builtin.put("oxanium_medium", R.font.oxanium_medium);
        builtin.put("oxanium_regular", R.font.oxanium_regular);
        builtin.put("oxanium_semibold", R.font.oxanium_semibold);
        builtin.put("precious", R.font.precious);
        builtin.put("rawline_bold", R.font.rawline_bold);
        builtin.put("rawline_bolditalic", R.font.rawline_bolditalic);
        builtin.put("rawline_light", R.font.rawline_light);
        builtin.put("rawline_lightitalic", R.font.rawline_lightitalic);
        builtin.put("rawline_regular", R.font.rawline_regular);
        builtin.put("rawline_regularitalic", R.font.rawline_regularitalic);
        builtin.put("unique", R.font.unique);
        builtin.put("xolonium_bold", R.font.xolonium_bold);
        builtin.put("xolonium_regular", R.font.xolonium_regular);
        builtinKeySet = builtin.keySet();
    }

    public static void applyFont(Typeface font, View view, View... exclude) {
        for (View v : exclude) {
            if (v.equals(view))
                return;
        }

        if (view instanceof TextView) {
            ((TextView)view).setTypeface(font);
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup)view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyFont(font, vg.getChildAt(i), exclude);
            }
        }
    }

    public static Set<String> getBuiltinPaths() {
        return builtinKeySet;
    }

    public static String[] getSystemFonts() {
        File fontDir = new File(SYSTEM_FONT_DIR);
        if (!fontDir.exists())
            return new String[0];

        return fontDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".ttf") || s.endsWith(".otf");
            }
        });
    }

    public static void setSystemTypeface(String path) {
        Typeface tf = getTypefaceFromPath(path, getSystemTypeface());
        if (tf.equals(getSystemTypeface()))
            return;

        system = tf;
        systemPath = path;
    }

    public static Typeface getDefault() {
        return system;
    }

    public static Typeface getSystemTypeface() {
        return system;
    }

    public static void setEditorTypeface(String path) {
        Typeface tf = getTypefaceFromPath(path, getEditorTypeface());
        if (tf.equals(getEditorTypeface()))
            return;

        editor = tf;
        editorPath = path;
    }

    public static Typeface getEditorTypeface() {
        return editor;
    }

    public static Typeface getTitleTypeface() { return getTypefaceFromPath("bebedera", Typeface.DEFAULT); }

    public static String getEditorPath() {
        return editorPath;
    }

    public static String getSystemPath() {
        return systemPath;
    }

    public static void setEditorSize(int pointSize) {
        editorSize = (pointSize < 8) ? 8 : (pointSize > 72) ? 72 : pointSize;
    }

    public static int getEditorSize() { return editorSize; }

    public static String[] getEditorPointSizes() {
        String[] sizes = new String[63];
        for (int n = 8; n < 71; n++) {
            sizes[n - 8] = Integer.toString(n);
        }

        return sizes;
    }

    public static String[] getLocalEditorPointSizes() {
        String[] sizes = new String[64];
        sizes[0] = ctx.getString(R.string.useglobal);
        for (int n = 8; n < 71; n++) {
            sizes[n - 7] = Integer.toString(n);
        }

        return sizes;
    }

    public static Typeface getTypefaceFromPath(String path) {
        return getTypefaceFromPath(path, Typeface.DEFAULT);
    }

    public static Typeface getTypefaceFromPath(String path, Typeface defaultTypeface) {
        Typeface tf;
        SoftReference<Typeface> sr = cache.get(path);

        if (sr == null) {
            if (builtin.containsKey(path)) {
                tf = ResourcesCompat.getFont(ctx, builtin.get(path));
                cache.put(path, new SoftReference<>(tf));

                return tf;
            }

            File file = new File(path);
            if (!file.exists() || !file.canRead()) {
                Log.w("TEdit Font", "The specified font could not be read: " + path);
                return defaultTypeface == null ? Typeface.DEFAULT : defaultTypeface;
            }

            tf = Typeface.createFromFile(file);
            if (tf == null) {
                Log.w("TEdit Font", "The specified font could not be read: " + path);
                return defaultTypeface == null ? Typeface.DEFAULT : defaultTypeface;
            }
            cache.put(path, new SoftReference<>(tf));

            return tf;
        }

        tf = sr.get();
        if (tf == null) {
            if (builtin.containsKey(path)) {
                tf = ResourcesCompat.getFont(ctx, builtin.get(path));
                cache.put(path, new SoftReference<>(tf));

                return tf;
            }

            File file = new File(path);
            if (!file.exists() || !file.canRead()) {
                Log.w("TEdit Font", "The specified font could not be read: " + path);
                return defaultTypeface == null ? Typeface.DEFAULT : defaultTypeface;
            }

            tf = Typeface.createFromFile(file);
            if (tf == null) {
                Log.w("TEdit Font", "The specified font could not be read: " + path);
                return defaultTypeface == null ? Typeface.DEFAULT : defaultTypeface;
            }
            cache.put(path, new SoftReference<>(tf));

            return tf;
        }

        return tf;
    }

    public static String getTypefaceName(String path) {
        int idx = 0;
        for (String p : builtinKeySet) {
            if (p.equals(path))
                return builtinNames[idx];
            idx++;
        }

        idx = path.lastIndexOf("/");
        return (idx >= 0 && idx < path.length() - 1) ? path.substring(idx + 1, path.length()) : path;
    }
}
