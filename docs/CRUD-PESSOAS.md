# CRUD de Pessoas - Documentação Técnica

## Visão Geral

Implementação completa do CRUD da entidade **Pessoa**, com relacionamentos 1:1 para **Cliente** (perfil CLIENTE) e **Funcionário** (perfil MECANICO/ADMIN).

## Estrutura de Entidades

### 1. Pessoa (Entidade Principal)
**Arquivo:** `domain/model/Pessoa.java`

**Campos:**
- `id` (UUID) - Chave primária
- `numeroDocumento` (String) - CPF ou CNPJ (único)
- `tipoPessoa` (Enum) - FISICA ou JURIDICA
- `name` (String) - Nome completo
- `email` (String) - Email (único)
- `phone` (String) - Telefone (opcional)
- `cargo` (String) - Cargo/função (opcional)
- `perfil` (Enum) - CLIENTE, MECANICO ou ADMIN
- `createdAt` (LocalDateTime) - Data de criação
- `updatedAt` (LocalDateTime) - Data de atualização

**Relacionamentos:**
- `@OneToOne` com `Cliente` (quando perfil = CLIENTE)
- `@OneToOne` com `Funcionario` (quando perfil = MECANICO ou ADMIN)

**Validações:**
- Documento deve ter tamanho correto conforme TipoPessoa
- Nome não pode ser vazio
- Email deve ser válido

---

### 2. Cliente (Relacionado a Pessoa)
**Arquivo:** `domain/model/Cliente.java`

**Refatorado - Campos removidos:**
- ❌ `nome` (agora em Pessoa)
- ❌ `cpfCnpj` (agora em Pessoa como numeroDocumento)
- ❌ `telefone` (agora em Pessoa como phone)
- ❌ `email` (agora em Pessoa)

**Campos atuais:**
- `id` (UUID) - Chave primária
- `pessoa_id` (UUID) - FK para Pessoa (UNIQUE, NOT NULL)
- `veiculos` (List<Veiculo>) - Veículos do cliente

**Validação:**
- Pessoa deve ter `perfil = CLIENTE`

**Relacionamentos:**
- `@OneToOne` com `Pessoa`
- `@OneToMany` com `Veiculo`

**Construtor:**
```java
public Cliente(Pessoa pessoa)
```

---

### 3. Funcionario (Nova Entidade)
**Arquivo:** `domain/model/Funcionario.java`

**Campos:**
- `id` (UUID) - Chave primária
- `pessoa_id` (UUID) - FK para Pessoa (UNIQUE, NOT NULL)
- `dataAdmissao` (LocalDate) - Data de admissão
- `setor` (String) - Setor/departamento
- `salario` (Double) - Salário
- `createdAt` / `updatedAt` (LocalDateTime)

**Validações:**
- Pessoa não pode ter `perfil = CLIENTE`
- Pessoa não pode ser nula

---

## Repositórios

### PessoaRepository
**Arquivo:** `domain/repository/PessoaRepository.java`

**Métodos:**
- `findByNumeroDocumento(String numeroDocumento)`
- `findByEmail(String email)`
- `existsByNumeroDocumento(String numeroDocumento)`
- `existsByEmail(String email)`

### FuncionarioRepository
**Arquivo:** `domain/repository/FuncionarioRepository.java`

**Métodos:**
- `findByPessoa(Pessoa pessoa)`
- `findByPessoaId(UUID pessoaId)`

---

## DTOs

### PessoaRequestDTO
**Arquivo:** `application/dto/PessoaRequestDTO.java`

**Campos:**
```java
String numeroDocumento
TipoPessoa tipoPessoa
String name
String email
String phone
String cargo
Perfil perfil
// Campos para Funcionário (se aplicável)
String setor
Double salario
```

**Validações:**
- Todos os campos obrigatórios não podem ser nulos/vazios

---

### PessoaResponseDTO
**Arquivo:** `application/dto/PessoaResponseDTO.java`

**Retorna:**
- Todos os dados da Pessoa
- Dados do Funcionário (se existir): `funcionarioId`, `dataAdmissao`, `setor`, `salario`

**Factory Method:**
```java
PessoaResponseDTO.from(Pessoa pessoa)
```

---

## Use Cases (Regras de Negócio)

### 1. CreatePessoaUseCase
**Arquivo:** `application/usecases/CreatePessoaUseCase.java`

**Funcionalidade:**
- Valida se documento e email já existem
- Cria a entidade Pessoa
- Se perfil = MECANICO ou ADMIN, cria também Funcionario
- Retorna `PessoaResponseDTO`

