---
name: Pull Request
about: Template para abertura de Pull Requests
title: "[FEAT/FIX/DOCS] Breve descrição"
labels: ''
assignees: ''
---

## 📋 Descrição

<!-- Descreva de forma clara e concisa as mudanças propostas neste PR -->

## 🎯 Tipo de Mudança

<!-- Marque com 'x' o tipo de mudança -->

- [ ] 🐛 Bug fix (correção de problema)
- [ ] ✨ Nova feature (nova funcionalidade)
- [ ] 💥 Breaking change (mudança que quebra compatibilidade)
- [ ] 📝 Documentação
- [ ] ♻️ Refatoração
- [ ] ⚡ Melhoria de performance
- [ ] ✅ Adição ou atualização de testes
- [ ] 🔧 Configuração ou infraestrutura

## 🔗 Issue Relacionada

<!-- Link para a issue relacionada (se aplicável) -->

Fixes #(issue_number)

## ✅ Checklist

### Código

- [ ] Meu código segue o style guide do projeto
- [ ] Realizei self-review do código
- [ ] Comentei código complexo quando necessário
- [ ] Não introduzi novos warnings
- [ ] Segui os princípios SOLID e clean code

### Testes

- [ ] Adicionei testes unitários para novas funcionalidades
- [ ] Adicionei testes de integração quando aplicável
- [ ] Todos os testes passam localmente (`mvn test`)
- [ ] Cobertura de código mantida/aumentada (mínimo 80%)
- [ ] Testei cenários de erro/exceção

### Deploy e Infraestrutura

- [ ] Atualizei `template.yaml` se necessário
- [ ] Atualizei `samconfig.toml` para todos os ambientes
- [ ] Testei localmente com `sam local start-api`
- [ ] Build SAM executado com sucesso (`sam build`)
- [ ] Não há hardcoded values (credenciais, URLs, etc)

### Documentação

- [ ] Atualizei README.md (se necessário)
- [ ] Atualizei SWAGGER.md (se mudanças na API)
- [ ] Atualizei comentários no código
- [ ] Adicionei/atualizei exemplos de uso
- [ ] Documentei breaking changes no changelog

### Segurança

- [ ] Não expus credenciais ou secrets
- [ ] Validei inputs/outputs corretamente
- [ ] Tratei erros de forma segura (sem expor detalhes internos)
- [ ] Não introduzi vulnerabilidades conhecidas
- [ ] Secrets são obtidos via Secrets Manager

## 🧪 Como Testar

<!-- Descreva os passos para testar as mudanças -->

```bash
# 1. Build local
sam build

# 2. Executar testes
mvn test

# 3. Rodar localmente
sam local start-api

# 4. Testar endpoint
curl -X POST http://localhost:3000/auth \
  -H "Content-Type: application/json" \
  -d '{"cpf": "12345678901"}'
```

## 📸 Screenshots (se aplicável)

<!-- Adicione screenshots ou logs relevantes -->

## 🚀 Deploy

<!-- Marque os ambientes onde o deploy deve ser feito -->

- [ ] DEV (automático após merge em `develop`)
- [ ] STAGING (automático após merge em `staging`)
- [ ] PROD (requer aprovação manual após merge em `main`)

## ⚠️ Notas Adicionais

<!-- Informações extras para os reviewers -->

## 📝 Aprovações Necessárias

<!-- Conforme branch protection rules -->

- **develop**: 1 aprovação
- **staging**: 1 aprovação
- **main**: 2 aprovações + testes passando

---

## Para os Reviewers

### Pontos de Atenção

- [ ] Código está legível e bem estruturado
- [ ] Testes cobrem casos de uso principais
- [ ] Não há impacto negativo em performance
- [ ] Documentação está adequada
- [ ] Não há riscos de segurança
- [ ] Breaking changes estão documentados

### Sugestões de Review

```bash
# 1. Fazer checkout do branch
git fetch origin
git checkout <branch-name>

# 2. Executar testes
mvn clean test

# 3. Validar SAM template
sam validate --lint

# 4. Testar localmente
sam build
sam local start-api

# 5. Verificar cobertura
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

---

**Obrigado pela contribuição! 🎉**
