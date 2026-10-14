#!/usr/bin/env bash
set -euo pipefail
# Run only as the repository owner. Never enable shell tracing here.
cd "$(dirname "$0")/.."
apk="$(realpath "${1:?Usage: publish-v1.sh APK PRIVATE_BACKUP_DIRECTORY}")"
private_backup="$(realpath "${2:?Private directory containing nafba.jks and password.txt}")"
test -f "$apk"
test -f "$private_backup/nafba.jks"
test -f "$private_backup/password.txt"
case "$private_backup/" in "$PWD/"*) echo 'Keep private signing files outside the repository.' >&2; exit 1;; esac
account="$(gh api user --jq .login)"
test "$account" = Fufi1925 || { echo 'Expected GitHub account Fufi1925.' >&2; exit 1; }
if gh repo view Fufi1925/Nafba >/dev/null 2>&1; then
  echo 'Repository already exists. Refusing to overwrite it.' >&2; exit 1
fi
git init -b main
git add .
git -c user.name=Fufi1925 -c user.email=Fufi1925@users.noreply.github.com commit -m 'feat: Nafba V1 Android island and release pipeline'
gh repo create Fufi1925/Nafba --public --source=. --remote=origin \
  --description 'Native Android Dynamic Island mit iOS-inspirierter Glas-Optik, Akkuwarnungen und anpassbarer Position für POCO/Redmi.'
gh repo edit Fufi1925/Nafba --add-topic android --add-topic poco --add-topic redmi --add-topic dynamic-island --add-topic battery --add-topic java --add-topic glass-ui
base64 < "$private_backup/nafba.jks" | tr -d '\n' | gh secret set NAFBA_KEYSTORE_BASE64 -R Fufi1925/Nafba
gh secret set NAFBA_STORE_PASSWORD -R Fufi1925/Nafba < "$private_backup/password.txt"
gh secret set NAFBA_KEY_PASSWORD -R Fufi1925/Nafba < "$private_backup/password.txt"
git push -u origin main
git tag -a v1.0.0 -m 'Nafba V1.0.0 — first preview'
git push origin v1.0.0
release_dir="$(mktemp -d)"
trap 'rm -rf "$release_dir"' EXIT
cp "$apk" "$release_dir/Nafba-v1.0.0.apk"
(cd "$release_dir" && sha256sum Nafba-v1.0.0.apk > SHA256SUMS.txt)
if gh release view v1.0.0 -R Fufi1925/Nafba >/dev/null 2>&1; then
  gh release upload v1.0.0 "$release_dir"/* -R Fufi1925/Nafba --clobber
else
  gh release create v1.0.0 "$release_dir"/* -R Fufi1925/Nafba \
    --title 'Nafba V1.0.0' --notes-file docs/RELEASE_NOTES.md --prerelease --verify-tag
fi
echo 'Published: https://github.com/Fufi1925/Nafba'
