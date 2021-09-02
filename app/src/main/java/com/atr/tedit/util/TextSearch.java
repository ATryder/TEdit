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

import android.text.Editable;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

/**
 * The TextSearch class is used for finding and/or replacing instances of a search phrase
 * in an {@link android.text.Editable}.
 * <br>
 * <br>
 * It is important to note that a cache of search results will be created when calling
 * any of the search methods or the {@link #replaceAll(Editable, String)} method. If
 * another call to any of these methods is made using an {@link android.text.Editable}
 * that has text that differs from the one used in the previous search or replaceAll
 * call then {@link #clearSearchCache()} must first be called to clear the cached
 * results from the previous search.
 *
 * @author: Adam T. Ryder
 * <a href="https://www.inventati.org/1337gallery">https://www.inventati.org/1337gallery</a>
 */
public class TextSearch {
    private SoftReference<SearchResult[]> searchCache;

    private String searchPhrase = "";

    private boolean wholeWord = false;
    private boolean matchCase = false;

    /**
     * Clears the search cache.
     *
     * @see #getCache(Editable)
     * @see #setCache(Editable)
     */
    public void clearSearchCache() {
        searchCache = null;
    }

    /**
     * Sets the string of characters that will be located within an
     * {@link android.text.Editable}. Setting a new value here will
     * automatically clear the current search cache.
     *
     * @param searchPhrase The phrase to search for within the {@link android.text.Editable}
     */
    public void setSearchPhrase(String searchPhrase) {
        if (searchPhrase.equals(this.searchPhrase))
            return;
        this.searchPhrase = searchPhrase;
        clearSearchCache();
    }

    /**
     * Gets the currently set search phrase.
     *
     * @return The currently set search phrase.
     *
     * @see #setSearchPhrase(String)
     */
    public String getSearchPhrase() {
        return searchPhrase;
    }

    /**
     * Sets whether or not searches should include results in which individual
     * letters do not match the lowercase/uppercase state of their respective
     * couterparts in the search phrase. Setting a new value here will
     * automatically clear the current search cache.
     *
     * @param matchCase Set to true to force search results to match the
     * lowercase/uppercase state of the search phrase.
     */
    public void setMatchCase(boolean matchCase) {
        if (matchCase == this.matchCase)
            return;
        this.matchCase = matchCase;
        clearSearchCache();
    }

    /**
     * Whether or not searches are required to match the lowercase/uppercase
     * state of the search phrase.
     *
     * @return True if search results should match the lowercase/uppercase state
     * of the search phrase.
     *
     * @see #setMatchCase(boolean)
     */
    public boolean isMatchCase() {
        return matchCase;
    }

    /**
     * Sets whether or not search results can be contained within other larger words.
     * For example: When set to false the word "sand" would be included when searching
     * for "and" otherwise "sand" would not be included. Setting a new value here will
     * automatically clear the current search cache.
     *
     * @param wholeWord Set to true if search results should only include entire words.
     */
    public void setWholeWord(boolean wholeWord) {
        if (wholeWord == this.wholeWord)
            return;
        this.wholeWord = wholeWord;
        clearSearchCache();
    }

    /**
     * Whether or not search results should include instances of the search phrase
     * when it is contained within other larger words or sections of text.
     *
     * @return True if search results should contain only whole words.
     *
     * @see #setWholeWord(boolean)
     */
    public boolean isWholeWord() {
        return wholeWord;
    }

