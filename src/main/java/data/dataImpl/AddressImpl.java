package data.dataImpl;

import data.Address;

public class AddressImpl implements Address {
    String country;
    String region;
    String city;
    String postalCode;
    String streetAddress;
    String postOfficeBoxNumber;

    public AddressImpl(String country, String region, String city, String postalCode, String streetAddress, String postOfficeBoxNumber) {
        this.country = country;
        this.region = region;
        this.city = city;
        this.postalCode = postalCode;
        this.streetAddress = streetAddress;
        this.postOfficeBoxNumber = postOfficeBoxNumber;
    }

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
}
