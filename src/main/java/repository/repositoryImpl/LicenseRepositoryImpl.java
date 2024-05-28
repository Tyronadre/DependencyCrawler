package repository.repositoryImpl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.License;
import logger.Logger;
import repository.LicenseRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LicenseRepositoryImpl implements LicenseRepository {
    static Logger logger = Logger.of("LicenseRepository");

    HashMap<String, License> idToLicense;
    HashMap<String, License> nameToLicense;
    List<JsonObject> exceptions;


    private static LicenseRepositoryImpl instance;

    public static LicenseRepository getInstance() {
        if (instance == null) instance = new LicenseRepositoryImpl();
        return instance;
    }

    private LicenseRepositoryImpl() {
        logger.info("Loading license list... ");
        this.nameToLicense = new HashMap<>();
        this.idToLicense = new HashMap<>();
        try {
            var json = JsonParser.parseReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/spdx/license-list-data/main/json/licenses.json").openStream())).getAsJsonObject();
            logger.info(" parsing version: " + json.get("licenseListVersion").getAsString() + "...");

            for (var license : json.get("licenses").getAsJsonArray()) {
                var licenseActual = License.of(license.getAsJsonObject());
                nameToLicense.put(license.getAsJsonObject().get("name").getAsString(), licenseActual);
                idToLicense.put(license.getAsJsonObject().get("licenseId").getAsString(), licenseActual);
            }

            logger.successLine(" Loaded " + nameToLicense.size() + " licenses");

        } catch (IOException e) {
            logger.errorLine(" failed" + e.getMessage());
        }

        logger.info("Loading license exceptions list... ");
        this.exceptions = new ArrayList<>();
        try {
            var json = JsonParser.parseReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/spdx/license-list-data/main/json/exceptions.json").openStream())).getAsJsonObject();
            logger.info(" parsing version: " + json.get("licenseListVersion").getAsString() + "...");

            for (var exception : json.get("exceptions").getAsJsonArray()) {
                exceptions.add(exception.getAsJsonObject());
            }

            logger.successLine(" Loaded " + exceptions.size() + " exceptions");
        } catch (IOException e) {
            logger.errorLine(" failed " + e.getMessage());
        }

    }


    @Override
    public License getLicense(String name) {
        if (idToLicense.containsKey(name))
            return idToLicense.get(name);
        if (nameToLicense.containsKey(name))
            return nameToLicense.get(name);
        return switch (name) {
            case "The Apache Software License, Version 2.0" -> idToLicense.get("Apache-2.0");
            case "The Apache License, Version 2.0" -> idToLicense.get("Apache-2.0");
            default -> null;
        };
    }
}
