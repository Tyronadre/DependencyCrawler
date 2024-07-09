package data;

import java.util.List;

public interface License {

    String id();

    String name();

    String nameOrId();

    String text();

    String url();

    Licensing licensing();

    List<Property> properties();

    String acknowledgement();

    static License of(String id, String name, String text, String url, Licensing licensing, List<Property> properties, String acknowledgement) {
        return new LicenseRecord(id, name, text, url, licensing, properties, acknowledgement);
    }

    record LicenseRecord(String id, String name, String text, String url, Licensing licensing, List<Property> properties, String acknowledgement) implements License {
        public String nameOrId() {
            return name.isBlank() ? id : name;
        }
    }
}
