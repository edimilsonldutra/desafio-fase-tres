# Script de instalação do New Relic Infrastructure no Kubernetes
# Execute: ./install-newrelic.sh <NEW_RELIC_LICENSE_KEY>

if [ -z "$1" ]; then
    echo "Uso: ./install-newrelic.sh <NEW_RELIC_LICENSE_KEY>"
    exit 1
fi

LICENSE_KEY=$1

echo "=========================================="
echo "Instalando New Relic Infrastructure"
echo "=========================================="

# Criar namespace newrelic
echo "1. Criando namespace newrelic..."
kubectl create namespace newrelic --dry-run=client -o yaml | kubectl apply -f -

# Criar secret com license key
echo "2. Configurando license key..."
kubectl create secret generic newrelic-license-key \
  --from-literal=license-key=$LICENSE_KEY \
  -n newrelic \
  --dry-run=client -o yaml | kubectl apply -f -

# Deploy New Relic Infrastructure DaemonSet
echo "3. Deploying New Relic Infrastructure DaemonSet..."
kubectl apply -f ../infra-kubernetes-terraform/modules/newrelic-infrastructure.yaml

# Deploy Kube State Metrics
echo "4. Deploying Kube State Metrics..."
kubectl apply -f ../infra-kubernetes-terraform/modules/kube-state-metrics.yaml

# Aguardar pods ficarem prontos
echo "5. Aguardando pods ficarem prontos..."
kubectl wait --for=condition=ready pod -l app=newrelic-infrastructure -n newrelic --timeout=120s
kubectl wait --for=condition=ready pod -l app=kube-state-metrics -n newrelic --timeout=120s

# Criar secret no namespace oficina
echo "6. Configurando secret no namespace oficina..."
kubectl create namespace oficina --dry-run=client -o yaml | kubectl apply -f -
kubectl create secret generic newrelic-secret \
  --from-literal=license-key=$LICENSE_KEY \
  -n oficina \
  --dry-run=client -o yaml | kubectl apply -f -

# Verificar status
echo ""
echo "=========================================="
echo "Status da Instalação"
echo "=========================================="
echo ""
echo "Pods no namespace newrelic:"
kubectl get pods -n newrelic
echo ""
echo "DaemonSets:"
kubectl get daemonset -n newrelic
echo ""
echo "Services:"
kubectl get svc -n newrelic
echo ""
echo "Secrets no namespace oficina:"
kubectl get secrets -n oficina | grep newrelic
echo ""
echo "=========================================="
echo "Instalação Concluída!"
echo "=========================================="
echo ""
echo "Próximos passos:"
echo "1. Verifique os logs: kubectl logs -n newrelic -l app=newrelic-infrastructure"
echo "2. Acesse o New Relic One para verificar dados"
echo "3. Deploy da aplicação: cd ../oficina-service-k8s && kubectl apply -f k8s/base/"
echo ""
