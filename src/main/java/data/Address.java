package data;

public interface Address {

    String country();

    String region();

    String city();

    String postalCode();

    String streetAddress();

    String postOfficeBoxNumber();

    static Address of(String country, String region, String city, String postalCode, String streetAddress, String postOfficeBoxNumber) {
        return new AddressRecord(country, region, city, postalCode, streetAddress, postOfficeBoxNumber);
    }

    record AddressRecord(String country, String region, String city, String postalCode, String streetAddress,String postOfficeBoxNumber) implements Address {
    }

}
