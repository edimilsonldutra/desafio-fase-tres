package lambdavalida.service;

public class DocumentoValidator {

    /**
     * Validates both CPF and CNPJ documents
     * @param document The document to validate (CPF or CNPJ)
     * @return true if valid, false otherwise
     */
    public boolean isValid(String document) {
        if (document == null || document.isEmpty()) {
            return false;
        }

        String cleanDoc = document.replaceAll("\\D", "");

        if (cleanDoc.length() == 11) {
            return isValidCPF(cleanDoc);
        } else if (cleanDoc.length() == 14) {
            return isValidCNPJ(cleanDoc);
        }

        return false;
    }

    /**
     * Formats a document (CPF or CNPJ) with proper mask
     * @param document The document to format
     * @return Formatted document or null if invalid
     */
    public String format(String document) {
        if (document == null || document.isEmpty()) {
            return null;
        }

        String cleanDoc = document.replaceAll("\\D", "");

        if (cleanDoc.length() == 11) {
            return formatCPF(cleanDoc);
        } else if (cleanDoc.length() == 14) {
            return formatCNPJ(cleanDoc);
        }

        return null;
    }

    /**
     * Formats CPF: XXX.XXX.XXX-XX
     */
    private String formatCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return null;
        }
        return String.format("%s.%s.%s-%s",
            cpf.substring(0, 3),
            cpf.substring(3, 6),
            cpf.substring(6, 9),
            cpf.substring(9, 11)
        );
    }

    /**
     * Formats CNPJ: XX.XXX.XXX/XXXX-XX
     */
    private String formatCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return null;
        }
        return String.format("%s.%s.%s/%s-%s",
            cnpj.substring(0, 2),
            cnpj.substring(2, 5),
            cnpj.substring(5, 8),
            cnpj.substring(8, 12),
            cnpj.substring(12, 14)
        );
    }

    /**
     * Static method for CPF validation (for backward compatibility)
     */
    public static boolean isValidCPF(String cpf) {
        if (cpf == null || cpf.isEmpty()) return false;

        cpf = cpf.replaceAll("\\D", "");
        if (cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false;

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int fd = 11 - (sum % 11);
            if (fd >= 10) fd = 0;
            if (fd != Character.getNumericValue(cpf.charAt(9))) return false;

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int sd = 11 - (sum % 11);
            if (sd >= 10) sd = 0;

            return sd == Character.getNumericValue(cpf.charAt(10));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates CNPJ
     */
    private boolean isValidCNPJ(String cnpj) {
        if (cnpj == null || cnpj.isEmpty()) return false;

        cnpj = cnpj.replaceAll("\\D", "");
        if (cnpj.length() != 14) return false;
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        try {
            // First check digit
            int sum = 0;
            int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            for (int i = 0; i < 12; i++) {
                sum += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
            }
            int firstDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);
            if (firstDigit != Character.getNumericValue(cnpj.charAt(12))) return false;

            // Second check digit
            sum = 0;
            int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            for (int i = 0; i < 13; i++) {
                sum += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
            }
            int secondDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

            return secondDigit == Character.getNumericValue(cnpj.charAt(13));
        } catch (Exception e) {
            return false;
        }
    }
}
