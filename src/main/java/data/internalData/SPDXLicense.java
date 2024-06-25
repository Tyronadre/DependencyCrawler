package data.internalData;

import com.google.gson.JsonObject;
import data.License;
import data.Licensing;
import data.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class SPDXLicense implements License {
    JsonObject data;
    JsonObject details;

    public SPDXLicense(JsonObject data, JsonObject details) {
        this.data = data;
        this.details = details;
    }

    @Override
    public String getId() {
        return data.get("licenseId").getAsString();
    }

    /**
     * Has to be null, as if this field is not null, the license will be considered a custom license.
     *
     * @return null
     */
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getNameOrId() {
        if (getId().isEmpty() || getId().isBlank()) return getName();
        return getId();
    }

    @Override
    public String getText() {
        if (details == null) return null;
        return details.get("licenseText").getAsString();
    }

    @Override
    public String getUrl() {
        return data.get("detailsUrl").getAsString();
    }

    @Override
    public Licensing getLicensing() {
        return null;
    }

    @Override
    public List<Property> getProperties() {
        var l = new ArrayList<Property>();

        l.add(Property.of("licenseId", getId()));
        Optional.ofNullable(data.get("seeAlso")).ifPresent(s -> l.add(Property.of("seeAlso", s.getAsJsonArray().toString())));
        Optional.ofNullable(data.get("isOsiApproved")).ifPresent(s -> l.add(Property.of("isOsiApproved", s.getAsString())));
        Optional.ofNullable(data.get("isFsfLibre")).ifPresent(s -> l.add(Property.of("isFsfLibre", s.getAsString())));

        if (details != null) {
            l.add(Property.of("standardLicenseTemplate", details.get("standardLicenseTemplate").getAsString()));
            l.add(Property.of("name", details.get("name").getAsString()));
            l.add(Property.of("crossRef", details.get("crossRef").getAsJsonArray().toString()));
        }

        return l;
    }

    @Override
    public String getAcknowledgement() {
        return null;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", SPDXLicense.class.getSimpleName() + "[", "]")
                .add("id=" + getId())
                .toString();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SPDXLicense that)) return false;

        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getId());
    }
}
