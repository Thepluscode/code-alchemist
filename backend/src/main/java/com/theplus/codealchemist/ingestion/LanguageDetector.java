package com.theplus.codealchemist.ingestion;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LanguageDetector {

    private static final Map<String, String> EXTENSION_MAP = Map.ofEntries(
            Map.entry(".cob", "COBOL"), Map.entry(".cbl", "COBOL"), Map.entry(".cpy", "COBOL"),
            Map.entry(".jcl", "COBOL"), Map.entry(".bms", "COBOL"),
            Map.entry(".rpg", "RPG"), Map.entry(".rpgle", "RPG"),
            Map.entry(".pli", "PLI"), Map.entry(".pl1", "PLI"),
            Map.entry(".vb", "VB6"), Map.entry(".cls", "VB6"), Map.entry(".frm", "VB6"),
            Map.entry(".bas", "VB6"),
            Map.entry(".asp", "CLASSIC_ASP"), Map.entry(".asa", "CLASSIC_ASP")
    );

    public String detect(String filename, String content) {
        String ext = filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.')).toLowerCase()
                : "";

        String byExtension = EXTENSION_MAP.get(ext);
        if (byExtension != null) return byExtension;

        // Fallback: content heuristics
        if (content != null) {
            String upper = content.toUpperCase();
            if (upper.contains("IDENTIFICATION DIVISION") || upper.contains("PROCEDURE DIVISION")) return "COBOL";
            if (upper.contains("BEGSR") || upper.contains("ENDSR") || upper.contains("C/EXEC")) return "RPG";
            if (upper.contains("SUB MAIN") || upper.contains("PRIVATE SUB") || upper.contains("DIM ")) return "VB6";
            if (upper.contains("<%") && upper.contains("RESPONSE.WRITE")) return "CLASSIC_ASP";
        }

        return "UNKNOWN";
    }
}
