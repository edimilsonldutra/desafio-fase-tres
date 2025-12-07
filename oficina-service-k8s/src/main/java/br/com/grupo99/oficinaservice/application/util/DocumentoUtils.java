package br.com.grupo99.oficinaservice.application.util;

public class DocumentoUtils {
    public static String removerMascara(String documento) {
        if (documento == null) return null;
        return documento.replaceAll("[.\\-/]", ""); // Remove ponto, traço e barra
    }

    public static String aplicarMascara(String documento) {
        if (documento == null) return null;
        String doc = removerMascara(documento);
        if (doc.length() == 11) {
            // CPF: 000.000.000-00
            return doc.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        } else if (doc.length() == 14) {
            // CNPJ: 00.000.000/0000-00
            return doc.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }
        return documento;
    }
}
