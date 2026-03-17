package co.edu.escuelaing;

import co.edu.escuelaing.annotations.*;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";

    @GetMapping("/greeting")
    public String greeting(
            @RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format(template, name);
    }
}