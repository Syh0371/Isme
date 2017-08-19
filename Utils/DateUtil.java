package cn.together.common.util;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateUtil {

    public DateUtil(){}
    public static java.sql.Date replaceDate(java.util.Date date){

        return new java.sql.Date(date.getTime());
    }

    /**
     * 获取日期
     * @param x
     * @return
     */
    public static String getDate(int x) {
        String dateTime = "";
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        switch(x) {
            case 1:
                dateTime = sdf.format(calendar.getTime()); //获取当前时间
                break;
            case 2:
                calendar.add(Calendar.DAY_OF_MONTH, -1); //获取当前时间前一天
                dateTime = sdf.format(calendar.getTime());
                break;
            case 3:
                calendar.add(Calendar.DAY_OF_MONTH, 1); //获取当前时间后一天
                dateTime = sdf.format(calendar.getTime());
                break;
            case 4:
                dateTime = getWeekDate(QuantityUtil.STATUS_FOUR);//获取上周第一天
                break;
            case 5:
                dateTime = getWeekDate(QuantityUtil.STATUS_FIVE);//获取上周最后一天
                break;
            case 6:
                dateTime = getWeekDate(QuantityUtil.STATUS_SIX);//获取本周第一天
                break;
            case 7:
                dateTime = getWeekDate(QuantityUtil.STATUS_SEVEN);//获取本周最后一天
                break;
            case 8:
                dateTime = getUpMonth(QuantityUtil.STATUS_EIGHT);//获取获取上个月的第一天
                break;
            case 9:
                dateTime = getWeekDate(QuantityUtil.STATUS_NINE);//获取获取上个月的最后一天
                break;
            case 10:
                dateTime = getMonthStart();//获取获取本月的第一天
                break;
            case 11:
                dateTime = getMonthEnd();//获取获取本月的最后一天
                break;
            default:
                dateTime = sdf.format(calendar.getTime());//默认返回当前时间
                break;

        }

        return dateTime;
    }

    /**
     * 本周或上周的第一天或最后一天
     * @param x 4上周第一天 5上周最后一天 6本周第一天 7本周最后一天
     * @return
     */
    private static String getWeekDate(int x){
        String dateTime = "";
        Calendar calendar = Calendar.getInstance();
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int n = calendar.get(Calendar.DAY_OF_WEEK);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (n == 1) {
            n = 7;
        } else {
            n = n - 1;
        }

        switch(x) {
            case 4:
                calendar.set(Calendar.DAY_OF_MONTH, date - 6 - n);
                dateTime = sdf.format(calendar.getTime());
                break;
            case 5:
                calendar.set(Calendar.DAY_OF_MONTH, date - n);
                dateTime = sdf.format(calendar.getTime());
                break;
            case 6:
                calendar.set(Calendar.DAY_OF_MONTH, date + 1 - n);
                dateTime = sdf.format(calendar.getTime());
                break;
            case 7:
                calendar.set(Calendar.DAY_OF_MONTH, date + 7 - n);
                dateTime = sdf.format(calendar.getTime());
                break;
            default:
                calendar.set(Calendar.DAY_OF_MONTH, date + 1);
                dateTime = sdf.format(calendar.getTime());
                break;

        }

        return dateTime;
    }

    /**
     * 获取上个月的第一天和最后一天
     * @param x 8为第一天，9为最后一天
     * @return
     */
    private static String getUpMonth(int x) {
        String monthStr = "";
        Date nowdate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        /* 设置为当前时间 */
        cal.setTime(nowdate);
//        System.out.println("当前时间是：" + sdf.format(nowdate));
        /* 当前日期月份-1 */
        cal.add(Calendar.MONTH, -1);
        if (x == 8) {
            // 得到前一个月的第一天
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            System.out.println("上个月的第一天是：" + sdf.format(cal.getTime()));
            monthStr = sdf.format(cal.getTime());
        }else{
            // 得到前一个月的最后一天
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            System.out.println("上个月的最后一天是：" + sdf.format(cal.getTime()));
            monthStr = sdf.format(cal.getTime());
        }

        return monthStr;
    }

    /**
     * 获取当月第一天
     * @return
     */
    private static String  getMonthStart() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int index = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.add(Calendar.DATE, (1 - index));

        return sdf.format(calendar.getTime());
    }


    /**
     * 获取当月最后一天
     * @return
     */
    private static String getMonthEnd() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        int index = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.add(Calendar.DATE, (-index));

        return sdf.format(calendar.getTime());
    }

    /**
     * 日期转换成字符串
     * @param date
     * @return str
     */
    public static String DateToStr(Date date) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(date);
        return str;
    }

    /**
     * 获取当天23：59：59
     * @return
     */
    public static Date getnewDateEnd() {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY, 23);
        calendar1.set(Calendar.MINUTE, 59);
        calendar1.set(Calendar.SECOND, 59);
        calendar1.set(Calendar.MILLISECOND, 999);
        return calendar1.getTime();
    }

    /**
     * 字符串转换成日期
     * @param str
     * @return date
     */
    public static java.util.Date StrToDate(String str) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

