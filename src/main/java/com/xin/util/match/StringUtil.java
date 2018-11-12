// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.xin.util.match;


import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TeamCity inherits StringUtil: do not add private constructors!!!
@SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass"})
public class StringUtil extends StringUtilRt {

    @SuppressWarnings("SpellCheckingInspection")
    private static final String  VOWELS                       = "aeiouy";
    private static final Pattern EOL_SPLIT_KEEP_SEPARATORS    = Pattern.compile("(?<=(\r\n|\n))|(?<=\r)(?=[^\n])");
    private static final Pattern EOL_SPLIT_PATTERN            = Pattern.compile(" *(\r|\n|\r\n)+ *");
    private static final Pattern EOL_SPLIT_PATTERN_WITH_EMPTY = Pattern.compile(" *(\r|\n|\r\n) *");
    private static final Pattern EOL_SPLIT_DONT_TRIM_PATTERN  = Pattern.compile("(\r|\n|\r\n)+");


    public static String replace(String text, String oldS, String newS) {
        return replace(text, oldS, newS, false);
    }


    public static String replaceIgnoreCase(String text, String oldS, String newS) {
        return replace(text, oldS, newS, true);
    }

    /**
     * @deprecated Use {@link String#replace(char, char)} instead
     */


    @Deprecated
    public static String replaceChar(String buffer, char oldChar, char newChar) {
        return buffer.replace(oldChar, newChar);
    }


    public static String replace(final String text, final String oldS, final String newS, final boolean ignoreCase) {
        if (text.length() < oldS.length()) return text;

        StringBuilder newText = null;
        int i = 0;

        while (i < text.length()) {
            final int index = ignoreCase ? indexOfIgnoreCase(text, oldS, i) : text.indexOf(oldS, i);
            if (index < 0) {
                if (i == 0) {
                    return text;
                }

                newText.append(text, i, text.length());
                break;
            } else {
                if (newText == null) {
                    if (text.length() == oldS.length()) {
                        return newS;
                    }
                    newText = new StringBuilder(text.length() - i);
                }

                newText.append(text, i, index);
                newText.append(newS);
                i = index + oldS.length();
            }
        }
        return newText != null ? newText.toString() : "";
    }

    /**
     * Implementation copied from {@link String#indexOf(String, int)} except character comparisons made case insensitive
     */

    public static int indexOfIgnoreCase(String where, String what, int fromIndex) {
        int targetCount = what.length();
        int sourceCount = where.length();

        if (fromIndex >= sourceCount) {
            return targetCount == 0 ? sourceCount : -1;
        }

        if (fromIndex < 0) {
            fromIndex = 0;
        }

        if (targetCount == 0) {
            return fromIndex;
        }

        char first = what.charAt(0);
        int max = sourceCount - targetCount;

        for (int i = fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (!charsEqualIgnoreCase(where.charAt(i), first)) {
                //noinspection StatementWithEmptyBody,AssignmentToForLoopParameter
                while (++i <= max && !charsEqualIgnoreCase(where.charAt(i), first)) ;
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                //noinspection StatementWithEmptyBody
                for (int k = 1; j < end && charsEqualIgnoreCase(where.charAt(j), what.charAt(k)); j++, k++) ;

                if (j == end) {
                    /* Found whole string. */
                    return i;
                }
            }
        }

        return -1;
    }


    public static int indexOfIgnoreCase(String where, char what, int fromIndex) {
        int sourceCount = where.length();
        for (int i = Math.max(fromIndex, 0); i < sourceCount; i++) {
            if (charsEqualIgnoreCase(where.charAt(i), what)) {
                return i;
            }
        }

        return -1;
    }


    public static int lastIndexOfIgnoreCase(String where, char what, int fromIndex) {
        for (int i = Math.min(fromIndex, where.length() - 1); i >= 0; i--) {
            if (charsEqualIgnoreCase(where.charAt(i), what)) {
                return i;
            }
        }

        return -1;
    }


