package data;

public interface Address {
    String getCountry();

    String getRegion();

    String getCity();

    String getPostalCode();

    String getStreetAddress();

    String getPostOfficeBoxNumber();

    static Address of(String country, String region, String city, String postalCode, String streetAddress, String postOfficeBoxNumber) {
        return new Address() {
            @Override
            public String getCountry() {
                return country;
            }

            @Override
            public String getRegion() {
                return region;
            }

            @Override
            public String getCity() {
                return city;
            }

            @Override
            public String getPostalCode() {
                return postalCode;
            }

            @Override
            public String getStreetAddress() {
                return streetAddress;
            }

            @Override
            public String getPostOfficeBoxNumber() {
                return postOfficeBoxNumber;
            }
        };

    }
}
