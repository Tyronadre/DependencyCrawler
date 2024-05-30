package data.dataImpl;

import data.Metadata;

import java.util.HashMap;
import java.util.Map;

public class MetadataImpl implements Metadata {
    private final Map<String, Object> properties = new HashMap<>();

    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }
}
