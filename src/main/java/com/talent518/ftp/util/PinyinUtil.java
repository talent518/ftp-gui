package com.talent518.ftp.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtil {
	public static String toPinyin(String chinese) {
		String pinyinStr = "";
		char[] newChar = chinese.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		for (int i = 0; i < newChar.length; i++) {
			if (newChar[i] > 128) {
				try {
					pinyinStr += PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat)[0];
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pinyinStr += newChar[i];
			}
		}
		return pinyinStr;
	}

	public static int compareTo(String str1, String str2) {
		int len1 = str1.length();
		int len2 = str2.length();
		int lim = Math.min(len1, len2);
		char v1[] = str1.toCharArray();
		char v2[] = str2.toCharArray();

		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

		int k = 0;
		while (k < lim) {
			char c1 = v1[k];
			char c2 = v2[k];
			if (c1 != c2) {
				if (c1 > 128 && c2 <= 128)
					return 1;
				else if (c1 <= 128 && c2 > 128)
					return -1;
				else if (c1 > 128 && c2 > 128)
					try {
						return PinyinHelper.toHanyuPinyinStringArray(c1, defaultFormat)[0].compareTo(PinyinHelper.toHanyuPinyinStringArray(c2, defaultFormat)[0]);
					} catch (Throwable e) {
						return c1 - c2;
					}
				else
					return c1 - c2;
			}
			k++;
		}
		return len1 - len2;
	}

	public static int compareTo2(String str1, String str2) {
		return PinyinUtil.toPinyin(str1).compareTo(PinyinUtil.toPinyin(str2));
	}
}
