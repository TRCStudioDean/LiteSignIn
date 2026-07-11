package studio.trc.bukkit.litesignin.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class SignInDate
    implements Serializable, Comparable<SignInDate>
{
    @Getter
    @Setter
    private int year;
    @Getter
    @Setter
    private int month;
    @Getter
    @Setter
    private int day;
    @Getter
    @Setter
    private int hour;
    @Getter
    @Setter
    private int minute;
    @Getter
    @Setter
    private int second;
    @Setter
    private boolean timePeriodFound = false;
    
    public SignInDate(Date d) throws Exception {
        String[] date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(d).split("-");
        
        year = Integer.valueOf(date[0]);
        if (year < 1970) {
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
        
        if (year < 1970) {
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
        if (year < 1970) {
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
        if (year < 1970) {
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
            throw new IllegalArgumentException();
        }
        
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException();
        }
        
        int maxDays = getMaxDaysOfMonth(year, month);
        if (Integer.valueOf(date[2]) > maxDays) {
            day = maxDays;
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
    
    public static int getMaxDaysOfMonth(int year, int month) {
        if (year < 1970 || month < 0 || month > 12) {
            return -1;
        }
        int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            days[1] = 29;
        }
        return days[month - 1];
    }
    
    public int getWeek() {
        int[] weekDays = {7, 1, 2, 3, 4, 5, 6};
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        return weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1];
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
    
    @Override
    public int compareTo(SignInDate date){
        long thisTime = getMillisecond();
        long anotherTime = date.getMillisecond();
        return thisTime < anotherTime ? -1 : (thisTime == anotherTime ? 0 : 1);
    }
    
    public long getMillisecond() {
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
    
    /**
     * Get a instance by date format yyyy-MM-dd HH:mm:ss.
     * Supports: yyyy-MM-dd, MM-dd, dd, HH:mm:ss and combinations.
     * @param time String time.
     * @return SignInDate instance.
     */
    public static SignInDate getInstanceByFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return null;
        }
        time = time.trim();
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = 0;
        int minute = 0;
        int second = 0;
        String[] parts = time.split(" ");
        boolean hasDatePart = false;
        if (parts.length >= 1 && !parts[0].contains(":")) {
            String datePart = parts[0];
            hasDatePart = true;
            if (datePart.contains("-")) {
                String[] dateSection = datePart.split("-");
                if (dateSection.length < 2 || dateSection.length > 3) {
                    return null;
                }
                try {
                    if (dateSection.length == 3) {
                        // yyyy-MM-dd
                        year = Integer.valueOf(dateSection[0]);
                        if (year < 1970) {
                            return null;
                        }
                        month = Integer.valueOf(dateSection[1]);
                        day = Integer.valueOf(dateSection[2]);
                    } else if (dateSection.length == 2) {
                        // MM-dd
                        month = Integer.valueOf(dateSection[0]);
                        day = Integer.valueOf(dateSection[1]);
                    }
                    // Validate month and day
                    if (month < 1 || month > 12) {
                        return null;
                    }
                    if (day < 1 || day > getMaxDaysOfMonth(year, month)) {
                        return null;
                    }
                } catch (Exception ex) {
                    return null;
                }
            } else {
                try {
                    day = Integer.valueOf(datePart);
                    if (day < 1 || day > getMaxDaysOfMonth(year, month)) {
                        return null;
                    }
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        // Parse time part (must be complete HH:mm:ss)
        // Find the part that contains ":"
        String timePart = null;
        for (String part : parts) {
            if (part.contains(":")) {
                timePart = part;
                break;
            }
        }
        if (timePart != null) {
            // Time must be exactly HH:mm:ss format (3 parts)
            String[] timeSection = timePart.split(":");
            if (timeSection.length != 3) {
                return null;
            }
            try {
                hour = Integer.valueOf(timeSection[0]);
                if (hour < 0 || hour > 23) {
                    return null;
                }

                minute = Integer.valueOf(timeSection[1]);
                if (minute < 0 || minute > 59) {
                    return null;
                }

                second = Integer.valueOf(timeSection[2]);
                if (second < 0 || second > 59) {
                    return null;
                }
            } catch (Exception ex) {
                return null;
            }
        }
        // If there's no date part and no time part, invalid
        if (!hasDatePart && timePart == null) {
            return null;
        }
        // If there are extra parts that are neither date nor time, invalid
        for (String part : parts) {
            if (!part.contains(":") && !part.equals(parts[0])) {
                return null;
            }
        }
        SignInDate result = getInstance(year, month, day, hour, minute, second);
        if (timePart == null) {
            result.setTimePeriodFound(false);
        }
        return result;
    }
    
    public static SignInDate[] getTimeRange(String range) {
        if (range == null || range.trim().isEmpty()) {
            return null;
        }
        range = range.trim();
        if (!range.contains("~")) {
            SignInDate instance = getInstanceByFormat(range);
            return new SignInDate[] {instance, instance};
        }
        String[] parts = range.split("~");
        if (parts.length != 2) {
            return null;
        }
        String leftPart = parts[0];
        String rightPart = parts[1];
        if (leftPart.isEmpty() || rightPart.isEmpty()) {
            return null;
        }
        SignInDate leftDate = getInstanceByFormat(leftPart);
        SignInDate rightDate = getInstanceByFormat(rightPart);
        if (leftDate == null || rightDate == null) {
            return null;
        }
        // Left has date and right has no date
        if (leftPart.contains(" ") && !rightPart.contains(" ")) {
            rightDate.setYear(leftDate.getYear());
            rightDate.setMonth(leftDate.getMonth());
            rightDate.setDay(leftDate.getDay());
        }
        // Same as above
        if (!leftPart.contains(" ") && rightPart.contains(" ")) {
            leftDate.setYear(rightDate.getYear());
            leftDate.setMonth(rightDate.getMonth());
            leftDate.setDay(rightDate.getDay());
        }
        SignInDate[] result = new SignInDate[2];
        if (leftDate.compareTo(rightDate) <= 0) {
            result[0] = leftDate;
            result[1] = rightDate;
        } else {
            result[0] = rightDate;
            result[1] = leftDate;
        }
        return result;
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
    
    public static List<SignInDate> sort(List<SignInDate> dates) {
        dates.sort(SignInDate::compareTo);
        return dates;
    }
    
    public static int getContinuous(List<SignInDate> records) {
        int continuous = 0;
        if (records.isEmpty()) {
            return continuous;
        }
        int year = records.get(0).getYear();
        int month = records.get(0).getMonth();
        int day = records.get(0).getDay();
        for (SignInDate date : records) {
            date = SignInDate.getInstance(date.getYear(), date.getMonth(), date.getDay());
            boolean breakSign = true;
            if (year == date.getYear()) {
                int[] days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    days[1] = 29;
                }
                //Continuous sign-in is considered when the following conditions are met:
                if (days[month - 1] == day && month + 1 == date.getMonth() && date.getDay() == 1) {
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
        SignInDate today = getInstance(new Date());
        return today.getYear() != year || today.getMonth() != month || today.getDay() != day ? 0 : continuous;
    }
    
    public static int getContinuousOfMonth(List<SignInDate> dates) {
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
            if (year == date.getYear() && month == date.getMonth()) {
                if (day + 1 == date.getDay()) {
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
        SignInDate today = getInstance(new Date());
        return today.getYear() != year || today.getMonth() != month || today.getDay() != day ? 0 : continuous;
    }
}
