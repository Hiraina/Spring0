package mg.dto;

import java.util.Map;

public class MySession {
    private Map<String, Object> data;

    public MySession(Map<String, Object> data) {
        this.data = data;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void add(String key, Object value) {
        data.put(key, value);
    }

    public Map<String, Object> getData() {
        return data;
    }
}