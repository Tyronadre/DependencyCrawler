package repository.repositoryImpl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.License;
import data.dataImpl.CustomLicense;
import data.dataImpl.SPDXLicense;
import logger.Logger;
import repository.LicenseRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LicenseRepositoryImpl implements LicenseRepository {
    private static final Logger logger = Logger.of("LicenseRepository");

    HashMap<String, License> idToLicense;
    HashMap<String, License> nameToLicense;
    List<JsonObject> exceptions;


    private static LicenseRepositoryImpl instance;

    public static LicenseRepository getInstance() {
        if (instance == null) instance = new LicenseRepositoryImpl();
        return instance;
    }

    private LicenseRepositoryImpl() {
        logger.appendInfo("Loading license list... ");
        this.nameToLicense = new HashMap<>();
        this.idToLicense = new HashMap<>();
        try {
            var json = JsonParser.parseReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/spdx/license-list-data/main/json/licenses.json").openStream())).getAsJsonObject();
            logger.appendInfo(" parsing version: " + json.get("licenseListVersion").getAsString() + "...");

            for (var license : json.get("licenses").getAsJsonArray()) {
                var licenseActual = new SPDXLicense(license.getAsJsonObject());
                nameToLicense.put(license.getAsJsonObject().get("name").getAsString(), licenseActual);
                idToLicense.put(license.getAsJsonObject().get("licenseId").getAsString(), licenseActual);
            }

            logger.success(" Loaded " + nameToLicense.size() + " licenses");

        } catch (IOException e) {
            logger.error(" failed" + e.getMessage());
        }

        logger.appendInfo("Loading license exceptions list... ");
        this.exceptions = new ArrayList<>();
        try {
            var json = JsonParser.parseReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/spdx/license-list-data/main/json/exceptions.json").openStream())).getAsJsonObject();
            logger.appendInfo(" parsing version: " + json.get("licenseListVersion").getAsString() + "...");

            for (var exception : json.get("exceptions").getAsJsonArray()) {
                exceptions.add(exception.getAsJsonObject());
            }

            logger.success(" Loaded " + exceptions.size() + " exceptions");
        } catch (IOException e) {
            logger.error(" failed " + e.getMessage());
        }

    }


    @Override
    public License getLicense(String name, String url) {
        if (idToLicense.containsKey(name))
            return idToLicense.get(name);
        if (nameToLicense.containsKey(name))
            return nameToLicense.get(name);
        return switch (name) {
            case "The Apache Software License, Version 2.0", "Apache 2.0", "Apache License, Version 2.0",
                 "The Apache License, Version 2.0", "Apache Software License - Version 2.0", "Apache License v2.0",
                 "ASF 2.0", "Apache 2", "Apache Public License 2.0" -> idToLicense.get("Apache-2.0");
            case "The MIT License" -> idToLicense.get("MIT");
            case "GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1", "LGPL, version 2.1", "LGPL 2.1" ->
                    idToLicense.get("LGPL-2.1-only");
            case "BSD Licence 3", "BSD License 3", "Eclipse Distribution License - v 1.0", "The BSD 3-Clause License",
                 "BSD" -> idToLicense.get("BSD-3-Clause");
            case "The JSON License" -> idToLicense.get("JSON");
            case "Eclipse Public License", "Eclipse Public License - v 1.0" -> idToLicense.get("EPL-1.0");
            case "Eclipse Public License v2.0", "Eclipse Public License - Version 2.0" -> idToLicense.get("EPL-2.0");
            case "GNU General Public License, version 2 (GPL2), with the classpath exception" ->
                    idToLicense.get("GPL-2.0-only");
            case "Common Development and Distribution License (CDDL) v1.0",
                 "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0" -> idToLicense.get("CDDL-1.0");
            case "Public Domain, per Creative Commons CC0" -> idToLicense.get("CC0-1.0");
            case "Common Public License Version 1.0" -> idToLicense.get("CPL-1.0");
            case "GNU Lesser General Public License" -> idToLicense.get("LGPL-2.1-or-later");
            case "MPL 1.1" -> idToLicense.get("MPL-1.1");
            case "Mozilla Public License, Version 2.0" -> idToLicense.get("MPL-2.0");

            default -> new CustomLicense(name, url);
        };
    }
}
