#!/bin/bash

# CONFIGURAÇÕES
VM_DRIVER="hyperv"
K8S_DIR="./k8s"  # O script está na mesma pasta dos YAMLs
NAMESPACE_FILE="$K8S_DIR/namespace.yaml"
DEPLOY_FILE="$K8S_DIR/deploy.yaml"

echo "🚀 Iniciando Minikube com driver $VM_DRIVER..."
minikube start --driver=$VM_DRIVER

echo "✅ Minikube iniciado."

echo "🧩 Habilitando addons: ingress, dashboard e metrics-server..."
minikube addons enable ingress
minikube addons enable dashboard
minikube addons enable metrics-server

echo "📁 Aplicando o namespace..."
if [ -f "$NAMESPACE_FILE" ]; then
  kubectl apply -f "$NAMESPACE_FILE"
else
  echo "❌ Arquivo $NAMESPACE_FILE não encontrado!"
  exit 1
fi

echo "🧹 Limpando Jobs imutáveis do Ingress NGINX (caso existam)..."
kubectl delete job ingress-nginx-admission-create -n ingress-nginx --ignore-not-found
kubectl delete job ingress-nginx-admission-patch -n ingress-nginx --ignore-not-found

echo "📦 Aplicando o deploy principal (Ingress NGINX)..."
if [ -f "$DEPLOY_FILE" ]; then
  kubectl apply -f "$DEPLOY_FILE"
else
  echo "❌ Arquivo $DEPLOY_FILE não encontrado!"
  exit 1
fi

echo "⏳ Aguardando pods ingress-nginx ficarem prontos..."
kubectl wait --namespace ingress-nginx \
  --for=condition=Ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

echo "📂 Aplicando todos os outros YAMLs do diretório $K8S_DIR..."

for file in "$K8S_DIR"/*.yaml; do
  if [[ "$file" == *"$NAMESPACE_FILE" ]] || [[ "$file" == *"$DEPLOY_FILE" ]]; then
    continue
  fi
  echo "🔹 Aplicando: $file"
  kubectl apply -f "$file"
done

echo ""
echo "📡 Lembre-se de rodar este comando em um novo terminal com permissão de administrador:"
echo ""
echo "  👉 minikube tunnel"
echo ""
echo "⚠️ Deixe esse terminal aberto enquanto usar LoadBalancer."

echo ""
echo "🌐 Para acessar sua aplicação no navegador:"
echo ""
echo "🧾 Rode este comando para descobrir o IP externo:"
echo "👉 kubectl get svc -n loja-virtual"
echo ""
echo "🖱️ Copie o valor da coluna EXTERNAL-IP do serviço frontend e acesse no navegador:"
echo ""
echo "   👉 http://<EXTERNAL-IP>"
echo ""
echo "📦 Exemplo esperado:"
echo ""
echo "   NAME       TYPE           CLUSTER-IP     EXTERNAL-IP     PORT(S)         AGE"
echo "   frontend   LoadBalancer   10.105.19.79   10.105.19.79    80:30000/TCP    2m"
echo ""
echo "🌟 Quando abrir no navegador, você verá a sua loja virtual funcionando!"

echo ""
echo "📈 Abrindo o dashboard em segundo plano..."
minikube dashboard &

echo ""
echo "🎉 Tudo pronto! Sua loja virtual está rodando com Ingress, Dashboard e Métricas!"
