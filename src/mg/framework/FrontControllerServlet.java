package mg.framework;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpSession;

import mg.annotation.RestAPI;
import mg.dto.*;
import mg.utils.ClassScanner;

@MultipartConfig
public class FrontControllerServlet extends HttpServlet {

    private Map<URLMethod, Method> urlMappings;

    @Override
    public void init() {
        System.out.println("=== SPRING9 START ===");
        List<Class<?>> classes = ClassScanner.loadClasses();
        ClassScanner.printClasses(classes);
        urlMappings = ClassScanner.createUrlMappings(classes);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
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
                Class<?> controllerClass = method.getDeclaringClass();
                Object instance = controllerClass.getDeclaredConstructor().newInstance();

                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    Class<?> parameterType = parameter.getType();

                    if (parameterType == String.class) {
                        args[i] = request.getParameter(parameter.getName());

                    } else if (parameterType == MapFile.class) {
                        Part part = request.getPart("file");

                        if (part == null || part.getSubmittedFileName() == null
                                || part.getSubmittedFileName().isEmpty()) {
                            throw new IllegalArgumentException("Aucun fichier reçu.");
                        }

                        String fileName = part.getSubmittedFileName();
                        String extension = "";

                        if (fileName.contains(".")) {
                            extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                        }

                        byte[] bytesTable;

                        try (InputStream input = part.getInputStream()) {
                            bytesTable = input.readAllBytes();
                        }

                        MapFile file = new MapFile(fileName, extension, bytesTable);
                        args[i] = file;

                    } else if (parameterType == MySession.class) {
                        HttpSession httpSession = request.getSession();
                        Map<String, Object> data = new HashMap<>();
                        Enumeration<String> names = httpSession.getAttributeNames();

                        while (names.hasMoreElements()) {
                            String name = names.nextElement();
                            data.put(name, httpSession.getAttribute(name));
                        }

                        args[i] = new MySession(data);

                    } else {
                        throw new IllegalArgumentException(
                                "Type non pris en charge : " + parameterType.getName());
                    }
                }

                Object result = method.invoke(instance, args);

                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].getType() == MySession.class) {
                        MySession session = (MySession) args[i];
                        HttpSession httpSession = request.getSession();

                        for (Map.Entry<String, Object> entry : session.getData().entrySet()) {
                            httpSession.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }
                }

                boolean isRestAPI =
                        method.getDeclaringClass().isAnnotationPresent(RestAPI.class);

                if (isRestAPI) {
                    response.setContentType("application/json; charset=UTF-8");
                    out.println("{"
                            + "\"url\":\"" + url + "\","
                            + "\"httpMethod\":\"" + httpMethod + "\","
                            + "\"method\":\"" + method.getName() + "\""
                            + "}");
                    return;
                }

                if (result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;

                    for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    RequestDispatcher dispatcher =
                            request.getRequestDispatcher(mv.getView());

                    dispatcher.forward(request, response);
                    return;
                }

                if (result instanceof String) {
                    out.println(result);
                }

            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("404 Not Found : " + url + " (" + httpMethod + ")");
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}