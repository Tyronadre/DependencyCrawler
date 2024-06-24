package service;

import data.Component;
import data.LicenseCollision;

import java.util.List;

public interface LicenseCollisionService {

    /**
     * Check for license collisions in the given component, and any children.
     * @param component The component to check for license collisions.
     * @return A list of license collisions.
     */
    List<LicenseCollision> checkLicenseCollisions(Component component);

}
