package br.com.grupo99.oficinaservice.infrastructure.persistence.repository;

import br.com.grupo99.oficinaservice.domain.model.Peca;
import br.com.grupo99.oficinaservice.infrastructure.persistence.jpa.PecaJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("PecaRepositoryImpl - Testes Unitários")
class PecaRepositoryImplTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private PecaJpaRepository jpaRepository;

    @InjectMocks
    private PecaRepositoryImpl pecaRepository;

    private Peca peca;
    private UUID pecaId;

    @BeforeEach
    void setUp() {
        pecaId = UUID.randomUUID();
        peca = new Peca();
        peca.setId(pecaId);
        peca.setNome("Pastilha de Freio");
        peca.setFabricante("Bosch");
        peca.setPreco(BigDecimal.valueOf(150.00));
        peca.setEstoque(10);
    }

    @Test
    @DisplayName("Deve salvar uma peça com sucesso")
    void deveSalvarPecaComSucesso() {
        // Given
        when(jpaRepository.save(any(Peca.class))).thenReturn(peca);

        // When
        Peca pecaSalva = pecaRepository.save(peca);

        // Then
        assertThat(pecaSalva).isNotNull();
        assertThat(pecaSalva.getId()).isEqualTo(pecaId);
        assertThat(pecaSalva.getNome()).isEqualTo("Pastilha de Freio");
        verify(jpaRepository, times(1)).save(peca);
    }

    @Test
    @DisplayName("Deve buscar peça por ID com sucesso")
    void deveBuscarPecaPorIdComSucesso() {
        // Given
        when(jpaRepository.findById(pecaId)).thenReturn(Optional.of(peca));

        // When
        Optional<Peca> pecaEncontrada = pecaRepository.findById(pecaId);

        // Then
        assertThat(pecaEncontrada).isPresent();
        assertThat(pecaEncontrada.get().getId()).isEqualTo(pecaId);
        assertThat(pecaEncontrada.get().getNome()).isEqualTo("Pastilha de Freio");
        verify(jpaRepository, times(1)).findById(pecaId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando peça não encontrada")
    void deveRetornarOptionalVazioQuandoPecaNaoEncontrada() {
        // Given
        UUID idInexistente = UUID.randomUUID();
        when(jpaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When
        Optional<Peca> pecaEncontrada = pecaRepository.findById(idInexistente);

        // Then
        assertThat(pecaEncontrada).isEmpty();
        verify(jpaRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve deletar peça por ID com sucesso")
    void deveDeletarPecaPorIdComSucesso() {
        // Given
        doNothing().when(jpaRepository).deleteById(pecaId);

        // When
        pecaRepository.deleteById(pecaId);

        // Then
        verify(jpaRepository, times(1)).deleteById(pecaId);
    }

    @Test
    @DisplayName("Deve buscar todas as peças com sucesso")
    void deveBuscarTodasAsPecasComSucesso() {
        // Given
        Peca outraPeca = new Peca();
        outraPeca.setId(UUID.randomUUID());
        outraPeca.setNome("Filtro de Óleo");
        outraPeca.setFabricante("Mann");
        outraPeca.setPreco(BigDecimal.valueOf(25.50));
        outraPeca.setEstoque(20);

        List<Peca> pecas = Arrays.asList(peca, outraPeca);
        when(jpaRepository.findAll()).thenReturn(pecas);

        // When
        List<Peca> pecasEncontradas = pecaRepository.findAll();

        // Then
        assertThat(pecasEncontradas).hasSize(2);
        assertThat(pecasEncontradas).containsExactlyInAnyOrder(peca, outraPeca);
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há peças")
    void deveRetornarListaVaziaQuandoNaoHaPecas() {
        // Given
        when(jpaRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Peca> pecasEncontradas = pecaRepository.findAll();

        // Then
        assertThat(pecasEncontradas).isEmpty();
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve verificar se peça existe por ID - retorna true")
    void deveVerificarSePecaExistePorIdRetornaTrue() {
        // Given
        when(jpaRepository.existsById(pecaId)).thenReturn(true);

        // When
        boolean existe = pecaRepository.existsById(pecaId);

        // Then
        assertThat(existe).isTrue();
        verify(jpaRepository, times(1)).existsById(pecaId);
    }

    @Test
    @DisplayName("Deve verificar se peça existe por ID - retorna false")
    void deveVerificarSePecaExistePorIdRetornaFalse() {
        // Given
        UUID idInexistente = UUID.randomUUID();
        when(jpaRepository.existsById(idInexistente)).thenReturn(false);

        // When
        boolean existe = pecaRepository.existsById(idInexistente);

        // Then
        assertThat(existe).isFalse();
        verify(jpaRepository, times(1)).existsById(idInexistente);
    }
}