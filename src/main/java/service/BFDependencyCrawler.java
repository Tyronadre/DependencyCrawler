package service;

import data.Component;

public interface BFDependencyCrawler {
    /**
     * Crawls the dependencies of the parent component.
     *
     * @param parentComponent the parent component
     */
    void crawl(Component parentComponent, boolean updateDependenciesToNewestVersion);
}
