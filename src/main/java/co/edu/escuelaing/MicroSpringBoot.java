package co.edu.escuelaing;

import co.edu.escuelaing.annotations.RestController;
import co.edu.escuelaing.server.HttpServer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MicroSpringBoot {

    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer(8080);

        if (args.length > 0) {

            Class<?> controllerClass = Class.forName(args[0]);
            server.registerController(controllerClass);
        } else {
            System.out.println("Escaneando classpath en busca de @RestController...");
            List<Class<?>> controllers = findControllers();
            for (Class<?> c : controllers) {
                server.registerController(c);
            }
        }

        server.start();
    }

    /**
     * REFLEXIÓN: Escanea el classpath buscando clases con @RestController
     */
    private static List<Class<?>> findControllers() throws Exception {
        List<Class<?>> result = new ArrayList<>();

        String packageName = "co.edu.escuelaing.reflexionlab";
        String path = packageName.replace('.', '/');

        URL resource = Thread.currentThread()
                .getContextClassLoader().getResource(path);

        if (resource == null) return result;

        File directory = new File(resource.toURI());
        scanDirectory(directory, packageName, result);

        return result;
    }

    private static void scanDirectory(File dir, String packageName,
                                      List<Class<?>> result) throws Exception {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), result);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." +
                        file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(RestController.class)) {
                        System.out.println("Encontrado: " + className);
                        result.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    // ignorar clases no cargables
                }
            }
        }
    }
}
