package data;

public interface Timestamp {

    long seconds();

    int nanos();

    static Timestamp of(long seconds, int nanos) {
        return new TimestampRecord(seconds, nanos);
    }

    record TimestampRecord(long seconds, int nanos) implements Timestamp {
    }
}
