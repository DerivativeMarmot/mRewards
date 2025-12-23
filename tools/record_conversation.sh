#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: tools/record_conversation.sh [-f <log_file>] ["User: message" "Assistant: reply" ...]

Records a timestamped conversation entry to a markdown log. If no messages are
passed as arguments, the script reads the entry body from STDIN.

Options:
  -f <log_file>   Destination file (default: conversation-log.md)
  -h              Show this help text
EOF
}

log_file="conversation-log.md"

while getopts ":f:h" opt; do
  case "$opt" in
    f) log_file="$OPTARG" ;;
    h) usage; exit 0 ;;
    \?) echo "Unknown option: -$OPTARG" >&2; usage; exit 1 ;;
  esac
done
shift $((OPTIND - 1))

if [[ $# -eq 0 ]]; then
  if [[ -t 0 ]]; then
    echo "No conversation content provided." >&2
    usage
    exit 1
  fi
  content="$(cat)"
else
  content="$*"
fi

timestamp="$(date -u +"%Y-%m-%d %H:%M:%S UTC")"
mkdir -p "$(dirname "$log_file")"

{
  echo "## $timestamp"
  echo
  echo "$content"
  echo
  echo "---"
  echo
} >> "$log_file"
