package service.serviceImpl;

import com.google.protobuf.util.JsonFormat;
import data.Component;
import data.LicenseCollision;
import data.Timestamp;
import dependencyCrawler.LicenseCollisionOutputOuterClass;
import service.DocumentBuilder;
import service.converter.InternalMavenToBomConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LicenseCollisionBuilder implements DocumentBuilder<List<LicenseCollision>> {

    @Override
    public void buildDocument(Component root, String outputFileName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rebuildDocument(List<LicenseCollision> collisions, String path) {
        if (collisions.isEmpty()) {
            logger.info("No License Collisions to write.");
            return;
        }

        logger.info("Writing License Collisions to " + path + "...");

        var builder = LicenseCollisionOutputOuterClass.LicenseCollisionOutput.newBuilder();
        builder.setCreationDate(InternalMavenToBomConverter.buildTimestamp(Timestamp.of(System.currentTimeMillis() / 1000, 0)));
        for (LicenseCollision collision : collisions) {
            var collisionBuilder = LicenseCollisionOutputOuterClass.LicenseCollision.newBuilder();
            collisionBuilder.setParentPurl(collision.getParent().getPurl());
            collisionBuilder.setChildPurl(collision.getChild().getPurl());
            collisionBuilder.setParentLicense(collision.getParentLicense().getNameOrId());
            collisionBuilder.setChildLicense(collision.getChildLicense().getNameOrId());
            collisionBuilder.setCause(collision.getCause());
            builder.addLicenseCollisions(collisionBuilder.build());
        }
        var collisionsProto = builder.build();

        var outputFileDir = path.split("/", 2);
        if (outputFileDir.length > 1) {
            //create out dir if not exists
            File outDir = new File(outputFileDir[0]);
            if (!outDir.exists()) {
                outDir.mkdir();
            }
        }

        // json file
        try {
            var file = new File(path + ".license-collisions.json");
            var outputStream = new FileWriter(file, StandardCharsets.UTF_8);
            outputStream.write(JsonFormat.printer().print(collisionsProto));
            outputStream.close();
        } catch (IOException e) {
            logger.error("Failed writing to JSON.");
        }
    }
}
