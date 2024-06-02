package data;

public interface Hash {

    static Hash of(String alg, String value) {
        return new Hash() {
            @Override
            public String getAlgorithm() {
                return alg;
            }

            @Override
            public String getValue() {
                return value;
            }
        };
    }

    String getAlgorithm();
    String getValue();
}
