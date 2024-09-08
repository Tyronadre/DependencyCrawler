package data;

import java.util.Comparator;
import java.util.Objects;

public interface Version extends Comparable<Version> {

    String version();

    static Version of(String version) {
        return new VersionRecord(version);
    }

    record VersionRecord(String version) implements Version {
        @Override
        public int compareTo(Version otherVersion) {
            return VersionComparator.INSTANCE.compare(this, otherVersion);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VersionRecord that)) return false;

            return Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(version);
        }

        public static class VersionComparator implements Comparator<Version> {
            public static final VersionComparator INSTANCE = new VersionComparator();

            @Override
            public int compare(Version v1, Version v2) {
                String[] parts1 = splitVersion(v1.version());
                String[] parts2 = splitVersion(v2.version());

                int length = Math.min(parts1.length, parts2.length);

                for (int i = 0; i < length; i++) {
                    if (isNumeric(parts1[i]) && isNumeric(parts2[i])) {
                        int part1 = Integer.parseInt(parts1[i]);
                        int part2 = Integer.parseInt(parts2[i]);

                        if (part1 != part2) {
                            return Integer.compare(part1, part2);
                        }
                    } else {
                        // Stop comparison when reaching the first non-numeric part
                        return 0;
                    }
                }

                return Integer.compare(parts1.length, parts2.length);
            }

            private String[] splitVersion(String version) {
                return version.split("[-.]");
            }

            private boolean isNumeric(String str) {
                return str.matches("\\d+");
            }

        }
    }
}