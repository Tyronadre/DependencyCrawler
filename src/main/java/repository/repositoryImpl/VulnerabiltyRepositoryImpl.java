package repository.repositoryImpl;

import com.google.gson.JsonParser;
import data.Component;
import data.Vulnerability;
import data.dataImpl.OSVVulnerability;
import logger.Logger;
import repository.VulnerabilityRepository;
import service.serviceImpl.NVDVulnerabilityService;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

public class VulnerabiltyRepositoryImpl implements VulnerabilityRepository {
    private static final Logger logger = Logger.of("Vul_Service");

    String baseUrl = "https://api.osv.dev/v1/query";
    HttpClient client = HttpClient.newHttpClient();

    static VulnerabiltyRepositoryImpl instance;

    public static VulnerabiltyRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new VulnerabiltyRepositoryImpl();
        }
        return instance;
    }

    private VulnerabiltyRepositoryImpl() {
    }

    @Override
    public List<Vulnerability> getVulnerabilities(Component component) {
        var vulnerabilities = new ArrayList<Vulnerability>();

        StringBuilder body = new StringBuilder();
        body.append("{\"version\": \"")
                .append(component.getVersion().getVersion())
                .append("\", \"package\": {\"name\": \"")
                .append(component.getGroup())
                .append(":")
                .append(component.getName())
                .append("\", \"ecosystem\": \"Maven\"}}");
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(baseUrl).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Length", String.valueOf(body.length()));
            connection.setDoOutput(true);
            connection.getOutputStream().write(body.toString().getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                var inputStream = connection.getInputStream();
                var vulnerabilityData = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
                if (vulnerabilityData.has("vulns"))
                    for (var vulnerability : vulnerabilityData.get("vulns").getAsJsonArray())
                        vulnerabilities.add(new OSVVulnerability(component, vulnerability.getAsJsonObject()));
            }


        } catch (Exception e) {
            logger.error("Failed to get vulnerabilities from" + component + ". " + e.getMessage());
        }

        if (vulnerabilities.isEmpty()) {
            logger.info("No vulnerabilities found for " + component);
        } else {
            logger.info("Found " + vulnerabilities.size() + " vulnerabilities for " + component);
        }

        return vulnerabilities;
    }
}
