package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import data.Component;
import data.License;
import data.LicenseCollision;
import data.internalData.LicenseCollisionImpl;
import dependencyCrawler.LicenseCollisionSpecificationOuterClass;
import logger.Logger;
import service.LicenseCollisionService;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class LicenseCollisionServiceImpl implements LicenseCollisionService {
    private static final Logger logger = Logger.of("License_Coll_Service");
    private static final LicenseCollisionServiceImpl instance = new LicenseCollisionServiceImpl();


    private final HashMap<String, List<Collision>> collisions = new HashMap<>();

    private LicenseCollisionServiceImpl() {
        logger.info("Creating LicenseCollisionService.");

        var file = new File("data/license-collisions.json");
        if (file.exists()) {
            try {
                loadLicenseCollisions(file);
            } catch (Exception e) {
                logger.error("Failed to load license collisions from file.");
                createLicenseCollisions(file);
            }
        } else {
            createLicenseCollisions(file);
        }

    }

    public static LicenseCollisionServiceImpl getInstace() {
        return instance;
    }

    private void loadLicenseCollisions(File file) throws Exception {
        try {
            var data = LicenseCollisionSpecificationOuterClass.LicenseCollisionSpecification.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(new FileReader(file), data);
            for (var lc : data.getLicenseCollisionsList()) {
                this.collisions.computeIfAbsent(lc.getSpdxIDChild(), k -> new ArrayList<>()).add(new Collision(lc.getSpdxIDParentList(), lc.getSpdxIDChild(), lc.getForApplication(), lc.getCause(), lc.getParentExclusionList()));
            }
        } catch (Exception e) {
            logger.error("Could not load license collisions from file. Fallback to predefined collisions. ", e);
            for (var coll : this.getPredefinedLicenseCollisions()) {
                this.collisions.computeIfAbsent(coll.spdxIdChild(), k -> new ArrayList<>()).add(coll);
            }
        }
    }

    private void createLicenseCollisions(File file) {
        var dir = new File("data");
        if (!dir.exists()) {
            dir.mkdir();
        }

        var builder = LicenseCollisionSpecificationOuterClass.LicenseCollisionSpecification.newBuilder();

        for (var coll : this.getPredefinedLicenseCollisions()) {
            this.collisions.computeIfAbsent(coll.spdxIdChild(), k -> new ArrayList<>()).add(coll);
            builder.addLicenseCollisions(LicenseCollisionSpecificationOuterClass.LicenseCollision.newBuilder()
                    .addAllSpdxIDParent(Optional.ofNullable(coll.spdxIdParents()).orElse(List.of()))
                    .setSpdxIDChild(coll.spdxIdChild())
                    .setForApplication(coll.forApplication())
                    .setCause(coll.cause())
                    .addAllParentExclusion(Optional.ofNullable(coll.parentExclusion()).orElse(List.of()))
            );
        }

        try {
            var outputStream = new FileWriter(file, StandardCharsets.UTF_8);
            outputStream.write(JsonFormat.printer().print(builder.build()));
            outputStream.close();
        } catch (Exception e) {
            logger.error("Could not create license collisions file. ", e);
        }
    }

    private List<Collision> getPredefinedLicenseCollisions() {
        return List.of(
                new Collision(null, "GPL-1.0-only", true, "is not compatible with GPL-1.0-only as GPL-1.0-only is a copyleft license", List.of("GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only", "GPL-3.0-or-later")),
                new Collision(null, "GPL-1.0-or-later", true, "is not compatible with GPL-1.0-or-later as GPL-1.0-or-later is a copyleft license", List.of("GPL-1.0-only", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only", "GPL-3.0-or-later")),
                new Collision(null, "GPL-2.0-only", true, "is not compatible with GPL-2.0-only as GPL-2.0-only is a copyleft license", List.of("GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-or-later", "GPL-3.0-only", "GPL-3.0-or-later")),
                new Collision(null, "GPL-2.0-or-later", true, "is not compatible with GPL-2.0-or-later as GPL-2.0-or-later is a copyleft license", List.of("GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-3.0-only", "GPL-3.0-or-later")),
                new Collision(null, "GPL-3.0-only", true, "is not compatible with GPL-3.0-only as GPL-3.0-only is a copyleft license", List.of("GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-or-later")),
                new Collision(null, "GPL-3.0-or-later", true, "is not compatible with GPL-3.0-or-later as GPL-3.0-or-later is a copyleft license", List.of("GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only")),
                new Collision(null, "LGPL-2.0-only", false, "is not compatible with LGPL-2.0-only as LGPL-2.0-only is a weak copyleft license", List.of("LGPL-2.1-only", "LGPL-3.0-only", "LGPL-3.0-or-later", "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only", "GPL-3.0-or-later")),
                new Collision(null, "LGPL-2.1-only", false, "is not compatible with LGPL-2.1-only as LGPL-2.1-only is a weak copyleft license", List.of("LGPL-2.0-only", "LGPL-3.0-only", "LGPL-3.0-or-later", "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only", "GPL-3.0-or-later")),
                new Collision(null, "LGPL-3.0-only", false, "is not compatible with LGPL-3.0-only as LGPL-3.0-only is a weak copyleft license", List.of("LGPL-2.0-only", "LGPL-2.1-only", "LGPL-3.0-or-later", "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only", "GPL-3.0-or-later")),
                new Collision(null, "LGPL-3.0-or-later", false, "is not compatible with LGPL-3.0-or-later as LGPL-3.0-or-later is a weak copyleft license", List.of("LGPL-2.0-only", "LGPL-2.1-only", "LGPL-3.0-only", "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only", "GPL-3.0-or-later")),
                new Collision(null, "EPL-1.0", true, "is not compatible with EPL-1.0 as EPL-1.0 is a copyleft license", null),
                new Collision(null, "MPL-2.0", true, "is not compatible with MPL-2.0 as MPL-2.0 is a copyleft license", null),
                new Collision(null, "NGPL", true, "is not compatible with NGPL as NGPL is a copyleft license", null),
                new Collision(null, "Ms-RL", true, "is not compatible with Ms-RL as Ms-RL is a copyleft license", null),
                new Collision(null, "ODbL", true, "is not compatible with ODbL as ODbL is a copyleft license", null),
                new Collision(null, "RPL-1.5", true, "is not compatible with RPL-1.5 as RPL-1.5 is a copyleft license", null),
                new Collision(null, "RPSL-1.0", true, "is not compatible with RPSL-1.0 as RPSL-1.0 is a copyleft license", null),
                new Collision(null, "OCLC-2.0", true, "is not compatible with OCLC-2.0 as OCLC-2.0 is a copyleft license", null),
                new Collision(null, "MPL-1.1", false, "is not compatible with MPL-1.1 as MPL-1.1 is a weak copyleft license", null),
                new Collision(null, "CPL-1.0", false, "is not compatible with CPL-1.0 as CPL-1.0 is a weak copyleft license", null),
                new Collision(null, "CDDL-1.0", false, "is not compatible with CDDL-1.0 as CDDL-1.0 is a weak copyleft license", null),
                new Collision(null, "YPL-1.1", false, "is not compatible with YPL-1.1 as YPL-1.1 is a weak copyleft license", null),
                new Collision(null, "SPL-1.0", false, "is not compatible with SPL-1.0 as SPL-1.0 is a weak copyleft license", null),
                new Collision(null, "Nokia", false, "is not compatible with Nokia as Nokia is a weak copyleft license", null),
                new Collision(null, "APL-1.0", false, "is not compatible with APL-1.0 as APL-1.0 is a weak copyleft license", null)
        );
    }

    public String areLicensesCompatible(License parentLicense, License childLicense, boolean isParentApplication) {
        if (parentLicense.equals(childLicense)) {
            logger.info("License " + parentLicense.getId() + " is compatible with itself.");
            return null;
        }
        var parentLicenseSPDXId = parentLicense.getId();
        var childLicenseSPDXId = childLicense.getId();
        if (parentLicenseSPDXId == null) {
            logger.info("Skipping because parent license is not a detected spdx license: " + parentLicense.getName());
            return null;
        }
        if (childLicenseSPDXId == null) {
            logger.info("Skipping because child license is not a detected spdx license: " + childLicense.getName());
            return null;
        }


        if (!collisions.containsKey(childLicenseSPDXId)) {
            logger.info(childLicenseSPDXId + " allows " + parentLicenseSPDXId + " as a license in a parent library, as it does not have any collisions defined.");
            return null;
        }

        for (var collision : collisions.get(childLicenseSPDXId)) {
            if (collision.forApplication() != isParentApplication) continue;
            if (collision.spdxIdParents() == null || collision.spdxIdParents().isEmpty() || collision.spdxIdParents().contains(parentLicenseSPDXId)) {
                //we have a possible collision. check if the parent is excluded;
                if (collision.parentExclusion() != null && collision.parentExclusion().contains(parentLicenseSPDXId)) {
                    logger.info(childLicenseSPDXId + " allows " + parentLicenseSPDXId + " in parent as it is specified as an exclusion.");
                    return null;
                }
                logger.info(childLicenseSPDXId + " does not allow " + parentLicenseSPDXId + " in parent. Cause: " + collision.cause());
                return parentLicenseSPDXId + " " + collision.cause();
            }
        }

        logger.info(childLicenseSPDXId + " allows " + parentLicenseSPDXId + " as a license in a parent library, as no collisions could be applied.");
        return null;
    }

    @Override
    public List<LicenseCollision> checkLicenseCollisions(Component component) {
        logger.info("Checking for license collisions...");
        var licenseCollisions = new ArrayList<LicenseCollision>();
        var isParentApplication = true;

        checkLicenseCollisionsHelper(component, licenseCollisions, isParentApplication);

        logger.success("License collisions checked. Found " + licenseCollisions.size() + " collisions.");
        return licenseCollisions;
    }

    private void checkLicenseCollisionsHelper
            (Component component, ArrayList<LicenseCollision> licenseCollisions, boolean isParentApplication) {
        if (component == null || !component.isLoaded()) return;

        for (var dependency : component.getDependenciesFiltered()) {
            var childComponent = dependency.getComponent();
            if (childComponent == null) continue;

            licenseCollisions.addAll(helper(component, childComponent, isParentApplication));
            checkLicenseCollisionsHelper(childComponent, licenseCollisions, false);
        }
    }

    private List<LicenseCollision> helper(Component parentComponent, Component childComponent,
                                          boolean isParentApplication) {

        if (childComponent == null || !childComponent.isLoaded()) return List.of();

        var licenseCollisions = new ArrayList<LicenseCollision>();
        for (var licenseChoice1 : parentComponent.getAllLicenses()) {
            var parentLicense = licenseChoice1.getLicense();
            if (parentLicense == null) continue;

            for (var licenseChoice2 : childComponent.getAllLicenses()) {
                var childLicense = licenseChoice2.getLicense();
                if (childLicense == null) continue;

                var incompatibleCause = areLicensesCompatible(parentLicense, childLicense, isParentApplication);
                if (incompatibleCause != null) {
                    licenseCollisions.add(new LicenseCollisionImpl(parentLicense, parentComponent, childLicense, childComponent, incompatibleCause));
                }
            }
        }
        return licenseCollisions;

    }

    record Collision(List<String> spdxIdParents, String spdxIdChild, Boolean forApplication, String cause,
                     List<String> parentExclusion) {
    }
}
