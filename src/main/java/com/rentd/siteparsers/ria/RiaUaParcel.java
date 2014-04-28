package com.rentd.siteparsers.ria;

public class RiaUaParcel extends RiaUaSiteParser {

	public RiaUaParcel(int pageNum) {
		super(pageNum);
	}

	@Override
	protected int getCategory() {
		return 24;
	}

	@Override
	protected String getType() {
		return "Земельный участок";
	}
	
}

