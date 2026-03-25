#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "Setting up email-listener-service"
cd "$ROOT_DIR/email-listener-service"
./mvnw -q -DskipTests clean package

echo "Setting up email-processing-service"
cd "$ROOT_DIR/email-processing-service"
./mvnw -q -DskipTests clean package

echo "Setting up frontend"
cd "$ROOT_DIR/frontend"
npm install

echo "Setup complete"
