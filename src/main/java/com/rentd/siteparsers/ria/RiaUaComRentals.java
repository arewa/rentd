package com.rentd.siteparsers.ria;

public class RiaUaComRentals extends RiaUaSiteParser {

	public RiaUaComRentals(int pageNum) {
		super(pageNum);
	}

	@Override
	protected int getCategory() {
		return 13;
	}

	@Override
	protected String getType() {
		return "Коммерческая недвижимость";
	}
	
}