    /**
     * Returns a {@link SearchResult} representing the start and end positions of a string of
     * characters within the {@link android.text.Editable}. This will search for an instance
     * of the search phrase after startPos, if no instance is found the search will continue
     * from the beginning, startPos = 0, of the {@link android.text.Editable}. Results will
     * be cached. It is necessary to call {@link #clearSearchCache()} if supplying an
     * {@link android.text.Editable} that has been modified since the last time a cache
     * has been created such as after calling {@link #getCache(Editable)} or
     * {@link #previousSearchResult(Editable, int)}.
     * <br>
     * <br>
     * If no instance of the search phrase is found null is returned.
     *
     * @param text The {@link android.text.Editable} to search through for instances of the search phrase.
     * @param startPos The starting position within the {@link android.text.Editable} to begin the search.
     * @return A {@link SearchResult} representing the start and end positions of the discovered search
     * phrase or null if the search phrase was not found.
     * @throws OutOfMemoryError
     *
     * @see #setSearchPhrase(String)
     * @see SearchResult
     * @see #setWholeWord(boolean)
     * @see #setMatchCase(boolean)
     */
    public SearchResult nextSearchResult(final Editable text, final int startPos) throws OutOfMemoryError {
        if (text.length() == 0 || searchPhrase.isEmpty())
            return null;

        SearchResult[] cache = getCache(text);
        SearchResult sr = getNextCachedResult(cache, startPos);
        if (sr != null || startPos == 0)
            return sr;

        sr = getNextCachedResult(cache, 0);
        return sr;
    }

    /**
     * Returns a {@link SearchResult} representing the start and end positions of a string of
     * characters within the {@link android.text.Editable}. This will search for an instance
     * of the search phrase before startPos, if no instance is found the search will continue
     * from the ending, startPos = Editable.length(), of the {@link android.text.Editable}.
     * Results will be cached. It is necessary to call {@link #clearSearchCache()} if supplying
     * an {@link android.text.Editable} that has been modified since the last time a cache
     * has been created such as after calling {@link #getCache(Editable)} or
     * {@link #nextSearchResult(Editable, int)}.
     * <br>
     * <br>
     * If no instance of the search phrase is found null is returned.
     *
     * @param text The {@link android.text.Editable} to search through for instances of the search phrase.
     * @param startPos The starting position within the {@link android.text.Editable} to begin the search.
     * @return A {@link SearchResult} representing the start and end positions of the discovered search
     * phrase or null if the search phrase was not found.
     * @throws OutOfMemoryError
     *
     * @see #setSearchPhrase(String)
     * @see SearchResult
     * @see #setWholeWord(boolean)
     * @see #setMatchCase(boolean)
     */
    public SearchResult previousSearchResult(final Editable text, final int startPos) throws OutOfMemoryError {
        if (text.length() == 0 || searchPhrase.isEmpty())
            return null;

        SearchResult[] cache = getCache(text);
        SearchResult sr = getPreviousCachedResult(cache, startPos);
        if (sr != null || startPos == text.length())
            return sr;

        sr = getPreviousCachedResult(cache, text.length());
        return sr;
    }

    /**
     * Searches the cached search results, or creates a new cache if necessary, for a {@link SearchResult}
     * that starts at startPos and ends at endPos.
     * <br>
     * <br>
     * It is necessary to call {@link #clearSearchCache()} if supplying an
     * {@link android.text.Editable} that has been modified since the last time a cache
     * has been created such as after calling {@link #getCache(Editable)} or
     * {@link #nextSearchResult(Editable, int)}.
     *
     * @param text The {@link android.text.Editable} to search through for the search phrase.
     * @param startPos The starting position to match against a {@link SearchResult}
     * @param endPos The ending position to match against a {@link SearchResult}
     * @return The {@link SearchResult} that with start/end positions that match the supplied
     * startPos and endPos or null if not found.
     * @throws OutOfMemoryError
     *
     * @see SearchResult
     */
    public SearchResult getSelectedResult(final Editable text, int startPos, int endPos) throws OutOfMemoryError {
        if (text.length() == 0 || searchPhrase.isEmpty())
            return null;

        SearchResult[] cache = getCache(text);
        for (SearchResult sr : cache) {
            if (sr.start == startPos && sr.end == endPos)
                return  sr;
        }

        return null;
    }

