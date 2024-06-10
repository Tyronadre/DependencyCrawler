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

            @Override
            public String toString() {
                return name + ": " + value;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null || getClass() != obj.getClass()) {
                    return false;
                }
                Property property = (Property) obj;
                return name.equals(property.getName()) && value.equals(property.getValue());
            }

            @Override
            public int hashCode() {
                return name.hashCode() + value.hashCode();
            }
        };
    }
}
