package studio.trc.bukkit.litesignin.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SignInDate
{
    private final int year;
    private final int month;
    private final int day;
    private final int hour;
    private final int minute;
    private final int second;
    
    private boolean timePeriodFound = false;
    
    public SignInDate(Date d) throws Exception {
        String[] date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(d).split("-");
        
        year = Integer.valueOf(date[0]);
        if (year < 1970 || year > Calendar.getInstance().get(Calendar.YEAR)) {
            throw new Exception();
        }
        
        month = Integer.valueOf(date[1]);
        if (month < 1 || month > 12) {
            throw new Exception();
        }
        
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            days[1] = 29;
        }
        if (Integer.valueOf(date[2]) > days[month - 1]) {
            day = days[month -1];
        } else {
            day = Integer.valueOf(date[2]); 
        }
        
        hour = Integer.valueOf(date[3]);
        minute = Integer.valueOf(date[4]);
        second = Integer.valueOf(date[5]);
        timePeriodFound = true;
    }
    
    public SignInDate(String[] date) throws Exception {
        year = Integer.valueOf(date[0]);
        month = Integer.valueOf(date[1]);
        
        if (year < 1970 || year > Calendar.getInstance().get(Calendar.YEAR)) {
            throw new Exception();
        }
        
        if (month < 1 || month > 12) {
            throw new Exception();
        }
        
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            days[1] = 29;
        }
        if (Integer.valueOf(date[2]) > days[month - 1]) {
            day = days[month -1];
        } else {
            day = Integer.valueOf(date[2]);
        }
        
        if (date.length > 3) {
            hour = Integer.valueOf(date[3]);
            minute = Integer.valueOf(date[4]);
            second = Integer.valueOf(date[5]);
            timePeriodFound = true;
        } else {
            hour = 0;
            minute = 0;
            second = 0;
        }
    }
    
    public SignInDate(int year, int month, int day) throws Exception {
        if (year < 1970 || year > Calendar.getInstance().get(Calendar.YEAR)) {
            throw new Exception();
        }
        
        if (month < 1 || month > 12) {
            throw new Exception();
        }
        
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            days[1] = 29;
        }
        if (day > days[month - 1]) {
            day = days[month -1];
        }
        
        this.year = year;
        this.month = month;
        this.day = day;
        hour = 0;
        minute = 0;
        second = 0;
    }
    
    public SignInDate(int year, int month, int day, int hour, int minute, int second) throws Exception {
        if (year < 1970 || year > Calendar.getInstance().get(Calendar.YEAR)) {
            throw new Exception();
        }
        
        if (month < 1 || month > 12) {
            throw new Exception();
        }
        
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            days[1] = 29;
        }
        if (day > days[month - 1]) {
            day = days[month -1];
        }
        
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        timePeriodFound = true;
    }
    
    public SignInDate(String datatext) throws Exception {
        String[] date = datatext.split("-");
        year = Integer.valueOf(date[0]);
        month = Integer.valueOf(date[1]);
        
        if (year < 1970 || year > Calendar.getInstance().get(Calendar.YEAR)) {
            throw new Exception();
        }
        
        if (month < 1 || month > 12) {
            throw new Exception();
        }
        
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            days[1] = 29;
        }
        if (Integer.valueOf(date[2]) > days[month - 1]) {
            day = days[month -1];
        } else {
            day = Integer.valueOf(date[2]);
        }
        
        if (date.length > 3) {
            hour = Integer.valueOf(date[3]);
            minute = Integer.valueOf(date[4]);
            second = Integer.valueOf(date[5]);
            timePeriodFound = true;
        } else {
            hour = 0;
            minute = 0;
            second = 0;
        }
    }
    
    public int getWeek() {
        int[] weekDays = {7, 1, 2, 3, 4, 5, 6};
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        return weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1];
    }
    
    public int getYear() {
        return year;
    }
    
    public int getMonth() {
        return month;
    }
    
    public int getDay() {
        return day;
    }
    
    public int getHour() {
        return hour;
    }
    
    public int getMinute() {
        return minute;
    }
    
    public int getSecond() {
        return second;
    }
    
    public String getYearAsString() {
        return String.valueOf(year);
    }
    
    public String getMonthAsString() {
        if (month < 10) {
            return "0" + month;
        }
        return String.valueOf(month);
    }
    
    public String getDayAsString() {
        if (day < 10) {
            return "0" + day;
        }
        return String.valueOf(day);
    }
    
    public String getHourAsString() {
        if (hour < 10) {
            return "0" + hour;
        }
        return String.valueOf(hour);
    }
    
    public String getMinuteAsString() {
        if (minute < 10) {
            return "0" + minute;
        }
        return String.valueOf(minute);
    }
    
    public String getSecondAsString() {
        if (second < 10) {
            return "0" + second;
        }
        return String.valueOf(second);
    }
    
    public int compareTo(SignInDate date){
        long thisTime = getMillionTime();
        long anotherTime = date.getMillionTime();
        return thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1);
    }
    
    public long getMillionTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minute, second);
        return cal.getTimeInMillis();
    }
    
    public boolean hasTimePeriod() {
        return timePeriodFound;
    }
    
    public String getDataText(boolean timePeriod) {
        return timePeriod ? year + "-" + month + "-" + day + "-" + hour + "-" + minute + "-" + second : year + "-" + month + "-" + day;
    }
    
    public static List<SignInDate> sort(List<SignInDate> dates) {
        dates.sort(SignInDate::compareTo);
        return dates;
    }
    
    public static int getContinuous(List<SignInDate> dates) {
        int continuous = 0;
        if (dates.isEmpty()) {
            return continuous;
        }
        int year = dates.get(0).getYear();
        int month = dates.get(0).getMonth();
        int day = dates.get(0).getDay();
        for (SignInDate date : dates) {
            date = SignInDate.getInstance(date.getYear(), date.getMonth(), date.getDay());
            boolean breakSign = true;
            if (year == date.getYear()) {
                int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    days[1] = 29;
                }
                if (days[month - 1] == day && month + 1 == date.getMonth()) {
                    continuous++;
                    breakSign = false;
                } else if (day + 1 == date.getDay()) {
                    continuous++;
                    breakSign = false;
                }
            } else if (year + 1 == date.getYear()) {
                if (month == 12 && date.getMonth() == 1 && day == 31 && date.getDay() == 1) {
                    continuous++;
                    breakSign = false;
                }
            }
            if (breakSign) {
                continuous = 1;
            }
            year = date.getYear();
            month = date.getMonth();
            day = date.getDay();
        }
        return continuous;
    }
    
    public String getName(String format) {
        if (hour == -1 || minute == -1 || second == -1) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, day);
            return new SimpleDateFormat(format).format(new Date(cal.getTimeInMillis()));
        }
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minute, second);
        return new SimpleDateFormat(format).format(new Date(cal.getTimeInMillis()));
    }
    
    @Override
    public String toString() {
        return getDataText(timePeriodFound);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SignInDate) {
            SignInDate date = (SignInDate) obj;
            if (date.getYear() == year && date.getMonth() == month && date.getDay() == day) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.year;
        hash = 89 * hash + this.month;
        hash = 89 * hash + this.day;
        hash = 89 * hash + this.hour;
        hash = 89 * hash + this.minute;
        hash = 89 * hash + this.second;
        hash = 89 * hash + (this.timePeriodFound ? 1 : 0);
        return hash;
    }
    
    public static SignInDate getInstanceAsTimePeriod(String timePeriod) {
        String[] split = timePeriod.split(":");
        try {
            SignInDate today = SignInDate.getInstance(new Date());
            int hour = Integer.valueOf(split[0]);
            if (hour > 23 || hour < 0) {
                return null;
            }
            int minute = 0;
            if (split.length >= 2) {
                minute = Integer.valueOf(split[1]);
            }
            if (minute > 59 || minute < 0) {
                return null;
            }
            int second = 0;
            if (split.length >= 3) {
                second = Integer.valueOf(split[2]);
            }
            if (second > 59 || second < 0) {
                return null;
            }
            return new SignInDate(today.getYear(), today.getMonth(), today.getDay(), hour, minute, second);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public static SignInDate getInstance(String datatext) {
        try {
            return new SignInDate(datatext);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public static SignInDate getInstance(int year, int month, int day, int hour, int minute, int second) {
        try {
            return new SignInDate(year, month, day, hour, minute, second);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public static SignInDate getInstance(int year, int month, int day) {
        try {
            return new SignInDate(year, month, day);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public static SignInDate getInstance(String[] date) {
        try {
            return new SignInDate(date);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public static SignInDate getInstance(Date date) {
        try {
            return new SignInDate(date);
        } catch (Exception ex) {
            return null;
        }
    }
}
