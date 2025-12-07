package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.application.util.DocumentoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Testes unitários para ClienteResponseDTO")
class ClienteResponseDTOTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Test
    @DisplayName("Deve criar ClienteResponseDTO corretamente com todos os campos")
    void shouldCreateClienteResponseDTOWithAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        String nome = "João Silva";
        String cpfCnpj = "123.456.789-00";
        String telefone = "11999887766";
        String email = "joao@email.com";

        // When
        ClienteResponseDTO dto = new ClienteResponseDTO(id, nome, cpfCnpj, telefone, email);

        // Then
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.nome()).isEqualTo(nome);
        assertThat(dto.cpfCnpj()).isEqualTo(cpfCnpj);
        assertThat(dto.telefone()).isEqualTo(telefone);
        assertThat(dto.email()).isEqualTo(email);
    }

    @Test
    @DisplayName("Deve criar ClienteResponseDTO a partir de entidade Cliente")
    void shouldCreateDTOFromClienteEntity() {
        // Given
        Cliente cliente = criarClienteComPessoa("Maria Santos", "98765432100");
        cliente.setId(UUID.randomUUID());
        cliente.getPessoa().setPhone("11888776655");
        cliente.getPessoa().setEmail("maria@email.com");

        // When
        ClienteResponseDTO dto = ClienteResponseDTO.fromDomain(cliente);

        // Then
        assertThat(dto.id()).isEqualTo(cliente.getId());
        assertThat(dto.nome()).isEqualTo(cliente.getPessoa().getName());
        assertThat(dto.cpfCnpj()).isEqualTo(DocumentoUtils.aplicarMascara(cliente.getPessoa().getNumeroDocumento()));
        assertThat(dto.telefone()).isEqualTo(cliente.getPessoa().getPhone());
        assertThat(dto.email()).isEqualTo(cliente.getPessoa().getEmail());
    }

    @Test
    @DisplayName("Deve criar ClienteResponseDTO a partir de Cliente com campos opcionais nulos")
    void shouldCreateDTOFromClienteWithNullOptionalFields() {
        // Given
        Cliente cliente = criarClienteComPessoa("Pedro Costa", "11122233344");
        cliente.setId(UUID.randomUUID());
        // telefone e email ficam null

        // When
        ClienteResponseDTO dto = ClienteResponseDTO.fromDomain(cliente);

        // Then
        assertThat(dto.id()).isEqualTo(cliente.getId());
        assertThat(dto.nome()).isEqualTo(cliente.getPessoa().getName());
        assertThat(dto.cpfCnpj()).isEqualTo(DocumentoUtils.aplicarMascara(cliente.getPessoa().getNumeroDocumento()));
        assertThat(dto.telefone()).isNull();
        assertThat(dto.email()).isNull();
    }

    @Test
    @DisplayName("Deve manter igualdade entre dois DTOs com mesmos valores")
    void shouldMaintainEqualityBetweenSameDTOs() {
        // Given
        UUID id = UUID.randomUUID();
        ClienteResponseDTO dto1 = new ClienteResponseDTO(id, "Nome", "123.456.789-00", "11999887766", "email@test.com");
        ClienteResponseDTO dto2 = new ClienteResponseDTO(id, "Nome", "123.456.789-00", "11999887766", "email@test.com");

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString() válido")
    void shouldHaveValidToString() {
        // Given
        UUID id = UUID.randomUUID();
        ClienteResponseDTO dto = new ClienteResponseDTO(id, "Nome Teste", "123.456.789-00", "11999887766", "test@email.com");

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("Nome Teste");
        assertThat(toString).contains("123.456.789-00");
        assertThat(toString).contains("11999887766");
        assertThat(toString).contains("test@email.com");
        assertThat(toString).contains(id.toString());
    }
}