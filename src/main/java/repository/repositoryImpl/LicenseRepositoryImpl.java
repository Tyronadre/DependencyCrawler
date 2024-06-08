package repository.repositoryImpl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.License;
import data.Licensing;
import data.Property;
import data.dataImpl.SPDXLicense;
import logger.Logger;
import repository.LicenseRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

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

        var licenseListFile = new File("licenses.json");
        JsonObject licenseListNet = null;
        try {
            licenseListNet = JsonParser.parseReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/spdx/license-list-data/main/json/licenses.json").openStream())).getAsJsonObject();
        } catch (IOException e) {
            logger.error("Could not load license list from net. " + e.getMessage());
        }
        if (licenseListFile.exists()) {
            readLicensesFromFile(licenseListFile, licenseListNet);
        } else {
            if (licenseListNet == null) {
                return;
            }
            readLicensesFromNet(licenseListFile, licenseListNet);
        }

    }

    /**
     * Reads all licenses from the file. If the file is outdated, it will be updated from the net.
     * If the file does not have a version, it will be updated from the net.
     *
     * @param file The file to read from
     * @param json The json object from the net
     */
    private void readLicensesFromFile(File file, JsonObject json) {
        logger.appendInfo("Loading license list from file... ");

        String netVersion = null;
        if (json.has("licenseListVersion")) {
            netVersion = json.get("licenseListVersion").getAsString();
        }
        try (var reader = new FileReader(file)) {
            var parsed = JsonParser.parseReader(reader);

            if (parsed.isJsonNull()) {
                readLicensesFromNet(file, json);
                return;
            }

            var jsonFile = parsed.getAsJsonObject();
            var fileVersion = jsonFile.get("licenseListVersion");

            if (netVersion != null) {
                if (fileVersion == null) {
                    logger.info("Could not find version in file. Updating licenseFile...");
                    readLicensesFromNet(file, json);
                    return;
                }
                if (!netVersion.equals(fileVersion.getAsString())) {
                    logger.info("File version is outdated. Updating from " + fileVersion.getAsString() + " to " + netVersion + "...");
                    readLicensesFromNet(file, json);
                    return;
                }
            }

            if (fileVersion == null) {
                logger.error("Could not find version in file. Licenses will not be loaded.");
                return;
            }

            if (netVersion != null && jsonFile.has("licenseListVersion") && jsonFile.get("licenseListVersion").getAsString().equals(netVersion)) {
                for (var license : jsonFile.get("licenses").getAsJsonArray()) {
                    var data = license.getAsJsonObject().get("data").getAsJsonObject();
                    var details = license.getAsJsonObject().get("details").getAsJsonObject();
                    SPDXLicense licenseActual = new SPDXLicense(data, details);
                    nameToLicense.put(data.get("name").getAsString(), licenseActual);
                    idToLicense.put(data.get("licenseId").getAsString(), licenseActual);
                }
                logger.success("Loaded " + nameToLicense.size() + " licenses");
            } else {
                readLicensesFromNet(file, json);
            }
        } catch (IOException e) {
            readLicensesFromNet(file, json);
        }
    }

    private void readLicensesFromNet(File file, JsonObject json) {
        logger.info("Loading license list from net... ");
        JsonArray fileLicenses = new JsonArray();
        try {
            ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(10);

            for (var license : json.get("licenses").getAsJsonArray()) {
                var licenseObject = license.getAsJsonObject();
                logger.info("Loading " + licenseObject.get("name") + "...");
                executor.execute(() -> {
                    try {
                        var startTime = System.currentTimeMillis();
                        var data = license.getAsJsonObject();
                        var details = JsonParser.parseReader(new InputStreamReader(new URL(licenseObject.get("detailsUrl").getAsString()).openStream())).getAsJsonObject();
                        var licenseJson = new JsonObject();
                        licenseJson.add("data", data);
                        licenseJson.add("details", details);
                        fileLicenses.add(licenseJson);
                        SPDXLicense licenseActual = new SPDXLicense(data, details);
                        nameToLicense.put(licenseObject.get("name").getAsString(), licenseActual);
                        idToLicense.put(licenseObject.get("licenseId").getAsString(), licenseActual);
                        logger.success("Loaded " + licenseObject.get("name") + " (" + (System.currentTimeMillis() - startTime) + "ms)");
                    } catch (IOException e) {
                        logger.error("Could not load " + license + ". " + e.getMessage());
                        return;
                    }
                });
            }

            executor.shutdown();
            if (!executor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                executor.shutdownNow();
                throw new RuntimeException("License loading took too long (>1m). Terminating...");
            }

            logger.success(" Loaded " + nameToLicense.size() + " licenses");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        try (var writer = new FileWriter(file)) {
            var jsonFile = new JsonObject();
            jsonFile.addProperty("licenseListVersion", json.get("licenseListVersion").getAsString());
            jsonFile.add("licenses", fileLicenses);
            writer.write(jsonFile.toString());
        } catch (IOException e) {
            logger.error("Could not write licenses to file. " + e.getMessage());
        }

//        logger.appendInfo("Loading license exceptions list... ");
//        this.exceptions = new ArrayList<>();
//        try {
//            var json = JsonParser.parseReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/spdx/license-list-data/main/json/exceptions.json").openStream())).getAsJsonObject();
//            logger.appendInfo(" parsing version: " + json.get("licenseListVersion").getAsString() + "...");
//
//            for (var exception : json.get("exceptions").getAsJsonArray()) {
//                exceptions.add(exception.getAsJsonObject());
//            }
//
//            logger.success(" Loaded " + exceptions.size() + " exceptions");
//        } catch (IOException e) {
//            logger.error(" failed " + e.getMessage());
//        }
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

            default -> new License() {
                @Override
                public String getId() {
                    return name;
                }

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getText() {
                    return null;
                }

                @Override
                public String getUrl() {
                    return url;
                }

                @Override
                public Licensing getLicensing() {
                    return null;
                }

                @Override
                public List<Property> getProperties() {
                    return null;
                }

                @Override
                public String getAcknowledgement() {
                    return null;
                }
            };
        };
    }
}