**Exemplo:**
```java
PessoaRequestDTO request = new PessoaRequestDTO(
    "12345678901", // CPF
    TipoPessoa.FISICA,
    "João Silva",
    "joao@email.com",
    "11999999999",
    "Gerente",
    Perfil.ADMIN,
    "TI",
    5000.0
);
PessoaResponseDTO response = createPessoaUseCase.execute(request);
```

---

### 2. GetPessoaByIdUseCase
**Arquivo:** `application/usecases/GetPessoaByIdUseCase.java`

**Funcionalidade:**
- Busca Pessoa por ID
- Retorna `PessoaResponseDTO` ou lança exceção se não encontrada

---

### 3. ListAllPessoasUseCase
**Arquivo:** `application/usecases/ListAllPessoasUseCase.java`

**Funcionalidade:**
- Lista todas as Pessoas cadastradas
- Retorna `List<PessoaResponseDTO>`

---

### 4. UpdatePessoaUseCase
**Arquivo:** `application/usecases/UpdatePessoaUseCase.java`

**Funcionalidade:**
- Valida se documento/email já existem para outra pessoa
- Atualiza dados da Pessoa
- Gerencia relacionamento com Funcionário:
  - Se mudou para MECANICO/ADMIN: cria ou atualiza Funcionário
  - Se mudou para CLIENTE: remove Funcionário
- Retorna `PessoaResponseDTO`

**Exemplo:**
```java
// Alterar perfil de CLIENTE para ADMIN
PessoaRequestDTO request = new PessoaRequestDTO(..., Perfil.ADMIN, "RH", 6000.0);
PessoaResponseDTO response = updatePessoaUseCase.execute(pessoaId, request);
// Funcionario será criado automaticamente
```

---

### 5. DeletePessoaUseCase
**Arquivo:** `application/usecases/DeletePessoaUseCase.java`

**Funcionalidade:**
- Deleta Pessoa por ID
- Cascade delete remove automaticamente Cliente ou Funcionario relacionado

---

## REST Controller

### PessoaRestController
**Arquivo:** `infrastructure/controller/PessoaRestController.java`

**Base URL:** `/api/v1/pessoas`

### Endpoints

#### 1. Criar Pessoa
```http
POST /api/v1/pessoas
Authorization: Bearer <token>
Content-Type: application/json

{
  "numeroDocumento": "12345678901",
  "tipoPessoa": "FISICA",
  "name": "João Silva",
  "email": "joao@email.com",
  "phone": "11999999999",
  "cargo": "Gerente",
  "perfil": "ADMIN",
  "setor": "TI",
  "salario": 5000.0
}
```

**Autorização:** `@RequiresRole({Perfil.ADMIN})`

**Resposta:** `201 Created` + `PessoaResponseDTO`

---

#### 2. Buscar Pessoa por ID
```http
GET /api/v1/pessoas/{id}
Authorization: Bearer <token>
```

**Autorização:** `@RequiresRole({Perfil.MECANICO, Perfil.ADMIN})`

**Resposta:** `200 OK` + `PessoaResponseDTO`

---

#### 3. Listar Todas as Pessoas
```http
GET /api/v1/pessoas
Authorization: Bearer <token>
```

**Autorização:** `@RequiresRole({Perfil.MECANICO, Perfil.ADMIN})`

**Resposta:** `200 OK` + `List<PessoaResponseDTO>`

---

#### 4. Atualizar Pessoa
```http
PUT /api/v1/pessoas/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "numeroDocumento": "12345678901",
  "tipoPessoa": "FISICA",
  "name": "João Silva Atualizado",
  "email": "joao.novo@email.com",
  "phone": "11999999999",
  "cargo": "Diretor",
  "perfil": "ADMIN",
  "setor": "Diretoria",
  "salario": 10000.0
}
```

**Autorização:** `@RequiresRole({Perfil.ADMIN})`

**Resposta:** `200 OK` + `PessoaResponseDTO`

---

#### 5. Deletar Pessoa
```http
DELETE /api/v1/pessoas/{id}
Authorization: Bearer <token>
```

**Autorização:** `@RequiresRole({Perfil.ADMIN})`

**Resposta:** `204 No Content`

---

## Banco de Dados

### Schema SQL

