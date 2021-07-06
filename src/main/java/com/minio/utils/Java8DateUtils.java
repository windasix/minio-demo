package com.minio.utils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by zli on 2017/6/21.
 *
 * 日期工具
 */
public class Java8DateUtils {

  public static final String DATE_FORMAT = "yyyy-MM-dd";

  public static final String DATE_FORMAT_SERIAL = "yyyyMMdd";

  public static final String TIME_FORMAT = "HH:mm:ss";

  public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  public static final String DATE_TIME_CN_FORMAT = "yyyy年MM月dd日";

  public static final String DATE_MINUTE_FORMAT = "yyyy-MM-dd HH:mm";

  /**
   * 00：00：00
   */
  public static final String START_TIME = " 00:00:00";
  /**
   * 59:59:59
   */
  public static final String END_TIME = " 23:59:59";

  public static final String SECOND_START = ":00";

  public static final String SECOND_END = ":59";

  /**
   * 获取日期
   */
  public static Date getDate(String date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);
    ZoneId zone = ZoneId.systemDefault();
    Instant instant = localDateTime.atZone(zone).toInstant();
    return Date.from(instant);
  }

  public static Date getDate(LocalDate localDate) {
    ZoneId zone = ZoneId.systemDefault();
    Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
    return Date.from(instant);
  }

  public static Date getDate(LocalDateTime localDateTime) {
    ZoneId zoneId = ZoneId.systemDefault();
    ZonedDateTime zdt = localDateTime.atZone(zoneId);
    return Date.from(zdt.toInstant());
  }

  public static LocalDateTime getLocalDateTime(String date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return LocalDateTime.parse(date, formatter);
  }

  public static LocalDateTime getLocalDateTime(Date date) {
    Instant instant = date.toInstant();
    ZoneId zone = ZoneId.systemDefault();
    return LocalDateTime.ofInstant(instant, zone);
  }

  public static LocalDate getLocalDate(Date date) {
    Instant instant = date.toInstant();
    ZoneId zoneId = ZoneId.systemDefault();
    // atZone()方法返回在指定时区从此Instant生成的ZonedDateTime。
    return instant.atZone(zoneId).toLocalDate();
  }

  /**
   * @param dateStr yyyy-MM-dd 格式
   */
  public static LocalDate getLocalDate(String dateStr) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    return LocalDate.parse(dateStr, formatter);
  }

  /**
   * @param dateStr 日期String
   * @param formatter 时间格式
   */
  public static LocalDate getLocalDate(String dateStr, String formatter) {
    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(formatter));
  }

  public static void main(String[] args) {

  }

  /***
   * 返回当前年，如：2015
   *
   * @return
   */
  public static String getCurrentYear() {
    return String.valueOf(LocalDate.now().getYear());
  }

  /***
   * 返回当前月,如：07
   *
   * @return
   */
  public static String getCurrentMonth() {
    String month = String.valueOf(LocalDate.now().getMonthValue());
    if (month.length() == 1) {
      return "0" + month;
    }
    return month;
  }

  /***
   * 返回当前日,如：26
   *
   * @return
   */
  public static String getCurrentDayOfMonth() {
    String day = String.valueOf(LocalDate.now().getDayOfMonth());
    if (day.length() == 1) {
      return "0" + day;
    }
    return day;
  }

  /**
   * 格式化
   *
   * @param date 日期
   * @param pattern 格式化格式
   * @return string型日期
   */
  public static String formatter(Date date, String pattern) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
    LocalDateTime localDateTime = getLocalDateTime(date);
    return localDateTime.format(dateFormatter);
  }

  /**
   * 获取某日期当前周的周一
   * @param date
   * @return
   */
  public static LocalDate getWeekFirstDay(LocalDate date) {
    TemporalAdjuster firstOfWeek = TemporalAdjusters
        .ofDateAdjuster(localDate -> localDate.minusDays(localDate.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue()));
    return date.with(firstOfWeek);
  }
  public static LocalDate currentWeekFirstDay() {
    return getWeekFirstDay(LocalDate.now());
  }

  /**
   * 获取某日期当前周的周日
   * @param date
   * @return
   */
  public static LocalDate getWeekLastDay(LocalDate date) {
    TemporalAdjuster lastOfWeek =
        TemporalAdjusters.ofDateAdjuster(localDate -> localDate
            .plusDays(DayOfWeek.SUNDAY.getValue() - localDate.getDayOfWeek().getValue()));
    return date.with(lastOfWeek);
  }

  public static LocalDate currentWeekLastDay() {
    return getWeekLastDay(LocalDate.now());
  }

  /**
   * 获取某月第一天
   * @return
   */
  public static LocalDate firstDayOfMonth(LocalDate localDate) {
    return localDate.withDayOfMonth(1);
  }
  public static LocalDate firstDayOfCurrentMonth() {
    return firstDayOfMonth(LocalDate.now());
  }

  /**
   * 获取某月最后一天
   * @return
   */
  public static LocalDate lastDayOfMonth(LocalDate localDate) {
    return localDate.with(TemporalAdjusters.lastDayOfMonth());
  }
  public static LocalDate lastDayOfCurrentMonth() {
    return lastDayOfMonth(LocalDate.now());
  }

  /**
   * 根据时间获得前num天的时间  yyyy-MM-dd
   * @param date
   * @param num
   * @return
   */
  public static String getDayBefore(Date date, int num) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Calendar calendar1 = Calendar.getInstance();
    calendar1.add(Calendar.DATE, -num);
    Date today = calendar1.getTime();
    return sdf.format(today);
  }

  /**
   * 将类似于2019年9月12日 转换成yyyy-MM-dd
   * @param timeStr
   * @return
   */
  public static String getTransTime(String timeStr) {
    DateTimeFormatter df = null;
    if(Pattern
        .matches("\\d{4}[\\u4e00-\\u9fa5]\\d{1}[\\u4e00-\\u9fa5]\\d{2}[\\u4e00-\\u9fa5]", timeStr)){
      df = DateTimeFormatter.ofPattern("yyyy年M月dd日");
    }else if(Pattern.matches("\\d{4}[\\u4e00-\\u9fa5]\\d{2}[\\u4e00-\\u9fa5]\\d{1}[\\u4e00-\\u9fa5]",
        timeStr)){
      df = DateTimeFormatter.ofPattern("yyyy年MM月d日");
    }else if(Pattern.matches("\\d{4}[\\u4e00-\\u9fa5]\\d{1}[\\u4e00-\\u9fa5]\\d{1}[\\u4e00-\\u9fa5]",
        timeStr)){
      df = DateTimeFormatter.ofPattern("yyyy年M月d日");
    }else{
      df = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    }
    LocalDate rq = LocalDate.parse(timeStr, df);
    DateTimeFormatter f2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String transTime = f2.format(rq);
    return transTime;
  }

}