    public static boolean containsIgnoreCase(String where, String what) {
        return indexOfIgnoreCase(where, what, 0) >= 0;
    }


    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return StringUtilRt.endsWithIgnoreCase(str, suffix);
    }


    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return StringUtilRt.startsWithIgnoreCase(str, prefix);
    }


    public static String stripHtml(String html, boolean convertBreaks) {
        if (convertBreaks) {
            html = html.replaceAll("<br/?>", "\n\n");
        }

        return html.replaceAll("<(.|\n)*?>", "");
    }

    public static String toLowerCase(final String str) {
        return str == null ? null : str.toLowerCase();
    }


    public static String getPackageName(String fqName) {
        return getPackageName(fqName, '.');
    }

    /**
     * Given a fqName returns the package name for the type or the containing type.
     * <p/>
     * <ul>
     * <li>{@code java.lang.String} -> {@code java.lang}</li>
     * <li>{@code java.util.Map.Entry} -> {@code java.util.Map}</li>
     * </ul>
     *
     * @param fqName    a fully qualified type name. Not supposed to contain any type arguments
     * @param separator the separator to use. Typically '.'
     * @return the package name of the type or the declarator of the type. The empty string if the given fqName is unqualified
     */


    public static String getPackageName(String fqName, char separator) {
        int lastPointIdx = fqName.lastIndexOf(separator);
        if (lastPointIdx >= 0) {
            return fqName.substring(0, lastPointIdx);
        }
        return "";
    }


    public static int getLineBreakCount(CharSequence text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                count++;
            } else if (c == '\r') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    //noinspection AssignmentToForLoopParameter
                    i++;
                }
                count++;
            }
        }
        return count;
    }


    public static boolean containsLineBreak(CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isLineBreak(c)) return true;
        }
        return false;
    }


    public static boolean isLineBreak(char c) {
        return c == '\n' || c == '\r';
    }


    public static String escapeLineBreak(String text) {
        StringBuilder buffer = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                default:
                    buffer.append(c);
            }
        }
        return buffer.toString();
    }


    public static boolean endsWithLineBreak(CharSequence text) {
        int len = text.length();
        return len > 0 && isLineBreak(text.charAt(len - 1));
    }


    public static int lineColToOffset(CharSequence text, int line, int col) {
        int curLine = 0;
        int offset = 0;
        while (line != curLine) {
            if (offset == text.length()) return -1;
            char c = text.charAt(offset);
            if (c == '\n') {
                curLine++;
            } else if (c == '\r') {
                curLine++;
                if (offset < text.length() - 1 && text.charAt(offset + 1) == '\n') {
                    offset++;
                }
            }
            offset++;
        }
        return offset + col;
    }


    /**
     * Classic dynamic programming algorithm for string differences.
     */

    public static int difference(String s1, String s2) {
        int[][] a = new int[s1.length()][s2.length()];

        for (int i = 0; i < s1.length(); i++) {
            a[i][0] = i;
        }

        for (int j = 0; j < s2.length(); j++) {
            a[0][j] = j;
        }

        for (int i = 1; i < s1.length(); i++) {
            for (int j = 1; j < s2.length(); j++) {

                a[i][j] = Math.min(Math.min(a[i - 1][j - 1] + (s1.charAt(i) == s2.charAt(j) ? 0 : 1), a[i - 1][j] + 1), a[i][j - 1] + 1);
            }
        }

        return a[s1.length() - 1][s2.length() - 1];
    }


    public static String wordsToBeginFromUpperCase(String s) {
        return fixCapitalization(s, ourPrepositions, true);
    }


    public static String wordsToBeginFromLowerCase(String s) {
        return fixCapitalization(s, ourPrepositions, false);
    }


    private static String fixCapitalization(String s, String[] prepositions, boolean title) {
        StringBuilder buffer = null;
        for (int i = 0; i < s.length(); i++) {
            char prevChar = i == 0 ? ' ' : s.charAt(i - 1);
            char currChar = s.charAt(i);
            if (!Character.isLetterOrDigit(prevChar) && prevChar != '\'') {
                if (Character.isLetterOrDigit(currChar)) {
                    if (title || Character.isUpperCase(currChar)) {
                        int j = i;
                        for (; j < s.length(); j++) {
                            if (!Character.isLetterOrDigit(s.charAt(j))) {
                                break;
                            }
                        }
                        if (!title && j > i + 1 && !Character.isLowerCase(s.charAt(i + 1))) {
                            // filter out abbreviations like I18n, SQL and CSS
                            continue;
                        }
                        if (!isPreposition(s, i, j - 1, prepositions)) {
                            if (buffer == null) {
                                buffer = new StringBuilder(s);
                            }
                            buffer.setCharAt(i, title ? toUpperCase(currChar) : toLowerCase(currChar));
                        }
                    }
                }
            }
        }
        return buffer == null ? s : buffer.toString();
    }

    private static final String[] ourPrepositions = {
            "a", "an", "and", "as", "at", "but", "by", "down", "for", "from", "if", "in", "into", "not", "of", "on", "onto", "or", "out", "over",
            "per", "nor", "the", "to", "up", "upon", "via", "with"
    };


    public static boolean isPreposition(String s, int firstChar, int lastChar) {
        return isPreposition(s, firstChar, lastChar, ourPrepositions);
    }


    public static boolean isPreposition(String s, int firstChar, int lastChar, String[] prepositions) {
        for (String preposition : prepositions) {
            boolean found = false;
            if (lastChar - firstChar + 1 == preposition.length()) {
                found = true;
                for (int j = 0; j < preposition.length(); j++) {
                    if (toLowerCase(s.charAt(firstChar + j)) != preposition.charAt(j)) {
                        found = false;
                    }
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }


    public static void escapeStringCharacters(int length, String str, StringBuilder buffer) {
        escapeStringCharacters(length, str, "\"", buffer);
    }


    public static StringBuilder escapeStringCharacters(int length,
                                                       String str,
                                                       String additionalChars,
                                                       StringBuilder buffer) {
        return escapeStringCharacters(length, str, additionalChars, true, buffer);
    }


    public static StringBuilder escapeStringCharacters(int length,
                                                       String str,
                                                       String additionalChars,
                                                       boolean escapeSlash,
                                                       StringBuilder buffer) {
        return escapeStringCharacters(length, str, additionalChars, escapeSlash, true, buffer);
    }


    public static StringBuilder escapeStringCharacters(int length,
                                                       String str,
                                                       String additionalChars,
                                                       boolean escapeSlash,
                                                       boolean escapeUnicode,
                                                       StringBuilder buffer) {
        char prev = 0;
        for (int idx = 0; idx < length; idx++) {
            char ch = str.charAt(idx);
            switch (ch) {
                case '\b':
                    buffer.append("\\b");
                    break;

                case '\t':
                    buffer.append("\\t");
                    break;

                case '\n':
                    buffer.append("\\n");
                    break;

                case '\f':
                    buffer.append("\\f");
                    break;

                case '\r':
                    buffer.append("\\r");
                    break;

                default:
                    if (escapeSlash && ch == '\\') {
                        buffer.append("\\\\");
                    } else if (additionalChars != null && additionalChars.indexOf(ch) > -1 && (escapeSlash || prev != '\\')) {
                        buffer.append("\\").append(ch);
                    } else if (escapeUnicode && !isPrintableUnicode(ch)) {
                        CharSequence hexCode = StringUtilRt.toUpperCase(Integer.toHexString(ch));
                        buffer.append("\\u");
                        int paddingCount = 4 - hexCode.length();
                        while (paddingCount-- > 0) {
                            buffer.append(0);
                        }
                        buffer.append(hexCode);
                    } else {
                        buffer.append(ch);
                    }
            }
            prev = ch;
        }
        return buffer;
    }


    public static boolean isPrintableUnicode(char c) {
        int t = Character.getType(c);
        return t != Character.UNASSIGNED && t != Character.LINE_SEPARATOR && t != Character.PARAGRAPH_SEPARATOR &&
                t != Character.CONTROL && t != Character.FORMAT && t != Character.PRIVATE_USE && t != Character.SURROGATE;
    }


    public static String escapeStringCharacters(String s) {
        StringBuilder buffer = new StringBuilder(s.length());
        escapeStringCharacters(s.length(), s, "\"", buffer);
        return buffer.toString();
    }


    public static String escapeCharCharacters(String s) {
        StringBuilder buffer = new StringBuilder(s.length());
        escapeStringCharacters(s.length(), s, "\'", buffer);
        return buffer.toString();
    }


    public static String unescapeStringCharacters(String s) {
        StringBuilder buffer = new StringBuilder(s.length());
        unescapeStringCharacters(s.length(), s, buffer);
        return buffer.toString();
    }

    private static boolean isQuoteAt(String s, int ind) {
        char ch = s.charAt(ind);
        return ch == '\'' || ch == '\"';
    }


    public static boolean isQuotedString(String s) {
        return StringUtilRt.isQuotedString(s);
    }


    public static String unquoteString(String s) {
        return StringUtilRt.unquoteString(s);
    }


    public static String unquoteString(String s, char quotationChar) {
        return StringUtilRt.unquoteString(s, quotationChar);
    }

    private static void unescapeStringCharacters(int length, String s, StringBuilder buffer) {
        boolean escaped = false;
        for (int idx = 0; idx < length; idx++) {
            char ch = s.charAt(idx);
            if (!escaped) {
                if (ch == '\\') {
                    escaped = true;
                } else {
                    buffer.append(ch);
                }
            } else {
                int octalEscapeMaxLength = 2;
                switch (ch) {
                    case 'n':
                        buffer.append('\n');
                        break;

                    case 'r':
                        buffer.append('\r');
                        break;

                    case 'b':
                        buffer.append('\b');
                        break;

                    case 't':
                        buffer.append('\t');
                        break;

                    case 'f':
                        buffer.append('\f');
                        break;

                    case '\'':
                        buffer.append('\'');
                        break;

                    case '\"':
                        buffer.append('\"');
                        break;

                    case '\\':
                        buffer.append('\\');
                        break;

                    case 'u':
                        if (idx + 4 < length) {
                            try {
                                int code = Integer.parseInt(s.substring(idx + 1, idx + 5), 16);
                                //noinspection AssignmentToForLoopParameter
                                idx += 4;
                                buffer.append((char) code);
                            } catch (NumberFormatException e) {
                                buffer.append("\\u");
                            }
                        } else {
                            buffer.append("\\u");
                        }
                        break;

                    case '0':
                    case '1':
                    case '2':
                    case '3':
                        octalEscapeMaxLength = 3;
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                        int escapeEnd = idx + 1;
                        while (escapeEnd < length && escapeEnd < idx + octalEscapeMaxLength && isOctalDigit(s.charAt(escapeEnd)))
                            escapeEnd++;
                        try {
                            buffer.append((char) Integer.parseInt(s.substring(idx, escapeEnd), 8));
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Couldn't parse " + s.substring(idx, escapeEnd), e); // shouldn't happen
                        }
                        //noinspection AssignmentToForLoopParameter
                        idx = escapeEnd - 1;
                        break;

                    default:
                        buffer.append(ch);
                        break;
                }
                escaped = false;
            }
        }

        if (escaped) buffer.append('\\');
    }


    public static String capitalizeWords(String text,
                                         boolean allWords) {
        return capitalizeWords(text, " \t\n\r\f", allWords, false);
    }


    public static String capitalizeWords(String text,
                                         String tokenizerDelim,
                                         boolean allWords,
                                         boolean leaveOriginalDelims) {
        final StringTokenizer tokenizer = new StringTokenizer(text, tokenizerDelim, leaveOriginalDelims);
        final StringBuilder out = new StringBuilder(text.length());
        boolean toCapitalize = true;
        while (tokenizer.hasMoreTokens()) {
            final String word = tokenizer.nextToken();
            if (!leaveOriginalDelims && out.length() > 0) {
                out.append(' ');
            }
            out.append(toCapitalize ? capitalize(word) : word);
            if (!allWords) {
                toCapitalize = false;
            }
        }
        return out.toString();
    }


    public static String decapitalize(String s) {
        return Introspector.decapitalize(s);
    }


    public static boolean isVowel(char c) {
        return VOWELS.indexOf(c) >= 0;
    }

    /**
     * Capitalize the first letter of the sentence.
     */


    public static String capitalize(String s) {
        if (s.isEmpty()) return s;
        if (s.length() == 1) return StringUtilRt.toUpperCase(s).toString();

        // Optimization
        if (Character.isUpperCase(s.charAt(0))) return s;
        return toUpperCase(s.charAt(0)) + s.substring(1);
    }


    public static boolean isCapitalized(String s) {
        return s != null && !s.isEmpty() && Character.isUpperCase(s.charAt(0));
    }


    public static String capitalizeWithJavaBeanConvention(String s) {
        if (s.length() > 1 && Character.isUpperCase(s.charAt(1))) {
            return s;
        }
        return capitalize(s);
    }


    public static int stringHashCode(CharSequence chars, int from, int to) {
        int h = 0;
        for (int off = from; off < to; off++) {
            h = 31 * h + chars.charAt(off);
        }
        return h;
    }


    public static int stringHashCode(char[] chars, int from, int to) {
        int h = 0;
        for (int off = from; off < to; off++) {
            h = 31 * h + chars[off];
        }
        return h;
    }


    public static int stringHashCodeInsensitive(char[] chars, int from, int to) {
        int h = 0;
        for (int off = from; off < to; off++) {
            h = 31 * h + toLowerCase(chars[off]);
        }
        return h;
    }


    public static int stringHashCodeInsensitive(CharSequence chars, int from, int to) {
        int h = 0;
        for (int off = from; off < to; off++) {
            h = 31 * h + toLowerCase(chars.charAt(off));
        }
        return h;
    }


    public static int stringHashCodeInsensitive(CharSequence chars) {
        return stringHashCodeInsensitive(chars, 0, chars.length());
    }


    public static int stringHashCodeIgnoreWhitespaces(char[] chars, int from, int to) {
        int h = 0;
        for (int off = from; off < to; off++) {
            char c = chars[off];
            if (!isWhiteSpace(c)) {
                h = 31 * h + c;
            }
        }
        return h;
    }


    public static int stringHashCodeIgnoreWhitespaces(CharSequence chars, int from, int to) {
        int h = 0;
        for (int off = from; off < to; off++) {
            char c = chars.charAt(off);
            if (!isWhiteSpace(c)) {
                h = 31 * h + c;
            }
        }
        return h;
    }


    public static int stringHashCodeIgnoreWhitespaces(CharSequence chars) {
        return stringHashCodeIgnoreWhitespaces(chars, 0, chars.length());
    }

    /**
     * Equivalent to string.startsWith(prefixes[0] + prefixes[1] + ...) but avoids creating an object for concatenation.
     */

    public static boolean startsWithConcatenation(String string, String... prefixes) {
        int offset = 0;
        for (String prefix : prefixes) {
            int prefixLen = prefix.length();
            if (!string.regionMatches(offset, prefix, 0, prefixLen)) {
                return false;
            }
            offset += prefixLen;
        }
        return true;
    }

    public static String trim(String s) {
        return s == null ? null : s.trim();
    }


    public static String trimEnd(String s, String suffix) {
        return trimEnd(s, suffix, false);
    }


    public static String trimEnd(String s, String suffix, boolean ignoreCase) {
        boolean endsWith = ignoreCase ? endsWithIgnoreCase(s, suffix) : s.endsWith(suffix);
        if (endsWith) {
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }


    public static String trimEnd(String s, char suffix) {
        if (endsWithChar(s, suffix)) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }


    public static String trimLog(final String text, final int limit) {
        if (limit > 5 && text.length() > limit) {
            return text.substring(0, limit - 5) + " ...\n";
        }
        return text;
    }


    public static String trimLeading(String string) {
        return trimLeading((CharSequence) string).toString();
    }


    public static CharSequence trimLeading(CharSequence string) {
        int index = 0;
        while (index < string.length() && Character.isWhitespace(string.charAt(index))) index++;
        return string.subSequence(index, string.length());
    }


    public static String trimLeading(String string, char symbol) {
        int index = 0;
        while (index < string.length() && string.charAt(index) == symbol) index++;
        return string.substring(index);
    }


    public static StringBuilder trimLeading(StringBuilder builder, char symbol) {
        int index = 0;
        while (index < builder.length() && builder.charAt(index) == symbol) index++;
        if (index > 0) builder.delete(0, index);
        return builder;
    }


    public static String trimTrailing(String string) {
        return trimTrailing((CharSequence) string).toString();
    }


    public static CharSequence trimTrailing(CharSequence string) {
        int index = string.length() - 1;
        while (index >= 0 && Character.isWhitespace(string.charAt(index))) index--;
        return string.subSequence(0, index + 1);
    }


    public static String trimTrailing(String string, char symbol) {
        int index = string.length() - 1;
        while (index >= 0 && string.charAt(index) == symbol) index--;
        return string.substring(0, index + 1);
    }


    public static StringBuilder trimTrailing(StringBuilder builder, char symbol) {
        int index = builder.length() - 1;
        while (index >= 0 && builder.charAt(index) == symbol) index--;
        builder.setLength(index + 1);
        return builder;
    }


    public static boolean startsWithChar(CharSequence s, char prefix) {
        return s != null && s.length() != 0 && s.charAt(0) == prefix;
    }


    public static boolean endsWithChar(CharSequence s, char suffix) {
        return StringUtilRt.endsWithChar(s, suffix);
    }


    public static String trimStart(String s, String prefix) {
        if (s.startsWith(prefix)) {
            return s.substring(prefix.length());
        }
        return s;
    }


    public static String trimExtensions(String name) {
        int index = name.indexOf('.');
        return index < 0 ? name : name.substring(0, index);
    }


    public static String defaultIfEmpty(String value, String defaultValue) {
        return isEmpty(value) ? defaultValue : value;
    }


    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isEmpty(CharSequence cs) {
        return StringUtilRt.isEmpty(cs);
    }


    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }


    public static String notNullize(String s) {
        return StringUtilRt.notNullize(s);
    }


    public static String notNullize(String s, String defaultValue) {
        return StringUtilRt.notNullize(s, defaultValue);
    }


    public static String nullize(String s) {
        return nullize(s, false);
    }


    public static String nullize(String s, boolean nullizeSpaces) {
        boolean empty = nullizeSpaces ? isEmptyOrSpaces(s) : isEmpty(s);
        return empty ? null : s;
    }

    // we need to keep this method to preserve backward compatibility
    public static boolean isEmptyOrSpaces(String s) {
        return isEmptyOrSpaces((CharSequence) s);
    }

    public static boolean isEmptyOrSpaces(CharSequence s) {
        return StringUtilRt.isEmptyOrSpaces(s);
    }

    /**
     * Allows to answer if given symbol is white space, tabulation or line feed.
     *
     * @param c symbol to check
     * @return {@code true} if given symbol is white space, tabulation or line feed; {@code false} otherwise
     */

    public static boolean isWhiteSpace(char c) {
        return c == '\n' || c == '\t' || c == ' ';
    }


    public static String repeat(String s, int count) {
        assert count >= 0 : count;
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }


    public static List<String> splitHonorQuotes(String s, char separator) {
        return StringUtilRt.splitHonorQuotes(s, separator);
    }


    public static List<String> split(String s, String separator) {
        return split(s, separator, true);
    }


    public static List<CharSequence> split(CharSequence s, CharSequence separator) {
        return split(s, separator, true, true);
    }


    public static List<String> split(String s, String separator, boolean excludeSeparator) {
        return split(s, separator, excludeSeparator, true);
    }


    @SuppressWarnings("unchecked")
    public static List<String> split(String s, String separator, boolean excludeSeparator, boolean excludeEmptyStrings) {
        return (List) split((CharSequence) s, separator, excludeSeparator, excludeEmptyStrings);
    }


    public static List<CharSequence> split(CharSequence s, CharSequence separator, boolean excludeSeparator, boolean excludeEmptyStrings) {
        if (separator.length() == 0) {
            return Collections.singletonList(s);
        }
        List<CharSequence> result = new ArrayList<CharSequence>();
        int pos = 0;
        while (true) {
            int index = indexOf(s, separator, pos);
            if (index == -1) break;
            final int nextPos = index + separator.length();
            CharSequence token = s.subSequence(pos, excludeSeparator ? index : nextPos);
            if (token.length() != 0 || !excludeEmptyStrings) {
                result.add(token);
            }
            pos = nextPos;
        }
        if (pos < s.length() || !excludeEmptyStrings && pos == s.length()) {
            result.add(s.subSequence(pos, s.length()));
        }
        return result;
    }


    public static Iterable<String> tokenize(final StringTokenizer tokenizer) {
        return new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return tokenizer.hasMoreTokens();
                    }

                    @Override
                    public String next() {
                        return tokenizer.nextToken();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }


    public static String join(final String[] strings, final String separator) {
        return join(strings, 0, strings.length, separator);
    }


    public static String join(final String[] strings, int startIndex, int endIndex, final String separator) {
        final StringBuilder result = new StringBuilder();
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) result.append(separator);
            result.append(strings[i]);
        }
        return result.toString();
    }


    /**
     * Consider using {@link StringUtil#unquoteString(String)} instead.
     * Note: this method has an odd behavior:
     * Quotes are removed even if leading and trailing quotes are different or
     * if there is only one quote (leading or trailing).
     */


    public static String stripQuotesAroundValue(String text) {
        final int len = text.length();
        if (len > 0) {
            final int from = isQuoteAt(text, 0) ? 1 : 0;
            final int to = len > 1 && isQuoteAt(text, len - 1) ? len - 1 : len;
            if (from > 0 || to < len) {
                return text.substring(from, to);
            }
        }
        return text;
    }

    /**
     * Formats given file size in metric (1 kB = 1000 B) units (example: {@code formatFileSize(1234) = "1.23 KB"}).
     */


    public static String formatFileSize(long fileSize) {
        return StringUtilRt.formatFileSize(fileSize);
    }

    /**
     * Formats given file size in metric (1 kB = 1000 B) units (example: {@code formatFileSize(1234, "") = "1.23KB"}).
     */


    public static String formatFileSize(long fileSize, String unitSeparator) {
        return StringUtilRt.formatFileSize(fileSize, unitSeparator);
    }

    /**
     * Formats given duration as a sum of time units (example: {@code formatDuration(123456) = "2 m 3 s 456 ms"}).
     */


    public static String formatDuration(long duration) {
        return formatDuration(duration, " ");
    }

    private static final String[] TIME_UNITS       = {"ms", "s", "m", "h", "d", "mo", "yr", "c", "ml", "ep"};
    private static final long[]   TIME_MULTIPLIERS = {1, 1000, 60, 60, 24, 30, 12, 100, 10, 10000};

    /**
     * Formats given duration as a sum of time units (example: {@code formatDuration(123456, "") = "2m 3s 456ms"}).
     */


    public static String formatDuration(long duration, String unitSeparator) {
        String[] units = TIME_UNITS;
        long[] multipliers = TIME_MULTIPLIERS;

        StringBuilder sb = new StringBuilder();
        long count = duration, remainder;
        int i = 1;
        for (; i < units.length && count > 0; i++) {
            long multiplier = multipliers[i];
            if (count < multiplier) break;
            remainder = count % multiplier;
            count /= multiplier;
            if (remainder != 0 || sb.length() > 0) {
                if (units[i - 1].length() > 0) {
                    sb.insert(0, units[i - 1]);
                    sb.insert(0, unitSeparator);
                }
                sb.insert(0, remainder).insert(0, " ");
            } else {
                remainder = Math.round(remainder * 100 / (double) multiplier);
                count += remainder / 100;
            }
        }
        if (units[i - 1].length() > 0) {
            sb.insert(0, units[i - 1]);
            sb.insert(0, unitSeparator);
        }
        sb.insert(0, count);
        return sb.toString();
    }


    public static boolean containsAlphaCharacters(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLetter(value.charAt(i))) return true;
        }
        return false;
    }


    public static boolean containsAnyChar(final String value, final String chars) {
        return chars.length() > value.length()
                ? containsAnyChar(value, chars, 0, value.length())
                : containsAnyChar(chars, value, 0, chars.length());
    }


    public static boolean containsAnyChar(final String value,
                                          final String chars,
                                          final int start, final int end) {
        for (int i = start; i < end; i++) {
            if (chars.indexOf(value.charAt(i)) >= 0) {
                return true;
            }
        }

        return false;
    }


    public static boolean containsChar(final String value, final char ch) {
        return value.indexOf(ch) >= 0;
    }


    public static List<String> findMatches(String s, Pattern pattern) {
        return findMatches(s, pattern, 1);
    }


    public static List<String> findMatches(String s, Pattern pattern, int groupIndex) {
        List<String> result = new ArrayList<>();
        Matcher m = pattern.matcher(s);
        while (m.find()) {
            String group = m.group(groupIndex);
            if (group != null) {
                result.add(group);
            }
        }
        return result;
    }


    public static String replaceSubstring(String string, TextRange range, String replacement) {
        return range.replace(string, replacement);
    }


    public static boolean startsWithWhitespace(String text) {
        return !text.isEmpty() && Character.isWhitespace(text.charAt(0));
    }


    public static boolean isChar(CharSequence seq, int index, char c) {
        return index >= 0 && index < seq.length() && seq.charAt(index) == c;
    }


    public static boolean startsWith(CharSequence text, CharSequence prefix) {
        int l1 = text.length();
        int l2 = prefix.length();
        if (l1 < l2) return false;

        for (int i = 0; i < l2; i++) {
            if (text.charAt(i) != prefix.charAt(i)) return false;
        }

        return true;
    }


    public static boolean startsWith(CharSequence text, int startIndex, CharSequence prefix) {
        int tl = text.length();
        if (startIndex < 0 || startIndex > tl) {
            throw new IllegalArgumentException("Index is out of bounds: " + startIndex + ", length: " + tl);
        }
        int l1 = tl - startIndex;
        int l2 = prefix.length();
        if (l1 < l2) return false;

        for (int i = 0; i < l2; i++) {
            if (text.charAt(i + startIndex) != prefix.charAt(i)) return false;
        }

        return true;
    }


    public static boolean endsWith(CharSequence text, CharSequence suffix) {
        int l1 = text.length();
        int l2 = suffix.length();
        if (l1 < l2) return false;

        for (int i = l1 - 1; i >= l1 - l2; i--) {
            if (text.charAt(i) != suffix.charAt(i + l2 - l1)) return false;
        }

        return true;
    }


    public static String commonPrefix(String s1, String s2) {
        return s1.substring(0, commonPrefixLength(s1, s2));
    }


    public static int commonPrefixLength(CharSequence s1, CharSequence s2) {
        return commonPrefixLength(s1, s2, false);
    }


    public static int commonPrefixLength(CharSequence s1, CharSequence s2, boolean ignoreCase) {
        int i;
        int minLength = Math.min(s1.length(), s2.length());
        for (i = 0; i < minLength; i++) {
            if (!charsMatch(s1.charAt(i), s2.charAt(i), ignoreCase)) {
                break;
            }
        }
        return i;
    }


    public static String commonSuffix(String s1, String s2) {
        return s1.substring(s1.length() - commonSuffixLength(s1, s2));
    }


    public static int commonSuffixLength(CharSequence s1, CharSequence s2) {
        int s1Length = s1.length();
        int s2Length = s2.length();
        if (s1Length == 0 || s2Length == 0) return 0;
        int i;
        for (i = 0; i < s1Length && i < s2Length; i++) {
            if (s1.charAt(s1Length - i - 1) != s2.charAt(s2Length - i - 1)) {
                break;
            }
        }
        return i;
    }

    /**
     * Allows to answer if target symbol is contained at given char sequence at {@code [start; end)} interval.
     *
     * @param s     target char sequence to check
     * @param start start offset to use within the given char sequence (inclusive)
     * @param end   end offset to use within the given char sequence (exclusive)
     * @param c     target symbol to check
     * @return {@code true} if given symbol is contained at the target range of the given char sequence;
     * {@code false} otherwise
     */

    public static boolean contains(CharSequence s, int start, int end, char c) {
        return indexOf(s, c, start, end) >= 0;
    }


    public static boolean containsWhitespaces(CharSequence s) {
        if (s == null) return false;

        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) return true;
        }
        return false;
    }


    public static int indexOf(CharSequence s, char c) {
        return indexOf(s, c, 0, s.length());
    }


    public static int indexOf(CharSequence s, char c, int start) {
        return indexOf(s, c, start, s.length());
    }


    public static int indexOf(CharSequence s, char c, int start, int end) {
        end = Math.min(end, s.length());
        for (int i = Math.max(start, 0); i < end; i++) {
            if (s.charAt(i) == c) return i;
        }
        return -1;
    }


    public static boolean contains(CharSequence sequence, CharSequence infix) {
        return indexOf(sequence, infix) >= 0;
    }


    public static int indexOf(CharSequence sequence, CharSequence infix) {
        return indexOf(sequence, infix, 0);
    }


    public static int indexOf(CharSequence sequence, CharSequence infix, int start) {
        return indexOf(sequence, infix, start, sequence.length());
    }


    public static int indexOf(CharSequence sequence, CharSequence infix, int start, int end) {
        for (int i = start; i <= end - infix.length(); i++) {
            if (startsWith(sequence, i, infix)) {
                return i;
            }
        }
        return -1;
    }


    public static int indexOf(CharSequence s, char c, int start, int end, boolean caseSensitive) {
        end = Math.min(end, s.length());
        for (int i = Math.max(start, 0); i < end; i++) {
            if (charsMatch(s.charAt(i), c, !caseSensitive)) return i;
        }
        return -1;
    }


    public static int indexOf(char[] s, char c, int start, int end, boolean caseSensitive) {
        end = Math.min(end, s.length);
        for (int i = Math.max(start, 0); i < end; i++) {
            if (charsMatch(s[i], c, !caseSensitive)) return i;
        }
        return -1;
    }


    public static int indexOfSubstringEnd(String text, String subString) {
        int i = text.indexOf(subString);
        if (i == -1) return -1;
        return i + subString.length();
    }


    public static int indexOfAny(final String s, final String chars) {
        return indexOfAny(s, chars, 0, s.length());
    }


    public static int indexOfAny(final CharSequence s, final String chars) {
        return indexOfAny(s, chars, 0, s.length());
    }


    public static int indexOfAny(final String s, final String chars, final int start, final int end) {
        return indexOfAny((CharSequence) s, chars, start, end);
    }


    public static int indexOfAny(final CharSequence s, final String chars, final int start, int end) {
        end = Math.min(end, s.length());
        for (int i = Math.max(start, 0); i < end; i++) {
            if (containsChar(chars, s.charAt(i))) return i;
        }
        return -1;
    }


    public static int lastIndexOfAny(CharSequence s, final String chars) {
        for (int i = s.length() - 1; i >= 0; i--) {
            if (containsChar(chars, s.charAt(i))) return i;
        }
        return -1;
    }


    public static String substringBefore(String text, String subString) {
        int i = text.indexOf(subString);
        if (i == -1) return null;
        return text.substring(0, i);
    }


    public static String substringBeforeLast(String text, String subString) {
        int i = text.lastIndexOf(subString);
        if (i == -1) return text;
        return text.substring(0, i);
    }


    public static String substringAfter(String text, String subString) {
        int i = text.indexOf(subString);
        if (i == -1) return null;
        return text.substring(i + subString.length());
    }


    public static String substringAfterLast(String text, String subString) {
        int i = text.lastIndexOf(subString);
        if (i == -1) return null;
        return text.substring(i + subString.length());
    }

    /**
     * Allows to retrieve index of last occurrence of the given symbols at {@code [start; end)} sub-sequence of the given text.
     *
     * @param s     target text
     * @param c     target symbol which last occurrence we want to check
     * @param start start offset of the target text (inclusive)
     * @param end   end offset of the target text (exclusive)
     * @return index of the last occurrence of the given symbol at the target sub-sequence of the given text if any;
     * {@code -1} otherwise
     */

    public static int lastIndexOf(CharSequence s, char c, int start, int end) {
        return StringUtilRt.lastIndexOf(s, c, start, end);
    }


    public static String first(String text, final int maxLength, final boolean appendEllipsis) {
        return text.length() > maxLength ? text.substring(0, maxLength) + (appendEllipsis ? "..." : "") : text;
    }


    public static CharSequence first(CharSequence text, final int length, final boolean appendEllipsis) {
        if (text.length() <= length) {
            return text;
        }
        if (appendEllipsis) {
            return text.subSequence(0, length) + "...";
        }
        return text.subSequence(0, length);
    }


    public static CharSequence last(CharSequence text, final int length, boolean prependEllipsis) {
        if (text.length() <= length) {
            return text;
        }
        if (prependEllipsis) {
            return "..." + text.subSequence(text.length() - length, text.length());
        }
        return text.subSequence(text.length() - length, text.length());
    }


    public static String firstLast(String text, int length) {
        return text.length() > length
                ? text.subSequence(0, length / 2) + "\u2026" + text.subSequence(text.length() - length / 2 - 1, text.length())
                : text;
    }


    public static String escapeChar(final String str, final char character) {
        return escapeChars(str, character);
    }


    public static String escapeChars(final String str, final char... character) {
        final StringBuilder buf = new StringBuilder(str);
        for (char c : character) {
            escapeChar(buf, c);
        }
        return buf.toString();
    }

    public static void escapeChar(final StringBuilder buf, final char character) {
        int idx = 0;
        while ((idx = indexOf(buf, character, idx)) >= 0) {
            buf.insert(idx, "\\");
            idx += 2;
        }
    }


    public static String escapeQuotes(final String str) {
        return escapeChar(str, '"');
    }

    public static void escapeQuotes(final StringBuilder buf) {
        escapeChar(buf, '"');
    }


    public static String escapeSlashes(final String str) {
        return escapeChar(str, '/');
    }


    public static String escapeBackSlashes(final String str) {
        return escapeChar(str, '\\');
    }

    public static void escapeSlashes(final StringBuilder buf) {
        escapeChar(buf, '/');
    }


    public static String unescapeSlashes(final String str) {
        final StringBuilder buf = new StringBuilder(str.length());
        unescapeChar(buf, str, '/');
        return buf.toString();
    }


    public static String unescapeBackSlashes(final String str) {
        final StringBuilder buf = new StringBuilder(str.length());
        unescapeChar(buf, str, '\\');
        return buf.toString();
    }


    public static String unescapeChar(final String str, char unescapeChar) {
        final StringBuilder buf = new StringBuilder(str.length());
        unescapeChar(buf, str, unescapeChar);
        return buf.toString();
    }

    private static void unescapeChar(StringBuilder buf, String str, char unescapeChar) {
        final int length = str.length();
        final int last = length - 1;
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            if (ch == '\\' && i != last) {
                //noinspection AssignmentToForLoopParameter
                i++;
                ch = str.charAt(i);
                if (ch != unescapeChar) buf.append('\\');
            }

            buf.append(ch);
        }
    }

    public static void quote(final StringBuilder builder) {
        quote(builder, '\"');
    }

    public static void quote(final StringBuilder builder, final char quotingChar) {
        builder.insert(0, quotingChar);
        builder.append(quotingChar);
    }


    public static String wrapWithDoubleQuote(String str) {
        return '\"' + str + "\"";
    }

    private static final List<String> REPLACES_REFS = Arrays.asList("&lt;", "&gt;", "&amp;", "&#39;", "&quot;");
    private static final List<String> REPLACES_DISP = Arrays.asList("<", ">", "&", "'", "\"");

    public static String unescapeXml(final String text) {
        return text == null ? null : replace(text, REPLACES_REFS, REPLACES_DISP);
    }

    public static String escapeXml(final String text) {
        return text == null ? null : replace(text, REPLACES_DISP, REPLACES_REFS);
    }


    private static final List<String> MN_QUOTED = Arrays.asList("&&", "__");
    private static final List<String> MN_CHARS  = Arrays.asList("&", "_");

    public static String escapeMnemonics(String text) {
        return text == null ? null : replace(text, MN_CHARS, MN_QUOTED);
    }


    public static String htmlEmphasize(String text) {
        return "<b><code>" + escapeXml(text) + "</code></b>";
    }


    public static String escapeToRegexp(String text) {
        final StringBuilder result = new StringBuilder(text.length());
        return escapeToRegexp(text, result).toString();
    }


    public static StringBuilder escapeToRegexp(CharSequence text, StringBuilder builder) {
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c == ' ' || Character.isLetter(c) || Character.isDigit(c) || c == '_') {
                builder.append(c);
            } else if (c == '\n') {
                builder.append("\\n");
            } else if (c == '\r') {
                builder.append("\\r");
            } else {
                builder.append('\\').append(c);
            }
        }

        return builder;
    }


    public static boolean isEscapedBackslash(char[] chars, int startOffset, int backslashOffset) {
        if (chars[backslashOffset] != '\\') {
            return true;
        }
        boolean escaped = false;
        for (int i = startOffset; i < backslashOffset; i++) {
            if (chars[i] == '\\') {
                escaped = !escaped;
            } else {
                escaped = false;
            }
        }
        return escaped;
    }


    public static boolean isEscapedBackslash(CharSequence text, int startOffset, int backslashOffset) {
        if (text.charAt(backslashOffset) != '\\') {
            return true;
        }
        boolean escaped = false;
        for (int i = startOffset; i < backslashOffset; i++) {
            if (text.charAt(i) == '\\') {
                escaped = !escaped;
            } else {
                escaped = false;
            }
        }
        return escaped;
    }

    /**
     * @deprecated Use {@link #replace(String, List, List)}
     */
    @Deprecated


    public static String replace(String text, String[] from, String[] to) {
        return replace(text, Arrays.asList(from), Arrays.asList(to));
    }


    public static String replace(String text, List<String> from, List<String> to) {
        assert from.size() == to.size();
        StringBuilder result = null;
        replace:
        for (int i = 0; i < text.length(); i++) {
            for (int j = 0; j < from.size(); j += 1) {
                String toReplace = from.get(j);
                String replaceWith = to.get(j);

                final int len = toReplace.length();
                if (text.regionMatches(i, toReplace, 0, len)) {
                    if (result == null) {
                        result = new StringBuilder(text.length());
                        result.append(text, 0, i);
                    }
                    result.append(replaceWith);
                    //noinspection AssignmentToForLoopParameter
                    i += len - 1;
                    continue replace;
                }
            }

            if (result != null) {
                result.append(text.charAt(i));
            }
        }
        return result == null ? text : result.toString();
    }


    public static int countNewLines(CharSequence text) {
        return countChars(text, '\n');
    }


    public static int countChars(CharSequence text, char c) {
        return countChars(text, c, 0, false);
    }


    public static int countChars(CharSequence text, char c, int offset, boolean stopAtOtherChar) {
        return countChars(text, c, offset, text.length(), stopAtOtherChar);
    }


    public static int countChars(CharSequence text, char c, int start, int end, boolean stopAtOtherChar) {
        int count = 0;
        boolean forward = start <= end;
        start = forward ? Math.max(0, start) : Math.min(text.length(), start);
        end = forward ? Math.min(text.length(), end) : Math.max(0, end);
        for (int i = forward ? start : start - 1; forward && i < end || !forward && i >= end; i += forward ? 1 : -1) {
            if (text.charAt(i) == c) {
                count++;
            } else if (stopAtOtherChar) {
                break;
            }
        }
        return count;
    }


    public static String capitalsOnly(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                b.append(s.charAt(i));
            }
        }

        return b.toString();
    }

    /**
     * @param args Strings to join.
     * @return {@code null} if any of given Strings is {@code null}.
     */


    public static String joinOrNull(String... args) {
        StringBuilder r = new StringBuilder();
        for (String arg : args) {
            if (arg == null) return null;
            r.append(arg);
        }
        return r.toString();
    }


    public static String getPropertyName(String methodName) {
        if (methodName.startsWith("get")) {
            return Introspector.decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is")) {
            return Introspector.decapitalize(methodName.substring(2));
        }
        if (methodName.startsWith("set")) {
            return Introspector.decapitalize(methodName.substring(3));
        }
        return null;
    }


    public static boolean isJavaIdentifierStart(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || Character.isJavaIdentifierStart(c);
    }


    public static boolean isJavaIdentifierPart(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || Character.isJavaIdentifierPart(c);
    }


    public static boolean isJavaIdentifier(String text) {
        int len = text.length();
        if (len == 0) return false;

        if (!isJavaIdentifierStart(text.charAt(0))) return false;

        for (int i = 1; i < len; i++) {
            if (!isJavaIdentifierPart(text.charAt(i))) return false;
        }

        return true;
    }

    /**
     * Escape property name or key in property file. Unicode characters are escaped as well.
     *
     * @param input an input to escape
     * @param isKey if true, the rules for key escaping are applied. The leading space is escaped in that case.
     * @return an escaped string
     */


    public static String escapeProperty(String input, final boolean isKey) {
        final StringBuilder escaped = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            final char ch = input.charAt(i);
            switch (ch) {
                case ' ':
                    if (isKey && i == 0) {
                        // only the leading space has to be escaped
                        escaped.append('\\');
                    }
                    escaped.append(' ');
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\\':
                case '#':
                case '!':
                case ':':
                case '=':
                    escaped.append('\\');
                    escaped.append(ch);
                    break;
                default:
                    if (20 < ch && ch < 0x7F) {
                        escaped.append(ch);
                    } else {
                        escaped.append("\\u");
                        escaped.append(Character.forDigit((ch >> 12) & 0xF, 16));
                        escaped.append(Character.forDigit((ch >> 8) & 0xF, 16));
                        escaped.append(Character.forDigit((ch >> 4) & 0xF, 16));
                        escaped.append(Character.forDigit((ch) & 0xF, 16));
                    }
                    break;
            }
        }
        return escaped.toString();
    }


    public static String getQualifiedName(String packageName, String className) {
        if (packageName == null || packageName.isEmpty()) {
            return className;
        }
        return packageName + '.' + className;
    }


    public static int compareVersionNumbers(String v1, String v2) {
        // todo duplicates com.intellij.util.text.VersionComparatorUtil.compare
        // todo please refactor next time you make changes here
        if (v1 == null && v2 == null) {
            return 0;
        }
        if (v1 == null) {
            return -1;
        }
        if (v2 == null) {
            return 1;
        }

        String[] part1 = v1.split("[._\\-]");
        String[] part2 = v2.split("[._\\-]");

        int idx = 0;
        for (; idx < part1.length && idx < part2.length; idx++) {
            String p1 = part1[idx];
            String p2 = part2[idx];

            int cmp;
            if (p1.matches("\\d+") && p2.matches("\\d+")) {
                cmp = new Integer(p1).compareTo(new Integer(p2));
            } else {
                cmp = part1[idx].compareTo(part2[idx]);
            }
            if (cmp != 0) return cmp;
        }

        if (part1.length != part2.length) {
            boolean left = part1.length > idx;
            String[] parts = left ? part1 : part2;

            for (; idx < parts.length; idx++) {
                String p = parts[idx];
                int cmp;
                if (p.matches("\\d+")) {
                    cmp = new Integer(p).compareTo(0);
                } else {
                    cmp = 1;
                }
                if (cmp != 0) return left ? cmp : -cmp;
            }
        }
        return 0;
    }


    public static int getOccurrenceCount(String text, final char c) {
        int res = 0;
        int i = 0;
        while (i < text.length()) {
            i = text.indexOf(c, i);
            if (i >= 0) {
                res++;
                i++;
            } else {
                break;
            }
        }
        return res;
    }


    public static int getOccurrenceCount(String text, String s) {
        int res = 0;
        int i = 0;
        while (i < text.length()) {
            i = text.indexOf(s, i);
            if (i >= 0) {
                res++;
                i++;
            } else {
                break;
            }
        }
        return res;
    }


    public static int getIgnoreCaseOccurrenceCount(String text, String s) {
        int res = 0;
        int i = 0;
        while (i < text.length()) {
            i = indexOfIgnoreCase(text, s, i);
            if (i >= 0) {
                res++;
                i++;
            } else {
                break;
            }
        }
        return res;
    }


    public static String fixVariableNameDerivedFromPropertyName(String name) {
        if (isEmptyOrSpaces(name)) return name;
        char c = name.charAt(0);
        if (isVowel(c)) {
            return "an" + Character.toUpperCase(c) + name.substring(1);
        }
        return "a" + Character.toUpperCase(c) + name.substring(1);
    }


    public static String sanitizeJavaIdentifier(String name) {
        final StringBuilder result = new StringBuilder(name.length());

        for (int i = 0; i < name.length(); i++) {
            final char ch = name.charAt(i);
            if (Character.isJavaIdentifierPart(ch)) {
                if (result.length() == 0 && !Character.isJavaIdentifierStart(ch)) {
                    result.append("_");
                }
                result.append(ch);
            }
        }

        return result.toString();
    }


    public static String tail(String s, final int idx) {
        return idx >= s.length() ? "" : s.substring(idx);
    }

    /**
     * Splits string by lines.
     *
     * @param string String to split
     * @return array of strings
     */


    public static String[] splitByLines(String string) {
        return splitByLines(string, true);
    }

    /**
     * Splits string by lines. If several line separators are in a row corresponding empty lines
     * are also added to result if {@code excludeEmptyStrings} is {@code false}.
     *
     * @param string String to split
     * @return array of strings
     */


    public static String[] splitByLines(String string, boolean excludeEmptyStrings) {
        return (excludeEmptyStrings ? EOL_SPLIT_PATTERN : EOL_SPLIT_PATTERN_WITH_EMPTY).split(string);
    }


    public static String[] splitByLinesDontTrim(String string) {
        return EOL_SPLIT_DONT_TRIM_PATTERN.split(string);
    }

    /**
     * Splits string by lines, keeping all line separators at the line ends and in the empty lines.
     * <br> E.g. splitting text
     * <blockquote>
     * foo\r\n<br>
     * \n<br>
     * bar\n<br>
     * \r\n<br>
     * baz\r<br>
     * \r<br>
     * </blockquote>
     * will return the following array: foo\r\n, \n, bar\n, \r\n, baz\r, \r
     */


    public static String[] splitByLinesKeepSeparators(String string) {
        return EOL_SPLIT_KEEP_SEPARATORS.split(string);
    }


    public static boolean isDecimalDigit(char c) {
        return c >= '0' && c <= '9';
    }


    public static int compare(String s1, String s2, boolean ignoreCase) {
        //noinspection StringEquality
        if (s1 == s2) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        return ignoreCase ? s1.compareToIgnoreCase(s2) : s1.compareTo(s2);
    }


    public static int comparePairs(String s1, String t1, String s2, String t2, boolean ignoreCase) {
        final int compare = compare(s1, s2, ignoreCase);
        return compare != 0 ? compare : compare(t1, t2, ignoreCase);
    }


    public static boolean equals(CharSequence s1, CharSequence s2) {
        if (s1 == null ^ s2 == null) {
            return false;
        }

        if (s1 == null) {
            return true;
        }

        if (s1.length() != s2.length()) {
            return false;
        }
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return false;
            }
        }
        return true;
    }


    public static boolean equalsIgnoreCase(CharSequence s1, CharSequence s2) {
        if (s1 == null ^ s2 == null) {
            return false;
        }

        if (s1 == null) {
            return true;
        }

        if (s1.length() != s2.length()) {
            return false;
        }
        for (int i = 0; i < s1.length(); i++) {
            if (!charsEqualIgnoreCase(s1.charAt(i), s2.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    public static boolean equalsIgnoreWhitespaces(CharSequence s1, CharSequence s2) {
        if (s1 == null ^ s2 == null) {
            return false;
        }

        if (s1 == null) {
            return true;
        }

        int len1 = s1.length();
        int len2 = s2.length();

        int index1 = 0;
        int index2 = 0;
        while (index1 < len1 && index2 < len2) {
            if (s1.charAt(index1) == s2.charAt(index2)) {
                index1++;
                index2++;
                continue;
            }

            boolean skipped = false;
            while (index1 != len1 && isWhiteSpace(s1.charAt(index1))) {
                skipped = true;
                index1++;
            }
            while (index2 != len2 && isWhiteSpace(s2.charAt(index2))) {
                skipped = true;
                index2++;
            }

            if (!skipped) return false;
        }

        for (; index1 != len1; index1++) {
            if (!isWhiteSpace(s1.charAt(index1))) return false;
        }
        for (; index2 != len2; index2++) {
            if (!isWhiteSpace(s2.charAt(index2))) return false;
        }

        return true;
    }


    public static boolean findIgnoreCase(String toFind, String... where) {
        for (String string : where) {
            if (equalsIgnoreCase(toFind, string)) return true;
        }
        return false;
    }


    public static int compare(char c1, char c2, boolean ignoreCase) {
        // duplicating String.equalsIgnoreCase logic
        int d = c1 - c2;
        if (d == 0 || !ignoreCase) {
            return d;
        }
        // If characters don't match but case may be ignored,
        // try converting both characters to uppercase.
        // If the results match, then the comparison scan should
        // continue.
        char u1 = StringUtilRt.toUpperCase(c1);
        char u2 = StringUtilRt.toUpperCase(c2);
        d = u1 - u2;
        if (d != 0) {
            // Unfortunately, conversion to uppercase does not work properly
            // for the Georgian alphabet, which has strange rules about case
            // conversion.  So we need to make one last check before
            // exiting.
            d = StringUtilRt.toLowerCase(u1) - StringUtilRt.toLowerCase(u2);
        }
        return d;
    }


    public static boolean charsMatch(char c1, char c2, boolean ignoreCase) {
        return compare(c1, c2, ignoreCase) == 0;
    }


    public static String formatLinks(String message) {
        Pattern linkPattern = Pattern.compile("http://[a-zA-Z0-9./\\-+]+");
        StringBuffer result = new StringBuffer();
        Matcher m = linkPattern.matcher(message);
        while (m.find()) {
            m.appendReplacement(result, "<a href=\"" + m.group() + "\">" + m.group() + "</a>");
        }
        m.appendTail(result);
        return result.toString();
    }


    public static boolean isHexDigit(char c) {
        return '0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F';
    }


    public static boolean isOctalDigit(char c) {
        return '0' <= c && c <= '7';
    }


    public static String shortenTextWithEllipsis(final String text, final int maxLength, final int suffixLength) {
        return shortenTextWithEllipsis(text, maxLength, suffixLength, false);
    }


    public static String trimMiddle(String text, int maxLength) {
        return shortenTextWithEllipsis(text, maxLength, maxLength >> 1, true);
    }


    public static String shortenTextWithEllipsis(final String text,
                                                 final int maxLength,
                                                 final int suffixLength,
                                                 String symbol) {
        final int textLength = text.length();
        if (textLength > maxLength) {
            final int prefixLength = maxLength - suffixLength - symbol.length();
            assert prefixLength > 0;
            return text.substring(0, prefixLength) + symbol + text.substring(textLength - suffixLength);
        } else {
            return text;
        }
    }


    public static String shortenTextWithEllipsis(final String text,
                                                 final int maxLength,
                                                 final int suffixLength,
                                                 boolean useEllipsisSymbol) {
        String symbol = useEllipsisSymbol ? "\u2026" : "...";
        return shortenTextWithEllipsis(text, maxLength, suffixLength, symbol);
    }


    public static String shortenPathWithEllipsis(final String path, final int maxLength, boolean useEllipsisSymbol) {
        return shortenTextWithEllipsis(path, maxLength, (int) (maxLength * 0.7), useEllipsisSymbol);
    }


    public static String shortenPathWithEllipsis(final String path, final int maxLength) {
        return shortenPathWithEllipsis(path, maxLength, false);
    }


    public static boolean charsEqualIgnoreCase(char a, char b) {
        return charsMatch(a, b, true);
    }


    public static char toUpperCase(char a) {
        return StringUtilRt.toUpperCase(a);
    }

    public static String toUpperCase(String a) {
        return a == null ? null : StringUtilRt.toUpperCase(a).toString();
    }


    public static char toLowerCase(final char a) {
        return StringUtilRt.toLowerCase(a);
    }


    public static boolean isUpperCase(CharSequence sequence) {
        for (int i = 0; i < sequence.length(); i++) {
            if (!Character.isUpperCase(sequence.charAt(i))) return false;
        }
        return true;
    }


    public static String convertLineSeparators(String text) {
        return StringUtilRt.convertLineSeparators(text);
    }


    public static String convertLineSeparators(String text, boolean keepCarriageReturn) {
        return StringUtilRt.convertLineSeparators(text, keepCarriageReturn);
    }


    public static String convertLineSeparators(String text, String newSeparator) {
        return StringUtilRt.convertLineSeparators(text, newSeparator);
    }


    public static String convertLineSeparators(String text, String newSeparator, int[] offsetsToKeep) {
        return StringUtilRt.convertLineSeparators(text, newSeparator, offsetsToKeep);
    }


    public static int parseInt(String string, int defaultValue) {
        return StringUtilRt.parseInt(string, defaultValue);
    }


    public static long parseLong(String string, long defaultValue) {
        return StringUtilRt.parseLong(string, defaultValue);
    }


    public static double parseDouble(String string, double defaultValue) {
        return StringUtilRt.parseDouble(string, defaultValue);
    }


    public static <E extends Enum<E>> E parseEnum(String string, E defaultValue, Class<E> clazz) {
        return StringUtilRt.parseEnum(string, defaultValue, clazz);
    }


    public static String getShortName(Class aClass) {
        return StringUtilRt.getShortName(aClass);
    }


    public static String getShortName(String fqName) {
        return StringUtilRt.getShortName(fqName);
    }


    public static String getShortName(String fqName, char separator) {
        return StringUtilRt.getShortName(fqName, separator);
    }

    /**
     * Equivalent for {@code getShortName(fqName).equals(shortName)}, but could be faster.
     *
     * @param fqName    fully-qualified name (dot-separated)
     * @param shortName a short name, must not contain dots
     * @return true if specified short name is a short name of fully-qualified name
     */
    public static boolean isShortNameOf(String fqName, String shortName) {
        final char separator = '.';
        if (fqName.length() < shortName.length()) return false;
        if (fqName.length() == shortName.length()) return fqName.equals(shortName);
        int diff = fqName.length() - shortName.length();
        if (fqName.charAt(diff - 1) != separator) return false;
        return fqName.regionMatches(diff, shortName, 0, shortName.length());
    }

    /**
     * Strips class name from Object#toString if present.
     * To be used as custom data type renderer for java.lang.Object.
     * To activate just add {@code StringUtil.toShortString(this)}
     * expression in <em>Settings | Debugger | Data Views</em>.
     */
    @SuppressWarnings("UnusedDeclaration")
    static String toShortString(Object o) {
        if (o == null) return null;
        if (o instanceof CharSequence) return o.toString();
        String className = o.getClass().getName();
        String s = o.toString();
        if (!s.startsWith(className)) return s;
        return s.length() > className.length() && !Character.isLetter(s.charAt(className.length())) ?
                trimStart(s, className) : s;
    }


    public static boolean trimEnd(StringBuilder buffer, CharSequence end) {
        if (endsWith(buffer, end)) {
            buffer.delete(buffer.length() - end.length(), buffer.length());
            return true;
        }
        return false;
    }

    /**
     * Say smallPart = "op" and bigPart="open". Method returns true for "Ope" and false for "ops"
     */

    @SuppressWarnings("StringToUpperCaseOrToLowerCaseWithoutLocale")
    public static boolean isBetween(String string, String smallPart, String bigPart) {
        final String s = string.toLowerCase();
        return s.startsWith(smallPart.toLowerCase()) && bigPart.toLowerCase().startsWith(s);
    }

    /**
     * Does the string have an uppercase character?
     *
     * @param s the string to test.
     * @return true if the string has an uppercase character, false if not.
     */
    public static boolean hasUpperCaseChar(String s) {
        char[] chars = s.toCharArray();
        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does the string have a lowercase character?
     *
     * @param s the string to test.
     * @return true if the string has a lowercase character, false if not.
     */
    public static boolean hasLowerCaseChar(String s) {
        char[] chars = s.toCharArray();
        for (char c : chars) {
            if (Character.isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }

    private static final Pattern UNICODE_CHAR = Pattern.compile("\\\\u[0-9a-fA-F]{4}");

    public static String replaceUnicodeEscapeSequences(String text) {
        if (text == null) return null;

        final Matcher matcher = UNICODE_CHAR.matcher(text);
        if (!matcher.find()) return text; // fast path

        matcher.reset();
        int lastEnd = 0;
        final StringBuilder sb = new StringBuilder(text.length());
        while (matcher.find()) {
            sb.append(text, lastEnd, matcher.start());
            final char c = (char) Integer.parseInt(matcher.group().substring(2), 16);
            sb.append(c);
            lastEnd = matcher.end();
        }
        sb.append(text.substring(lastEnd));
        return sb.toString();
    }

    /**
     * Expirable CharSequence. Very useful to control external library execution time,
     * i.e. when java.util.regex.Pattern match goes out of control.
     */
    public abstract static class BombedCharSequence implements CharSequence {
        private final CharSequence delegate;
        private       int          i;
        private       boolean      myDefused;

        public BombedCharSequence(CharSequence sequence) {
            delegate = sequence;
        }

        @Override
        public int length() {
            check();
            return delegate.length();
        }

        @Override
        public char charAt(int i) {
            check();
            return delegate.charAt(i);
        }

        protected void check() {
            if (myDefused) {
                return;
            }
            if ((++i & 1023) == 0) {
                checkCanceled();
            }
        }

        public final void defuse() {
            myDefused = true;
        }


        @Override
        public String toString() {
            check();
            return delegate.toString();
        }

        protected abstract void checkCanceled();


        @Override
        public CharSequence subSequence(int i, int i1) {
            check();
            return delegate.subSequence(i, i1);
        }
    }


    public static String toHexString(byte[] bytes) {
        @SuppressWarnings("SpellCheckingInspection") String digits = "0123456789abcdef";
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) sb.append(digits.charAt((b >> 4) & 0xf)).append(digits.charAt(b & 0xf));
        return sb.toString();
    }


    public static byte[] parseHexString(String str) {
        int len = str.length();
        if (len % 2 != 0) throw new IllegalArgumentException("Non-even-length: " + str);
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**
     * @deprecated use {@link #startsWithConcatenation(String, String...)} (to remove in IDEA 15)
     */
    @Deprecated
    public static boolean startsWithConcatenationOf(String string, String firstPrefix, String secondPrefix) {
        return startsWithConcatenation(string, firstPrefix, secondPrefix);
    }

    /**
     * @return <code>true</code> if the passed string is not <code>null</code> and not empty
     * and contains only latin upper- or lower-case characters and digits; <code>false</code> otherwise.
     */

    public static boolean isLatinAlphanumeric(CharSequence str) {
        if (isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || Character.isDigit(c)) {
                continue;
            }
            return false;
        }
        return true;
    }
}