#### Tabela pessoas
```sql
CREATE TABLE IF NOT EXISTS pessoas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_documento VARCHAR(14) UNIQUE NOT NULL,
    tipo_pessoa VARCHAR(10) NOT NULL CHECK (tipo_pessoa IN ('FISICA', 'JURIDICA')),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    cargo VARCHAR(100),
    perfil VARCHAR(20) NOT NULL CHECK (perfil IN ('CLIENTE', 'MECANICO', 'ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Tabela clientes
```sql
CREATE TABLE IF NOT EXISTS clientes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pessoa_id UUID UNIQUE NOT NULL REFERENCES pessoas(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Nota:** Campos nome, cpf_cnpj, telefone e email foram removidos pois agora estão na tabela `pessoas`.

#### Tabela funcionarios
```sql
CREATE TABLE IF NOT EXISTS funcionarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pessoa_id UUID UNIQUE NOT NULL REFERENCES pessoas(id) ON DELETE CASCADE,
    data_admissao DATE,
    setor VARCHAR(50),
    salario DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Regras de Negócio

### Relacionamentos
1. **Se Pessoa.perfil = CLIENTE:**
   - Deve ter registro na tabela `clientes`
   - FK: `clientes.pessoa_id` → `pessoas.id`

2. **Se Pessoa.perfil = MECANICO ou ADMIN:**
   - Deve ter registro na tabela `funcionarios`
   - FK: `funcionarios.pessoa_id` → `pessoas.id`

### Validações
- Documento único no sistema
- Email único no sistema
- Documento deve ter tamanho correto:
  - CPF (FISICA): 11 dígitos
  - CNPJ (JURIDICA): 14 dígitos

### Cascade Delete
- Deletar Pessoa → deleta automaticamente Cliente ou Funcionário relacionado

---

## Autorização

### Perfis de Acesso

| Endpoint | ADMIN | MECANICO | CLIENTE |
|----------|-------|----------|---------|
| POST /pessoas | ✅ | ❌ | ❌ |
| GET /pessoas/{id} | ✅ | ✅ | ❌ |
| GET /pessoas | ✅ | ✅ | ❌ |
| PUT /pessoas/{id} | ✅ | ❌ | ❌ |
| DELETE /pessoas/{id} | ✅ | ❌ | ❌ |

---

## Fluxo de Criação

### Cliente (Perfil CLIENTE)
```mermaid
graph LR
    A[POST /pessoas] --> B[Validar dados]
    B --> C[Criar Pessoa]
    C --> D[Salvar Pessoa]
    D --> E[Não cria Funcionário]
    E --> F[Retornar DTO]
```

### Funcionário (Perfil MECANICO/ADMIN)
```mermaid
graph LR
    A[POST /pessoas] --> B[Validar dados]
    B --> C[Criar Pessoa]
    C --> D[Salvar Pessoa]
    D --> E[Criar Funcionário]
    E --> F[Salvar Funcionário]
    F --> G[Retornar DTO completo]
```

---

## Compilação

```bash
cd oficina-service-k8s
mvn clean compile
```

**Resultado:** ✅ BUILD SUCCESS - 123 arquivos compilados

---

## Próximos Passos

1. ✅ Entidades criadas (Pessoa, Funcionario, Cliente atualizado)
2. ✅ Repositórios criados
3. ✅ DTOs criados
4. ✅ Use Cases implementados
5. ✅ Controller REST implementado
6. ✅ Schema SQL atualizado
7. ✅ Compilação bem-sucedida

### Recomendações:
- [ ] Criar testes unitários para os Use Cases
- [ ] Criar testes de integração para o Controller
- [ ] Adicionar validação customizada para CPF/CNPJ
- [ ] Implementar paginação no endpoint de listagem
- [ ] Adicionar filtros de busca (por perfil, tipo pessoa, etc.)
- [ ] Implementar soft delete (ao invés de exclusão física)

---

## Exemplos de Uso

### 1. Criar um Cliente
```json
POST /api/v1/pessoas
{
  "numeroDocumento": "12345678901",
  "tipoPessoa": "FISICA",
  "name": "Maria Santos",
  "email": "maria@email.com",
  "phone": "11988887777",
  "perfil": "CLIENTE"
}
```

### 2. Criar um Mecânico
```json
POST /api/v1/pessoas
{
  "numeroDocumento": "98765432109",
  "tipoPessoa": "FISICA",
  "name": "Carlos Souza",
  "email": "carlos@email.com",
  "phone": "11977776666",
  "cargo": "Mecânico Sênior",
  "perfil": "MECANICO",
  "setor": "Oficina",
  "salario": 3500.0
}
```

### 3. Atualizar Perfil (Cliente → Admin)
```json
PUT /api/v1/pessoas/{id}
{
  "numeroDocumento": "12345678901",
  "tipoPessoa": "FISICA",
  "name": "Maria Santos",
  "email": "maria@email.com",
  "phone": "11988887777",
  "cargo": "Gerente Administrativo",
  "perfil": "ADMIN",
  "setor": "Administração",
  "salario": 7000.0
}
```
**Resultado:** Cliente será convertido em Funcionário automaticamente.

---

**Fim da Documentação**
