package br.com.grupo99.oficinaservice.infrastructure.persistence.repository;

import br.com.grupo99.oficinaservice.domain.model.Servico;
import br.com.grupo99.oficinaservice.infrastructure.persistence.jpa.ServicoJpaRepository;
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
@DisplayName("ServicoRepositoryImpl Tests")
class ServicoRepositoryImplTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private ServicoJpaRepository jpaRepository;

    @InjectMocks
    private ServicoRepositoryImpl servicoRepository;

    private Servico servico;
    private UUID servicoId;

    @BeforeEach
    void setUp() {
        servicoId = UUID.randomUUID();
        servico = new Servico("Serviço Teste", new BigDecimal("100.00"));
        servico.setId(servicoId);
        servico.setDescricao("Troca completa do óleo do motor");
        servico.setPreco(BigDecimal.valueOf(80.00));
    }

    @Test
    @DisplayName("Deve salvar serviço com sucesso")
    void shouldSaveServicoSuccessfully() {
        // Given
        when(jpaRepository.save(any(Servico.class))).thenReturn(servico);

        // When
        Servico result = servicoRepository.save(servico);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(servicoId);
        assertThat(result.getDescricao()).isEqualTo("Troca completa do óleo do motor");
        assertThat(result.getPreco()).isEqualTo(BigDecimal.valueOf(80.00));
        
        verify(jpaRepository).save(servico);
    }

    @Test
    @DisplayName("Deve buscar serviço por ID com sucesso")
    void shouldFindServicoByIdSuccessfully() {
        // Given
        when(jpaRepository.findById(servicoId)).thenReturn(Optional.of(servico));

        // When
        Optional<Servico> result = servicoRepository.findById(servicoId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(servicoId);
        assertThat(result.get().getDescricao()).isEqualTo("Troca completa do óleo do motor");
        
        verify(jpaRepository).findById(servicoId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando serviço não encontrado por ID")
    void shouldReturnEmptyOptionalWhenServicoNotFoundById() {
        // Given
        when(jpaRepository.findById(servicoId)).thenReturn(Optional.empty());

        // When
        Optional<Servico> result = servicoRepository.findById(servicoId);

        // Then
        assertThat(result).isEmpty();
        
        verify(jpaRepository).findById(servicoId);
    }

    @Test
    @DisplayName("Deve deletar serviço por ID")
    void shouldDeleteServicoById() {
        // Given
        doNothing().when(jpaRepository).deleteById(servicoId);

        // When
        servicoRepository.deleteById(servicoId);

        // Then
        verify(jpaRepository).deleteById(servicoId);
    }

    @Test
    @DisplayName("Deve listar todos os serviços")
    void shouldFindAllServicos() {
        // Given
        Servico servico2 = new Servico("Serviço Teste 2", new BigDecimal("200.00"));
        servico2.setDescricao("Alinhamento das rodas");
        servico2.setPreco(BigDecimal.valueOf(120.00));
        List<Servico> servicos = Arrays.asList(servico, servico2);
        when(jpaRepository.findAll()).thenReturn(servicos);

        // When
        List<Servico> result = servicoRepository.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(servico, servico2);
        
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há serviços")
    void shouldReturnEmptyListWhenNoServicos() {
        // Given
        when(jpaRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Servico> result = servicoRepository.findAll();

        // Then
        assertThat(result).isEmpty();
        
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Deve verificar se serviço existe por ID")
    void shouldCheckIfServicoExistsById() {
        // Given
        when(jpaRepository.existsById(servicoId)).thenReturn(true);

        // When
        boolean result = servicoRepository.existsById(servicoId);

        // Then
        assertThat(result).isTrue();
        
        verify(jpaRepository).existsById(servicoId);
    }

    @Test
    @DisplayName("Deve retornar false quando serviço não existe por ID")
    void shouldReturnFalseWhenServicoNotExistsById() {
        // Given
        when(jpaRepository.existsById(servicoId)).thenReturn(false);

        // When
        boolean result = servicoRepository.existsById(servicoId);

        // Then
        assertThat(result).isFalse();
        
        verify(jpaRepository).existsById(servicoId);
    }
}