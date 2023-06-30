package com.xin.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author 497668869@qq.com
 * @since 1.0
 */
@Slf4j
public class FuzzyMatchUtils {


    public static boolean match(String name, String keyword) {
        return match(name.toLowerCase(), keyword.toLowerCase(), 0, 0);
    }

    private static boolean match(String name, String keyword, int nameIndex, int keywordIndex) {
        if (keyword.length() > 10) {
            //当key长度较大是, 使用contains可以加快匹配速度
            return name.contains(keyword);
        }
        if (nameIndex > name.length()
                || name.length() - nameIndex < keyword.length() - keywordIndex) {
            return false;
        }
        if (keyword.length() == keywordIndex) {
            return true;
        }
        for (int i = nameIndex; i < name.length(); i++) {
            if (i == 0
                    || (Character.isUpperCase(name.charAt(i)))
                    || (name.charAt(i) > 'z' || name.charAt(i) < 'a')
                    || ((name.charAt(i - 1) > 'z' || name.charAt(i - 1) < 'a') && (name.charAt(i - 1) > 'Z' || name.charAt(i - 1) < 'A'))) {

                if (keyword.charAt(keywordIndex) == Character.toLowerCase(name.charAt(i))) {
                    if (match(name, keyword, i + 1, keywordIndex + 1)) {
                        return true;
                    }
                }
            }
        }
        if (nameIndex != 0) {
            if (keyword.charAt(keywordIndex) == Character.toLowerCase(name.charAt(nameIndex))) {
                if (match(name, keyword, nameIndex + 1, keywordIndex + 1)) {
                    return true;
                }
            }
        }

        return false;
    }
}
