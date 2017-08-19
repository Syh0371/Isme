package cn.together.common.core.util;

import com.alibaba.fastjson.util.TypeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    public static class TimeUnit {
        public static final int MONTH = 1;
        public static final int QUARTER = 2;
        public static final int YEAR = 3;
    }

    public static final DateFormat STANDARD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat STANDARD_DATE_FORMAT_ACTIVITY = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
    public static final DateFormat STANDARD_DATE_HOUR_MINUTE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final DateFormat YEAR_MONTH_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("yyyy-MM");
    public static final DateFormat MONTH_DATE_FORMAT = new SimpleDateFormat("MM-dd");
    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public static final DateFormat ONE_LINE_YEAR_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private static final String[] WEEK_DAYS = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
    private static final String[] AM_PM = { "上午", "下午" };

    public static Date castToDate(String timeStr) {
        try {
            return TypeUtils.castToDate(timeStr);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getWeekDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return WEEK_DAYS[calendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    public static String todayYearMonthDayFormat() {
        return YEAR_MONTH_DATE_FORMAT.format(new Date());
    }

    public static String dayStartFormat(Date date) {
        return YEAR_MONTH_DATE_FORMAT.format(date) + " 00:00:00";
    }

    public static String dayEndFormat(Date date) {
        return YEAR_MONTH_DATE_FORMAT.format(date) + " 23:59:59";
    }

    public static boolean isSameDay(Date day1, Date day2) {
        return SHORT_DATE_FORMAT.format(day1).equals(SHORT_DATE_FORMAT.format(day2));
    }

    /**
     * 得到几天后的时间
     * @param d
     * @param day
     * @return
     */
    public static Date getDateAfter(Date d,int day){
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE,now.get(Calendar.DATE)+day);
        return now.getTime();
    }

    /**
     * 得到几天前的时间
     * @param d
     * @param day
     * @return
     */
    public static Date getDateBefore(Date d,int day){
        Calendar now =Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE,now.get(Calendar.DATE)-day);
        return now.getTime();
    }

    public static String getAmPm(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return AM_PM[calendar.get(Calendar.AM_PM)];
    }

    public static String formatAge(Date birthday) {
        float age = getAge(birthday);

        if (age <= 0) {
            return "未出生";
        } else if (age > 0 && age < 1) {
            int month = (int) (age * 12);
            if (month == 0) month = 1;
            return month + "个月";
        } else {
            return ((int) age) + "岁";
        }
    }

    private static float getAge(Date birthday) {
        Calendar calendar = Calendar.getInstance();
        int yearNow = calendar.get(Calendar.YEAR);
        int monthNow = calendar.get(Calendar.MONTH);
        calendar.setTime(birthday);
        int yearBorn = calendar.get(Calendar.YEAR);
        int monthBorn = calendar.get(Calendar.MONTH);

        int year = yearNow - yearBorn;
        if (year >= 1) return year;

        int month = monthNow - monthBorn;
        if (month > 0 && month < 1) month = 1;
        return month / 12.0F;
    }

    public static Date add(Date startTime, int time, int timeUnit) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);

        switch (timeUnit) {
            case TimeUnit.MONTH:
                calendar.add(Calendar.MONTH, time);
                break;
            case TimeUnit.QUARTER:
                calendar.add(Calendar.MONTH, time * 3);
                break;
            case TimeUnit.YEAR:
                calendar.add(Calendar.YEAR, time);
                break;
            default: break;
        }

        return calendar.getTime();
    }

    public static String formatAddTime(Date addTime) {
        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        int curMonth = calendar.get(Calendar.MONTH);
        int curDay = calendar.get(Calendar.DATE);

        calendar.setTime(addTime);
        int addYear = calendar.get(Calendar.YEAR);
        int addMonth = calendar.get(Calendar.MONTH);
        int addDay = calendar.get(Calendar.DATE);

        if (addYear != curYear) return YEAR_MONTH_DATE_FORMAT.format(addTime);
        if (addMonth != curMonth || addDay != curDay) return MONTH_DATE_FORMAT.format(addTime);
        return TIME_FORMAT.format(addTime);
    }

    public static String getWeek(Date date) {
        String[] weeks = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index < 0) {
            week_index = 0;
        }
        return weeks[week_index];
    }
}
