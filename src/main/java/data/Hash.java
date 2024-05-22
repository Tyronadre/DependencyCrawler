package data;

import cyclonedx.v1_6.Bom16;

public interface Hash extends Bom16Component<Bom16.Hash> {

    String getAlgorithm();
    String getValue();
}
