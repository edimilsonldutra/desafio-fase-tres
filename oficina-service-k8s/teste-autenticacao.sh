#!/bin/bash

echo "============================================"
echo "TESTE DE AUTENTICAÇÃO COM BANCO DE DADOS"
echo "============================================"
echo ""

cd "$(dirname "$0")"

echo "[1/3] Compilando o projeto..."
./mvnw clean compile -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Erro na compilação!"
    exit 1
fi
echo "✓ Compilação bem-sucedida!"
echo ""

echo "[2/3] Executando testes de UserDetailsImpl..."
./mvnw test -Dtest=UserDetailsImplTest
if [ $? -ne 0 ]; then
    echo "❌ Testes de UserDetailsImpl falharam!"
    exit 1
fi
echo "✓ Testes de UserDetailsImpl passaram!"
echo ""

echo "[3/3] Executando testes de UserDetailsServiceImpl..."
./mvnw test -Dtest=UserDetailsServiceImplTest
if [ $? -ne 0 ]; then
    echo "❌ Testes de UserDetailsServiceImpl falharam!"
    exit 1
fi
echo "✓ Testes de UserDetailsServiceImpl passaram!"
echo ""

echo "============================================"
echo "✅ TODOS OS TESTES PASSARAM COM SUCESSO!"
echo "============================================"
echo ""
echo "Resumo da implementação:"
echo "- Entidade Pessoa com campos senha e ativo"
echo "- UserDetailsImpl adapta Pessoa para Spring Security"
echo "- UserDetailsServiceImpl busca usuários do banco"
echo "- Perfis convertidos em ROLE_ADMIN, ROLE_MECANICO, ROLE_CLIENTE"
echo ""
