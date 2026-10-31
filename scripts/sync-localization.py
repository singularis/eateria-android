#!/usr/bin/env python3
"""Sync localization keys from en.json into every locale JSON.

- Adds any keys present in en.json but missing elsewhere (English fallback).
- Reports placeholders where value == key.
- Does not overwrite existing non-placeholder translations.

--from-ios imports missing and changed strings from the iOS Localization folder,
maps pt-BR.json onto pt-br.json, copies quote files that differ, and drops
Android-only keys that no Kotlin or XML source references.

Usage:
  python3 scripts/sync-localization.py
  python3 scripts/sync-localization.py --check   # report only
  python3 scripts/sync-localization.py --from-ios /path/to/eater/Localization
"""

from __future__ import annotations

import argparse
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "assets" / "Localization"
ANDROID_ROOT = Path(__file__).resolve().parents[1]
KEEP_PREFIXES = (
    "profile.theme.name.",
    "activities.stats.range.",
    "rec.json.",
)


def load(path: Path) -> dict[str, str]:
  return json.loads(path.read_text(encoding="utf-8"))


def save(path: Path, data: dict[str, str]) -> None:
  ordered = dict(sorted(data.items(), key=lambda kv: kv[0]))
  path.write_text(
    json.dumps(ordered, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
  )


def android_locale_name(ios_name: str) -> str:
    if ios_name == "pt-BR.json":
        return "pt-br.json"
    return ios_name


def referenced_keys() -> set[str]:
    blob_parts: list[str] = []
    java = ANDROID_ROOT / "app" / "src" / "main"
    for path in java.rglob("*"):
        if path.suffix not in {".kt", ".xml"}:
            continue
        if "build" in path.parts:
            continue
        blob_parts.append(path.read_text(encoding="utf-8", errors="ignore"))
    blob = "\n".join(blob_parts)
    return set(), blob


def key_is_used(key: str, blob: str) -> bool:
    if key in blob:
        return True
    return any(key.startswith(prefix) for prefix in KEEP_PREFIXES)


def import_from_ios(ios_dir: Path) -> None:
    ios_files = {android_locale_name(p.name): p for p in ios_dir.glob("*.json")}
    if "en.json" not in ios_files:
        raise SystemExit(f"No en.json in {ios_dir}")
    ios_en = load(ios_files["en.json"])
    and_en_path = ROOT / "en.json"
    and_en = load(and_en_path)
    _, blob = referenced_keys()

    missing = sorted(set(ios_en) - set(and_en))
    changed = sorted(k for k in set(ios_en) & set(and_en) if ios_en[k] != and_en[k])
    android_only = sorted(set(and_en) - set(ios_en))
    dropped = [key for key in android_only if not key_is_used(key, blob)]
    kept = [key for key in android_only if key_is_used(key, blob)]

    for key in missing + changed:
        and_en[key] = ios_en[key]
    for key in dropped:
        and_en.pop(key, None)
    save(and_en_path, and_en)

    for path in sorted(ROOT.glob("*.json")):
        if path.name == "en.json":
            continue
        data = load(path)
        ios_path = ios_files.get(path.name)
        ios_locale = load(ios_path) if ios_path else {}
        for key in missing + changed:
            data[key] = ios_locale.get(key, ios_en[key])
        for key in dropped:
            data.pop(key, None)
        for key in set(and_en) - set(data):
            data[key] = and_en[key]
        save(path, data)

    quotes_ios = ios_dir / "quotes"
    quotes_and = ROOT / "quotes"
    quotes_and.mkdir(parents=True, exist_ok=True)
    copied = 0
    if quotes_ios.is_dir():
        for src in quotes_ios.iterdir():
            if not src.is_file():
                continue
            dest = quotes_and / src.name
            if not dest.exists() or dest.read_bytes() != src.read_bytes():
                shutil.copyfile(src, dest)
                copied += 1
    print(
        f"from ios: missing={len(missing)} changed={len(changed)} "
        f"dropped_unused={len(dropped)} kept_android_only={len(kept)} quotes_copied={copied}"
    )
    if kept:
        print("kept android-only (still referenced):")
        for key in kept:
            print(f"  {key}")


def main() -> int:
  parser = argparse.ArgumentParser(description=__doc__)
  parser.add_argument(
    "--check", action="store_true", help="Report gaps only; do not write files"
  )
  parser.add_argument(
    "--from-ios",
    default="",
    help="iOS Localization directory. Imports missing and changed strings, then drops unused Android-only keys.",
  )
  args = parser.parse_args()

  if args.from_ios:
    import_from_ios(Path(args.from_ios))

  en_path = ROOT / "en.json"
  en = load(en_path)
  locales = sorted(p for p in ROOT.glob("*.json"))

  total_added = 0
  problems = 0

  for path in locales:
    data = load(path)
    missing = sorted(set(en) - set(data))
    placeholders = sorted(k for k, v in data.items() if v == k)

    if path.name != "en.json" and missing and not args.check:
      for key in missing:
        data[key] = en[key]
        total_added += 1
      save(path, data)
      print(f"{path.stem}: added {len(missing)} keys")
    elif missing or placeholders:
      problems += 1
      print(
        f"{path.stem}: missing={len(missing)} placeholders={len(placeholders)}"
      )
      for key in missing[:10]:
        print(f"  + {key}")
      for key in placeholders[:10]:
        print(f"  ! {key}")
    else:
      print(f"{path.stem}: OK ({len(data)} keys)")

  print(f"\nen keys={len(en)} added={total_added} problem_files={problems}")
  return 1 if args.check and problems else 0


if __name__ == "__main__":
  raise SystemExit(main())
