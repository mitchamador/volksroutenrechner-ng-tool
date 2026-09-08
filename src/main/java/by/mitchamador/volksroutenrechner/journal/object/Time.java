package by.mitchamador.volksroutenrechner.journal.object;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import static by.mitchamador.volksroutenrechner.journal.object.JournalItem.*;

public class Time {
    /*
        typedef struct {
            uint8_t minute, hour, day, month, year;
        } trip_time_t;                  // 5 bytes
     */

    private int minute;
    private int hour;
    private int day;
    private int month;
    private int year;

    public Time(byte[] array) {
        int c = 0;
        minute = bcd2int(Byte.toUnsignedInt(array[c++]));
        hour = bcd2int(Byte.toUnsignedInt(array[c++]));
        day = bcd2int(Byte.toUnsignedInt(array[c++]));
        month = bcd2int(Byte.toUnsignedInt(array[c++])) - 1;
        year = 2000 + bcd2int(Byte.toUnsignedInt(array[c]));
    }

    public Time() {
        minute = hour = day = month = 0;
        year = 2000;
    }

    public Date getDate() {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month, day, hour, minute);
        return c.getTime();
    }

    private static int bcd2int(int bcd) {
        return ((bcd & 0xf0) >> 4) * 10 + (bcd & 0x0f);
    }

    private static int int2bcd(int v) {
        return ((v / 10) << 4) | ((v % 10) & 0x0f);
    }

    public String getPrintableString() {
        return getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public boolean equals(Time time) {
        return compareTo(time) == 0;
    }

    public int compareTo(Time time) {
        return getDate().compareTo(time.getDate());
    }

    public int toByteArray(byte[] array, int index) {
        index += putByteToArray(int2bcd(minute), array, index);
        index += putByteToArray(int2bcd(hour), array, index);
        index += putByteToArray(int2bcd(day), array, index);
        index += putByteToArray(int2bcd(month + 1), array, index);
        putByteToArray(int2bcd(year - 2000), array, index);
        return 5;
    }
}
