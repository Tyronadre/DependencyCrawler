package service.serviceImpl;

import com.google.gson.JsonParser;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LicenseCollisionServiceImpl implements LicenseCollisionService {
    private static final Logger logger = Logger.of("License_Coll_Service");
    private static final LicenseCollisionServiceImpl instance = new LicenseCollisionServiceImpl();
    private static final List<String> copyLeftLicense = List.of(
            "GPL-3.0-only",
            "GPL-3.0-or-later",
            "GPL-2.0-only",
            "GPL-2.0-or-later",
            "GPL-1.0-only",
            "GPL-1.0-or-later",
            "AGPL-3.0-only",
            "AGPL-3.0-or-later",
            "EPL-1.0",
            "MPL-2.0",
            "NGPL",
            "Ms-RL",
            "ODbL",
            "RPL-1.5",
            "RPSL-1.0",
            "OCLC-2.0"
    );
    private static final List<String> weakCopyLeftLicense = List.of(
            "LGPL-3.0-only",
            "LGPL-3.0-or-later",
            "LGPL-2.1-only",
            "LGPL-2.1-or-later",
            "LGPL-2.0-only",
            "LGPL-2.0-or-later",
            "MPL-1.1",
            "CPL-1.0",
            "CDDL-1.0",
            "YPL-1.1",
            "SPL-1.0",
            "Nokia",
            "APL-1.0"
    );

    static {
        var row = new String[]{
                "MIT", "Apache-2.0", "BSD-2-Clause", "BSD-3-Clause", "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only", "GPL-3.0-or-later", "LGPL-2.0-only", "LGPL-2.1-only", "LGPL-3.0-only", "LGPL-3.0-or-later", "JSON", "EPL-1.0", "EPL-2.0", "CDDL-1.0", "CC0-1.0", "CPL-1.0", "MPL-1.1", "MPL-2.0"
        };
        var matrix = new Boolean[][]{
                /*MIT*/                 {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /*Apache-2.0*/          {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /*BSD-2-Clause*/        {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /*BSD-3-Clause*/        {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /*GPL-1.0-only*/        {false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
                /*GPL-1.0-or-later*/    {false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
                /*GPL-2.0-only*/        {false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false},
                /*GPL-2.0-or-later*/    {false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false},
                /*GPL-3.0-only*/        {false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false},
                /*GPL-3.0-or-later*/    {false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false},
                /*LGPL-2.0-only*/       {true, true, true, true, false, false, true, true, false, false, true, false, false, false, true, true, true, true, true, true, true},
                /*LGPL-2.1-only*/       {true, true, true, true, false, false, true, true, false, false, false, true, false, false, true, true, true, true, true, true, true},
                /*LGPL-3.0-only*/       {true, true, true, true, false, false, true, true, true, true, false, false, true, true, true, true, true, true, true, true, true},
                /*LGPL-3.0-or-later*/   {true, true, true, true, false, false, true, true, true, true, false, false, true, true, true, true, true, true, true, true, true},
                /*JSON */               {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /* EPL-1.0*/            {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /* EPL-2.0 */           {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /*CDDL-1.0 */           {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /*CC0-1.0*/             {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /*CPL-1.0*/             {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /*MPL-1.1*/             {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
                /*MPL-2.0*/             {true, true, true, true, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true}

        };
    }

    private final HashMap<String, Collision> collisions = new HashMap<>();
    private final List<String> comparedLicenses = new ArrayList<>();

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
                this.collisions.put(lc.getSpdxIDChild(), new Collision(lc.getSpdxIDParent(), lc.getSpdxIDChild(), lc.getCause()));
            }
        } catch (Exception e) {
            logger.error("Could not load license collisions from file.", e);
            throw e;
        }
    }

    private void createLicenseCollisions(File file) {
        // TODO: Implement this method
        var dir = new File("data");
        if (!dir.exists()) {
            dir.mkdir();
        }

        var builder = LicenseCollisionSpecificationOuterClass.LicenseCollisionSpecification.newBuilder();

        for (var coll : this.getPredefinedLicenseCollisions()) {
            this.collisions.put(coll.spdxIdChild(), coll);
            builder.addLicenseCollisions(LicenseCollisionSpecificationOuterClass.LicenseCollision.newBuilder()
                    .setSpdxIDParent(coll.spdxIdParent())
                    .setSpdxIDChild(coll.spdxIdChild())
                    .setCause(coll.cause())
            );
        }

        try {
            JsonFormat.printer().appendTo(builder.build(), new FileWriter(file));
        } catch (Exception e) {
            logger.error("Could not create license collisions file.", e);
        }
    }

    private List<Collision> getPredefinedLicenseCollisions() {
        return List.of(
                new Collision("ANY", "GPL-1.0-only", true,  "Incompatible: MIT is not compatible with GPL-1.0-only as GPL-1.0-only is a copyleft license"),
                new Collision("ANY", "GPL-1.0-or-later",  true, "Incompatible: MIT is not compatible with GPL-1.0-or-later as GPL-1.0-or-later is a copyleft license"),
                new Collision("ANY", "GPL-2.0-only",  true, "Incompatible: MIT is not compatible with GPL-2.0-only as GPL-2.0-only is a copyleft license"),
                new Collision("ANY", "GPL-2.0-or-later",  true, "Incompatible: MIT is not compatible with GPL-2.0-or-later as GPL-2.0-or-later is a copyleft license"),
                new Collision("ANY", "GPL-3.0-only",  true, "Incompatible: MIT is not compatible with GPL-3.0-only as GPL-3.0-only is a copyleft license"),
                new Collision("ANY", "GPL-3.0-or-later",  true, "Incompatible: MIT is not compatible with GPL-3.0-or-later as GPL-3.0-or-later is a copyleft license"),
                new Collision("ANY", "LGPL-2.0-only",  false, "Incompatible: MIT is not compatible with LGPL-2.0-only as LGPL-2.0-only is a weak copyleft license"),
                new Collision("ANY", "LGPL-2.1-only",  false, "Incompatible: MIT is not compatible with LGPL-2.1-only as LGPL-2.1-only is a weak copyleft license"),
        )
    }

    public String areLicensesCompatible(License parentLicense, License childLicense, boolean isParentApplication) {
        if (parentLicense.equals(childLicense)) return null;
        var parentLicenseSPDXId = parentLicense.getId();
        var childLicenseSPDXId = childLicense.getId();
        if (parentLicenseSPDXId == null || childLicenseSPDXId == null) return null;

        var collision = switch (childLicenseSPDXId) {
            case "MIT" -> switch (parentLicenseSPDXId) {
                case "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only",
                     "GPL-3.0-or-later" -> "Incompatible: MIT is not compatible with " + parentLicenseSPDXId;
                default -> null;
            };
            case "Apache-2.0" -> switch (parentLicenseSPDXId) {
                case "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only",
                     "GPL-3.0-or-later" -> "Incompatible: Apache-2.0 is not compatible with " + parentLicenseSPDXId;
                default -> null;
            };
            case "BSD-2-Clause" -> switch (parentLicenseSPDXId) {
                case "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only",
                     "GPL-3.0-or-later" -> "Incompatible: BSD-2-Clause is not compatible with " + parentLicenseSPDXId;
                default -> null;
            };
            case "BSD-3-Clause" -> switch (parentLicenseSPDXId) {
                case "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only",
                     "GPL-3.0-or-later" -> "Incompatible: BSD-3-Clause is not compatible with " + parentLicenseSPDXId;
                default -> null;
            };
            case "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only",
                 "GPL-3.0-or-later" -> switch (parentLicenseSPDXId) {
                case "MIT", "Apache-2.0", "BSD-2-Clause", "BSD-3-Clause", "ISC" ->
                        "Incompatible: GPL-2.0 is not compatible with " + parentLicenseSPDXId;
                default -> null;
            };
            case "LGPL-2.0-only" -> switch (parentLicenseSPDXId) {
                case "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only",
                     "GPL-3.0-or-later" -> "Incompatible: LGPL-2.0 is not compatible with " + parentLicenseSPDXId;
                default -> null;
            };
            case "LGPL-2.1-only" -> switch (parentLicenseSPDXId) {
                case "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later", "GPL-3.0-only",
                     "GPL-3.0-or-later" -> "Incompatible: LGPL-2.1 is not compatible with " + parentLicenseSPDXId;
                default -> null;
            };
            case "LGPL-3.0-only" -> switch (parentLicenseSPDXId) {
                case "GPL-1.0-only", "GPL-1.0-or-later", "GPL-2.0-only", "GPL-2.0-or-later" ->
                        "Incompatible: LGPL-3.0 is not compatible with " + parentLicenseSPDXId;
                default -> null;
            };
            default -> {
                //check for copyleft licenses, meaning parent needs to include child license
                if (copyLeftLicense.contains(childLicenseSPDXId)) {
                    yield "Parent license is a copyleft license (" + parentLicenseSPDXId + "), child license (" + childLicenseSPDXId + ") is not.";
                }

                //check for weak copyleft licenses, meaning parent needs to include child license
                if (!isParentApplication && weakCopyLeftLicense.contains(parentLicenseSPDXId) && !weakCopyLeftLicense.contains(childLicenseSPDXId)) {
                    yield "Parent license is a weak copyleft license (" + parentLicenseSPDXId + "), child license (" + childLicenseSPDXId + ") is not.";
                }

                yield null;
            }
        };

        if (collision == null) {
            if (!comparedLicenses.contains(parentLicenseSPDXId + childLicenseSPDXId)) {
                logger.info(parentLicenseSPDXId + " and " + childLicenseSPDXId + " are compatible.");
                comparedLicenses.add(parentLicenseSPDXId + childLicenseSPDXId);
            }
            return null;
        }
        logger.info(childLicenseSPDXId + " does not allow " + parentLicenseSPDXId + " in parent. Cause: " + collision);
        return collision;
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

    record Collision(String spdxIdParent, String spdxIdChild, Boolean forApplication, String cause) {
    }
}
