package data;

public interface ExternalReference {

    String type();

    String url();

    String comment();

    static ExternalReference of(String type, String url, String comment) {
        return new ExternalReferenceRecord(type, url, comment);
    }

    record ExternalReferenceRecord(String type, String url, String comment) implements ExternalReference {
    }

}
