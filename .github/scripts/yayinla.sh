#!/usr/bin/env bash
# Kullanim: yayinla.sh <klasor> <etiket> <baslik> <jar> <dosya-adi-koku> <readme-surum> <not-alt-yazisi> <latest:true|false>
# Release yoksa olusturur; varsa jar + kaynak zip'i yeniler, notu gunceller ve etiketi bu gonderime tasir.
set -euo pipefail
dir=$1 tag=$2 title=$3 jar=$4 base=$5 ver=$6 footer=$7 latest=$8

out=$(mktemp -d)
cp "$jar" "$out/$base.jar"
git archive --format=zip --prefix="$base-kaynak/" -o "$out/$base-kaynak.zip" "HEAD:$dir"

# README'deki bu surumun bolumu ("**<surum>" ile baslayan satirdan bir sonraki surum basligina kadar) not olur
awk -v v="**$ver" 'index($0, v)==1 {p=1; print; next} p && /^\*\*[0-9]/ {exit} p && /^## / {exit} p' "$dir/README.md" > "$out/notes.md" || true
[ -s "$out/notes.md" ] || echo "$title" > "$out/notes.md"
printf '\n---\n%s\n' "$footer" >> "$out/notes.md"

if gh release view "$tag" >/dev/null 2>&1; then
  gh release upload "$tag" "$out/$base.jar" "$out/$base-kaynak.zip" --clobber
  gh release edit "$tag" --title "$title" --notes-file "$out/notes.md"
  git tag -f "$tag"
  git push -f origin "refs/tags/$tag"
else
  gh release create "$tag" "$out/$base.jar" "$out/$base-kaynak.zip" --target "$GITHUB_SHA" \
    --title "$title" --notes-file "$out/notes.md" --latest="$latest"
fi
echo "Yayinlandi: $tag ($base.jar, $base-kaynak.zip)"
