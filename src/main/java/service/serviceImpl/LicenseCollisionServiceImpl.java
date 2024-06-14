package service.serviceImpl;

import data.Component;
import data.LicenseChoice;
import data.LicenseCollision;
import logger.Logger;
import org.spdx.rdfparser.license.License;
import service.LicenseCollisionService;

import java.util.List;
import java.util.Objects;

public class LicenseCollisionServiceImpl implements LicenseCollisionService {
    private static LicenseCollisionServiceImpl instance;
    private static final Logger logger = Logger.of("License_Coll_Service");

    public static LicenseCollisionServiceImpl getInstance() {
        if (instance == null)
            instance = new LicenseCollisionServiceImpl();
        return instance;
    }

    private LicenseCollisionServiceImpl() {
    }

    // This is a simplified compatibility check. In a real-world scenario, you would need a comprehensive method.
    public static boolean areLicensesCompatible(License license1, License license2) {
        // Simplified logic to check compatibility
        // In reality, you would use a comprehensive compatibility matrix or library.
        // For demonstration purposes, assuming MIT and Apache-2.0 are compatible with anything,
        // and GPL-2.0-only is incompatible with everything else.
        if ("MIT".equals(license1.getLicenseId()) || "MIT".equals(license2.getLicenseId())) {
            return true;
        }
        if ("Apache-2.0".equals(license1.getLicenseId()) || "Apache-2.0".equals(license2.getLicenseId())) {
            return true;
        }
        return license1.getLicenseId().equals(license2.getLicenseId());
    }

    @Override
    public List<LicenseCollision> checkLicenseCollisions(Component component) {
        var licenses = component.getDependencyComponentsFlatFiltered().stream()
                .map(Component::getAllLicenses)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(LicenseChoice::getLicense)
                .distinct()
                .toList();
        logger.info("Checking license collisions for " + licenses);



        return List.of();
    }
}
