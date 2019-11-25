package com.talent518.ftp.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class FileUtils {
	private static final String[] units = { "", "K", "M", "G", "T" };
	private static final double[] sizes = { 1, 1024, 1024 * 1024, 1024 * 1024 * 1024, 1024 * 1024 * 1024 * 1024 };

	public static String formatSize(long size) {
		if (size <= 0)
			return "0";

		int i = (int) (Math.log(size) / Math.log(1024));
		if (i >= units.length)
			i = units.length - 1;

		NumberFormat df = DecimalFormat.getInstance();
		df.setMaximumFractionDigits(2);

		return df.format(size / sizes[i]) + units[i];
	}
}
