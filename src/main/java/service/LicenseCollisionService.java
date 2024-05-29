package service;

import data.Component;
import data.LicenseCollision;
import service.serviceImpl.LicenseCollisionServiceImpl;

import java.util.List;

public interface LicenseCollisionService {

    static LicenseCollisionService getInstance() {
        return LicenseCollisionServiceImpl.getInstance();
    }

    /**
     * Check for license collisions in the given component, and any children.
     * @param component The component to check for license collisions.
     * @return A list of license collisions.
     */
    List<LicenseCollision> checkLicenseCollisions(Component component);

}
