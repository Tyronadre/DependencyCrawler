package data;

public interface LicenseChoice {
    License getLicense();
    String getExpression();
    String getAcknowledgement();

    static LicenseChoice of(License license, String expression, String acknowledgement) {
        return new LicenseChoice() {
            @Override
            public License getLicense() {
                return license;
            }

            @Override
            public String getExpression() {
                return expression;
            }

            @Override
            public String getAcknowledgement() {
                return acknowledgement;
            }
        };
    }
}
