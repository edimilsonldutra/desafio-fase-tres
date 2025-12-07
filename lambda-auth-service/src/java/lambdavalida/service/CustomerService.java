package lambdavalida.service;

import com.newrelic.api.agent.NewRelic;
import lambdavalida.model.Customer;

public class CustomerService {
    public CustomerService() {}

    public Customer findByCPF(String cpf) {
        try {
            if ("11144477735".equals(cpf)) {
                return new Customer(cpf, "João Silva", "joao@example.com", "ACTIVE");
            }
            return null;
        } catch (Exception e) {
            NewRelic.noticeError(e);
            throw new RuntimeException("Customer query failed", e);
        }
    }
}
