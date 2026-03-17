const output = document.getElementById("output");
const raw    = document.getElementById("raw");

async function call(path) {
    output.innerHTML = "Consultando <code>" + path + "</code> …";
    raw.textContent  = "";
    try {
        const res  = await fetch(path);
        const text = await res.text();

        // Extract the <body> content from the framework's HTML wrapper
        const match = text.match(/<body>([\s\S]*?)<\/body>/i);
        const body  = match ? match[1] : text;

        output.innerHTML = body;
        raw.textContent  = "HTTP " + res.status + "  ←  GET " + path;
    } catch (err) {
        output.textContent = "Error: " + err.message;
    }
}

function calificar() {
    const nota = document.getElementById("notaInput").value.trim();
    if (!nota) { output.textContent = "Ingresa una nota."; return; }
    call("/calificar?nota=" + encodeURIComponent(nota));
}

function promedio() {
    const notas = document.getElementById("notasInput").value.trim();
    if (!notas) { output.textContent = "Ingresa las notas separadas por coma."; return; }
    call("/promedio?notas=" + encodeURIComponent(notas));
}

function aprobo() {
    const nota = document.getElementById("aproboInput").value.trim();
    if (!nota) { output.textContent = "Ingresa una nota."; return; }
    call("/aprobo?nota=" + encodeURIComponent(nota));
}
