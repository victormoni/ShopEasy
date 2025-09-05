#!/usr/bin/env bash
# One-file installer for ShopEasy K8s on Minikube
# Works on Linux/macOS and Windows (Git Bash/WSL). Requires: kubectl, minikube.
set -euo pipefail

# ======================
# Config (can be overridden via env vars)
# ======================
DRIVER="${DRIVER:-docker}"          # docker | hyperv | virtualbox | qemu | none
MEMORY="${MEMORY:-5000}"            # MB
CPUS="${CPUS:-4}"
NAMESPACE="${NAMESPACE:-shopeasy}"
K8S_DIR="${K8S_DIR:-./k8s}"
# Admission behavior
ADMISSION_BYPASS_FALLBACK="${ADMISSION_BYPASS_FALLBACK:-true}"
ADMISSION_WAIT_SECONDS="${ADMISSION_WAIT_SECONDS:-600}"
# API server wait
API_WAIT_SECONDS="${API_WAIT_SECONDS:-120}"
SKIP_API_WAIT="${SKIP_API_WAIT:-false}"
# ===== Output controls =====
VERBOSE="${VERBOSE:-false}"         # true to see full kubectl/minikube output
SHOW_FILES="${SHOW_FILES:-false}"   # true to echo each applied file name

usage() {
  cat <<EOF
Usage:
  ./setup.sh [driver]

Examples:
  ./setup.sh docker
  ./setup.sh hyperv

Env vars:
  DRIVER=docker MEMORY=5000 CPUS=4 NAMESPACE=shopeasy K8S_DIR=./k8s ./minikube.sh
  API_WAIT_SECONDS=120  SKIP_API_WAIT=true
  ADMISSION_BYPASS_FALLBACK=true|false   ADMISSION_WAIT_SECONDS=600
  VERBOSE=true|false (default: false)
  SHOW_FILES=true|false (default: false)
EOF
}

# Positional driver like: ./setup.sh hyperv
if [[ $# -ge 1 ]]; then
  case "${1}" in
    -h|--help) usage; exit 0 ;;
    docker|hyperv|virtualbox|qemu|none) DRIVER="${1}"; shift ;;
    *) echo "⚠️  Invalid driver: '${1}'" >&2; usage; exit 1 ;;
  esac
fi

# ======================
# Helpers
# ======================
info(){ echo -e "👉 $*"; }
ok(){ echo -e "✅ $*"; }
warn(){ echo -e "⚠️  $*"; }
err(){ echo -e "❌ $*" >&2; }

