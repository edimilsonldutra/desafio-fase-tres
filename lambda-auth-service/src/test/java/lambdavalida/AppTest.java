package lambdavalida;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for ValidaPessoaFunction
 */
public class AppTest {

    @Test
    public void testValidaPessoaFunctionExists() {
        // Test that the function can be instantiated
        ValidaPessoaFunction function = new ValidaPessoaFunction();
        assertNotNull("ValidaPessoaFunction should be instantiated", function);
    }

    @Test
    public void testInvalidRequestReturns400() {
        ValidaPessoaFunction function = new ValidaPessoaFunction();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody(null); // Invalid body

        APIGatewayProxyResponseEvent response = function.handleRequest(request, null);

        assertNotNull("Response should not be null", response);
        assertEquals("Should return 400 for invalid request",
                     Integer.valueOf(400), response.getStatusCode());
    }

    @Test
    public void testValidRequestStructure() {
        ValidaPessoaFunction function = new ValidaPessoaFunction();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setBody("{\"cpf\":\"invalid\"}");

        APIGatewayProxyResponseEvent response = function.handleRequest(request, null);

        assertNotNull("Response should not be null", response);
        assertNotNull("Response should have status code", response.getStatusCode());
        assertNotNull("Response should have body", response.getBody());
    }
}