    /**
     * Replaces the characters ine the {@link android.text.Editable} startng at startPos
     * and ending at endPos - 1 with the supplied phrase. It is not necessary to clear
     * the current search cache before using this method with a modified or different
     * {@link android.text.Editable}. The current cache will be cleared when this
     * method is invoked.
     *
     * @param text The {@link android.text.Editable} to replace characters in.
     * @param phrase The string of characters to replace the characters in the
     * {@link android.text.Editable} with.
     * @param startPos The first character within the {@link android.text.Editable} to be replaced.
     * @param endPos The position behind the last character within the {@link android.text.Editable}
     * to be replaced.
     * @return The modified {@link android.text.Editable} which is the same one passed as an argument.
     */
    public Editable replace(final Editable text, String phrase, int startPos, int endPos) {
        if (endPos > text.length() || endPos < startPos || startPos < 0)
            return text;

        clearSearchCache();

        return text.replace(startPos, endPos, phrase);
    }

    /**
     * Replaces all instances of the currently set search phrase in the supplied
     * {@link android.text.Editable} with phrase.
     * <br>
     * <br>
     * It is necessary to call {@link #clearSearchCache()} if supplying an
     * {@link android.text.Editable} that has been modified since the last time a cache
     * has been created such as after calling {@link #getCache(Editable)} or
     * {@link #nextSearchResult(Editable, int)}. The cache will be cleared upon
     * completion of this method as well.
     *
     * @param text The {@link android.text.Editable} to be modified.
     * @param phrase The string of characters that should replace all instances of the
     * currently set search phrase within the {@link android.text.Editable}.
     * @return The modified {@link android.text.Editable} which is the same one passed as an argument.
     * @throws OutOfMemoryError
     *
     * @see #setSearchPhrase(String)
     * @see #setWholeWord(boolean)
     * @see #setMatchCase(boolean)
     */
    public Editable replaceAll(final Editable text, final String phrase) throws OutOfMemoryError {
        SearchResult[] cache = getCache(text);
        if (cache.length == 0)
            return text;

        if (cache.length == 1)
            return replace(text, phrase, cache[0].start, cache[0].end);

        int diff = searchPhrase.length() - phrase.length();
        int totalDiff = 0;
        for (SearchResult sr : cache) {
            text.replace(sr.start - totalDiff, sr.end - totalDiff, phrase);
            totalDiff += diff;
        }

        clearSearchCache();

        return text;
    }

    /**
     * Gets the currently cached array of search results. If the cache has been freed from
     * memory due to memory constraints a new cache will be created using the supplied
     * {@link android.text.Editable}, currently set search phrase and options.
     * <br>
     * <br>
     * This method is called whenever a search is performed in the other methods of this
     * class such as {@link #nextSearchResult(Editable, int)} or
     * {@link #replaceAll(Editable, String)}.
     *
     * @param text The {@link android.text.Editable} to search through in the event
     * the current cache has been freed or not yet created.
     * @return An array of {@link SearchResult} containing the current search results.
     * @throws OutOfMemoryError
     *
     * @see SearchResult
     * @see #setCache(android.text.Editable)
     */
    public SearchResult[] getCache(final Editable text) throws OutOfMemoryError {
        if (searchCache == null)
            return setCache(text);

        SearchResult[] cache = searchCache.get();
        if (cache != null)
            return cache;

        return setCache(text);
    }

    /**
     * Creates a cache of {@link SearchResult} by searching through the entire
     * {@link android.text.Editable} for instances of the currently set
     * search phrase constrained by whole word and match case options.
     *
     * @param editable The {@link android.text.Editable} to search through.
     * @return An Array of {@link SearchResult} representing the locations of
     * instances of the search phrase within the supplied {@link android.text.Editable}.
     * @throws OutOfMemoryError
     *
     * @see #setSearchPhrase(String)
     * @see #setWholeWord(boolean)
     * @see #setMatchCase(boolean)
     * @see #getCache(android.text.Editable)
     */
    private SearchResult[] setCache(final Editable editable) throws OutOfMemoryError {
        String text;
        String searchPhrase;

        if (!matchCase) {
            try {
                text = editable.toString().toLowerCase();
                searchPhrase = this.searchPhrase.toLowerCase();
            } catch (OutOfMemoryError e) {
                throw e;
            }
        } else {
            text = editable.toString();
            searchPhrase = this.searchPhrase;
        }

        ArrayList<SearchResult> results = new ArrayList<>();
        SearchResult[] cache;
        try {
            int index = 0;
            do {
                index = text.indexOf(searchPhrase, index);
                if (index < 0)
                    break;

                SearchResult sr = new SearchResult(index, index + searchPhrase.length());
                if (!wholeWord || isWholeWord(editable, sr.start, sr.end))
                    results.add(sr);

                index = sr.end;
            } while (index < text.length() - 1);

            cache = results.toArray(new SearchResult[results.size()]);
        } catch (OutOfMemoryError e) {
            throw e;
        }

        if (results.isEmpty()) {
            cache = new SearchResult[0];
            searchCache = new SoftReference<>(cache);
            return cache;
        }

        searchCache = new SoftReference<>(cache);

        return cache;
    }

