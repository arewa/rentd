package com.rentd.siteparsers.ria;

public class RiaUaOffices extends RiaUaSiteParser {

	public RiaUaOffices(int pageNum) {
		super(pageNum);
	}

	@Override
	protected int getCategory() {
		return 10;
	}

	@Override
	protected String getType() {
		return "Офис";
	}
	
}
