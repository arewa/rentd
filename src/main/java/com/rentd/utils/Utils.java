package com.rentd.utils;

import java.util.ArrayList;
import java.util.List;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

public class Utils {
	public static String cleanString(String s) {
		if (s == null || "".equals(s)) {
			return "";
		}

		return s.replace("\r\n", " ").replace("\n", " ")
				.replaceAll("\\s+", " ").trim();
	}

	public static String getHtmlText(TagNode n, String expr) {
		Object[] els;
		try {
			els = n.evaluateXPath(expr);
		} catch (XPatherException e1) {
			return "";
		}

		for (Object e : els) {
			if (e instanceof TagNode) {
				TagNode t = (TagNode) e;
				return Utils.cleanString(t.getText().toString());
			}
		}

		return "";
	}

	public static String[] getHtmlTexts(TagNode n, String expr) {
		List<String> r = new ArrayList<String>();
		Object[] els;
		try {
			els = n.evaluateXPath(expr);
		} catch (XPatherException e1) {
			return new String[]{};
		}

		for (Object e : els) {
			if (e instanceof TagNode) {
				TagNode t = (TagNode) e;
				r.add(t.getText().toString());
			}
		}

		return r.toArray(new String[r.size()]);
	}
	
	public static String getHtmlAttr(TagNode n, String expr) {
		Object[] els;
		try {
			els = n.evaluateXPath(expr);
		} catch (XPatherException e1) {
			return "";
		}

		for (Object e : els) {
			if (e instanceof String) {
				return Utils.cleanString(String.valueOf(e));
			}
		}

		return "";
	}
}
