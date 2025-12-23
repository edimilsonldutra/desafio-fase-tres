@echo off
chcp 65001 > nul
echo ============================================
echo TESTE DE AUTENTICAÇÃO COM BANCO DE DADOS
echo ============================================
echo.

cd /d "%~dp0"

echo [1/3] Compilando o projeto...
call mvnw.cmd clean compile -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ❌ Erro na compilação!
    pause
    exit /b 1
)
echo ✓ Compilação bem-sucedida!
echo.

echo [2/3] Executando testes de UserDetailsImpl...
call mvnw.cmd test -Dtest=UserDetailsImplTest
if %ERRORLEVEL% neq 0 (
    echo ❌ Testes de UserDetailsImpl falharam!
    pause
    exit /b 1
)
echo ✓ Testes de UserDetailsImpl passaram!
echo.

echo [3/3] Executando testes de UserDetailsServiceImpl...
call mvnw.cmd test -Dtest=UserDetailsServiceImplTest
if %ERRORLEVEL% neq 0 (
    echo ❌ Testes de UserDetailsServiceImpl falharam!
    pause
    exit /b 1
)
echo ✓ Testes de UserDetailsServiceImpl passaram!
echo.

echo ============================================
echo ✅ TODOS OS TESTES PASSARAM COM SUCESSO!
echo ============================================
echo.
echo Resumo da implementação:
echo - Entidade Pessoa com campos senha e ativo
echo - UserDetailsImpl adapta Pessoa para Spring Security
echo - UserDetailsServiceImpl busca usuários do banco
echo - Perfis convertidos em ROLE_ADMIN, ROLE_MECANICO, ROLE_CLIENTE
echo.

pause
