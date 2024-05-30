package data;

import cyclonedx.sbom.Bom16;
import cyclonedx.v1_6.Bom16;
import data.dataImpl.ExternalReferenceImpl;

import java.util.List;

public interface ExternalReference extends Bom16Component<Bom16.ExternalReference> {

    static ExternalReference of(String type, String url, String comment, List<Hash> hashes) {
        return new ExternalReferenceImpl(type, url, comment, hashes);
    }

    String getType();
    String getUrl();
    String getComment();
    List<Hash> getHashes();
}
