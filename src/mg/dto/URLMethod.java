package mg.dto;

import java.util.Objects;

public class URLMethod {

    private String url;
    private String method;

    public URLMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof URLMethod)) {
            return false;
        }

        URLMethod other = (URLMethod) obj;

        return url.equals(other.url)
                && method.equals(other.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }

}