// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.xin.util.match;


import com.sun.istack.internal.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Stripped-down version of {@code com.intellij.openapi.util.text.StringUtil}.
 * Intended to use by external (out-of-IDE-process) runners and helpers so it should not contain any library dependencies.
 *
 * @since 12.0
 */
public class StringUtilRt {

    public static boolean charsEqualIgnoreCase(char a, char b) {
        return a == b || toUpperCase(a) == toUpperCase(b) || toLowerCase(a) == toLowerCase(b);
    }


    public static CharSequence toUpperCase(CharSequence s) {
        StringBuilder answer = null;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            char upCased = toUpperCase(c);
            if (answer == null && upCased != c) {
                answer = new StringBuilder(s.length());
                answer.append(s.subSequence(0, i));
            }
            if (answer != null) {
                answer.append(upCased);
            }
        }
        return answer == null ? s : answer;
    }


    public static char toUpperCase(char a) {
        if (a < 'a') return a;
        if (a <= 'z') return (char) (a + ('A' - 'a'));
        return Character.toUpperCase(a);
    }


    public static char toLowerCase(char a) {
        if (a < 'A' || a >= 'a' && a <= 'z') return a;
        if (a <= 'Z') return (char) (a + ('a' - 'A'));
        return Character.toLowerCase(a);
    }

    /**
     * Converts line separators to {@code "\n"}
     */


    public static String convertLineSeparators(String text) {
        return convertLineSeparators(text, false);
    }


    public static String convertLineSeparators(String text, boolean keepCarriageReturn) {
        return convertLineSeparators(text, "\n", null, keepCarriageReturn);
    }


    public static String convertLineSeparators(String text, String newSeparator) {
        return convertLineSeparators(text, newSeparator, null);
    }


    public static CharSequence convertLineSeparators(CharSequence text, String newSeparator) {
        return unifyLineSeparators(text, newSeparator, null, false);
    }


    public static String convertLineSeparators(String text, String newSeparator, @Nullable int[] offsetsToKeep) {
        return convertLineSeparators(text, newSeparator, offsetsToKeep, false);
    }


    public static String convertLineSeparators(String text,
                                               String newSeparator,
                                               @Nullable int[] offsetsToKeep,
                                               boolean keepCarriageReturn) {
        return unifyLineSeparators(text, newSeparator, offsetsToKeep, keepCarriageReturn).toString();
    }


    private static CharSequence unifyLineSeparators(CharSequence text,
                                                    String newSeparator,
                                                    @Nullable int[] offsetsToKeep,
                                                    boolean keepCarriageReturn) {
        StringBuilder buffer = null;
        int intactLength = 0;
        boolean newSeparatorIsSlashN = "\n".equals(newSeparator);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                if (!newSeparatorIsSlashN) {
                    if (buffer == null) {
                        buffer = new StringBuilder(text.length());
                        buffer.append(text, 0, intactLength);
                    }
                    buffer.append(newSeparator);
                    shiftOffsets(offsetsToKeep, buffer.length(), 1, newSeparator.length());
                } else if (buffer == null) {
                    intactLength++;
                } else {
                    buffer.append(c);
                }
            } else if (c == '\r') {
                boolean followedByLineFeed = i < text.length() - 1 && text.charAt(i + 1) == '\n';
                if (!followedByLineFeed && keepCarriageReturn) {
                    if (buffer == null) {
                        intactLength++;
                    } else {
                        buffer.append(c);
                    }
                    continue;
                }
                if (buffer == null) {
                    buffer = new StringBuilder(text.length());
                    buffer.append(text, 0, intactLength);
                }
                buffer.append(newSeparator);
                if (followedByLineFeed) {
                    //noinspection AssignmentToForLoopParameter
                    i++;
                    shiftOffsets(offsetsToKeep, buffer.length(), 2, newSeparator.length());
                } else {
                    shiftOffsets(offsetsToKeep, buffer.length(), 1, newSeparator.length());
                }
            } else {
                if (buffer == null) {
                    intactLength++;
                } else {
                    buffer.append(c);
                }
            }
        }
        return buffer == null ? text : buffer;
    }

    private static void shiftOffsets(int[] offsets, int changeOffset, int oldLength, int newLength) {
        if (offsets == null) return;
        int shift = newLength - oldLength;
        if (shift == 0) return;
        for (int i = 0; i < offsets.length; i++) {
            int offset = offsets[i];
            if (offset >= changeOffset + oldLength) {
                offsets[i] += shift;
            }
        }
    }


    public static int parseInt(@Nullable String string, int defaultValue) {
        if (string != null) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }


    public static long parseLong(@Nullable String string, long defaultValue) {
        if (string != null) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }


    public static double parseDouble(@Nullable String string, double defaultValue) {
        if (string != null) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }


    static <E extends Enum<E>> E parseEnum(String string, E defaultValue, Class<E> clazz) {
        try {
            return Enum.valueOf(clazz, string);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    public static String getShortName(Class aClass) {
        return getShortName(aClass.getName());
    }


    public static String getShortName(String fqName) {
        return getShortName(fqName, '.');
    }


    public static String getShortName(String fqName, char separator) {
        int lastPointIdx = fqName.lastIndexOf(separator);
        if (lastPointIdx >= 0) {
            return fqName.substring(lastPointIdx + 1);
        }
        return fqName;
    }


    public static boolean endsWithChar(@Nullable CharSequence s, char suffix) {
        return s != null && s.length() != 0 && s.charAt(s.length() - 1) == suffix;
    }


    public static boolean startsWithIgnoreCase(String str, String prefix) {
        int stringLength = str.length();
        int prefixLength = prefix.length();
        return stringLength >= prefixLength && str.regionMatches(true, 0, prefix, 0, prefixLength);
    }


    public static boolean endsWithIgnoreCase(CharSequence text, CharSequence suffix) {
        int l1 = text.length();
        int l2 = suffix.length();
        if (l1 < l2) return false;

        for (int i = l1 - 1; i >= l1 - l2; i--) {
            if (!charsEqualIgnoreCase(text.charAt(i), suffix.charAt(i + l2 - l1))) {
                return false;
            }
        }

        return true;
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
        start = Math.max(start, 0);
        for (int i = Math.min(end, s.length()) - 1; i >= start; i--) {
            if (s.charAt(i) == c) return i;
        }
        return -1;
    }


    public static boolean isEmpty(@Nullable CharSequence cs) {
        return cs == null || cs.length() == 0;
    }


    public static boolean isEmptyOrSpaces(@Nullable CharSequence s) {
        if (isEmpty(s)) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > ' ') {
                return false;
            }
        }
        return true;
    }


    public static String notNullize(@Nullable String s) {
        return notNullize(s, "");
    }


    public static String notNullize(@Nullable String s, String defaultValue) {
        return s == null ? defaultValue : s;
    }


    public static List<String> splitHonorQuotes(String s, char separator) {
        List<String> result = new ArrayList<String>();
        StringBuilder builder = new StringBuilder(s.length());
        boolean inQuotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == separator && !inQuotes) {
                if (builder.length() > 0) {
                    result.add(builder.toString());
                    builder.setLength(0);
                }
                continue;
            }
            if ((c == '"' || c == '\'') && !(i > 0 && s.charAt(i - 1) == '\\')) {
                inQuotes = !inQuotes;
            }
            builder.append(c);
        }
        if (builder.length() > 0) {
            result.add(builder.toString());
        }
        return result;
    }


    public static String formatFileSize(long fileSize) {
        return formatFileSize(fileSize, " ");
    }


    public static String formatFileSize(long fileSize, String unitSeparator) {
        if (fileSize < 0) throw new IllegalArgumentException("Invalid value: " + fileSize);
        if (fileSize == 0) return '0' + unitSeparator + 'B';
        int rank = (int) ((Math.log10(fileSize) + 0.0000021714778384307465) / 3);  // (3 - Math.log10(999.995))
        double value = fileSize / Math.pow(1000, rank);
        String[] units = {"B", "kB", "MB", "GB", "TB", "PB", "EB"};
        return new DecimalFormat("0.##").format(value) + unitSeparator + units[rank];
    }


    public static boolean isQuotedString(String s) {
        return s.length() > 1 && (s.charAt(0) == '\'' || s.charAt(0) == '\"') && s.charAt(0) == s.charAt(s.length() - 1);
    }


    public static String unquoteString(String s) {
        return isQuotedString(s) ? s.substring(1, s.length() - 1) : s;
    }


    public static String unquoteString(String s, char quotationChar) {
        boolean quoted = s.length() > 1 && quotationChar == s.charAt(0) && quotationChar == s.charAt(s.length() - 1);
        return quoted ? s.substring(1, s.length() - 1) : s;
    }
}