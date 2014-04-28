package com.rentd.siteparsers.ria;

public class RiaUaGarages extends RiaUaSiteParser {

	public RiaUaGarages(int pageNum) {
		super(pageNum);
	}

	@Override
	protected int getCategory() {
		return 30;
	}

	@Override
	protected String getType() {
		return "Гараж";
	}
	
}
