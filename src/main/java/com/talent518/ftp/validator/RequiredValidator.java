package com.talent518.ftp.validator;

public class RequiredValidator extends Validator {
	public RequiredValidator() {
		super();
		
		setMessage("validator.required");
	}
	
	@Override
	public boolean validate(String val) {
		return val != null && val.length() > 0;
	}
}
