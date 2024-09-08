package converter.cycloneDX;

import converter.Converter;
import org.cyclonedx.model.Component;

public class ScopeConverter implements Converter<String, Component.Scope> {

    @Override
    public Component.Scope convert(String s) {
        if (s == null) return null;
        return switch (s) {
            case "provided", "test" -> Component.Scope.OPTIONAL;
            default -> Component.Scope.REQUIRED;
        };
    }
}
