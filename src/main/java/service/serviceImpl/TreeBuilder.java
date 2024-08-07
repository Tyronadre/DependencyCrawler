package service.serviceImpl;

import data.Component;
import data.Dependency;
import logger.Logger;
import service.DocumentBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Comparator;

public class TreeBuilder implements DocumentBuilder<Component, Component> {
    private static final Logger logger = Logger.of("TreeBuilder");
    private final boolean showUnresolved;

    public TreeBuilder(boolean showUnresolved) {
        this.showUnresolved = showUnresolved;
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
            printTree(root, 0, "", writer);
        } catch (FileNotFoundException e) {
            logger.error("Failed writing to tree.", e);
        }

        logger.success(new File(outputFileName).getAbsolutePath() + ".tree.txt saved (" + (System.currentTimeMillis() - start) + "ms)");
    }


    private void printTree(Component component, int depth, String prependRow, PrintWriter writer) {
        if (component == null) return;
        if (component.isLoaded()) writer.println(component.getQualifiedName());
        else writer.println("[ERROR]: " + component.getQualifiedName() + "?");
        writer.flush();

        var dependencies = component.getDependencies().stream().sorted(Comparator.comparing(Dependency::getQualifiedName)).toList();
        if (dependencies.isEmpty()) return;

        for (int i = 0; i < dependencies.size(); i++) {
            Dependency dependency = dependencies.get(i);
            if (dependency == null) continue;
            if (!dependency.shouldResolveByScope() && !showUnresolved) continue;
            if (!dependency.isNotOptional() && !showUnresolved) continue;

            writer.print(prependRow);
            writer.flush();

            if (i == dependencies.size() - 1) {
                writer.print("└──");
                writer.flush();
                if (dependency.shouldResolveByScope() && dependency.isNotOptional() && dependency.getComponent() != null)
                    printTree(dependency.getComponent(), depth + 1, prependRow + "   ", writer);
                else {
                    if (!dependency.isNotOptional())
                        writer.print("► [OPTIONAL]: " + dependency.getQualifiedName() + "\n");
                    else if (!dependency.shouldResolveByScope())
                        writer.print("► [" + dependency.getScope().toUpperCase() + "]: " + dependency.getQualifiedName() + "\n");
                    else writer.print("► [ERROR]: " + dependency.getQualifiedName() + "\n");
                }
                writer.flush();


            } else {
                writer.print("├──");
                writer.flush();
                if (dependency.shouldResolveByScope() && dependency.isNotOptional())
                    printTree(dependency.getComponent(), depth + 1, prependRow + "│  ", writer);
                else {
                    if (!dependency.isNotOptional())
                        writer.print("► [OPTIONAL]: " + dependency.getQualifiedName() + "\n");
                    else if (!dependency.shouldResolveByScope())
                        writer.print("► [" + dependency.getScope().toUpperCase() + "]: " + dependency.getQualifiedName() + "\n");
                    else writer.print("► [ERROR]: " + dependency.getQualifiedName() + "\n");
                }
                writer.flush();
            }
        }
    }
}