    /**
     * Used internally to find a {@link SearchResult} in the supplied cache that has
     * a start postion at or after the supplied startPos.
     *
     * @param cache An Array of {@link SearchResult} to search through.
     * @param startPos An Integer to compare against the {@link SearchResult}'s start point.
     * @return The first {@link SearchResult} that has a start point at or after the
     * supplied startPos or null if not found.
     *
     * @see #nextSearchResult(Editable, int)
     */
    private SearchResult getNextCachedResult(final SearchResult[] cache, final int startPos) {
        for (SearchResult sr : cache) {
            if (sr.start >= startPos)
                return sr;
        }

        return null;
    }

    /**
     * Used internally to find a {@link SearchResult} in the supplied cache that has
     * a end postion before startPos or a start postion before startPos and an end
     * position after startPos.
     *
     * @param cache An Array of {@link SearchResult} to search through.
     * @param startPos An Integer to compare against the {@link SearchResult}'s start
     * and end points.
     * @return The last {@link SearchResult} that has an end point before the
     * supplied startPos or a start point before the supplied
     * startPos and an endPoint after it. Returns null if not found.
     *
     * @see #previousSearchResult(Editable, int)
     */
    private SearchResult getPreviousCachedResult(final SearchResult[] cache, final int startPos) {
        for (int i = cache.length - 1; i >= 0; i--) {
            SearchResult sr = cache[i];
            if (sr.end < startPos || (sr.start < startPos && sr.end >= startPos))
                return sr;
        }

        return null;
    }

    /**
     * Used internally to determine if a section of characters within an
     * {@link android.text.Editable} is a whole word. This simply determines
     * if the character before start and at end are letters or digits. If
     * the character before start and the character at end are not letters
     * or digits then the section of text is considered to be a whole word.
     *
     * @param text The {@link android.text.Editable} containing the section
     * of text in question.
     * @param start The index at the start of the text section.
     * @param end The index just passed the last character of the text section,
     * firstCharacterIndex + length.
     * @return True if the character at start - 1 is neither a letter or digit and
     * the character at end is neither a letter or digit.
     */
    private boolean isWholeWord(final Editable text, final int start, final int end) {
        if (end != text.length()) {
            char c = text.charAt(end);
            if (Character.isLetter(c) || Character.isDigit(c))
                return false;
        }

        if (start == 0)
            return true;

        char c = text.charAt(start - 1);
        if (Character.isLetter(c) || Character.isDigit(c))
            return false;

        return true;
    }

    /**
     * Represents a section of characters within a character array. The start
     * index represents the first character in the array while the end index
     * represents the index just after the last character or start + length.
     * <br>
     * <br>
     * For example:
     * <br>
     * {@code
     * String string = "Hello World";
     * SearchResult sr = new SearchResult(6, 11);
     * Log.i("Last SearchResult Character", string.charAt(sr.end - 1));
     * }
     *
     * @author Adam T. Ryder
     */
    public class SearchResult {
        public final int start;
        public final int end;

        private SearchResult(final int start, final int end) {
            this.start = start;
            this.end = end;
        }
    }
}
