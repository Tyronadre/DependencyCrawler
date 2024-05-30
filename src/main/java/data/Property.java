package data;

public interface Property {
    String getName();
    String getValue();

    static Property of(String name, String value) {
        return new Property() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getValue() {
                return value;
            }
        };
    }
}
