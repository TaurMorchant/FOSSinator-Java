#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------
# Config: repositories (GitHub URLs)
# ---------------------------------
REPOS=(
  "https://github.com/Netcracker/qubership-core-blue-green-state-monitor"
  "https://github.com/Netcracker/qubership-core-blue-green-state-monitor-quarkus"
  "https://github.com/Netcracker/qubership-core-context-propagation"
  "https://github.com/Netcracker/qubership-core-context-propagation-quarkus"
  "https://github.com/Netcracker/qubership-core-error-handling"
  "https://github.com/Netcracker/qubership-core-junit-k8s-extension"
  "https://github.com/Netcracker/qubership-core-microservice-dependencies"
  "https://github.com/Netcracker/qubership-core-microservice-framework"
  "https://github.com/Netcracker/qubership-core-microservice-framework-extensions"
  "https://github.com/Netcracker/qubership-core-mongo-evolution"
  "https://github.com/Netcracker/qubership-core-process-orchestrator"
  "https://github.com/Netcracker/qubership-core-quarkus-extensions"
  "https://github.com/Netcracker/qubership-core-rest-libraries"
  "https://github.com/Netcracker/qubership-core-restclient"
  "https://github.com/Netcracker/qubership-core-springboot-starter"
  "https://github.com/Netcracker/qubership-core-utils"
  "https://github.com/Netcracker/qubership-dbaas-client"
  "https://github.com/Netcracker/qubership-maas-client"
  "https://github.com/Netcracker/qubership-maas-client-quarkus"
  "https://github.com/Netcracker/qubership-maas-client-spring"
  "https://github.com/Netcracker/qubership-maas-declarative-client-commons"
  "https://github.com/Netcracker/qubership-maas-declarative-client-quarkus"
  "https://github.com/Netcracker/qubership-maas-declarative-client-spring"
)

# ---------------------------------
# Requirements check
# ---------------------------------
need_bin() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "[ERROR] '$1' is required but not installed" >&2
    exit 1
  }
}
need_bin bash
need_bin git
need_bin xmlstarlet
need_bin find
need_bin sort
need_bin grep
need_bin sed
need_bin awk
need_bin tr

# ---------------------------------
# Output file (next to the script)
# ---------------------------------
OUTFILE="$(pwd)/output.yaml"
# Write YAML header once
cat > "$OUTFILE" <<'EOL'
java:
  dependencies-to-replace:
EOL

# ---------------------------------
# Working directory
# ---------------------------------
WORKDIR="$(mktemp -d)"
trap 'rm -rf "$WORKDIR"' EXIT
cd "$WORKDIR"

NS='x=http://maven.apache.org/POM/4.0.0'

# ---------------------------------
# Helpers
# ---------------------------------

# Return latest semver tag from remote via git
# Accepts tags like: v1.2.3, 1.2.3, 1.2.3-RC1, 1.2.3+build.7
get_latest_semver_tag() {
  local repo_url="$1"

  git ls-remote --tags --quiet "$repo_url" \
  | awk '{
      ref=$2
      sub(/^refs\/tags\//,"",ref)   # drop "refs/tags/"
      sub(/\^\{\}$/,"",ref)         # drop peeled suffix "^{}"
      print ref
    }' \
  | sort -u \
  | grep -E '^v?[0-9]+\.[0-9]+\.[0-9]+([-.+][0-9A-Za-z.-]+)?$' \
  | awk '{
      tag = $0
      normalized = tag
      sub(/^v/, "", normalized)
      print normalized "\t" tag
    }' \
  | sort -V -k1,1 \
  | tail -n 1 \
  | cut -f2
}

# Extract root groupId/version with fallback to <parent>; print "group|version"
get_root_coord() {
  local pom="$1"
  local group version

  # Try direct groupId first, fallback to parent if empty
  group="$(xmlstarlet sel -N "$NS" -t -v '/x:project/x:groupId' "$pom" 2>/dev/null || true)"
  if [[ -z "${group:-}" ]]; then
    group="$(xmlstarlet sel -N "$NS" -t -v '/x:project/x:parent/x:groupId' "$pom" 2>/dev/null || true)"
  fi

  # Try direct version first, fallback to parent if empty
  version="$(xmlstarlet sel -N "$NS" -t -v '/x:project/x:version' "$pom" 2>/dev/null || true)"
  if [[ -z "${version:-}" ]]; then
    version="$(xmlstarlet sel -N "$NS" -t -v '/x:project/x:parent/x:version' "$pom" 2>/dev/null || true)"
  fi

  echo "${group}|${version}"
}

# ---------------------------------
# Main
# ---------------------------------
for repo_url in "${REPOS[@]}"; do
  repo_name="${repo_url##*/}"       # last path component

  echo "[INFO] Processing: $repo_url"
  echo "[INFO] Resolving latest semver tag via git ls-remote..."

  last_tag="$(get_latest_semver_tag "$repo_url" || true)"
  if [[ -z "${last_tag:-}" ]]; then
    echo "[ERROR] No semver tags found on remote: $repo_url" >&2
    exit 1
  fi

  echo "[INFO] Cloning at tag $last_tag ..."
  git -c advice.detachedHead=false clone --quiet --branch "$last_tag" --depth=1 "$repo_url" "$repo_name"
  cd "$repo_name"

  if [[ ! -f "pom.xml" ]]; then
    echo "[ERROR] Root pom.xml not found in $repo_url@$last_tag" >&2
    exit 1
  fi

  gv_line="$(get_root_coord "pom.xml")"
  root_groupId="${gv_line%%|*}"
  root_version="${gv_line##*|}"

  if [[ -z "${root_groupId:-}" || -z "${root_version:-}" ]]; then
    echo "[ERROR] Could not resolve root groupId/version in $repo_name@$last_tag" >&2
    exit 1
  fi

  root_artifactId="$(xmlstarlet sel -N "$NS" -t -v '/x:project/x:artifactId' pom.xml)"
  if [[ -z "${root_artifactId:-}" ]]; then
    echo "[ERROR] Could not resolve root artifactId in $repo_name@$last_tag" >&2
    exit 1
  fi

  {
    printf "    # %s@%s\n" "$repo_name" "$last_tag"
    cat <<EOL
    - old-group-id: $root_groupId
      old-artifact-id: $root_artifactId
      new-group-id: $root_groupId
      new-artifact-id: $root_artifactId
      new-version: $root_version
EOL
  } >> "$OUTFILE"

  # Iterate child pom.xml safely (handles spaces due to -print0 + read -d '')
  while IFS= read -r -d '' pom; do
    child_artifactId="$(xmlstarlet sel -N "$NS" -t -v '/x:project/x:artifactId' "$pom")"
    if [[ -z "${child_artifactId:-}" ]]; then
      echo "[WARN] Skipping module without artifactId: $pom"
      continue
    fi
    cat <<EOL >> "$OUTFILE"
    - old-group-id: $root_groupId
      old-artifact-id: $child_artifactId
      new-group-id: $root_groupId
      new-artifact-id: $child_artifactId
      new-version: $root_version
EOL
  done < <(find . -name pom.xml ! -path "./pom.xml" -print0)

  echo "[INFO] Done: $repo_name@$last_tag"
  echo ""
  cd ..
done

echo "[INFO] All done. Results saved to $OUTFILE"
