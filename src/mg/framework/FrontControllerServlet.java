package mg.framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mg.annotation.RestAPI;
import mg.dto.ModelAndView;
import mg.dto.URLMethod;
import mg.utils.ClassScanner;

public class FrontControllerServlet extends HttpServlet {

    private Map<URLMethod, Method> urlMappings;

    @Override
    public void init() {

        System.out.println("=== SPRING5 START ===");

        List<Class<?>> classes = ClassScanner.loadClasses();

        // Afficher les classes détectées
        ClassScanner.printClasses(classes);

        urlMappings = ClassScanner.createUrlMappings(classes);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException, ServletException {

        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException, ServletException {

        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html; charset=UTF-8");

        PrintWriter out = response.getWriter();

        String url = request.getRequestURI();

        String contextPath = request.getContextPath();

        url = url.replace(contextPath, "");

        String httpMethod = request.getMethod();

        try {

            URLMethod key = new URLMethod(url, httpMethod);

            Method method = urlMappings.get(key);

            if (method != null) {

                Class<?> controllerClass =
                        method.getDeclaringClass();

                Object instance =
                        controllerClass
                                .getDeclaredConstructor()
                                .newInstance();

                Object result;

                // Méthode avec HttpServletRequest et HttpServletResponse
                if (method.getParameterCount() == 2) {

                    result = method.invoke(
                            instance,
                            request,
                            response
                    );

                }

                // Méthode sans paramètre
                else {

                    result = method.invoke(instance);

                }

                // ===== Sprint 6 =====
                // Si la classe possède @RestAPI
                boolean isRestAPI =
                        method.getDeclaringClass()
                                .isAnnotationPresent(RestAPI.class);

                if (isRestAPI) {

                    response.setContentType(
                            "application/json; charset=UTF-8"
                    );

                    out.println(
                            "{"
                            + "\"url\":\"" + url + "\","
                            + "\"httpMethod\":\"" + httpMethod + "\","
                            + "\"method\":\"" + method.getName() + "\""
                            + "}"
                    );

                    return;
                }

                // ===== Sprint 5 =====
                // Si la méthode retourne un ModelAndView
                if (result instanceof ModelAndView) {

                    ModelAndView mv =
                            (ModelAndView) result;

                    for (Map.Entry<String, Object> entry
                            : mv.getData().entrySet()) {

                        request.setAttribute(
                                entry.getKey(),
                                entry.getValue()
                        );
                    }

                    RequestDispatcher dispatcher =
                            request.getRequestDispatcher(
                                    mv.getView()
                            );

                    dispatcher.forward(request, response);

                    return;
                }

                // Si la méthode retourne une String
                if (result instanceof String) {

                    out.println(result);
                }

            } else {

                response.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                out.println(
                        "404 Not Found : "
                                + url
                                + " ("
                                + httpMethod
                                + ")"
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}