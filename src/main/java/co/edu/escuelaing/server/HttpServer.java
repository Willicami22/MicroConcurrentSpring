package co.edu.escuelaing.server;


import co.edu.escuelaing.annotations.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class HttpServer {

    // Mapa URI -> Método que la maneja
    private Map<String, Method> routeMap = new HashMap<>();
    // Mapa URI -> Instancia del controlador
    private Map<String, Object> instanceMap = new HashMap<>();

    private int port;

    public HttpServer(int port) {
        this.port = port;
    }

    /**
     * REFLEXIÓN: Registra un controlador leyendo sus anotaciones
     */
    public void registerController(Class<?> controllerClass) throws Exception {
        // Verificar que tenga @RestController
        if (!controllerClass.isAnnotationPresent(RestController.class)) {
            System.out.println("Clase sin @RestController, ignorando.");
            return;
        }

        // Crear instancia del POJO
        Object instance = controllerClass.getDeclaredConstructor().newInstance();

        // Buscar métodos con @GetMapping
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                String uri = method.getAnnotation(GetMapping.class).value();
                routeMap.put(uri, method);
                instanceMap.put(uri, instance);
                System.out.println("Ruta registrada: GET " + uri);
            }
        }
    }

    /**
     * Inicia el loop del servidor HTTP
     */
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor escuchando en puerto " + port);

        // Múltiples solicitudes no concurrentes (secuencial)
        while (true) {
            Socket clientSocket = serverSocket.accept();
            handleRequest(clientSocket);
        }
    }

    /**
     * Maneja una solicitud HTTP entrante
     */
    private void handleRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        OutputStream rawOut = clientSocket.getOutputStream();

        // Leer la primera línea: "GET /ruta HTTP/1.1"
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            clientSocket.close();
            return;
        }

        System.out.println("Solicitud: " + requestLine);

        String[] parts = requestLine.split(" ");
        String method = parts[0];   // GET
        String fullUri = parts[1];  // /greeting?name=Carlos

        // Separar URI base de query string
        String uri = fullUri.contains("?") ? fullUri.split("\\?")[0] : fullUri;
        String query = fullUri.contains("?") ? fullUri.split("\\?")[1] : "";

        // Leer y descartar headers
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {}

        // 1. Intentar servir archivo estático (HTML, PNG)
        if (serveStaticFile(uri, out, rawOut)) {
            clientSocket.close();
            return;
        }

        // 2. Buscar ruta en los controladores registrados
        if (routeMap.containsKey(uri)) {
            String response = invokeController(uri, query);
            sendTextResponse(out, response);
        } else {
            send404(out);
        }

        clientSocket.close();
    }

    /**
     * REFLEXIÓN: Invoca el método del controlador con sus parámetros
     */
    private String invokeController(String uri, String query) {
        Method method = routeMap.get(uri);
        Object instance = instanceMap.get(uri);

        try {
            Parameter[] params = method.getParameters();

            if (params.length == 0) {
                // Sin parámetros: invocar directamente
                return (String) method.invoke(instance);
            }

            // Con parámetros: resolver @RequestParam
            Object[] args = resolveParams(params, query);
            return (String) method.invoke(instance, args);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error interno: " + e.getMessage();
        }
    }

    /**
     * Resuelve los parámetros anotados con @RequestParam del query string
     */
    private Object[] resolveParams(Parameter[] params, String query) {
        // Parsear query string: "name=Carlos&age=30"
        Map<String, String> queryParams = new HashMap<>();
        if (!query.isEmpty()) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    queryParams.put(kv[0], kv[1]);
                }
            }
        }

        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = params[i].getAnnotation(RequestParam.class);
                // Usar valor del query o el defaultValue
                args[i] = queryParams.getOrDefault(rp.value(), rp.defaultValue());
            }
        }
        return args;
    }

    /**
     * Sirve archivos estáticos desde /webroot
     */
    private boolean serveStaticFile(String uri, PrintWriter out, OutputStream rawOut) {
        try {
            // Mapear URI a archivo en webroot
            String filePath = "src/main/resources/webroot" + uri;
            File file = new File(filePath);

            if (!file.exists() || file.isDirectory()) return false;

            String contentType = getContentType(uri);
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            // Enviar headers HTTP
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + fileBytes.length);
            out.println("");
            out.flush();

            // Enviar cuerpo binario
            rawOut.write(fileBytes);
            rawOut.flush();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String getContentType(String uri) {
        if (uri.endsWith(".html")) return "text/html";
        if (uri.endsWith(".png"))  return "image/png";
        if (uri.endsWith(".jpg"))  return "image/jpeg";
        if (uri.endsWith(".css"))  return "text/css";
        return "text/plain";
    }

    private void sendTextResponse(PrintWriter out, String body) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/plain");
        out.println("Content-Length: " + body.length());
        out.println("");
        out.println(body);
    }

    private void send404(PrintWriter out) {
        String body = "404 - Página no encontrada";
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/plain");
        out.println("Content-Length: " + body.length());
        out.println("");
        out.println(body);
    }
}