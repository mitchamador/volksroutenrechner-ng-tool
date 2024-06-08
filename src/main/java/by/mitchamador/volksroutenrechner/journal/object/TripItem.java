package by.mitchamador.volksroutenrechner.journal.object;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TripItem extends JournalItem {
    public static final int LENGTH = 18;

    private final static double ODO_CONST = 16000.0;

    /*
        typedef struct {
            uint8_t status; // 1 byte
            trip_time_t start_time; // 5 byte
            union {
                trip_t trip; // 12 bytes (old version 0xA5)
                print_trip_t trip; // 12 bytes (new version 0xAA)
            };
        } journal_trip_item_t; // 18 bytes total

        typedef struct {
            uint16_t odo;
            uint16_t odo_temp;
            uint8_t fuel_tmp1, fuel_tmp2;
            uint16_t fuel;
            uint32_t time;
        } trip_t;                       // 12 bytes

        typedef struct {
            uint16_t odo;
            uint16_t average_speed;
            uint16_t average_fuel;
            uint16_t fuel;
            uint16_t time;
            uint16_t dummy;
        } print_trip_t;                 // 12 bytes

     */

    private int odo;
    private int odo_temp;
    private int fuel_tmp1;
    private int fuel_tmp2;
    private int fuel;
    private int total_time;

    private int pOdo;
    private int pAverageSpeed;
    private int pAverageFuel;
    private int pTotalFuel;
    private int pTime;

    public TripItem(byte[] array) {
        super(array);

        if (status == JournalItem.ITEM_V1) {
            odo = (Byte.toUnsignedInt(array[7]) << 8) + Byte.toUnsignedInt(array[6]);
            odo_temp = (Byte.toUnsignedInt(array[9]) << 8) + Byte.toUnsignedInt(array[8]);
            fuel_tmp1 = Byte.toUnsignedInt(array[10]);
            fuel_tmp2 = Byte.toUnsignedInt(array[11]);
            fuel = (Byte.toUnsignedInt(array[13]) << 8) + Byte.toUnsignedInt(array[12]);
            total_time = (Byte.toUnsignedInt(array[17]) << 24) + (Byte.toUnsignedInt(array[16]) << 16) + ((Byte.toUnsignedInt(array[15])) << 8) + Byte.toUnsignedInt(array[14]);
            fillPrintedData();
        } else if (status == JournalItem.ITEM_V2) {
            pOdo = (Byte.toUnsignedInt(array[7]) << 8) + Byte.toUnsignedInt(array[6]);
            pAverageSpeed = (Byte.toUnsignedInt(array[9]) << 8) + Byte.toUnsignedInt(array[8]);
            pAverageFuel = (Byte.toUnsignedInt(array[11]) << 8) + Byte.toUnsignedInt(array[10]);
            pTotalFuel = (Byte.toUnsignedInt(array[13]) << 8) + Byte.toUnsignedInt(array[12]);
            pTime = (Byte.toUnsignedInt(array[15]) << 8) + Byte.toUnsignedInt(array[14]);
            //dummy = (Byte.toUnsignedInt(array[17]) << 8) + Byte.toUnsignedInt(array[16]);
        }
    }

    public void fixTime(String dateFix) {
        if (status == JournalItem.ITEM_V1) {
            if (time.getDate() != null && time.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().compareTo(
                    LocalDateTime.of(LocalDate.from(DateTimeFormatter.ofPattern("dd.MM.yyyy").parse(dateFix)), LocalTime.MIDNIGHT)) < 0) {
                total_time <<= 1;
            }
            fillPrintedData();
        } else if (status == JournalItem.ITEM_V2) {
            // convert to v1
            odo = pOdo / 10;
            odo_temp = (int) ODO_CONST * (pOdo % 10) / 10;
            fuel_tmp1 = fuel_tmp2 = 0;
            fuel = pTotalFuel * 10;
            total_time = pTime * 60;
        }
    }

    @Override
    public String getPrintableString(boolean units) {
        return time.getPrintableString() + ";" +
                String.format(Locale.ROOT, units ? " %.1f км; %.1f л/100км; %.1f км/ч; %02d:%02d; %.1f л" : "%.1f;%.1f;%.1f;%02d:%02d;%.1f",
                        pOdo / 10.0,
                        pAverageFuel / 10.0,
                        pAverageSpeed / 10.0,
                        TimeUnit.MINUTES.toHours(pTime),
                        TimeUnit.MINUTES.toMinutes(pTime % TimeUnit.HOURS.toMinutes(1)),
                        pTotalFuel / 10.0
                );
    }

    @Override
    public int toByteArray(byte[] array, int index, int version) {
        index += headerToByteArray(array, index, version);
        if (version == JournalItem.ITEM_V1) {
            index += putShortToArray(odo, array, index);
            index += putShortToArray(odo_temp, array, index);
            index += putByteToArray(fuel_tmp1, array, index);
            index += putByteToArray(fuel_tmp2, array, index);
            index += putShortToArray(fuel, array, index);
            putLongToArray(total_time, array, index);
        } else if (version == JournalItem.ITEM_V2) {
            index += putShortToArray(pOdo, array, index);
            index += putShortToArray(pAverageSpeed, array, index);
            index += putShortToArray(pAverageFuel, array, index);
            index += putShortToArray(pTotalFuel, array, index);
            index += putShortToArray(pTime, array, index);
            putShortToArray(0, array, index);
        }
        return LENGTH;
    }

    private void fillPrintedData() {
        pOdo = (int) Math.round((odo + (odo_temp / ODO_CONST)) * 10.0);
        pAverageFuel = (int) Math.round(((fuel / 100.0) / ((odo + (odo_temp / ODO_CONST)) / 100.0)) * 10.0);
        pAverageSpeed = (int) Math.round(((odo + (odo_temp / ODO_CONST)) * TimeUnit.HOURS.toSeconds(1) / total_time) * 10.0);
        pTotalFuel = (int) Math.round((fuel / 100.0) * 10.0);
        pTime = (int) TimeUnit.SECONDS.toMinutes(total_time);
    }

}
