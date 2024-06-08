package data;

import java.util.List;

public interface License {

    String getId();
    String getName();
    String getText();
    String getUrl();
    Licensing getLicensing();
    List<Property> getProperties();
    String getAcknowledgement();
}
