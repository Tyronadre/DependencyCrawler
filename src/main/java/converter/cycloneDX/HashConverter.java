package converter.cycloneDX;

import converter.Converter;

public class HashConverter implements Converter<data.Hash, org.cyclonedx.model.Hash> {
    @Override
    public org.cyclonedx.model.Hash convert(data.Hash hash) {
        if (hash == null) return null;

        return new org.cyclonedx.model.Hash(getAlg(hash), hash.value());
    }

    private String getAlg(data.Hash hash) {
        return switch (hash.algorithm()) {
            case "md5" -> "md5";
            case "sha1" -> "sha-1";
            case "sha256" -> "sha-256";
            case "sha384" -> "sha-384";
            case "sha512" -> "sha-512";
            default -> "HASH_ALG_NULL";
        };
    }
}
