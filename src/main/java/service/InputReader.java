package service;

import data.Component;

import java.io.File;

/**
 * Reads an input file and creates the artifacts and dependencies.
 * <p>
 *     The file should be in a specific format.
 *     In the first line, the type of app is given. {@link }
 *     In the first line, the data of the parent artifact should be given.
 *     In the following lines, the data of the dependencies should be given.
 */
public interface InputReader {

    Component createRootComponentAndLoadDependencies(File file);

}
