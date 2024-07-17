package repository;

import data.License;
import data.LicenseChoice;
import repository.repositoryImpl.LicenseRepositoryImpl;

public interface LicenseRepository {

    static LicenseRepository getInstance() {
        return LicenseRepositoryImpl.getInstance();
    }

    /**
     * Returns a license given the name.
     * Tries to match the string to an spdx license.
     * If that is not possible returns a license that has the name.
     * <p>
     * If used for parsing a whole license text, will try to read the beginning of the text and match it to an spdx license.
     * If that is not possible, it will return a license without name and without id, but the text as a description.
     *
     * @param name the name of the license.
     * @param url  the url of the license, if present.
     * @return the license.
     */
    License getLicense(String name, String url, String componentName);

    /**
     * Returns a license choice given the string to parse.
     */
    LicenseChoice getLicenseChoice(String licenseString, String url, String componentName);

}
