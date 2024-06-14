package service.serviceImpl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.Version;
import data.readData.ReadVexComponent;
import data.readData.ReadVexVulnerability;
import logger.Logger;
import repository.repositoryImpl.ReadVulnerabilityRepository;
import service.DocumentReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class VexReader implements DocumentReader<List<ReadVexComponent>> {
    private static final Logger logger = Logger.of("VexReader");

    @Override
    public List<ReadVexComponent> readDocument(String inputFileName) {
        logger.info("Reading document as VEX: " + inputFileName);

        //read from file
        var file = new File(inputFileName);
        JsonObject json;
        try {
            json = JsonParser.parseReader(new InputStreamReader(new FileInputStream(file))).getAsJsonObject();
        } catch (Exception e) {
            logger.error("Could not read from file: " + file.getAbsolutePath() + ". Cause: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
        if (json == null || json.isJsonNull() || json.get("vulnerabilities") == null || json.get("vulnerabilities").isJsonNull() || json.get("vulnerabilities").getAsJsonArray().isEmpty()) {
            logger.error("Error reading file: File is empty");
            throw new RuntimeException("Error reading file: File is empty");
        }

        logger.info("Read from file. Parsing... ");

        var vulnerabilities = json.get("vulnerabilities").getAsJsonArray();
        var components = new ArrayList<ReadVexComponent>();
        for (JsonElement vulnerability : vulnerabilities) {
            var vul = vulnerability.getAsJsonObject();
            var ref = vul.get("ref").getAsString();
            var refSplit = ref.split(":");
            if (refSplit.length != 3) {
                logger.error("Error reading vulnerability: Invalid ref: " + ref);
                continue;
            }
            if (!vul.has("id")) {
                logger.error("Error reading vulnerability: Missing id: " + ref);
                continue;
            }
            var newComp = new ReadVexComponent(refSplit[0], refSplit[1], Version.of(refSplit[2]));
            var newVul = new ReadVexVulnerability(newComp, vul);
            newComp.setData("addVulnerability", newVul);
            ReadVulnerabilityRepository.getInstance().addReadVulnerability(newVul);
            components.add(newComp);
        }


        return components;
    }
}