//    public static void main(String args[]) {
//        int i= compare_date("1999-12-12 15:21:08", "1999-12-12 15:21:08");
//        System.out.println("i=="+i);
//    }

    /**
     * 比较两个时间的大小，1前边大,－1后边大,0一样大
     * @param DATE1
     * @param DATE2
     * @return
     */
    public static int compare_date(String DATE1, String DATE2) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() > dt2.getTime()) {
                System.out.println("dt1 在dt2前");
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                System.out.println("dt1在dt2后");
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    /**
     * 判断是否双休日
     * @param currentDate
     * @return
     */
    public static boolean isWeek(String currentDate) {
        boolean reData = false;
        DateFormat df = new SimpleDateFormat("yy-MM-dd");//日期格式化辅助类
        Date d;
        try {
            d = df.parse(currentDate);//格式化日期
            //判断是不是双休日
            if (d.getDay() == 0 || d.getDay() == 6) {
//                System.out.println("日期:[" + currentDate + "] 是双休日");
//                System.out.println("\r\n");

                reData = true;
            }else{
//                System.out.println("日期:[" + currentDate + "] 不是双休日");
//                System.out.println("\r\n");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return reData;
    }

    /**
     * 获取星期
     * @param currentDate
     * @return
     */
    public static String getWeekStr(String currentDate) {
        DateFormat df = new SimpleDateFormat("yy-MM-dd");//日期格式化辅助类
        Date d;
        String weekStr = "星期一";
        try {
            d = df.parse(currentDate);//格式化日期
            switch (d.getDay()){
                case 0:
                    weekStr = "星期日";
                    break;
                case 1:
                    weekStr = "星期一";
                    break;
                case 2:
                    weekStr = "星期二";
                    break;
                case 3:
                    weekStr = "星期三";
                    break;
                case 4:
                    weekStr = "星期四";
                    break;
                case 5:
                    weekStr = "星期五";
                    break;
                case 6:
                    weekStr = "星期六";
                    break;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return weekStr;
    }

    /**
     * 计算两个日期之间相差的天数
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(Date smdate,Date bdate) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        try {
            smdate = sdf.parse(sdf.format(smdate));
            bdate = sdf.parse(sdf.format(bdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days=(time2-time1)/(1000*3600*24);

        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 日期时间转换中文 可以带星期
     * @param str
     * @return
     */
    public static String DateToStr_Cn(String str){
        //可根据不同样式请求显示不同日期格式，要显示星期可以添加E参数

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            if (str.length() > 10) {
                date = format2.parse(str);
            }else{
                date = format1.parse(str);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat format3 = new SimpleDateFormat("MM月dd日 E");
        SimpleDateFormat format4 = new SimpleDateFormat("yyyy年MM月dd日 E kk点mm分");
        String reStr = "";
        if (str.length() > 10) {
            reStr = format4.format(date);
        }else{
            reStr = format3.format(date);
        }


        return reStr;
    }

    /**
     * 日期时间转换中文 不带日期
     * @param str
     * @return
     */
    public static String getTimeCn_NotWeek(String str){
        //可根据不同样式请求显示不同日期格式，要显示星期可以添加E参数

        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 kk点mm分");
        String reStr = format.format(StrToDate(str));

        return reStr;
    }

    /**
     * 获取加减之后的日期
     * @param date
     * @param day
     * @return
     */
    public static String getDateIntStr(Date date,int day){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar   =   new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE,day);//把日期往后增加一天.整数往后推,负数往前移动
        date = calendar.getTime();   //这个时间就是日期往后推一天的结果

        String reStr = format.format(date);

        return reStr;
    }



    /**
     * 获取当前时间 格式 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String NowDateStr() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }

    /**
     * 获取当前时间 格式 yyyy-MM-dd
     * @return
     */
    public static String NowDateW() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(new Date());
    }

    /**
     * 获取当前时间 格式 yyyyMMdd
     * @return
     */
    public static String NowDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        return df.format(new Date());
    }

    /**
     * 计算时间的毫秒
     * @param time
     * @return
     */
    public static long getDateExpired(int time) {
        Date date = new Date();
        long int_time = time * 60*1000;
        long expired = date.getTime() + int_time;
        return expired;
    }

    /**
     * 根据出生日期计算年龄
     * @param birthStr
     * @return
     */
    public static int getAge(String birthStr) {
        SimpleDateFormat format_str = new SimpleDateFormat("yyyy-MM-dd");
        Date birthDate = null;
        try {
            birthDate = format_str.parse(birthStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int age = 0;

        Date now = new Date();

        SimpleDateFormat format_y = new SimpleDateFormat("yyyy");
        SimpleDateFormat format_M = new SimpleDateFormat("MM");

        String birth_year = format_y.format(birthDate);
        String this_year = format_y.format(now);

        String birth_month = format_M.format(birthDate);
        String this_month = format_M.format(now);

        // 初步，估算
        age = Integer.parseInt(this_year) - Integer.parseInt(birth_year);

        // 如果未到出生月份，则age - 1
        if (this_month.compareTo(birth_month) < 0)
            age -= 1;
        if (age < 0)
            age = 0;
        return age;
    }
    /**
     * 获取当前时间日期或前一天日期
     *
     * @param x 1当前日期 2前一天日期
     * @return
     */
    public static Date getNowOrNextDay(int x) {
        String dateTime = "";
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (x == 1) {

        } else {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            dateTime = sdf.format(calendar.getTime());
        }
        return calendar.getTime();
    }

    /**
     * 获取当月第一天或最后一天日期
     *
     * @param x 1为第一天 2为最后一天
     * @return
     */
    public static   Date getNowMonth(int x) {
        Date dateTime = null;
        if (x == 1) {
            dateTime = getMonthStartDate();
        } else {
            dateTime = getMonthEndDate();
        }

        return dateTime;
    }
    private static Date getMonthStartDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int index = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.add(Calendar.DATE, (1 - index));
        return calendar.getTime();
//        return sdf.format(calendar.getTime());
    }
    private static Date getMonthEndDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        int index = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.add(Calendar.DATE, (-index));
//        return sdf.format(calendar.getTime());
        return calendar.getTime();
    }

    /**
     * 获取上个月的第一天和最后一天
     *
     * @param x 1为第一天，2为最后一天
     * @return
     */
    public  static  Date getUpMonthDate(int x) {
        String monthStr = "";
        Date nowdate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        /* 设置为当前时间 */
        cal.setTime(nowdate);
//        System.out.println("当前时间是：" + sdf.format(nowdate));
        /* 当前日期月份-1 */
        cal.add(Calendar.MONTH, -1);
        if (x == 1) {
            // 得到前一个月的第一天
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            System.out.println("上个月的第一天是：" + sdf.format(cal.getTime()));
            monthStr = sdf.format(cal.getTime());
            nowdate = cal.getTime();
        } else {
            // 得到前一个月的最后一天
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            System.out.println("上个月的最后一天是：" + sdf.format(cal.getTime()));
            monthStr = sdf.format(cal.getTime());
            nowdate = cal.getTime();
        }

        return nowdate;
    }

    public static Date getDateTime(int x) {
        Date dateTime =null;
        java.util.Date date = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        switch(x) {
            case 1:
                dateTime = calendar.getTime();
                break;
            case 2:
                calendar.add(5, -1);
                dateTime = calendar.getTime();
                break;
            case 3:
                calendar.add(5, 1);
                dateTime = calendar.getTime();
                break;
            case 4:
                dateTime = getWeek(4);
                break;
            case 5:
                dateTime = getWeek(5);
                break;
            case 6:
                dateTime = getWeek(6);
                break;
            case 7:
                dateTime = getWeek(7);
                break;
            default:
                dateTime = calendar.getTime();
        }

        return dateTime;
    }
    private static Date getWeek(int x) {
        Date dateTime = null;
        Calendar calendar = Calendar.getInstance();
        int date = calendar.get(5);
        int n = calendar.get(7);
        if(n == 1) {
            n = 7;
        } else {
            --n;
        }
        switch(x) {
            case 4:
                calendar.set(5, date - 6 - n);
                dateTime = calendar.getTime();
                break;
            case 5:
                calendar.set(5, date - n);
                dateTime = calendar.getTime();
                break;
            case 6:
                calendar.set(5, date + 1 - n);
                dateTime = calendar.getTime();
                break;
            case 7:
                calendar.set(5, date + 7 - n);
                dateTime = calendar.getTime();
                break;
            default:
                calendar.set(5, date + 1);
                dateTime = calendar.getTime();
        }

        return dateTime;
    }
}
