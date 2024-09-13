package converter.fromCycloneDX;

import converter.Converter;
import data.Component;
import data.readData.ReadDependency;
import org.cyclonedx.model.Dependency;

public class DependencyConverter implements Converter<Dependency, data.Dependency> {

    private final Component component;
    private final Component parent;

    public DependencyConverter(Component component, Component parent) {
        this.component = component;
        this.parent = parent;
    }

    @Override
    public data.Dependency convert(Dependency dependency) {
        return new ReadDependency(component, parent);
    }
}
