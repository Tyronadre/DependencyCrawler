package data;

public interface Property {

    String name();

    String value();

    static Property of(String name, String value) {
        return new PropertyRecord(name, value);
    }

    record PropertyRecord(String name, String value) implements Property {
    }
}
