package co.edu.escuelaing;

import co.edu.escuelaing.annotations.*;

@RestController
public class FirstWebService {

    @GetMapping("/v1")
    public String index() {
        return "Greetings from MicroSpring!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}
