package com.pit.core.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * DateUtil
 *
 * @author gy
 * @date 2020/3/20
 */
public class DateUtil {
	private DateUtil() {
	}

	/**
	 * 返回当前秒数
	 * @return
	 */
	public static Long currentTimeSeconds() {
		return System.currentTimeMillis() / 1000;
	}

	public static Long getCurrentDayDesignationHour(int hour) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		return calendar.getTime().getTime() / 1000;
	}

	public static Long getNextDayDesignationHour(int hour) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		return calendar.getTime().getTime() / 1000;
	}

	public static Long getPreDayDesignationHour(int hour) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		return calendar.getTime().getTime() / 1000;
	}

	public static int convertToDateNumber(Long millis) {
		Date date = new Date(millis);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		return Integer.valueOf(dateFormat.format(date));
	}

	public static int currentDateNumber() {
		return convertToDateNumber(System.currentTimeMillis());
	}

	public static int nextDateNumber() {
		return convertToDateNumber(getNextDayDesignationHour(0) * 1000);
	}

	public static int preDateNumber() {
		return convertToDateNumber(getPreDayDesignationHour(0) * 1000);
	}

	/**
	 * 功能描述：返回月
	 *
	 * @param date
	 *            Date 日期
	 * @return 返回月份
	 */
	public static int getMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * 功能描述：返回月的天
	 *
	 * @param date
	 *            Date 日期
	 * @return 返回日份
	 */
	public static int getDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 功能描述：返回小时
	 *
	 * @param date
	 *            日期
	 * @return 返回小时
	 */
	public static int getHour(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 功能描述：返回分
	 *
	 * @param date
	 *            日期
	 * @return 返回分钟
	 */
	public static int getMinute(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MINUTE);
	}

	/**
	 * 返回秒钟
	 *
	 * @param date
	 *            Date 日期
	 * @return 返回秒钟
	 */
	public static int getSecond(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.SECOND);
	}

	/**
	 * 功能描述：返回毫秒
	 *
	 * @param date
	 *            日期
	 * @return 返回毫
	 */
	public static long getMillis(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getTimeInMillis();
	}
}