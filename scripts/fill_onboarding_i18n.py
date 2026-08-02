#!/usr/bin/env python3
"""Fill missing onboarding Localization keys for every Android locale."""

from __future__ import annotations

import json
import re
import sys
import time
from pathlib import Path

from deep_translator import GoogleTranslator

ROOT = Path(__file__).resolve().parents[1]
AND = ROOT / "app" / "src" / "main" / "assets" / "Localization"
KT = ROOT / "app" / "src" / "main" / "java"

ALIASES = {
    "common.next": "onboarding.next",
    "settings.appearance": "profile.appearance",
    "settings.appearance.dark": "appearance.dark",
    "settings.appearance.light": "appearance.light",
    "settings.appearance.system": "appearance.system",
    "settings.reduce_motion": "profile.reduce_motion",
}

GT_TARGET = {
    "pt-br": "pt",
    "zh": "zh-CN",
}

EXTRA_EN = {
    "onboarding.plates.score": "Score: %d",
    "settings.reduce_motion.desc": "Minimize animations",
}


def log(msg: str) -> None:
    print(msg, flush=True)


def unescape(s: str) -> str:
    return (
        s.replace("\\n", "\n")
        .replace('\\"', '"')
        .replace("\\'", "'")
        .replace("\\\\", "\\")
        .replace("\\uD83D\\uDC9C", "💜")
    )


def extract_en_from_kotlin() -> dict[str, str]:
    pat = re.compile(
        r'Localization\.tr\(\s*(?:LocalContext\.current|context)\s*,\s*"([^"]+)"\s*,\s*"((?:\\.|[^"\\])*)"',
        re.S,
    )
    keys: dict[str, str] = {}
    for p in list(KT.rglob("*Onboard*.kt")) + list(KT.rglob("*ProgressiveOnboarding*.kt")):
        text = p.read_text(encoding="utf-8", errors="ignore")
        for m in pat.finditer(text):
            keys[m.group(1)] = unescape(m.group(2))
    keys.update(EXTRA_EN)
    return keys


def load(path: Path) -> dict[str, str]:
    return json.loads(path.read_text(encoding="utf-8"))


def save(path: Path, data: dict[str, str]) -> None:
    ordered = dict(sorted(data.items()))
    path.write_text(json.dumps(ordered, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def protect(text: str) -> str:
    return text.replace("%d", "<<<D>>>").replace("%@", "<<<AT>>>").replace("%s", "<<<S>>>")


def unprotect(text: str) -> str:
    return text.replace("<<<D>>>", "%d").replace("<<<AT>>>", "%@").replace("<<<S>>>", "%s")


def translate_batch(texts: list[str], target: str) -> list[str]:
    if not texts:
        return []
    protected = [protect(t) for t in texts]
    translator = GoogleTranslator(source="en", target=target)
    # Chunk to avoid request limits
    out: list[str] = []
    chunk_size = 20
    for i in range(0, len(protected), chunk_size):
        chunk = protected[i : i + chunk_size]
        try:
            translated = translator.translate_batch(chunk)
            if not isinstance(translated, list):
                translated = [translated]
            # Pad / repair length mismatches
            while len(translated) < len(chunk):
                translated.append(chunk[len(translated)])
            out.extend(unprotect(t or chunk[j]) for j, t in enumerate(translated[: len(chunk)]))
        except Exception as e:
            log(f"  batch fail ({target} @{i}): {e}; falling back one-by-one")
            for t in chunk:
                try:
                    out.append(unprotect(translator.translate(t) or t))
                    time.sleep(0.08)
                except Exception as e2:
                    log(f"  single fail: {e2}")
                    out.append(unprotect(t))
        time.sleep(0.2)
    return out


def main() -> int:
    en_src = extract_en_from_kotlin()
    en_path = AND / "en.json"
    en_data = load(en_path)
    for k, v in en_src.items():
        en_data[k] = v
    save(en_path, en_data)
    log(f"en: ensured {len(en_src)} keys (total {len(en_data)})")

    locales = sorted(p for p in AND.glob("*.json") if p.stem != "en")
    for path in locales:
        code = path.stem
        data = load(path)
        target = GT_TARGET.get(code, code)

        to_fill: list[tuple[str, str]] = []
        for key, en_val in en_src.items():
            if key in ALIASES and ALIASES[key] in data and data[ALIASES[key]]:
                data[key] = data[ALIASES[key]]
                continue
            existing = data.get(key)
            if existing and existing != en_val and existing != key:
                continue
            to_fill.append((key, en_val))

        if not to_fill:
            log(f"{code}: already complete")
            save(path, data)
            continue

        log(f"{code}: translating {len(to_fill)} strings -> {target}")
        translated = translate_batch([v for _, v in to_fill], target)
        for (key, _), value in zip(to_fill, translated):
            data[key] = value
        save(path, data)
        log(f"{code}: done")

    uk = load(AND / "uk.json")
    log(f"uk tools.title: {uk.get('onboarding.tools.title')}")
    log(f"uk plates.score: {uk.get('onboarding.plates.score')}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