need_bin(){ command -v "$1" >/dev/null 2>&1; }
has_files(){ shopt -s nullglob; local a=("$1"); shopt -u nullglob; [[ ${#a[@]} -gt 0 ]]; }

jsonpath() { # jsonpath <ns> <kind> <name> <expr>
  local ns="$1"; local kind="$2"; local name="$3"; local expr="$4"
  kubectl -n "$ns" get "$kind" "$name" -o jsonpath="$expr" 2>/dev/null || true
}

# kubectl wrapper with retries on transient network/TLS errors
kubectl_safe() {
  local attempt=1 max=8 delay=2
  while :; do
    if kubectl --request-timeout=30s "$@"; then
      return 0
    fi
    local rc=$?
    if (( attempt >= max )); then
      err "kubectl failed after ${max} attempts: kubectl $*"
      return $rc
    fi
    warn "kubectl transient failure; retrying in ${delay}s (attempt ${attempt}/${max})..."
    sleep "$delay"
    delay=$(( delay * 2 ))
    (( attempt++ ))
  done
}

# Run command quietly unless VERBOSE=true; if it fails, print full output once.
run_quiet_or_echo_on_error() {
  if [[ "${VERBOSE}" == "true" ]]; then
    "$@"
    return
  fi
  local out rc
  set +e
  out="$("$@" 2>&1)"; rc=$?
  set -e
  if (( rc != 0 )); then
    echo "${out}" >&2
    return $rc
  fi
}

admission_has_endpoints() {
  [[ -n "$(jsonpath ingress-nginx endpoints ingress-nginx-controller-admission '{.subsets[0].addresses[0].ip}')" ]]
}


# Wait for all PVCs in a namespace to become Bound
wait_for_pvcs_bound() {
  local ns="$1"; local timeout="${2:-180}"
  info "Waiting up to ${timeout}s for PVCs in namespace '${ns}' to be Bound..."
  local end=$(( $(date +%s) + timeout ))
  while [[ $(date +%s) -lt $end ]]; do
    local pending
    pending="$(kubectl -n "$ns" get pvc --no-headers 2>/dev/null | awk '$2 != "Bound" {print $1}')"
    if [[ -z "$pending" ]]; then
      ok "All PVCs are Bound."
      return 0
    fi
    printf "\r⏳ waiting PVCs: %s " "$pending"
    sleep 3
  done
  echo
  warn "Some PVCs not Bound within ${timeout}s. Continuing anyway."
}

# Wait for pods by label to be Ready (with hourglass + countdown)
wait_for_pods_ready() {
  local ns="$1"; local selector="$2"; local timeout="${3:-300}"
  info "Waiting for pods ($selector) to become ready in '${ns}' (timeout=${timeout}s)..."

  local end=$(( $(date +%s) + timeout ))
  while [[ $(date +%s) -lt $end ]]; do
    # Try a short wait to quickly detect readiness without blocking long
    if kubectl -n "$ns" wait --for=condition=Ready pod -l "$selector" --timeout=3s >/dev/null 2>&1; then
      printf "\r\033[K"  # clear line
      ok "Pods Ready."
      return 0
    fi
    local remaining=$(( end - $(date +%s) ))
    printf "\r⏳ waiting for pods... %03ds remaining " "$remaining"
    sleep 1
  done
  echo
  warn "Pods not Ready within ${timeout}s. Check: kubectl -n ${ns} describe pod -l ${selector}"
  return 1
}

# ======================
# Preflight
# ======================
if ! need_bin kubectl; then err "kubectl not found. Please install it first."; exit 1; fi
if ! need_bin minikube; then err "minikube not found. Please install it first."; exit 1; fi
if [[ ! -d "$K8S_DIR" ]]; then err "Directory $K8S_DIR not found."; exit 1; fi
if ! has_files "$K8S_DIR"/*.yaml; then err "No .yaml files found in $K8S_DIR"; exit 1; fi

# ======================
# Start Minikube
# ======================

minikube start --driver="${DRIVER}" --memory="${MEMORY}" --cpus="${CPUS}"
ok "Minikube started."
kubectl config use-context minikube >/dev/null 2>&1 || true

# ======================
# Addons
# ======================
info "Enabling addons (ingress, dashboard, metrics, storage)..."
minikube addons enable ingress
minikube addons enable dashboard
minikube addons enable metrics-server
minikube addons enable storage-provisioner || true
minikube addons enable default-storageclass || true
ok "Addons enabled."

# ======================
# Namespace
# ======================
if grep -qE 'kind:\s*Namespace\b' "$K8S_DIR"/namespace.yaml 2>/dev/null; then
  info "Applying namespace from $K8S_DIR/namespace.yaml..."
  run_quiet_or_echo_on_error kubectl apply -f "$K8S_DIR/namespace.yaml"
else
  info "Ensuring namespace '${NAMESPACE}' exists (no namespace.yaml found)..."
  run_quiet_or_echo_on_error kubectl get ns "${NAMESPACE}" >/dev/null 2>&1 || run_quiet_or_echo_on_error kubectl create ns "${NAMESPACE}"
fi
ok "Namespace ready."

# ======================
# Wait for Ingress Controller
# ======================
info "Waiting for Ingress Controller to be ready..."
run_quiet_or_echo_on_error kubectl -n ingress-nginx \
  wait --for=condition=Ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=300s
ok "Ingress Controller is ready."

# ======================
# Wait for Admission Webhook
# ======================
info "Waiting up to ${ADMISSION_WAIT_SECONDS}s for admission webhook endpoints..."
deadline=$(( $(date +%s) + ADMISSION_WAIT_SECONDS ))
admission_ready="false"
while [[ $(date +%s) -lt $deadline ]]; do
  if admission_has_endpoints; then
    ok "Admission webhook is ready."
    admission_ready="true"
    break
  fi
  sleep 3
done

if [[ "${admission_ready}" != "true" ]]; then
  warn "Admission webhook still has no endpoints. Recreating cert jobs..."
  run_quiet_or_echo_on_error kubectl -n ingress-nginx delete job ingress-nginx-admission-create --ignore-not-found || true
  run_quiet_or_echo_on_error kubectl -n ingress-nginx delete job ingress-nginx-admission-patch  --ignore-not-found || true
  sleep 8
  if admission_has_endpoints; then
    ok "Admission webhook became ready after recreating jobs."
    admission_ready="true"
  fi
fi

bypass_used="false"
if [[ "${admission_ready}" != "true" && "${ADMISSION_BYPASS_FALLBACK}" == "true" ]]; then
  warn "Applying temporary bypass (failurePolicy=Ignore) to create Ingress resources..."
  run_quiet_or_echo_on_error kubectl patch ValidatingWebhookConfiguration ingress-nginx-admission \
    --type='json' \
    -p='[{"op":"replace","path":"/webhooks/0/failurePolicy","value":"Ignore"}]' || true
  trap 'kubectl patch ValidatingWebhookConfiguration ingress-nginx-admission --type=json -p '\''[{"op":"replace","path":"/webhooks/0/failurePolicy","value":"Fail"}]'\'' 2>/dev/null || true' EXIT
  bypass_used="true"
fi

# ======================
# Apply resources in safe order
# ======================
apply_by_kind() {
  local kinds_regex="$1"
  local title="$2"
  info "Applying ${title}..."
  local found=false
  shopt -s nullglob
  for file in "$K8S_DIR"/*.yaml; do
    if grep -qE "kind:\s*(${kinds_regex})\b" "$file"; then
      if [[ "${SHOW_FILES}" == "true" ]]; then
        echo "   • $file"
      fi
      if ! run_quiet_or_echo_on_error kubectl apply -f "$file"; then
        err "Failed applying: $file"
        return 1
      fi
      found=true
    fi
  done
  shopt -u nullglob
  if [[ "$found" == false ]]; then
    warn "No files for ${title}."
  else
    ok "${title} applied."
  fi
}

apply_by_kind "ConfigMap|Secret" "ConfigMaps/Secrets"
apply_by_kind "PersistentVolume|PersistentVolumeClaim" "Storage (PV/PVC)"

# Wait PVCs Bound before moving on
wait_for_pvcs_bound "${NAMESPACE}" 180

apply_by_kind "Service" "Services"
apply_by_kind "Deployment|StatefulSet|DaemonSet|Job|CronJob" "Workloads"

# Wait for MySQL pods to be ready (with countdown)
wait_for_pods_ready "${NAMESPACE}" "app=mysql" 180

# ===== Ingress with countdown (wait-until-ready then apply once) =====
INGRESS_WAIT_SECONDS="${INGRESS_WAIT_SECONDS:-180}"
info "Waiting for admission webhook to be ready before applying Ingress (timeout: ${INGRESS_WAIT_SECONDS}s)..."

# Discover Ingress files
shopt -s nullglob
ingress_files=()
for f in "$K8S_DIR"/*.yaml; do
  if grep -qE "kind:\s*Ingress\b" "$f"; then
    ingress_files+=("$f")
  fi
done
shopt -u nullglob

if (( ${#ingress_files[@]} == 0 )); then
  warn "No Ingress files found."
else
  end=$(( $(date +%s) + INGRESS_WAIT_SECONDS ))
  admission_ready_now="false"
  while [[ $(date +%s) -lt $end ]]; do
    if admission_has_endpoints; then
      admission_ready_now="true"
      break
    fi
    printf "\r⏳ waiting admission... %02ds " $(( end - $(date +%s) ))
    sleep 1
  done
  echo

  bypass_used_local="false"
  if [[ "$admission_ready_now" != "true" ]]; then
    if [[ "${ADMISSION_BYPASS_FALLBACK}" == "true" ]]; then
      warn "Admission not ready after ${INGRESS_WAIT_SECONDS}s; applying temporary bypass (failurePolicy=Ignore)."
      run_quiet_or_echo_on_error kubectl patch ValidatingWebhookConfiguration ingress-nginx-admission \
        --type='json' \
        -p='[{"op":"replace","path":"/webhooks/0/failurePolicy","value":"Ignore"}]' || true
      trap 'kubectl patch ValidatingWebhookConfiguration ingress-nginx-admission --type=json -p '\''[{"op":"replace","path":"/webhooks/0/failurePolicy","value":"Fail"}]'\'' 2>/dev/null || true' EXIT
      bypass_used_local="true"
    else
      err "Admission not ready within ${INGRESS_WAIT_SECONDS}s and bypass is disabled (ADMISSION_BYPASS_FALLBACK=false)."
      exit 1
    fi
  fi

  info "Applying Ingress resources..."
  apply_ok="true"
  for f in "${ingress_files[@]}"; do
    if [[ "${SHOW_FILES}" == "true" ]]; then
      echo "   • $f"
    fi
    if ! run_quiet_or_echo_on_error kubectl apply -f "$f"; then
      apply_ok="false"
    fi
  done

  if [[ "$bypass_used_local" == "true" ]]; then
    info "Restoring failurePolicy=Fail on admission webhook..."
    run_quiet_or_echo_on_error kubectl patch ValidatingWebhookConfiguration ingress-nginx-admission \
      --type='json' \
      -p='[{"op":"replace","path":"/webhooks/0/failurePolicy","value":"Fail"}]' || true
  fi

  if [[ "$apply_ok" == "true" ]]; then
    ok "Ingress resources applied."
  else
    err "Failed to apply one or more Ingress resources."
    exit 1
  fi
fi

# Restore admission webhook policy if bypass was used
if [[ "$bypass_used" == "true" ]]; then
  info "Restoring failurePolicy=Fail on admission webhook..."
  run_quiet_or_echo_on_error kubectl patch ValidatingWebhookConfiguration ingress-nginx-admission \
    --type='json' \
    -p='[{"op":"replace","path":"/webhooks/0/failurePolicy","value":"Fail"}]' || true
fi

# ======================
# Dashboard & tips
# ======================
info "Opening Minikube Dashboard (in background)..."
(minikube dashboard &) || warn "Could not open dashboard automatically."

echo ""
ok "Cluster ready 🎉"
echo ""
echo "If your Ingress uses LoadBalancer, run in ANOTHER terminal and keep it open:"
echo "  minikube tunnel"
echo ""
echo "Check the EXTERNAL-IP of the Ingress Controller:"
echo "  kubectl get svc -n ingress-nginx"
echo ""
echo "Tips:"
echo "- You can pass driver: ./setup.sh docker | hyperv | virtualbox | qemu | none"
echo "- Override with env vars: DRIVER=..., MEMORY=5000, CPUS=4, NAMESPACE=shopeasy, K8S_DIR=./k8s"
echo "- Admission behavior: ADMISSION_BYPASS_FALLBACK=true|false (default true), ADMISSION_WAIT_SECONDS=600"
echo "- API wait: API_WAIT_SECONDS=120, or SKIP_API_WAIT=true to skip"
echo "- VERBOSE=false (default) keeps output clean; set VERBOSE=true to debug."
echo "- SHOW_FILES=true prints each applied file."
echo "- Reapply manifests: kubectl -n ${NAMESPACE} apply -f ${K8S_DIR}/"
