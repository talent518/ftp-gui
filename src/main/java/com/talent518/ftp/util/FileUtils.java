package com.talent518.ftp.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

	public static void deleteDirectory(File file) throws SecurityException, IOException {
		deleteDirectory(file, null);
	}

	public static void deleteDirectory(File file, DeleteListener listener) throws SecurityException, IOException {
		if (!file.exists())
			return;

		if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
			File[] files = file.listFiles();
			if (files != null) {
				if (listener != null)
					listener.ls(file.getAbsolutePath());
				for (File f : files) {
					if (f.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
						deleteDirectory(f, listener);
						if (listener != null)
							listener.rmdir(f.getAbsolutePath());
					} else {
						f.delete();
						if (listener != null)
							listener.unlink(f.getAbsolutePath());
					}
				}
			}
			file.delete();
		} else {
			file.delete();
		}
	}

	public interface DeleteListener {
		public void ls(String path);

		public void rmdir(String path);

		public void unlink(String path);
	}
}
