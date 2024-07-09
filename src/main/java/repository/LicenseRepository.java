package repository;

import data.License;
import repository.repositoryImpl.LicenseRepositoryImpl;

public interface LicenseRepository {

    static LicenseRepository getInstance() {
        return LicenseRepositoryImpl.getInstance();
    }

    License getLicense(String name, String url);

}
