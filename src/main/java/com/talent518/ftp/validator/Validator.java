package com.talent518.ftp.validator;

import java.util.Formatter;

import com.talent518.ftp.dao.Settings;

public abstract class Validator {
	private String message;

	public Validator() {
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String format, Object... args) {
		try {
			format = Settings.language().getString(format);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (args.length == 0)
			message = format;
		else
			message = new Formatter().format(format, args).toString();
	}

	public abstract boolean validate(String val);
}
