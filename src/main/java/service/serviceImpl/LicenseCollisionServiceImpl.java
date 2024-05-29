package service.serviceImpl;

import data.Component;
import data.Dependency;
import data.LicenseCollision;
import service.LicenseCollisionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LicenseCollisionServiceImpl implements LicenseCollisionService {
    private static LicenseCollisionServiceImpl instance;

    public static LicenseCollisionServiceImpl getInstance() {
        if (instance == null)
            instance = new LicenseCollisionServiceImpl();
        return instance;
    }

    private LicenseCollisionServiceImpl() {
    }


    @Override
    public List<LicenseCollision> checkLicenseCollisions(Component component) {
        var licenses = new ArrayList<String>();
        for (var dependency : component.getDependencies()) {
            if (dependency == null || dependency.getComponent() == null || dependency.getComponent().getLicenseExpression() == null || dependency.getComponent().getLicenseExpression().isEmpty())
                continue;
            licenses.add(dependency.getComponent().getLicenseExpression());
        }

        System.out.println(licenses);
        return List.of();
    }
}
