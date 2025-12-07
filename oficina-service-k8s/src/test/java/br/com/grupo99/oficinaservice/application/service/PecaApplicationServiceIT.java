package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.PecaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.Peca;
import br.com.grupo99.oficinaservice.domain.repository.PecaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Teste de Integração Completo - PecaApplicationService")
class PecaApplicationServiceIT {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Autowired
    private PecaApplicationService pecaService;

    @Autowired
    private PecaRepository pecaRepository;

    @Test
    @DisplayName("Dado um DTO de peça válido, Quando criar, Então a peça deve ser salva")
    void givenValidPecaRequest_whenCreate_thenShouldSavePeca() {
        // Given
        PecaRequestDTO request = new PecaRequestDTO("Filtro de Óleo", "Mann Filter", new BigDecimal("35.50"), 20);

        // When
        PecaResponseDTO response = pecaService.create(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Filtro de Óleo");
        assertThat(pecaRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Dado uma peça existente, Quando adicionar estoque, Então o estoque deve ser atualizado")
    void givenExistingPeca_whenAddEstoque_thenShouldUpdateEstoque() {
        // Given
        Peca peca = pecaRepository.save(createPeca("Vela", 10));

        // When
        PecaResponseDTO response = pecaService.adicionarEstoque(peca.getId(), 5);

        // Then
        assertThat(response.estoque()).isEqualTo(15);
    }

    @Test
    @DisplayName("Dado um ID de peça inexistente, Quando adicionar estoque, Então deve lançar exceção")
    void givenNonExistingPecaId_whenAddEstoque_thenShouldThrowException() {
        // Given
        UUID idInexistente = UUID.randomUUID();

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> pecaService.adicionarEstoque(idInexistente, 5));
    }

    @Test
    @DisplayName("Dado uma peça existente, Quando buscar pelo ID, Então deve retornar a peça correta")
    void givenExistingPeca_whenGetById_thenShouldReturnPeca() {
        // Given
        Peca peca = pecaRepository.save(createPeca("Amortecedor", 8));

        // When
        PecaResponseDTO response = pecaService.getById(peca.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Amortecedor");
    }

    @Test
    @DisplayName("Dado que existem peças, Quando listar todas, Então deve retornar a lista de peças")
    void givenPecasExist_whenGetAll_thenShouldReturnPecaList() {
        // Given
        pecaRepository.save(createPeca("Peca A", 1));
        pecaRepository.save(createPeca("Peca B", 2));

        // When
        List<PecaResponseDTO> pecas = pecaService.getAll();

        // Then
        assertThat(pecas).hasSize(2);
    }

    @Test
    @DisplayName("Dado uma peça existente e novos dados, Quando atualizar, Então a peça deve ser atualizada")
    void givenExistingPecaAndNewData_whenUpdate_thenShouldUpdatePeca() {
        // Given
        Peca peca = pecaRepository.save(createPeca("Nome Antigo", 10));
        PecaRequestDTO request = new PecaRequestDTO("Nome Novo", "Novo Fabricante", new BigDecimal("99.99"), 50);

        // When
        PecaResponseDTO response = pecaService.update(peca.getId(), request);

        // Then
        assertThat(response.nome()).isEqualTo("Nome Novo");
        assertThat(response.estoque()).isEqualTo(50);
    }

    @Test
    @DisplayName("Dado uma peça existente, Quando deletar, Então a peça não deve mais existir")
    void givenExistingPeca_whenDelete_thenShouldNotExistAnymore() {
        // Given
        Peca peca = pecaRepository.save(createPeca("Peca para Deletar", 1));
        UUID pecaId = peca.getId();

        // When
        pecaService.delete(pecaId);

        // Then
        assertThat(pecaRepository.findById(pecaId)).isEmpty();
    }

    @Test
    @DisplayName("Deve criar uma nova peça")
    void deveCriarPeca() {
        PecaRequestDTO request = new PecaRequestDTO("Filtro de Óleo", "Mann Filter", new BigDecimal("35.50"), 20);
        PecaResponseDTO response = pecaService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Filtro de Óleo");
        assertThat(pecaRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Deve adicionar estoque a uma peça existente")
    void deveAdicionarEstoque() {
        Peca peca = createPeca("Vela", 10);
        peca = pecaRepository.save(peca);

        PecaResponseDTO response = pecaService.adicionarEstoque(peca.getId(), 5);

        assertThat(response.estoque()).isEqualTo(15);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar estoque a peça inexistente")
    void deveLancarExcecaoAoAdicionarEstoquePecaInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> pecaService.adicionarEstoque(UUID.randomUUID(), 5));
    }

    @Test
    @DisplayName("Deve buscar uma peça pelo ID")
    void deveBuscarPecaPorId() {
        Peca peca = createPeca("Amortecedor", 8);
        peca = pecaRepository.save(peca);

        PecaResponseDTO response = pecaService.getById(peca.getId());
        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Amortecedor");
    }

    @Test
    @DisplayName("Deve atualizar uma peça existente")
    void deveAtualizarPeca() {
        Peca peca = pecaRepository.save(createPeca("Nome Antigo", 10));
        PecaRequestDTO request = new PecaRequestDTO("Nome Novo", "Novo Fabricante", new BigDecimal("99.99"), 50);

        PecaResponseDTO response = pecaService.update(peca.getId(), request);

        assertThat(response.nome()).isEqualTo("Nome Novo");
        assertThat(response.estoque()).isEqualTo(50);
    }

    @Test
    @DisplayName("Deve deletar uma peça existente")
    void deveDeletarPeca() {
        Peca peca = pecaRepository.save(createPeca("Peca para Deletar", 1));
        UUID pecaId = peca.getId();

        pecaService.delete(pecaId);

        assertThat(pecaRepository.findById(pecaId)).isEmpty();
    }

    private Peca createPeca(String nome, int estoque) {
        Peca peca = new Peca();
        peca.setNome(nome);
        peca.setEstoque(estoque);
        peca.setPreco(BigDecimal.ONE);
        return peca;
    }
}