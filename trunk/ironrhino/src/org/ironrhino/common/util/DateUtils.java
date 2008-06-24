package org.ironrhino.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	private static final DateFormat date10 = new SimpleDateFormat("yyyy-MM-dd");

	private static final DateFormat date8 = new SimpleDateFormat("yyyyMMdd");

	private static final DateFormat time6 = new SimpleDateFormat("HHmmss");

	private static final DateFormat datetime = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private static final DateFormat timestamp = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSSS");

	public static String getDate10(Date date) {
		return date10.format(date);
	}

	public static String getDate8(Date date) {
		return date8.format(date);
	}

	public static String getTime6(Date date) {
		return time6.format(date);
	}

	public static String getTimestamp(Date date) {
		return timestamp.format(date);
	}

	public static String getDatetime(Date date) {
		return datetime.format(date);
	}

	public static Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, days);
		return cal.getTime();
	}

	public static Date parseDate10(String string) {
		try {
			return date10.parse(string);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Date parseDatetime(String string) {
		try {
			return datetime.parse(string);
		} catch (ParseException e) {
			return null;
		}
	}

	public static boolean isMonthEnd(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH) == cal
				.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public static boolean isYearEnd(Date date) {
		return getDate10(date).endsWith("12-31");
	}

	public static boolean isMonthStart(Date date) {
		return getDate10(date).endsWith("01");
	}

	public static boolean isYearStart(Date date) {
		return getDate10(date).endsWith("01-01");
	}

	public static String humanRead(Date date) {
		Date now = new Date();
		long delta = now.getTime() - date.getTime();
		boolean before = (delta >= 0);
		delta = delta < 0 ? -delta : delta;
		delta /= 1000;
		String s;
		if (delta <= 60) {
			return "1分钟内";
		} else if (delta < 3600) {
			delta /= 60;
			if (delta == 30)
				s = "半个小时";
			else
				s = delta + "分钟";
		} else if (delta < 86400) {
			double d = delta / 3600d;
			long h = (long) d;
			long m = (long) ((d - h) * 3600);
			m /= 60;
			if (m == 0)
				m = 1;
			if (m == 30)
				s = h + "个半小时";
			else
				s = h + "个小时" + m + "分钟";
		} else if (delta < 2592000) {
			s = delta / 86400 + "天";
		} else if (delta < 31104000) {
			s = delta / 2592000 + "个月";
		} else {
			s = delta / 31104000 + "年";
		}
		return s + (before ? "前" : "后");
	}

}
