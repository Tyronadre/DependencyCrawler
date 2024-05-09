package data.dataImpl;

import cyclonedx.v1_6.Bom16;
import data.License;

import java.util.Optional;
import java.util.UUID;

public class LicenseImpl implements License {
    String name;
    String url;
    String distribution;
    String comments;
    Integer id;

    static int licenceId = 0;

    public LicenseImpl(String name, String url, String distribution, String comments) {
        this.name = name;
        this.url = url;
        this.distribution = distribution;
        this.comments = comments;
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
    public Bom16.LicenseChoice toBom16() {
        var licenseBuilder = Bom16.License.newBuilder();
        licenseBuilder.setBomRef(UUID.randomUUID().toString());
        licenseBuilder.setId(this.getName());
        licenseBuilder.setUrl(this.getUrl());
        Optional.ofNullable(this.getComments()).ifPresent(comments ->licenseBuilder.setText(Bom16.AttachedText.newBuilder().setValue(comments).build()));

        return Bom16.LicenseChoice.newBuilder().setLicense(licenseBuilder.build()).build();
    }
}
