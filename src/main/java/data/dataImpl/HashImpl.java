package data.dataImpl;

import cyclonedx.v1_6.Bom16;
import data.Hash;

public class HashImpl implements Hash {
    String algorithm;
    String value;

    public HashImpl(String algorithm, String value) {
        this.algorithm = algorithm;
        this.value = value;
    }

    public HashImpl(String algorithm) {
        this.algorithm = algorithm;
    }

    public HashImpl() {
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getValue() {
        return value;
    }


    @Override
    public Bom16.Hash toBom16() {
        var builder = Bom16.Hash.newBuilder();
        builder.setAlg(switch (this.algorithm.toUpperCase()) {
            case "MD5" -> Bom16.HashAlg.HASH_ALG_MD_5;
            case "SHA1" -> Bom16.HashAlg.HASH_ALG_SHA_1;
            case "SHA256" -> Bom16.HashAlg.HASH_ALG_SHA_256;
            case "SHA384" -> Bom16.HashAlg.HASH_ALG_SHA_384;
            case "SHA512" -> Bom16.HashAlg.HASH_ALG_SHA_512;
            case "SHA3-256" -> Bom16.HashAlg.HASH_ALG_SHA_3_256;
            case "SHA3-384" -> Bom16.HashAlg.HASH_ALG_SHA_3_384;
            case "SHA3-512" -> Bom16.HashAlg.HASH_ALG_SHA_3_512;
            case "BLAKE2B-256" -> Bom16.HashAlg.HASH_ALG_BLAKE_2_B_256;
            case "BLAKE2B-384" -> Bom16.HashAlg.HASH_ALG_BLAKE_2_B_384;
            case "BLAKE2B-512" -> Bom16.HashAlg.HASH_ALG_BLAKE_2_B_512;
            case "BLAKE3" -> Bom16.HashAlg.HASH_ALG_BLAKE_3;
            default -> Bom16.HashAlg.HASH_ALG_NULL;
        });
        builder.setValue(this.value);
        return builder.build();
    }
}
