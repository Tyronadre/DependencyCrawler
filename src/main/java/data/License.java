package data;

import cyclonedx.v1_6.Bom16;

public interface License extends Bom16Component<Bom16.License> {
    String getName();
    String getUrl();
    String getDistribution();
    String getComments();
    String getBomRef();

}
