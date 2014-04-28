package com.rentd.siteparsers.ria;

public class RiaUaAppartments extends RiaUaSiteParser {

	public RiaUaAppartments(int pageNum) {
		super(pageNum);
	}

	@Override
	protected int getCategory() {
		return 1;
	}

	@Override
	protected String getType() {
		return "Квартира";
	}
	
}
