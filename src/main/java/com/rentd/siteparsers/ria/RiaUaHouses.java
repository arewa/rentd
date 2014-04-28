package com.rentd.siteparsers.ria;

public class RiaUaHouses extends RiaUaSiteParser {

	public RiaUaHouses(int pageNum) {
		super(pageNum);
	}

	@Override
	protected int getCategory() {
		return 4;
	}

	@Override
	protected String getType() {
		return "Дом";
	}
	
}
