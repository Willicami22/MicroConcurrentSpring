package co.edu.escuelaing;

import java.util.Locale;

public class GradeService {

    private static final double MIN_GRADE = 0.0;
    private static final double MAX_GRADE = 5.0;
    private static final double PASSING_GRADE = 3.0;

    public String calificarNota(String notaRaw) {
        try {
            double nota = parseNota(notaRaw);

            if (nota >= 4.5) {
                return "Nota " + format(nota) + ": Excelente";
            }
            if (nota >= 4.0) {
                return "Nota " + format(nota) + ": Sobresaliente";
            }
            if (nota >= PASSING_GRADE) {
                return "Nota " + format(nota) + ": Aprobado";
            }
            return "Nota " + format(nota) + ": Reprobado";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    public String calcularPromedio(String notasRaw) {
        if (notasRaw == null || notasRaw.trim().isEmpty()) {
            return "Error: debes enviar al menos una nota.";
        }

        String[] partes = notasRaw.split(",");
        double suma = 0.0;
        int cantidad = 0;

        try {
            for (String parte : partes) {
                if (parte.trim().isEmpty()) {
                    continue;
                }
                double nota = parseNota(parte.trim());
                suma += nota;
                cantidad++;
            }
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }

        if (cantidad == 0) {
            return "Error: no se encontraron notas validas.";
        }

        double promedio = suma / cantidad;
        return "Promedio: " + format(promedio);
    }

    public String aprobo(String notaRaw) {
        try {
            double nota = parseNota(notaRaw);
            return nota >= PASSING_GRADE
                    ? "Si, aprobo con " + format(nota)
                    : "No, reprobo con " + format(nota);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private double parseNota(String notaRaw) {
        if (notaRaw == null || notaRaw.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: la nota es obligatoria.");
        }

        double nota;
        try {
            nota = Double.parseDouble(notaRaw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Error: la nota debe ser numerica.");
        }

        if (nota < MIN_GRADE || nota > MAX_GRADE) {
            throw new IllegalArgumentException("Error: la nota debe estar entre 0.0 y 5.0.");
        }

        return nota;
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}

