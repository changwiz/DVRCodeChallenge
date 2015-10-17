package com.groupon.codechallenge.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
	private Utils() {

	}

	public static Date maxDate(Date a, Date b) {
		if (a.after(b)) {
			return a;
		} else {
			return b;
		}
	}

	public static Date minDate(Date a, Date b) {
		if (a.before(b)) {
			return a;
		} else {
			return b;
		}
	}

	public static String printDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy kk:mm:ss");
		return format.format(date);
	}

	public static int compareDate(Date a, Date b) {
		if (a.before(b)) {
			return -1;
		} else if (a.after(b)) {
			return 1;
		} else {
			return 0;
		}
	}
}
