package data.dataImpl;

import cyclonedx.v1_6.Bom16;
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


    @Override
    public Bom16.ExternalReference toBom16() {
        var builder = Bom16.ExternalReference.newBuilder();
        builder.setType(switch (this.type) {
            case "url", "homepage" -> Bom16.ExternalReferenceType.EXTERNAL_REFERENCE_TYPE_WEBSITE;
            case "scm" -> Bom16.ExternalReferenceType.EXTERNAL_REFERENCE_TYPE_SOURCE_DISTRIBUTION;
            case "issueManagement" -> Bom16.ExternalReferenceType.EXTERNAL_REFERENCE_TYPE_ISSUE_TRACKER;
            case "ciManagement" -> Bom16.ExternalReferenceType.EXTERNAL_REFERENCE_TYPE_BUILD_SYSTEM;
            default -> Bom16.ExternalReferenceType.EXTERNAL_REFERENCE_TYPE_OTHER;
        });
        builder.setUrl(this.url);
        return builder.build();
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
}
