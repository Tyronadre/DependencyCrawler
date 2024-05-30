package data;

public interface Address {
    String getCountry();
    String getRegion();
    String getCity();
    String getPostalCode();
    String getStreetAddress();
    String getPostOfficeBoxNumber();

    static Address of(String country, String region, String city, String postalCode, String streetAddress, String postOfficeBoxNumber) {
        return new AddressImpl(street, city, postalCode, country, state, addresses);
    }
}
