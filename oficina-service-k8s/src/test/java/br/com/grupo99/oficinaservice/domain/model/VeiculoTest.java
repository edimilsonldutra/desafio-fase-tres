package br.com.grupo99.oficinaservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Teste Unitário da Entidade Veículo")
class VeiculoTest {

    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            "teste@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }

    @Test
    @DisplayName("Deve criar um veículo com seus atributos principais")
    void deveCriarVeiculo() {
        Veiculo veiculo = new Veiculo("XYZ-9876", "Chevrolet", "Onix", 2020);
        assertNotNull(veiculo);
        assertEquals("XYZ-9876", veiculo.getPlaca());
        assertEquals("Chevrolet", veiculo.getMarca());
        assertEquals("Onix", veiculo.getModelo());
        assertEquals(2020, veiculo.getAno());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar veículo com placa nula")
    void deveLancarExcecaoPlacaNula() {
        assertThrows(IllegalArgumentException.class, () -> new Veiculo(null, "Chevrolet", "Onix", 2020));
    }

    @Test
    @DisplayName("Deve atualizar dados do veículo")
    void deveAtualizarDadosVeiculo() {
        Veiculo veiculo = new Veiculo("XYZ-9876", "Chevrolet", "Onix", 2020);
        veiculo.setMarca("Fiat");
        veiculo.setModelo("Argo");
        veiculo.setAno(2022);
        assertEquals("Fiat", veiculo.getMarca());
        assertEquals("Argo", veiculo.getModelo());
        assertEquals(2022, veiculo.getAno());
    }

    @Test
    @DisplayName("Deve associar veículo a cliente")
    void deveAssociarVeiculoCliente() {
        Veiculo veiculo = new Veiculo("XYZ-9876", "Chevrolet", "Onix", 2020);
        Cliente cliente = criarClienteComPessoa("João da Silva", "12345678900");
        veiculo.setCliente(cliente);
        assertEquals(cliente, veiculo.getCliente());
    }

    @Test
    @DisplayName("Deve validar equals e hashCode")
    void deveValidarEqualsHashCode() {
        Veiculo v1 = new Veiculo("XYZ-9876", "Chevrolet", "Onix", 2020);
        Veiculo v2 = new Veiculo("XYZ-9876", "Chevrolet", "Onix", 2020);
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    @DisplayName("Deve validar toString")
    void deveValidarToString() {
        Veiculo veiculo = new Veiculo("XYZ-9876", "Chevrolet", "Onix", 2020);
        String str = veiculo.toString();
        assertTrue(str.contains("XYZ-9876"));
        assertTrue(str.contains("Chevrolet"));
        assertTrue(str.contains("Onix"));
    }

    @Test
    @DisplayName("Dado um veículo criado, quando consultar os dados, então deve retornar os valores corretos")
    void dadoVeiculoCriado_quandoConsultarDados_entaoRetornaCorreto() {
        // Given
        Veiculo veiculo = new Veiculo("JKL-3456", "Toyota", "Corolla", 2021);
        // When
        String placa = veiculo.getPlaca();
        String marca = veiculo.getMarca();
        String modelo = veiculo.getModelo();
        int ano = veiculo.getAno();
        // Then
        assertEquals("JKL-3456", placa);
        assertEquals("Toyota", marca);
        assertEquals("Corolla", modelo);
        assertEquals(2021, ano);
    }

    @Test
    @DisplayName("Dado um veículo, quando associar a um cliente, então o cliente deve ser atualizado")
    void dadoVeiculo_quandoAssociarCliente_entaoClienteAtualizado() {
        // Given
        Veiculo veiculo = new Veiculo("MNO-7890", "Honda", "Civic", 2019);
        Cliente cliente = criarClienteComPessoa("Pedro", "55566677788");
        // When
        veiculo.setCliente(cliente);
        // Then
        assertEquals(cliente, veiculo.getCliente());
    }

    @Test
    @DisplayName("Dado um veículo, quando atualizar marca/modelo/ano, então os dados devem ser atualizados")
    void dadoVeiculo_quandoAtualizarDados_entaoAtualiza() {
        // Given
        Veiculo veiculo = new Veiculo("PQR-1234", "Renault", "Sandero", 2017);
        // When
        veiculo.setMarca("Hyundai");
        veiculo.setModelo("HB20");
        veiculo.setAno(2022);
        // Then
        assertEquals("Hyundai", veiculo.getMarca());
        assertEquals("HB20", veiculo.getModelo());
        assertEquals(2022, veiculo.getAno());
    }

    @Test
    @DisplayName("Dado um veículo, quando criar com placa nula, então deve lançar exceção")
    void dadoVeiculo_quandoPlacaNula_entaoLancaExcecao() {
        // Given/When/Then
        assertThrows(IllegalArgumentException.class, () -> new Veiculo(null, "Chevrolet", "Onix", 2020));
    }
}
