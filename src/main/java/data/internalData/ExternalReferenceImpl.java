package data.internalData;

import data.ExternalReference;

import java.util.HashMap;

public class ExternalReferenceImpl implements ExternalReference {
    String type;
    String url;
    HashMap<String, String> data;

    public ExternalReferenceImpl(String type, String url) {
        this.type = type;
        this.url = url;
        this.data = new HashMap<>();
    }


    /**
     * Set the data of the external reference. If the data is null, it will not be set.
     *
     * @param key the key of the data
     * @param data the data
     */
    public void set(String key, String data) {
        if (data == null) {
            return;
        }
        this.data.put(key, data);
    }

    @Override
    public String type() {
        return switch (this.type) {
            case ("homepage") -> "EXTERNAL_REFERENCE_TYPE_WEBSITE";
            case ("scm") -> "EXTERNAL_REFERENCE_TYPE_VCS";
            case ("issueManagement") -> "EXTERNAL_REFERENCE_TYPE_ISSUE_TRACKER";
            case ("ciManagement") -> "EXTERNAL_REFERENCE_TYPE_BUILD_SYSTEM";
            case ("mailingList") -> "EXTERNAL_REFERENCE_TYPE_MAILING_LIST";
            case ("download") -> "EXTERNAL_REFERENCE_TYPE_DIST_SYSTEM";
            default -> "EXTERNAL_REFERENCE_TYPE_OTHER";
        };
    }

    @Override
    public String url() {
        return this.url;
    }

    @Override
    public String comment() {
        return null;
    }

}
