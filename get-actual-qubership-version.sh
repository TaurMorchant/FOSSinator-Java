#!/usr/bin/env bash
set -euo pipefail

REPOS=(
  "https://github.com/Netcracker/qubership-maas-client"
	"https://github.com/Netcracker/qubership-maas-client-spring"
	"https://github.com/Netcracker/qubership-maas-declarative-client-commons"
	"https://github.com/Netcracker/qubership-maas-declarative-client-quarkus"
	"https://github.com/Netcracker/qubership-core-blue-green-state-monitor-quarkus"
	"https://github.com/Netcracker/qubership-core-quarkus-extensions"
	"https://github.com/Netcracker/qubership-core-context-propagation-quarkus"
	"https://github.com/Netcracker/qubership-maas-client-quarkus"
	"https://github.com/Netcracker/qubership-core-utils"
	"https://github.com/Netcracker/qubership-core-blue-green-state-monitor"
	"https://github.com/Netcracker/qubership-dbaas-client"
	"https://github.com/Netcracker/qubership-core-microservice-dependencies"
	"https://github.com/Netcracker/qubership-core-rest-libraries"
	"https://github.com/Netcracker/qubership-core-context-propagation"
	"https://github.com/Netcracker/qubership-core-error-handling"
	"https://github.com/Netcracker/qubership-core-microservice-framework-extensions"
	"https://github.com/Netcracker/qubership-core-microservice-framework"
	"https://github.com/Netcracker/qubership-core-restclient"
	"https://github.com/Netcracker/qubership-core-mongo-evolution"
	"https://github.com/Netcracker/qubership-core-process-orchestrator"
)

OUTPUT="output.yaml"
REPO_DIR="repo"

rm -rf "$REPO_DIR"
mkdir -p "$REPO_DIR"
rm -f "$OUTPUT"

cat > "$OUTPUT" <<EOF
java:
  dependencies-to-replace:
EOF

for url in "${REPOS[@]}"; do
  repo_name=$(basename "$url" .git)
  target_dir="$REPO_DIR/$repo_name"
  echo "Cloning $repo_name..."
  git clone --depth 1 "$url" "$target_dir"
  pushd "$target_dir" >/dev/null

  groupId=$(xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" \
    -t -v "/x:project/x:groupId" pom.xml)
  version=$(xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" \
    -t -v "/x:project/x:version" pom.xml)
  artifactId=$(xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" \
    -t -v "/x:project/x:artifactId" pom.xml)

  oldGroupId="${groupId/org.qubership/com.netcracker}"

  {
    printf "    # %s\n" "$repo_name"
    cat <<EOL
    - old-group-id: $oldGroupId
      old-artifact-id: $artifactId
      new-group-id: $groupId
      new-artifact-id: $artifactId
      new-version: $version
EOL

    find . -name pom.xml ! -path "./pom.xml" -print0 |
    while IFS= read -r -d '' pom; do
      child_artifact=$(xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" \
        -t -v "/x:project/x:artifactId" "$pom")
      cat <<EOL
    - old-group-id: $oldGroupId
      old-artifact-id: $child_artifact
      new-group-id: $groupId
      new-artifact-id: $child_artifact
      new-version: $version
EOL
    done
  } >> "../../$OUTPUT"

  popd >/dev/null
  echo
done

echo "Done! Result in $OUTPUT"

