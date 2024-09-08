package repository.repositoryImpl;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.License;
import data.LicenseChoice;
import data.LicenseException;
import data.internalData.SPDXLicense;
import data.internalData.SPDXLicenseException;
import logger.Logger;
import repository.LicenseRepository;
import settings.Settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenseRepositoryImpl implements LicenseRepository {
    private static final Logger logger = Logger.of("LicenseRepository");
    private static final String baseURI = "https://spdx.org/licenses/";
    private static final String licenseListURL = baseURI + "licenses.json";
    private static final String licenseExceptionsURL = baseURI + "exceptions.json";

    HashMap<String, License> idToLicense;
    HashMap<String, List<String>> idToSpecialName;
    HashMap<String, License> nameToLicense;
    HashMap<String, LicenseException> idToException;

    private static LicenseRepositoryImpl instance;

    public static LicenseRepository getInstance() {
        if (instance == null) instance = new LicenseRepositoryImpl();
        return instance;
    }


    private LicenseRepositoryImpl() {
        logger.info("Loading license list... ");
        this.nameToLicense = new HashMap<>();
        this.idToLicense = new HashMap<>();
        this.idToSpecialName = new HashMap<>();
        this.idToException = new HashMap<>();

        // LOAD SPDX LICENSES
        var licenseListFile = new File(Settings.getDataFolder(), "licenses.json");
        try {
            Files.createDirectories(licenseListFile.getParentFile().toPath());
        } catch (IOException e) {
            logger.error("Could not create license list file. No licenses will be loaded. " + e.getMessage());
            return;
        }
        JsonObject licenseListNet = null;
        JsonObject licenseExceptionsNet = null;
        try {
            licenseListNet = JsonParser.parseReader(new InputStreamReader(URI.create(licenseListURL).toURL().openStream())).getAsJsonObject();
        } catch (IOException e) {
            logger.error("Could not load license list from net. " + e.getMessage());
        }
        try {
            licenseExceptionsNet = JsonParser.parseReader(new InputStreamReader(URI.create(licenseExceptionsURL).toURL().openStream())).getAsJsonObject();
        } catch (IOException e) {
            logger.error("Could not load license exception list from net. " + e.getMessage());
        }

        if (licenseListFile.exists()) {
            readLicensesFromFile(licenseListFile, licenseListNet, licenseExceptionsNet);
        } else {
            if (licenseListNet == null) {
                return;
            }
            readLicensesFromNet(licenseListFile, licenseListNet, licenseExceptionsNet);
        }

        // LOAD CUSTOM LICENSE NAMES
        var customLicenseNameFile = new File(Settings.getDataFolder(), "license-custom-names.json");
        if (customLicenseNameFile.exists()) {
            loadCustomLicenseNames();
        } else {
            createCustomLicenseNames(true);
        }
    }

    /**
     * Load the custom license names from the file.
     */
    private void loadCustomLicenseNames() {
        logger.info("Loading custom license names... ");
        try (var reader = new FileReader(new File(Settings.getDataFolder(), "license-custom-names.json"))) {
            var parsed = JsonParser.parseReader(reader);
            if (parsed.isJsonNull()) {
                createCustomLicenseNames(true);
                return;
            }
            var json = parsed.getAsJsonObject();
            idToSpecialName = new HashMap<>();
            for (var entry : json.entrySet()) {
                var l = new ArrayList<String>();
                for (com.google.gson.JsonElement jsonElement : entry.getValue().getAsJsonArray()) {
                    l.add(jsonElement.getAsString());
                }
                idToSpecialName.put(entry.getKey(), l);
            }
            logger.success("Loaded " + idToSpecialName.size() + " custom license names");
        } catch (IOException e) {
            logger.error("Could not load custom license names. " + e.getMessage());
            createCustomLicenseNames(false);
        }
    }

    /**
     * Creates the predefined custom license names and writes them to a file.
     *
     * @param writeToFile if the custom license names should be written to a file
     */
    private void createCustomLicenseNames(boolean writeToFile) {
        idToSpecialName.put("Apache-2.0", List.of(
                "The Apache Software License, Version 2.0",
                "Apache 2.0",
                "Apache License, Version 2.0",
                "The Apache License, Version 2.0",
                "Apache Software License - Version 2.0",
                "Apache License v2.0",
                "ASF 2.0",
                "Apache 2",
                "Apache Public License 2.0",
                "APACHE LICENSE 2.0",
                "Apache License, version 2.0",
                "Apache License Version 2.0",
                "ALv2",
                "Apache License 2.0"
        ));
        idToSpecialName.put("MIT", List.of("The MIT License", "The MIT License (MIT)"));
        idToSpecialName.put("LGPL-2.1-only", List.of("GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1", "LGPL, version 2.1", "LGPL 2.1"));
        idToSpecialName.put("BSD-3-Clause", List.of("BSD Licence 3", "BSD License 3", "Eclipse Distribution License - v 1.0", "The BSD 3-Clause License", "BSD", "EDL 1.0", "3-Clause BSD License", "Eclipse Public License - Version 1.0", "Eclipse Distribution License (EDL), Version 1.0"));
        idToSpecialName.put("BSD-2-Clause", List.of("BSD style", "New BSD License", "The BSD License", "BSD License", "The BSD 2-Clause License", "BSD 2-Clause License"));
        idToSpecialName.put("JSON", List.of("The JSON License"));
        idToSpecialName.put("EPL-1.0", List.of("Eclipse Public License", "Eclipse Public License - v 1.0", "Eclipse Public License v1.0", "Eclipse Public License 1.0", "Eclipse Public License (EPL), Version 1.0"));
        idToSpecialName.put("EPL-2.0", List.of("Eclipse Public License v2.0", "Eclipse Public License - Version 2.0", "EPL 2.0", "Eclipse Public License 2.0", "Eclipse Public License - v 2.0"));
        idToSpecialName.put("GPL-2.0-only", List.of("GNU General Public License Version 2"));
        idToSpecialName.put("CDDL-1.0", List.of("CDDL", "Common Development and Distribution License (CDDL) v1.0", "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0"));
        idToSpecialName.put("CDDL-1.1", List.of("CDDL 1.1"));
        idToSpecialName.put("CC0-1.0", List.of("Public Domain, per Creative Commons CC0", "CC0 1.0 Universal License"));
        idToSpecialName.put("CPL-1.0", List.of("Common Public License Version 1.0", "Common Public License - v 1.0"));
        idToSpecialName.put("LGPL-2.1-or-later", List.of("GNU Library General Public License v2.1 or later"));
        idToSpecialName.put("LGPL-2.0-only", List.of("GNU Lesser General Public License Version 2.1"));
        idToSpecialName.put("MPL-1.1", List.of("MPL 1.1", "Mozilla Public License version 1.1"));
        idToSpecialName.put("MPL-2.0", List.of("Mozilla Public License, Version 2.0", "Mozilla Public License version 2.0", "MPL 2.0"));
        idToSpecialName.put("GPL-3.0-only", List.of("GPL 3"));
        idToSpecialName.put("LGPL-1.0-only", List.of("GNU Lesser General Public License", "GNU Lesser Public License", "GNU LESSER GENERAL PUBLIC LICENSE", "GNU Lesser General Public Licence"));
        idToSpecialName.put("Apache-1.1", List.of("The Apache Software License, Version 1.1", "Apache License Version 1.1"));
        idToSpecialName.put("MPL-1.0", List.of("Mozilla Public License"));
        idToSpecialName.put("Apache-1.0", List.of("Apache License"));

        idToSpecialName.put("GPL-2.0-only WITH Classpath-exception-2.0", List.of("GNU General Public License, Version 2 with the Classpath Exception",
                "GPL2 w/ CPE",
                "GNU General Public License, version 2 with the GNU Classpath Exception",
                "GNU General Public License (GPL), version 2, with the Classpath exception",
                "GNU General Public License, Version 2 with the Classpath Exception",
                "GPLv2+CE"));
        idToSpecialName.put("CDDL OR GPL-2.0-only WITH Classpath-exception-2.0", List.of("CDDL or GPLv2 with exceptions", "CDDL/GPLv2+CE"));
        idToSpecialName.put("CDDL AND GPL-2.0-only", List.of("Dual license consisting of the CDDL v1.1 and GPL v2"));

        //GPLv2+CE -> Release 2.0 of the SPDX Specification introduced License Expressions that supports the ability
        // to identify common variations of SPDX-identified licenses without the need to define each potential variation
        // as a distinct license on the SPDX License List. This new syntax supports the ability to declare an
        // SPDX-identified license exception using the "WITH" operator (e.g. GPL-2.0-or-later WITH Autoconf-exception-2.0),
        // as well as the ability to use a simple "+" operator after a license short identifier to indicate "or later version".
        // SPDX has defined a list of license exceptions to use after the "WITH" operator. As a result, a number of licenses formerly
        // included on the SPDX License List have been deprecated, and correct usage employs the License Expression syntax as of v2.0.


        if (writeToFile) {
            try (var writer = new FileWriter(new File(Settings.getDataFolder(), "license-custom-names.json"))) {
                new GsonBuilder().setPrettyPrinting().create().toJson(idToSpecialName, writer);
            } catch (IOException e) {
                logger.error("Could not write custom license names to file. " + e.getMessage());
            }
        }

    }

    /**
     * Reads all licenses from the file. If the file is outdated, it will be updated from the net.
     * If the file does not have a version, it will be updated from the net.
     *
     * @param file                 The file to read from
     * @param licenseListNet       The json object from the net
     * @param licenseExceptionsNet The json object from the net
     */
    private void readLicensesFromFile(File file, JsonObject licenseListNet, JsonObject licenseExceptionsNet) {
        logger.info("Loading license list from file... ");

        String netVersion = null;
        if (licenseListNet.has("licenseListVersion") && licenseExceptionsNet.has("licenseListVersion")) {
            var licenseListVersion = licenseListNet.get("licenseListVersion").getAsString();
            var licenseExceptionsVersion = licenseExceptionsNet.get("licenseListVersion").getAsString();
            if (licenseListVersion.equals(licenseExceptionsVersion)) {
                netVersion = licenseListVersion;
            }
        }

        try (var reader = new FileReader(file)) {
            var parsed = JsonParser.parseReader(reader);

            if (parsed.isJsonNull()) {
                readLicensesFromNet(file, licenseListNet, licenseExceptionsNet);
                return;
            }

            var jsonFile = parsed.getAsJsonObject();
            var fileVersion = jsonFile.get("licenseListVersion");

            if (netVersion != null) {
                if (fileVersion == null) {
                    logger.info("Could not find version in file. Updating licenseFile...");
                    readLicensesFromNet(file, licenseListNet, licenseExceptionsNet);
                    return;
                }
                if (!netVersion.equals(fileVersion.getAsString())) {
                    logger.info("File version is outdated. Updating from " + fileVersion.getAsString() + " to " + netVersion + "...");
                    readLicensesFromNet(file, licenseListNet, licenseExceptionsNet);
                    return;
                }
            }

            if (fileVersion == null) {
                logger.error("Could not load local license and internet license list. No licenses will be loaded. The tool will not be able to determine SPDX License IDs!");
                return;
            }

            for (var license : jsonFile.get("licenses").getAsJsonArray()) {
                var data = license.getAsJsonObject().get("data").getAsJsonObject();
                var details = license.getAsJsonObject().get("details").getAsJsonObject();

                SPDXLicense licenseActual = new SPDXLicense(data, details);
                nameToLicense.put(data.get("name").getAsString(), licenseActual);
                idToLicense.put(data.get("licenseId").getAsString(), licenseActual);
            }

            for (var exception : jsonFile.get("exceptions").getAsJsonArray()) {
                var data = exception.getAsJsonObject().get("data").getAsJsonObject();
                var details = exception.getAsJsonObject().get("details").getAsJsonObject();

                idToException.put(data.get("licenseExceptionId").getAsString(), new SPDXLicenseException(data, details));
            }
            logger.success("Loaded " + nameToLicense.size() + " licenses");
            logger.success("Loaded " + idToException.size() + " license exceptions");

        } catch (IOException e) {
            logger.error("Could not read licenses from file. Loading from net...", e);
            readLicensesFromNet(file, licenseListNet, licenseExceptionsNet);
        }
    }

    /**
     * Reads all spdx licenses from the net and saves them to the file.
     * The currently newest version of the license list is saved in the file.
     *
     * @param file                 The file to write to
     * @param licenseListNet       The json object from the net
     * @param licenseExceptionsNet The json object from the net
     */
    private void readLicensesFromNet(File file, JsonObject licenseListNet, JsonObject licenseExceptionsNet) {
        logger.info("Loading license list from net... ");
        JsonArray fileLicenses = new JsonArray();
        JsonArray fileExceptions = new JsonArray();
        try {
            ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(Settings.crawlThreads);

            for (var license : licenseListNet.get("licenses").getAsJsonArray()) {
                var licenseObject = license.getAsJsonObject();
                executor.execute(() -> {
                    logger.info("Loading " + licenseObject.get("name") + "...");
                    try {
                        var startTime = System.currentTimeMillis();
                        var data = license.getAsJsonObject();
                        var details = JsonParser.parseReader(new InputStreamReader(URI.create(licenseObject.get("detailsUrl").getAsString()).toURL().openStream())).getAsJsonObject();
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
                    }
                });
            }


            for (var licenseExceptionO : licenseExceptionsNet.get("exceptions").getAsJsonArray()) {
                var licenseException = licenseExceptionO.getAsJsonObject();
                executor.execute(() -> {
                    try {
                        logger.info("Loading " + licenseException.get("reference") + "...");
                        var startTime = System.currentTimeMillis();
                        var data = JsonParser.parseReader(new InputStreamReader(URI.create(baseURI + licenseException.get("reference").getAsString().substring(2)).toURL().openStream())).getAsJsonObject();
                        var details = JsonParser.parseReader(new InputStreamReader(URI.create(baseURI + licenseException.get("reference").getAsString().substring(2)).toURL().openStream())).getAsJsonObject();
                        var licenseJson = new JsonObject();
                        licenseJson.add("data", data);
                        licenseJson.add("details", details);

                        fileExceptions.add(licenseJson);
                        SPDXLicenseException exceptionActual = new SPDXLicenseException(data, details);
                        idToException.put(licenseException.get("licenseExceptionId").getAsString(), exceptionActual);
                        logger.success("Loaded " + licenseException.get("reference") + " (" + (System.currentTimeMillis() - startTime) + "ms)");
                    } catch (IOException e) {
                        logger.error("Could not load " + licenseException + ". " + e.getMessage());
                    }
                });
            }

            executor.shutdown();
            if (!executor.awaitTermination(2, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                throw new RuntimeException("License loading took too long (>2m). Terminating...");
            }

            logger.success(" Loaded " + nameToLicense.size() + " licenses");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        try (var writer = new FileWriter(file)) {
            var jsonFile = new JsonObject();
            jsonFile.addProperty("licenseListVersion", licenseListNet.get("licenseListVersion").getAsString());
            jsonFile.add("licenses", fileLicenses);
            jsonFile.add("exceptions", fileExceptions);
            new GsonBuilder().setPrettyPrinting().create().toJson(jsonFile, writer);
        } catch (IOException e) {
            logger.error("Could not write licenses to file. " + e.getMessage());
        }
    }


    @Override
    public License getLicense(String name, String url, String componentName) {
        return new LicenseParser(name, url, componentName).getLicenseChoice().licenses().stream().findFirst().orElse(null);

    }

    @Override
    public LicenseChoice getLicenseChoice(String licenseString, String url, String componentName) {
        return new LicenseParser(licenseString, url, componentName).getLicenseChoice();
    }

    /**
     * Parses a license string for a single license and returns the license choice.
     */
    class LicenseParser {
        private final String input;
        private final String url;
        private final String componentName;
        private LicenseChoice licenseChoice;
        private boolean isSPDXLicense;

        public LicenseParser(String input, String url, String componentName) {
            this.input = input;
            this.url = url;
            this.componentName = componentName;

            parse();
        }

        private void parse() {
            this.isSPDXLicense = true;
            if (input.length() < 200) {
                parseIdOrName();
            } else {
                parseLicenseFile();
            }
        }

        private void parseIdOrName() {
            var input = this.input.replaceAll("\\(", Matcher.quoteReplacement("\\("));
            input = input.replaceAll("\\)", Matcher.quoteReplacement("\\)"));
            input = input.replaceAll("\\.", Matcher.quoteReplacement("\\."));
            var pattern = Pattern.compile(input, Pattern.CASE_INSENSITIVE);

            for (var idToLicenseEntry : idToLicense.entrySet()) {
                var matcher = pattern.matcher(idToLicenseEntry.getKey());
                if (matcher.find()) {
                    this.licenseChoice = LicenseChoice.of(List.of(idToLicenseEntry.getValue()), idToLicenseEntry.getValue().id(), null);
                    return;
                }
            }

            for (var exceptionEntry : idToException.entrySet()) {
                var matcher = pattern.matcher(exceptionEntry.getKey());
                if (matcher.find()) {
                    this.licenseChoice = LicenseChoice.of(List.of(exceptionEntry.getValue()), exceptionEntry.getValue().id(), null);
                    return;
                }
            }

            for (var nameToLicenseEntry : nameToLicense.entrySet()) {
                var matcher = pattern.matcher(nameToLicenseEntry.getKey());
                if (matcher.find()) {
                    this.licenseChoice = LicenseChoice.of(List.of(nameToLicenseEntry.getValue()), nameToLicenseEntry.getValue().id(), null);
                    return;
                }
            }

            for (var customNameEntry : idToSpecialName.entrySet()) {
                for (var name : customNameEntry.getValue()) {
                    var matcher = pattern.matcher(name);
                    if (matcher.find() || name.equalsIgnoreCase(input)) {
                        this.licenseChoice = new LicenseExpressionParser(customNameEntry.getKey(), url, componentName).getLicenseChoice();
                        return;
                    }
                }
            }

            this.isSPDXLicense = false;
            this.licenseChoice = LicenseChoice.of(List.of(License.of(null, input, null, url, null, null, null)), null, null);
        }

        private void parseLicenseFile() {
            // we only look at the beginning of the license, as the license may contain names of other licenses later on.
            var licenseData = input.substring(0, 200);

            for (var licenseToIdEntry : idToLicense.entrySet()) {
                if (Pattern.compile(".*" + licenseToIdEntry.getKey() + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(licenseData).find()) {
                    this.licenseChoice = LicenseChoice.of(List.of(licenseToIdEntry.getValue()), licenseToIdEntry.getValue().id(), null);
                    return;
                }
            }

            for (var exceptionEntry : idToException.entrySet()) {
                if (Pattern.compile(".*" + exceptionEntry.getKey() + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(licenseData).find()) {
                    this.licenseChoice = LicenseChoice.of(List.of(exceptionEntry.getValue()), exceptionEntry.getValue().id(), null);
                    return;
                }
            }

            for (var licenseToNameEntry : nameToLicense.entrySet()) {
                if (Pattern.compile(".*" + licenseToNameEntry.getKey() + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(licenseData).find()) {
                    this.licenseChoice = LicenseChoice.of(List.of(licenseToNameEntry.getValue()), licenseToNameEntry.getValue().name(), null);
                    return;
                }
            }

            for (var customNameEntry : idToSpecialName.entrySet()) {
                for (var name : customNameEntry.getValue()) {
                    if (Pattern.compile(".*" + name + ".*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(licenseData).find()) {
                        this.licenseChoice = LicenseRepositoryImpl.getInstance().getLicenseChoice(customNameEntry.getKey(), url, componentName);
                        return;
                    }
                }
            }

            this.licenseChoice = LicenseChoice.of(List.of(License.of(null, "unknown license", input, url, null, null, null)), null, null);
            this.isSPDXLicense = false;
        }

        public LicenseChoice getLicenseChoice() {
            return licenseChoice;
        }

        public License getLicense() {
            return licenseChoice.licenses().stream().findFirst().orElse(null);
        }

        public boolean isSPDXLicense() {
            return isSPDXLicense;
        }
    }

    /**
     * Parses a license expression string and returns the license choice.
     */
    class LicenseExpressionParser {

        private final String expression;
        private final StringBuilder parsedExpression;
        private final String componentName;
        private final String url;
        private final int length;
        private final List<License> resultLicenseList;
        private final List<Operator> resultOperatorList;
        private int index;


        public LicenseExpressionParser(String expression, String url, String componentName) {
            this.expression = expression;
            this.parsedExpression = new StringBuilder();
            this.url = url;
            this.length = expression.length();
            this.index = 0;
            this.resultLicenseList = new ArrayList<>();
            this.resultOperatorList = new ArrayList<>();
            this.componentName = componentName;

            parse();
        }

        private void parse() {
            if (index >= length) return;
            var start = index;
            while (index < length) {
                if (expression.startsWith(" AND ", index)) {
                    index += 5;
                    var license = parseLicense(expression.substring(start, index - 5));
                    resultLicenseList.add(license);
                    resultOperatorList.add(Operator.AND);
                    parsedExpression.append(license.nameOrId()).append(" AND ");
                    start = index;
                } else if (expression.startsWith(" OR ", index)) {
                    index += 4;
                    var license = parseLicense(expression.substring(start, index - 4));
                    resultLicenseList.add(license);
                    resultOperatorList.add(Operator.OR);
                    parsedExpression.append(license.nameOrId()).append(" OR ");
                    start = index;
                } else if (expression.startsWith(" WITH ", index)) {
                    index += 6;
                    var license = parseLicense(expression.substring(start, index - 6));
                    resultLicenseList.add(license);
                    resultOperatorList.add(Operator.WITH);
                    parsedExpression.append(license.nameOrId()).append(" WITH ");
                    start = index;
                } else if (expression.startsWith("+ ", index)) {
                    index += 2;
                    var license = parseLicense(expression.substring(start, index - 3));
                    resultLicenseList.add(license);
                    resultOperatorList.add(Operator.PLUS);
                    parsedExpression.append(license.nameOrId()).append(" + ");
                    start = index;
                } else if (expression.startsWith("+", index) && expression.length() == index + 1) {
                    index += 1;
                    var license = parseLicense(expression.substring(start, index - 1));
                    resultLicenseList.add(license);
                    resultOperatorList.add(Operator.PLUS);
                    parsedExpression.append(license.nameOrId()).append("+");
                    start = index;
                } else {
                    index++;
                }
            }
            var license = parseLicense(expression.substring(start));
            if (license == null) return;
            resultLicenseList.add(license);
            parsedExpression.append(license.nameOrId());

        }

        private License parseLicense(String license) {
            var licenseParser = new LicenseParser(license, url, componentName);
            if (!licenseParser.isSPDXLicense()) {
                logger.error("Parsed license in expression string : " + license + " in component " + componentName + " was not recognized as an SPDX license. This could lead to issues later.");
            }
            return licenseParser.getLicense();
        }

        public List<License> getResultLicenseList() {
            return resultLicenseList;
        }

        public List<Operator> getResultOperatorList() {
            return resultOperatorList;
        }

        public String getResultExpression() {
            return parsedExpression.toString();
        }

        public LicenseChoice getLicenseChoice() {
            return LicenseChoice.of(getResultLicenseList(), getResultExpression(), null);
        }

        enum Operator {
            AND, OR, WITH, PLUS
        }

    }

}
