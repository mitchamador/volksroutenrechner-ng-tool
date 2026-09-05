package by.mitchamador.volksroutenrechner.db.record;

public class AccelRecord {

    private Long id;
    private long startTime; // epoch millis
    private int lowerSpeed; // km/h
    private int upperSpeed; // km/h
    private int resultCs; // hundredths of a second

    public AccelRecord() {
    }

    public AccelRecord(long startTime, int lowerSpeed, int upperSpeed, int resultCs) {
        this.startTime = startTime;
        this.lowerSpeed = lowerSpeed;
        this.upperSpeed = upperSpeed;
        this.resultCs = resultCs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getLowerSpeed() {
        return lowerSpeed;
    }

    public void setLowerSpeed(int lowerSpeed) {
        this.lowerSpeed = lowerSpeed;
    }

    public int getUpperSpeed() {
        return upperSpeed;
    }

    public void setUpperSpeed(int upperSpeed) {
        this.upperSpeed = upperSpeed;
    }

    public int getResultCs() {
        return resultCs;
    }

    public void setResultCs(int resultCs) {
        this.resultCs = resultCs;
    }

    /**
     * "Мягкое" сравнение для дедупликации при импорте: совпадение всех полей данных.
     */
    public boolean sameDataAs(AccelRecord other) {
        return other != null
                && startTime == other.startTime
                && lowerSpeed == other.lowerSpeed
                && upperSpeed == other.upperSpeed
                && resultCs == other.resultCs;
    }
}
