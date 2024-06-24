package data;

import java.util.List;

public interface ExternalReference {

    static ExternalReference of(String type, String url, String comment, List<Hash> hashes) {
        return new ExternalReference() {
            @Override
            public String getCategory() {
                return null;
            }

            @Override
            public String getType() {
                return type;
            }

            @Override
            public String getUrl() {
                return url;
            }

            @Override
            public String getComment() {
                return comment;
            }

            @Override
            public List<Hash> getHashes() {
                return hashes;
            }
        };
    }

    String getCategory();
    String getType();
    String getUrl();
    String getComment();
    List<Hash> getHashes();
}
