package data;

import cyclonedx.sbom.Bom16;
import cyclonedx.v1_6.Bom16;
import data.dataImpl.HashImpl;

public interface Hash extends Bom16Component<Bom16.Hash> {

    static Hash of(String alg, String value) {
        return new HashImpl(alg, value);
    }

    String getAlgorithm();
    String getValue();
}
