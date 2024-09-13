package converter.toCycloneDX;

import converter.Converter;
import org.cyclonedx.model.license.Expression;

public class LicenseChoiceExpressionConverter implements Converter<String, org.cyclonedx.model.license.Expression> {

    @Override
    public Expression convert(String expression) {
        if (expression == null) return null;

        var expression1 = new org.cyclonedx.model.license.Expression();
        expression1.setValue(expression);
        expression1.setAcknowledgement(null);
        return expression1;
    }
}
