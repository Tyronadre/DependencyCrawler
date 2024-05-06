package data.dataImpl;

import cyclonedx.v1_6.Bom16;
import data.License;

public class LicenceImpl implements License {
    String name;
    String url;
    String distribution;
    String comments;
    Integer id;

    static int licenceId = 0;

    public LicenceImpl() {
        this.id = licenceId++;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getDistribution() {
        return distribution;
    }

    @Override
    public String getComments() {
        return comments;
    }

    @Override
    public String getBomRef() {
        return "licence-" + id;
    }

    @Override
    public Bom16.License toBom16() {
        var builder = Bom16.License.newBuilder();
        builder.setBomRef(getBomRef());
        builder.setName(getName());


        return null;
    }
}
