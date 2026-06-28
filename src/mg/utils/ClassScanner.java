package mg.utils;

import mg.annotation.Controller;
import mg.dto.URLMapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import mg.annotation.RequestMapping;

public class ClassScanner {


    public static List<Class<?>> loadClasses() {

        List<Class<?>> classes = new ArrayList<>();

        try {

            String path = Thread.currentThread()
                    .getContextClassLoader()
                    .getResource("")
                    .getPath();

            File root = new File(path);

            scanFolder(root, "", classes);


        } catch (Exception e) {

            e.printStackTrace();

        }

        return classes;
    }



    private static void scanFolder(
            File folder,
            String packageName,
            List<Class<?>> classes) {


        File[] files = folder.listFiles();

        if (files == null)
            return;


        for (File file : files) {


            if (file.isDirectory()) {


                String newPackage = packageName;


                if (!newPackage.isEmpty()) {
                    newPackage += ".";
                }


                newPackage += file.getName();


                scanFolder(
                        file,
                        newPackage,
                        classes
                );


            } else if (file.getName().endsWith(".class")) {


                String className =
                        packageName + "." +
                        file.getName()
                        .replace(".class", "");


                try {

                    Class<?> c =
                            Class.forName(className);

                    classes.add(c);


                } catch (Exception e) {

                    System.out.println(
                            "Cannot load: "
                            + className
                    );

                }
            }
        }
    }



    public static List<Class<?>> classesWithAnnotation(
            List<Class<?>> classes,
            Class<? extends java.lang.annotation.Annotation> annotation) {


        List<Class<?>> result = new ArrayList<>();


        for (Class<?> c : classes) {


            if (c.isAnnotationPresent(annotation)) {

                result.add(c);

            }
        }


        return result;
    }


    // affichage dans la console
    public static void printClasses(
            List<Class<?>> classes) {


        for (Class<?> c : classes) {

            System.out.println(
                    "Class: "
                    + c.getName()
            );

        }
    }

    public static Map<String, Method> getMappedMethods(Class<?> clazz) {

        Map<String, Method> map = new HashMap<>();

        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {

            if (method.isAnnotationPresent(RequestMapping.class)) {

                RequestMapping rm =
                        method.getAnnotation(RequestMapping.class);

                map.put(rm.value(), method);
            }
        }

        return map;
    }

    public static Map<String, URLMapping> createUrlMappings(
        List<Class<?>> classes) {

        Map<String, URLMapping> urlMap = new HashMap<>();

        for (Class<?> c : classes) {

            // On garde uniquement les controllers
            if (!c.isAnnotationPresent(Controller.class)) {
                continue;
            }

            // On inspecte toutes les méthodes
            for (Method m : c.getDeclaredMethods()) {

                if (m.isAnnotationPresent(RequestMapping.class)) {

                    RequestMapping rm = m.getAnnotation(RequestMapping.class);

                    URLMapping mapping = new URLMapping();

                    mapping.setController(c);
                    mapping.setMethod(m);

                    urlMap.put(rm.value(), mapping);
                }
            }
        }

        return urlMap;
    }
}