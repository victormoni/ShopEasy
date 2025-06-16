#!/bin/bash

K8S_DIR="./k8s"
NAMESPACE_FILE="$K8S_DIR/namespace.yaml"
DEPLOY_FILE="$K8S_DIR/deploy.yaml"
MEMORY="6000"
CPUS="4"
USER_DRIVER=$1
VM_DRIVER=""

if [[ -n "$USER_DRIVER" ]]; then
  if [[ "$USER_DRIVER" == "hyperv" || "$USER_DRIVER" == "docker" ]]; then
    VM_DRIVER=$USER_DRIVER
  else
    echo "❌ Driver inválido: '$USER_DRIVER'. Use 'hyperv' ou 'docker'."
    exit 1
  fi
fi  

echo "🚀  Iniciando Minikube com driver $VM_DRIVER..."
minikube start --driver=$VM_DRIVER --memory=${MEMORY} --cpus=${CPUS}
echo "✅  Minikube iniciado com driver: $VM_DRIVER"

echo "🧩  Habilitando addons: ingress, dashboard e metrics-server..."
minikube addons enable ingress
minikube addons enable dashboard
minikube addons enable metrics-server

echo "📁  Aplicando o namespace..."
if [ -f "$NAMESPACE_FILE" ]; then
  kubectl apply -f "$NAMESPACE_FILE"
else
  echo "❌  Arquivo $NAMESPACE_FILE não encontrado!"
  exit 1
fi

echo "🧹  Limpando Jobs imutáveis do Ingress NGINX (se existirem)..."
kubectl delete job ingress-nginx-admission-create -n ingress-nginx --ignore-not-found
kubectl delete job ingress-nginx-admission-patch -n ingress-nginx --ignore-not-found

echo "📦  Aplicando o deploy principal (Ingress NGINX)..."
if [ -f "$DEPLOY_FILE" ]; then
  kubectl apply -f "$DEPLOY_FILE"
else
  echo "❌  Arquivo $DEPLOY_FILE não encontrado!"
  exit 1
fi

echo "⏳  Aguardando o Ingress Controller ficar pronto..."
kubectl wait --namespace ingress-nginx \
  --for=condition=Ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

echo "📂  Aplicando todos os outros YAMLs do diretório $K8S_DIR..."

for file in "$K8S_DIR"/*.yaml; do
  if [[ "$file" == *"$NAMESPACE_FILE" ]] || [[ "$file" == *"$DEPLOY_FILE" ]]; then
    continue
  fi
  echo "🔹  Aplicando: $file"
  kubectl apply -f "$file"
done

echo ""
echo "📊  Abrindo o Dashboard..."
minikube dashboard &
echo "🎉  Cluster pronto! Loja virtual com Ingress, Dashboard e Métricas rodando 🚀"

echo ""
echo "📡  Rode este comando em outro terminal como administrador para a loja funcionar:"
echo "👉  minikube tunnel"
echo "⚠️  Deixe o tunnel aberto enquanto estiver usando."

echo ""
echo "🌐  Descubra o EXTERNAL-IP do Ingress NGINX com o comando:" 
echo "👉  kubectl get svc -n ingress-nginx"
echo ""
echo "NAME                                 TYPE           CLUSTER-IP      EXTERNAL-IP     PORT(S)                      AGE"
echo "ingress-nginx-controller             LoadBalancer   10.109.168.86   192.168.49.2    80:31945/TCP,443:31383/TCP   5m"
echo ""
echo "🚀  Depois acesse no navegador: http://<EXTERNAL-IP>/"