package service.serviceImpl;

import data.Component;
import data.Dependency;
import logger.Logger;
import service.DocumentBuilder;
import settings.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TreeBuilder implements DocumentBuilder<Component, Component> {
    private static final Logger logger = Logger.of("TreeBuilder");
    private final boolean showUnresolved;

    public TreeBuilder(boolean showUnresolved) {
        this.showUnresolved = showUnresolved;
    }

    public void printTreeToConsole(Component root) {
        try {
            printTree(root, 0, "", new PrintWriter(System.out), new ArrayList<>());
        } catch (Exception e) {
            logger.error("Failed writing to tree.", e);
        }
    }

    @Override
    public void buildDocument(Component root, String outputFileName) {
        var start = System.currentTimeMillis();
        logger.info("Creating TREE for " + root.getQualifiedName() + "...");

        var outputFileDir = outputFileName.split("/", 2);
        if (outputFileDir.length > 1) {
            //create out dir if not exists
            File outDir = new File(outputFileDir[0]);
            if (!outDir.exists()) {
                outDir.mkdir();
            }
        }

        try (PrintWriter writer = new PrintWriter(outputFileName + ".tree.txt")) {
            printTree(root, 0, "", writer, new ArrayList<>());
        } catch (FileNotFoundException e) {
            logger.error("Failed writing to tree.", e);
        }

        logger.success(new File(outputFileName).getAbsolutePath() + ".tree.txt saved (" + (System.currentTimeMillis() - start) + "ms)");
    }


    private void printTree(Component component, int depth, String prependRow, PrintWriter writer, List<Dependency> handledDependencies) {
        if (component == null) return;
        if (component.isLoaded()) {
            var prop = component.getAllProperties().stream().filter(it -> it.name().equals("overwritesDependencyVersion")).findFirst().orElse(null);
            if (prop != null)
                writer.println("[OVERRIDES]: " + component.getQualifiedName() + " (overwrites version of " + prop.value() + ")");
            else
                writer.println(component.getQualifiedName());
        } else writer.println("[ERROR]: " + component.getQualifiedName() + " (NOT LOADED)");

        var dependencies = component.getDependencies().stream().sorted(Comparator.comparing(Dependency::getQualifiedName)).toList();

        if (dependencies.isEmpty()) return;

        for (int i = 0; i < dependencies.size(); i++) {
            Dependency dependency = dependencies.get(i);
            if (dependency == null) continue;
            if (!dependency.shouldResolveByScope() && !showUnresolved) continue;
            if (!dependency.isNotOptional() && !showUnresolved) continue;

            writer.print(prependRow);

            if (i == dependencies.size() - 1) {
                writer.print("└──");
                if (handledDependencies.contains(dependency)) {
                    writer.print("► [CIRCULAR]: " + dependency.getQualifiedName() + "\n");
                    continue;
                }

                if ((dependency.shouldResolveByScope() || Settings.crawlEverything) && (dependency.isNotOptional() || Settings.crawlOptional || Settings.crawlEverything) && dependency.getComponent() != null) {
                    var l = new ArrayList<>(handledDependencies);
                    l.add(dependency);
                    printTree(dependency.getComponent(), depth + 1, prependRow + "   ", writer, l);
                } else {
                    if (!dependency.isNotOptional())
                        writer.print("► [OPTIONAL]: " + dependency.getQualifiedName() + "\n");
                    else if (!dependency.shouldResolveByScope())
                        writer.print("► [" + dependency.getScope().toUpperCase() + "]: " + dependency.getQualifiedName() + "\n");
                    else writer.print("► [ERROR]: " + dependency.getQualifiedName() + "\n");
                }


            } else {
                writer.print("├──");

                if (handledDependencies.contains(dependency)) {
                    writer.print("► [CIRCULAR]: " + dependency.getQualifiedName() + "\n");
                    continue;
                }

                if ((dependency.shouldResolveByScope() || Settings.crawlEverything) && (dependency.isNotOptional() || Settings.crawlOptional || Settings.crawlEverything) && dependency.getComponent() != null) {
                    var l = new ArrayList<>(handledDependencies);
                    l.add(dependency);
                    printTree(dependency.getComponent(), depth + 1, prependRow + "│  ", writer, l);
                } else {
                    if (!dependency.isNotOptional())
                        writer.print("► [OPTIONAL]: " + dependency.getQualifiedName() + "\n");
                    else if (!dependency.shouldResolveByScope())
                        writer.print("► [" + dependency.getScope().toUpperCase() + "]: " + dependency.getQualifiedName() + "\n");
                    else writer.print("► [ERROR]: " + dependency.getQualifiedName() + "\n");
                }
            }
        }
    }
}
