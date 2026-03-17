package co.edu.escuelaing;


import co.edu.escuelaing.annotations.GetMapping;
import co.edu.escuelaing.annotations.RequestParam;
import co.edu.escuelaing.annotations.RestController;

@RestController
public class GradeController {

    private final GradeService gradeService = new GradeService();

    @GetMapping("/calificar")
    public String calificar(@RequestParam(value = "nota", defaultValue = "0") String nota) {
        return gradeService.calificarNota(nota);
    }

    @GetMapping("/promedio")
    public String promedio(@RequestParam(value = "notas", defaultValue = "") String notas) {
        return gradeService.calcularPromedio(notas);
    }

    @GetMapping("/aprobo")
    public String aprobo(@RequestParam(value = "nota", defaultValue = "0") String nota) {
        return gradeService.aprobo(nota);
    }
}
