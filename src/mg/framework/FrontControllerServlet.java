package mg.framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mg.utils.ClassScanner;
import mg.annotation.Controller;
import mg.annotation.RequestMapping;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> controllers;

    @Override
    public void init() {
        System.out.println("=== SPRING0 START ===");
        List<Class<?>> classes = ClassScanner.loadClasses();

        controllers = ClassScanner.classesWithAnnotation(classes, Controller.class);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        processRequest(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        processRequest(request, response);
    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PrintWriter out = response.getWriter();

        String url = request.getRequestURI();
        String contextPath = request.getContextPath();

        url = url.replace(contextPath, "");

        out.println("Spring0 works");
        out.println("URL: " + url);


        try {

            // Search matching method
            boolean found = false;

            for (Class<?> controller : controllers) {

                Map<String, Method> methods =
                        ClassScanner.getMappedMethods(controller);

                if (methods.containsKey(url)) {

                    Method method = methods.get(url);

                    Object instance = controller.getDeclaredConstructor().newInstance();

                    method.invoke(instance, request, response);

                    found = true;
                    break;
                }
            }

            if (!found) {
                out.println("404 Not Found: " + url);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}