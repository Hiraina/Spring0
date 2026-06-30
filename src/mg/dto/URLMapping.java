package mg.dto;

import java.lang.reflect.Method;

public class URLMapping {

    private Class<?> controller;
    private Method method;


    public Class<?> getController() {
        return controller;
    }


    public void setController(Class<?> controller) {
        this.controller = controller;
    }


    public Method getMethod() {
        return method;
    }


    public void setMethod(Method method) {
        this.method = method;
    }


    @Override
    public String toString() {
        return "URLMapping{" +
                "controller=" + controller.getSimpleName() +
                ", method=" + method.getName() +
                '}';
    }
}