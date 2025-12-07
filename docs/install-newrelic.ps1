# Script de instalação do New Relic Infrastructure no Kubernetes (PowerShell)
# Execute: .\install-newrelic.ps1 -LicenseKey "YOUR_LICENSE_KEY"

param(
    [Parameter(Mandatory=$true)]
    [string]$LicenseKey
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Instalando New Relic Infrastructure" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Criar namespace newrelic
Write-Host "1. Criando namespace newrelic..." -ForegroundColor Yellow
kubectl create namespace newrelic --dry-run=client -o yaml | kubectl apply -f -

# Criar secret com license key
Write-Host "2. Configurando license key..." -ForegroundColor Yellow
kubectl create secret generic newrelic-license-key `
  --from-literal=license-key=$LicenseKey `
  -n newrelic `
  --dry-run=client -o yaml | kubectl apply -f -

# Deploy New Relic Infrastructure DaemonSet
Write-Host "3. Deploying New Relic Infrastructure DaemonSet..." -ForegroundColor Yellow
kubectl apply -f ..\infra-kubernetes-terraform\modules\newrelic-infrastructure.yaml

# Deploy Kube State Metrics
Write-Host "4. Deploying Kube State Metrics..." -ForegroundColor Yellow
kubectl apply -f ..\infra-kubernetes-terraform\modules\kube-state-metrics.yaml

# Aguardar pods ficarem prontos
Write-Host "5. Aguardando pods ficarem prontos..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=newrelic-infrastructure -n newrelic --timeout=120s
kubectl wait --for=condition=ready pod -l app=kube-state-metrics -n newrelic --timeout=120s

# Criar secret no namespace oficina
Write-Host "6. Configurando secret no namespace oficina..." -ForegroundColor Yellow
kubectl create namespace oficina --dry-run=client -o yaml | kubectl apply -f -
kubectl create secret generic newrelic-secret `
  --from-literal=license-key=$LicenseKey `
  -n oficina `
  --dry-run=client -o yaml | kubectl apply -f -

# Verificar status
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Status da Instalação" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pods no namespace newrelic:" -ForegroundColor Green
kubectl get pods -n newrelic
Write-Host ""
Write-Host "DaemonSets:" -ForegroundColor Green
kubectl get daemonset -n newrelic
Write-Host ""
Write-Host "Services:" -ForegroundColor Green
kubectl get svc -n newrelic
Write-Host ""
Write-Host "Secrets no namespace oficina:" -ForegroundColor Green
kubectl get secrets -n oficina | Select-String "newrelic"
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Instalação Concluída!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Próximos passos:" -ForegroundColor Yellow
Write-Host "1. Verifique os logs: kubectl logs -n newrelic -l app=newrelic-infrastructure"
Write-Host "2. Acesse o New Relic One para verificar dados"
Write-Host "3. Deploy da aplicação: cd ..\oficina-service-k8s; kubectl apply -f k8s\base\"
Write-Host ""
