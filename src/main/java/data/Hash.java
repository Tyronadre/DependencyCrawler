package data;

public interface Hash {

    String algorithm();

    String value();

    static Hash of(String alg, String value) {
        return new HashRecord(alg, value);
    }

    record HashRecord(String algorithm, String value) implements Hash {
    }

}
