package lambdavalida.service;

import org.junit.Test;
import static org.junit.Assert.*;

public class CPFValidatorTest {

    private final DocumentoValidator validator = new DocumentoValidator();

    @Test
    public void testValidCPF() {
        assertTrue(validator.isValid("11144477735"));
        assertTrue(validator.isValid("111.444.777-35"));
    }

    @Test
    public void testInvalidCPF() {
        assertFalse(validator.isValid("11144477736")); // Invalid check digit
        assertFalse(validator.isValid("111.444.777-36")); // Invalid check digit
    }

    @Test
    public void testCPFWithAllSameDigits() {
        assertFalse(validator.isValid("11111111111"));
        assertFalse(validator.isValid("00000000000"));
    }

    @Test
    public void testNullOrEmptyCPF() {
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(""));
    }

    @Test
    public void testCPFWithWrongLength() {
        assertFalse(validator.isValid("123"));
        assertFalse(validator.isValid("123456789012"));
    }

    @Test
    public void testFormatCPF() {
        assertEquals("111.444.777-35", validator.format("11144477735"));
        assertNull(validator.format("123"));
        assertNull(validator.format(null));
    }

    // ==================== CNPJ Tests ==================== //
    @Test
    public void testValidCNPJ() {
        assertTrue(validator.isValid("04.252.011/0001-10")); // Known valid
        assertTrue(validator.isValid("04252011000110"));
    }

    @Test
    public void testInvalidCNPJ() {
        assertFalse(validator.isValid("04.252.011/0001-11")); // Altered last digit
        assertFalse(validator.isValid("04252011000111"));
    }

    @Test
    public void testCNPJWithAllSameDigits() {
        assertFalse(validator.isValid("11111111111111"));
        assertFalse(validator.isValid("00000000000000"));
    }

    @Test
    public void testFormatCNPJ() {
        assertEquals("04.252.011/0001-10", validator.format("04252011000110"));
        assertNull(validator.format("123"));
    }
}
