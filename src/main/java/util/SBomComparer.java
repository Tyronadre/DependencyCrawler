package util;

import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.parsers.BomParserFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SBomComparer {

    public static void main(String[] args) throws Exception {
        File sbomFile1 = new File("C:\\Users\\Henrik\\Desktop\\Bachelorarbeit\\DependencyCrawler\\generated\\maven_tool_custom.sbom.json");
        File sbomFile2 = new File("C:\\Users\\Henrik\\Desktop\\Bachelorarbeit\\Eval\\MavenEval\\results\\maven_cycloneplugin.bom.json");

        Bom sbom1 = parseBom(sbomFile1);
        Bom sbom2 = parseBom(sbomFile2);

        Set<String> componentsInFile1 = extractComponentIdentifiers(sbom1);
        Set<String> componentsInFile2 = extractComponentIdentifiers(sbom2);

        // Find differences
        Set<String> onlyInFile1 = new HashSet<>(componentsInFile1);
        onlyInFile1.removeAll(componentsInFile2);

        Set<String> onlyInFile2 = new HashSet<>(componentsInFile2);
        onlyInFile2.removeAll(componentsInFile1);

        // Output the differences
        System.out.println("Components only in SBOM1:");
        onlyInFile1.forEach(System.out::println);

        System.out.println("\nComponents only in SBOM2:");
        onlyInFile2.forEach(System.out::println);

        System.out.println("\nNumber of components in SBOM1: " + componentsInFile1.size());
        System.out.println("Number of components in SBOM2: " + componentsInFile2.size());

        Map<String, Integer> componentInfoInFile1 = extractComponentInformation(sbom1);
        Map<String, Integer> componentInfoInFile2 = extractComponentInformation(sbom2);

        compareComponentInformation(componentInfoInFile1, componentInfoInFile2);

    }

    private static void compareComponentInformation(Map<String, Integer> file1Info, Map<String, Integer> file2Info) {
        System.out.println("Comparing components' information completeness:");

        Set<String> allComponents = new HashSet<>(file1Info.keySet());
        allComponents.addAll(file2Info.keySet());

        for (String component : allComponents) {
            int infoInFile1 = file1Info.getOrDefault(component, 0);
            int infoInFile2 = file2Info.getOrDefault(component, 0);

            if (infoInFile1 != infoInFile2) {
                System.out.println("Component: " + component);
                System.out.println("  Information in SBOM1: " + infoInFile1 + " fields");
                System.out.println("  Information in SBOM2: " + infoInFile2 + " fields\n");
            }
        }


        Double file1Avg = file1Info.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
        Double file2Avg = file2Info.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);

        System.out.println("Average information completeness in SBOM1: " + file1Avg);
        System.out.println("Average information completeness in SBOM2: " + file2Avg);
    }

    private static Map<String, Integer> extractComponentInformation(Bom bom) {
        Map<String, Integer> componentInformation = new HashMap<>();
        for (Component component : bom.getComponents()) {
            String identifier = component.getGroup() + ":" + component.getName() + ":" + component.getVersion();
            int informationCount = countNonNullFields(component);
            componentInformation.put(identifier, informationCount);
        }
        return componentInformation;
    }

    private static int countNonNullFields(Component component) {
        int count = 0;

        if (component.getDescription() != null) count++;
        if (component.getLicenses() != null) count++;
        if (component.getHashes() != null) count++;
        if (component.getPurl() != null) count++;
        if (component.getScope() != null) count++;
        if (component.getType() != null) count++;
        if (component.getSupplier() != null) count++;
        if (component.getModified() != null) count++;
        if (component.getExternalReferences() != null) count++;
        if (component.getProperties() != null) count++;

        return count;
    }

    // Parse SBOM file
    private static Bom parseBom(File file) throws Exception {
        try {
            return BomParserFactory.createParser(file).parse(file);
        } catch (Exception e) {
            throw new Exception("Failed to parse SBOM file" + file.getAbsoluteFile(), e);
        }
    }

    // Extract component identifiers based on group, name, and version
    private static Set<String> extractComponentIdentifiers(Bom bom) {
        Set<String> componentIdentifiers = new HashSet<>();
        for (Component component : bom.getComponents()) {
            String identifier = component.getGroup() + ":" + component.getName() + ":" + component.getVersion();
            componentIdentifiers.add(identifier);
        }
        return componentIdentifiers;
    }
}

