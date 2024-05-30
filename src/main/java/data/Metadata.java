package data;

public interface Metadata {
    void setProperty(String key, Object value);
    Object getProperty(String key);
}
