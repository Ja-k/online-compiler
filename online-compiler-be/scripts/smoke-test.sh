#!/usr/bin/env bash
#
# Smoke test for the /v1/run endpoint. Exercises all supported languages plus
# the compile-error and unsupported-language paths against a running backend.
#
# Usage:
#   ./scripts/smoke-test.sh [base_url]
#
# The backend must already be running (e.g. `./mvnw spring-boot:run`) and
# Docker must be available, since each case spins up real containers.

set -u

BASE_URL="${1:-http://localhost:9191}"
ENDPOINT="${BASE_URL}/v1/run"

PASS_COUNT=0
FAIL_COUNT=0

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# run_case NAME LANGUAGE VERSION CODE EXPECTED_STATUS EXPECTED_SUBSTRING
run_case() {
	local name="$1" language="$2" version="$3" code="$4" expected_status="$5" expected_substring="$6"

	local payload
	payload=$(printf '{"language":"%s","version":"%s","code":"%s"}' "$language" "$version" "$code")

	local response status body
	response=$(curl -s -w '\n%{http_code}' -X POST "$ENDPOINT" -H 'Content-Type: application/json' -d "$payload")
	status=$(echo "$response" | tail -n1)
	body=$(echo "$response" | sed '$d')

	if [[ "$status" != "$expected_status" ]]; then
		echo -e "${RED}FAIL${NC} ${name}: expected HTTP ${expected_status}, got ${status}"
		echo "  body: ${body}"
		FAIL_COUNT=$((FAIL_COUNT + 1))
		return
	fi

	if [[ -n "$expected_substring" && "$body" != *"$expected_substring"* ]]; then
		echo -e "${RED}FAIL${NC} ${name}: response did not contain '${expected_substring}'"
		echo "  body: ${body}"
		FAIL_COUNT=$((FAIL_COUNT + 1))
		return
	fi

	echo -e "${GREEN}PASS${NC} ${name}"
	PASS_COUNT=$((PASS_COUNT + 1))
}

echo "Running smoke tests against ${ENDPOINT}"
echo

run_case "Java - hello world" \
	"java" "17" 'public class Main {\n public static void main(String[] args) {\n System.out.println(\"Hello from Java!\");\n }\n}' \
	"200" "Hello from Java!"

run_case "Java - compile error returns 400" \
	"java" "17" 'public class Main { this is not valid java }' \
	"400" "error"

run_case "C - hello world" \
	"c" "17" '#include <stdio.h>\nint main() { printf(\"Hello from C!\\n\"); return 0; }' \
	"200" "Hello from C!"

run_case "C++ - hello world" \
	"cpp" "17" '#include <iostream>\nint main() { std::cout << \"Hello from C++!\" << std::endl; return 0; }' \
	"200" "Hello from C++!"

run_case "Python - hello world" \
	"python" "3.11" 'print(\"Hello from Python!\")' \
	"200" "Hello from Python!"

run_case "Rust - hello world" \
	"rust" "2021" 'fn main() { println!(\"Hello from Rust!\"); }' \
	"200" "Hello from Rust!"

run_case "Go - hello world" \
	"go" "1.22" 'package main\n\nimport \"fmt\"\n\nfunc main() {\n    fmt.Println(\"Hello from Go!\")\n}' \
	"200" "Hello from Go!"

run_case "C# - hello world" \
	"csharp" "8.0" 'Console.WriteLine(\"Hello from C#!\");' \
	"200" "Hello from C#!"

run_case "C# - compile error returns 400" \
	"csharp" "8.0" 'Console.WriteLine(\"missing paren\"' \
	"400" "error"

run_case "Unsupported language returns 400" \
	"cobol" "1" 'whatever' \
	"400" "Unsupported language"

echo
echo "Results: ${PASS_COUNT} passed, ${FAIL_COUNT} failed"

if [[ "$FAIL_COUNT" -gt 0 ]]; then
	exit 1
fi
