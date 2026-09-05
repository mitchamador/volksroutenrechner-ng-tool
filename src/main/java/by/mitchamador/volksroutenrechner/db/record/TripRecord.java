package by.mitchamador.volksroutenrechner.db.record;

public class TripRecord {

    private Long id;
    private char tripType; // 'C', 'A', 'B'
    private long time; // epoch millis
    private double odo; // km
    private double averageSpeed; // km/h
    private double averageFuel; // l/100km
    private double totalFuel; // l
    private int totalMinutes;

    public TripRecord() {
    }

    public TripRecord(char tripType, long time, double odo, double averageSpeed, double averageFuel, double totalFuel, int totalMinutes) {
        this.tripType = tripType;
        this.time = time;
        this.odo = odo;
        this.averageSpeed = averageSpeed;
        this.averageFuel = averageFuel;
        this.totalFuel = totalFuel;
        this.totalMinutes = totalMinutes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public char getTripType() {
        return tripType;
    }

    public void setTripType(char tripType) {
        this.tripType = tripType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getOdo() {
        return odo;
    }

    public void setOdo(double odo) {
        this.odo = odo;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getAverageFuel() {
        return averageFuel;
    }

    public void setAverageFuel(double averageFuel) {
        this.averageFuel = averageFuel;
    }

    public double getTotalFuel() {
        return totalFuel;
    }

    public void setTotalFuel(double totalFuel) {
        this.totalFuel = totalFuel;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    /**
     * "Мягкое" сравнение для дедупликации при импорте: совпадение всех полей данных
     * (id намеренно не участвует - его ещё может не быть).
     */
    public boolean sameDataAs(TripRecord other) {
        return other != null
                && tripType == other.tripType
                && time == other.time
                && Double.compare(odo, other.odo) == 0
                && Double.compare(averageSpeed, other.averageSpeed) == 0
                && Double.compare(averageFuel, other.averageFuel) == 0
                && Double.compare(totalFuel, other.totalFuel) == 0
                && totalMinutes == other.totalMinutes;
    }
}
