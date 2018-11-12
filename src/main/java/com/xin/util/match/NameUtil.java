// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.xin.util.match;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NameUtil {
    private static final Function<String, String> LOWERCASE_MAPPING = new Function<String, String>() {
        @Override
        public String apply(String s) {
            return s.toLowerCase();
        }

    };
    private static final int                      MAX_LENGTH        = 40;

    private NameUtil() {
    }


    public static String[] nameToWords(String name) {
        List<String> array = new ArrayList<String>();
        int index = 0;

        while (index < name.length()) {
            int wordStart = index;
            int upperCaseCount = 0;
            int lowerCaseCount = 0;
            int digitCount = 0;
            int specialCount = 0;
            while (index < name.length()) {
                char c = name.charAt(index);
                if (Character.isDigit(c)) {
                    if (upperCaseCount > 0 || lowerCaseCount > 0 || specialCount > 0) break;
                    digitCount++;
                } else if (Character.isUpperCase(c)) {
                    if (lowerCaseCount > 0 || digitCount > 0 || specialCount > 0) break;
                    upperCaseCount++;
                } else if (Character.isLowerCase(c)) {
                    if (digitCount > 0 || specialCount > 0) break;
                    if (upperCaseCount > 1) {
                        index--;
                        break;
                    }
                    lowerCaseCount++;
                } else {
                    if (upperCaseCount > 0 || lowerCaseCount > 0 || digitCount > 0) break;
                    specialCount++;
                }
                index++;
            }
            String word = name.substring(wordStart, index);
            if (!StringUtil.isEmptyOrSpaces(word)) {
                array.add(word);
            }
        }
        return array.toArray(new String[array.size()]);
    }


    public static String buildRegexp(String pattern, int exactPrefixLen, boolean allowToUpper, boolean allowToLower) {
        return buildRegexp(pattern, exactPrefixLen, allowToUpper, allowToLower, false, false);
    }


    public static String buildRegexp(String pattern,
                                     int exactPrefixLen,
                                     boolean allowToUpper,
                                     boolean allowToLower,
                                     boolean lowerCaseWords,
                                     boolean forCompletion) {
        final int eol = pattern.indexOf('\n');
        if (eol != -1) {
            pattern = pattern.substring(0, eol);
        }
        if (pattern.length() >= MAX_LENGTH) {
            pattern = pattern.substring(0, MAX_LENGTH);
        }

        final StringBuilder buffer = new StringBuilder();
        final boolean endsWithSpace = !forCompletion && StringUtil.endsWithChar(pattern, ' ');
        if (!forCompletion) {
            pattern = pattern.trim();
        }
        exactPrefixLen = Math.min(exactPrefixLen, pattern.length());
    /*final boolean uppercaseOnly = containsOnlyUppercaseLetters(pattern.substring(exactPrefixLen));
    if (uppercaseOnly) {
      allowToLower = false;
    }*/
        boolean prevIsUppercase = false;
        if (exactPrefixLen > 0) {
            char c = pattern.charAt(exactPrefixLen - 1);
            prevIsUppercase = Character.isUpperCase(c) || Character.isDigit(c);
        }

        for (int i = 0; i != exactPrefixLen; ++i) {
            final char c = pattern.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                buffer.append(c);
            } else {
                // for standard RegExp engine
                // buffer.append("\\u");
                // buffer.append(Integer.toHexString(c + 0x20000).substring(1));

                // for OROMATCHER RegExp engine
                buffer.append("\\").append(c);
                //buffer.append(Integer.toHexString(c + 0x20000).substring(2));
            }
        }

        if (exactPrefixLen == 0) {
            buffer.append("_*");  // ignore leading underscores
        }

        boolean firstIdentifierLetter = exactPrefixLen == 0;
        boolean lastIsUppercase = false;
        for (int i = exactPrefixLen; i < pattern.length(); i++) {
            final char c = pattern.charAt(i);
            lastIsUppercase = false;
            if (Character.isLetterOrDigit(c)) {
                prevIsUppercase = false;

                // This logic allows to use uppercase letters only to catch the name like PDM for PsiDocumentManager
                if (Character.isUpperCase(c) || Character.isDigit(c)) {
                    prevIsUppercase = true;
                    lastIsUppercase = true;

                    buffer.append('(');

                    if (!firstIdentifierLetter) {
                        buffer.append("[a-z\\s0-9\\$]*");
                    }

                    buffer.append(c);
                    if (allowToLower) {
                        buffer.append('|');
                        buffer.append(Character.toLowerCase(c));
                    }
                    if (!firstIdentifierLetter) {
                        buffer.append("|[A-Za-z\\s0-9\\$]*[_-]+[");
                        buffer.append(c);
                        buffer.append(Character.toLowerCase(c));
                        buffer.append("]");
                    }
                    buffer.append(')');
                } else if (Character.isLowerCase(c) && allowToUpper) {
                    buffer.append('[');
                    buffer.append(c);
                    buffer.append(Character.toUpperCase(c));
                    buffer.append(']');
                    if (lowerCaseWords) {
                        buffer.append("([a-z\\s0-9\\$]*[-_]+)?");
                    }
                } else {
                    buffer.append(c);
                }

                firstIdentifierLetter = false;
            } else if (c == '*') {
                buffer.append(".*");
                firstIdentifierLetter = true;
            } else if (c == '.') {
                if (!firstIdentifierLetter) {
                    buffer.append("[a-z\\s0-9\\$]*\\.");
                } else {
                    buffer.append("\\.");
                }
                firstIdentifierLetter = true;
            } else if (c == ' ') {
                buffer.append("([a-z\\s0-9\\$_-]*[\\ _-]+)+");
                firstIdentifierLetter = true;
            } else {
                if (c == ':' || prevIsUppercase) {
                    buffer.append("[A-Za-z\\s0-9\\$]*");
                }

                firstIdentifierLetter = true;
                // for standard RegExp engine
                // buffer.append("\\u");
                // buffer.append(Integer.toHexString(c + 0x20000).substring(1));

                // for OROMATCHER RegExp engine
                buffer.append("\\").append(c);
                //buffer.append(Integer.toHexString(c + 0x20000).substring(3));
            }
        }

        if (!endsWithSpace) {
            buffer.append(".*");
        } else if (lastIsUppercase) {
            buffer.append("[a-z\\s0-9\\$]*");
        }

        //System.out.println("rx = " + buffer.toString());
        return buffer.toString();
    }

    /**
     * Splits an identifier into words, separated with underscores or upper-case characters
     * (camel-case).
     *
     * @param name the identifier to split.
     * @return the array of strings into which the identifier has been split.
     */

    public static String[] splitNameIntoWords(String name) {
        final String[] underlineDelimited = name.split("_");
        List<String> result = new ArrayList<String>();
        for (String word : underlineDelimited) {
            addAllWords(word, result);
        }
        return result.toArray(new String[result.size()]);
    }


    static int nextWord(String text, int start) {
        if (!Character.isLetterOrDigit(text.charAt(start))) {
            return start + 1;
        }

        int i = start;
        while (i < text.length() && Character.isDigit(text.charAt(i))) i++;
        if (i > start) {
            // digits form a separate hump
            return i;
        }

        while (i < text.length() && Character.isUpperCase(text.charAt(i))) i++;

        if (i > start + 1) {
            // several consecutive uppercase letter form a hump
            if (i == text.length() || !Character.isLetter(text.charAt(i))) {
                return i;
            }
            return i - 1;
        }

        if (i == start) i++;
        while (i < text.length() && Character.isLetter(text.charAt(i)) && !isWordStart(text, i)) i++;
        return i;
    }

    private static void addAllWords(String text, List<? super String> result) {
        int start = 0;
        while (start < text.length()) {
            int next = nextWord(text, start);
            result.add(text.substring(start, next));
            start = next;
        }
    }

    static boolean isWordStart(String text, int i) {
        char c = text.charAt(i);
        if (Character.isUpperCase(c)) {
            if (i > 0 && Character.isUpperCase(text.charAt(i - 1))) {
                // check that we're not in the middle of an all-caps word
                return i + 1 < text.length() && Character.isLowerCase(text.charAt(i + 1));
            }
            return true;
        }
        if (Character.isDigit(c)) {
            return true;
        }
        if (!Character.isLetter(c)) {
            return false;
        }
        return i == 0 || !Character.isLetterOrDigit(text.charAt(i - 1)) || isHardCodedWordStart(text, i);
    }

    private static boolean isHardCodedWordStart(String text, int i) {
        return text.charAt(i) == 'l' &&
                i < text.length() - 1 && text.charAt(i + 1) == 'n' &&
                (text.length() == i + 2 || isWordStart(text, i + 2));
    }


    public static String splitWords(String text, char separator, Function<? super String, String> transformWord) {
        final String[] words = nameToWords(text);
        boolean insertSeparator = false;
        final StringBuilder buf = new StringBuilder();
        for (String word : words) {
            if (!Character.isLetterOrDigit(word.charAt(0))) {
                buf.append(separator);
                insertSeparator = false;
                continue;
            }
            if (insertSeparator) {
                buf.append(separator);
            } else {
                insertSeparator = true;
            }
            buf.append(transformWord.apply(word));
        }
        return buf.toString();

    }


}
