package mg.framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mg.utils.ClassScanner;
import mg.annotation.Controller;
import mg.dto.URLMapping;

public class FrontControllerServlet extends HttpServlet {

    private Map<String, URLMapping> urlMappings;

    @Override
    public void init() {
        System.out.println("=== SPRING2 START ===");

        List<Class<?>> classes = ClassScanner.loadClasses();

        urlMappings = ClassScanner.createUrlMappings(classes);
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

        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        String url = request.getRequestURI();
        String contextPath = request.getContextPath();

        url = url.replace(contextPath, "");

        try {

            URLMapping mapping = urlMappings.get(url);

            if (mapping != null) {

                Object instance =
                        mapping.getController()
                                .getDeclaredConstructor()
                                .newInstance();

                mapping.getMethod()
                        .invoke(instance, request, response);

            } else {

                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("404 Not Found: " + url);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}