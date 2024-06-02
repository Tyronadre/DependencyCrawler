package data;

public interface Timestamp {
    static Timestamp of(long seconds, int nano) {
        return new Timestamp() {
            @Override
            public int getNanos() {
                return nano;
            }

            @Override
            public long getSeconds() {
                return seconds;
            }
        };
    }

    int getNanos();
    long getSeconds();
}
