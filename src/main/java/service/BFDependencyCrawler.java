package service;

import data.Component;

public interface BFDependencyCrawler {
    void loadDependencies(Component parentComponent);
}
