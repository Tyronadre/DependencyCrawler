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
    public String id() {
        return data.get("licenseId").getAsString();
    }

    /**
     * Has to be null, as if this field is not null, the license will be considered a custom license.
     *
     * @return null
     */
    @Override
    public String name() {
        return null;
    }

    @Override
    public String nameOrId() {
        if (id().isEmpty() || id().isBlank()) return name();
        return id();
    }

    @Override
    public String text() {
        if (details == null) return null;
        return details.get("licenseText").getAsString();
    }

    @Override
    public String url() {
        return data.get("detailsUrl").getAsString();
    }

    @Override
    public Licensing licensing() {
        return null;
    }

    @Override
    public List<Property> properties() {
        var l = new ArrayList<Property>();

        l.add(Property.of("licenseId", id()));
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
    public String acknowledgement() {
        return null;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", SPDXLicense.class.getSimpleName() + "[", "]")
                .add("id=" + id())
                .toString();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SPDXLicense that)) return false;

        return Objects.equals(this.id(), that.id());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id());
    }
}
