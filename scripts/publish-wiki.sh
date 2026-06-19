#!/usr/bin/env bash
#
# publish-wiki.sh — mirror docs/wiki/ into the GitHub Wiki for this repo.
#
# The GitHub Wiki is a separate git repository (REPO.wiki.git) that lives behind
# the "Wiki" tab. This script clones it, copies the Markdown pages from
# docs/wiki/, rewrites the inter-page links into the wiki's flat namespace
# (which has no ".md" suffix and no subfolders), generates a navigation sidebar,
# and pushes.
#
# Run it from anywhere inside the repo, on a machine that has push access to the
# wiki (i.e. where `git push` to GitHub works for you):
#
#     ./scripts/publish-wiki.sh
#
# Prerequisites:
#   * The wiki must exist. GitHub creates the wiki repo only after the first page
#     is saved — open https://github.com/Trystar360/echoes-of-the-deep/wiki and
#     click "Create the first page" (any content) once, then run this script.
#   * Push access to the repo's wiki.
#
# Override the wiki remote if needed:
#     WIKI_REMOTE=git@github.com:Trystar360/echoes-of-the-deep.wiki.git ./scripts/publish-wiki.sh

set -euo pipefail

REPO_SLUG="Trystar360/echoes-of-the-deep"
WIKI_REMOTE="${WIKI_REMOTE:-https://github.com/${REPO_SLUG}.wiki.git}"

# Resolve repo root and the source wiki directory.
ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
SRC="${ROOT}/docs/wiki"

if [[ ! -d "$SRC" ]]; then
  echo "error: ${SRC} not found — run this from inside the repo." >&2
  exit 1
fi

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

echo "Cloning wiki: ${WIKI_REMOTE}"
if ! git clone --depth 1 "$WIKI_REMOTE" "$WORK/wiki" 2>/dev/null; then
  echo "error: could not clone the wiki repo." >&2
  echo "       Make sure the wiki has at least one page (create one in the UI)," >&2
  echo "       and that you have push access. See the header of this script." >&2
  exit 1
fi

# Copy images (icons + montages) so the wiki's relative image links resolve.
if [[ -d "$SRC/images" ]]; then
  echo "Copying images"
  rm -rf "$WORK/wiki/images"
  cp -R "$SRC/images" "$WORK/wiki/images"
fi

echo "Transforming pages from ${SRC}"
shopt -s nullglob
for f in "$SRC"/*.md; do
  name="$(basename "$f")"
  # Link rewrites for the wiki's flat namespace:
  #   * inter-page links:        (Getting-Started.md)  -> (Getting-Started)
  #   * links up to docs/ files: (../cosmology.md)     -> full blob URL on main
  sed -E \
    -e "s#\]\(\.\./([A-Za-z0-9_./-]+)\)#](https://github.com/${REPO_SLUG}/blob/main/docs/\1)#g" \
    -e 's#\]\(([A-Za-z0-9_-]+)\.md\)#](\1)#g' \
    "$f" > "$WORK/wiki/$name"
done

# Generate a sidebar for navigation (GitHub renders _Sidebar.md on every page).
cat > "$WORK/wiki/_Sidebar.md" <<'SIDEBAR'
### Octaves of the One

- [Home](Home)
- [Getting Started](Getting-Started)
- [Cosmology & Lore](Cosmology-and-Lore)
- [Energy System](Energy-System)
- [Blocks](Blocks)
- [Items & Gear](Items-and-Gear)
- [Wireless Transport](Wireless-Transport)
- [Ores & Worldgen](Ores-and-Worldgen)
- [Ambient Capture](Ambient-Capture)
- [Crafting & Progression](Crafting-and-Progression)
- [Compatibility](Compatibility)
- [Reference & FAQ](Reference-and-FAQ)
SIDEBAR

cd "$WORK/wiki"
git add -A
if git diff --cached --quiet; then
  echo "Wiki already up to date — nothing to push."
  exit 0
fi

git -c user.name="${GIT_AUTHOR_NAME:-$(git config user.name || echo wiki-bot)}" \
    -c user.email="${GIT_AUTHOR_EMAIL:-$(git config user.email || echo wiki-bot@users.noreply.github.com)}" \
    commit -m "Mirror docs/wiki -> GitHub Wiki" >/dev/null

git push origin HEAD
echo "Done. View at: https://github.com/${REPO_SLUG}/wiki"
