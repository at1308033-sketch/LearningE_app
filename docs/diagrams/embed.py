"""Gắn ảnh sơ đồ vào tài liệu Markdown theo manifest.json (chạy lại bao nhiêu lần cũng được).

    python docs/diagrams/embed.py

manifest.json: danh sách {"file": "usecase/uc-00-overview.puml", "caption": "...",
"doc": "project/Use-Cases.md", "heading": "<tiêu đề mục, khớp phần đầu>"}.
Mỗi ảnh được chèn ngay sau tiêu đề mục (sau các ảnh trước đó của cùng mục), bọc giữa
<!-- diagram:… --> và <!-- /diagram --> để lần chạy sau thay thế thay vì chèn trùng.
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent
DOCS = ROOT.parent
BLOCK = re.compile(r"\n?<!-- diagram:[^>]*-->\n.*?\n<!-- /diagram -->\n?", re.S)


def norm(s):
    return re.sub(r"[*`#]", "", s).strip().lower()


def main() -> int:
    items = json.loads((ROOT / "manifest.json").read_text(encoding="utf-8"))
    by_doc = {}
    for it in items:
        by_doc.setdefault(it["doc"], []).append(it)
    missing = 0
    for doc_rel, its in by_doc.items():
        path = DOCS / doc_rel
        text = BLOCK.sub("\n", path.read_text(encoding="utf-8"))
        lines = text.split("\n")
        inserts = {}
        for it in its:
            png = pathlib.Path(it["file"]).with_suffix(".png").as_posix()
            if not (ROOT / png).is_file():
                print("thiếu ảnh:", png); missing += 1; continue
            target = norm(it["heading"])
            idx = next((i for i, l in enumerate(lines) if l.startswith("#") and norm(l).startswith(target)), None)
            if idx is None:
                print(f"không thấy mục '{it['heading']}' trong {doc_rel}"); missing += 1; continue
            rel = pathlib.Path("../" * (len(pathlib.Path(doc_rel).parts) - 1) + "diagrams/" + png).as_posix()
            inserts.setdefault(idx, []).append(f"<!-- diagram:{it['file']} -->\n![{it['caption']}]({rel})\n<!-- /diagram -->")
        for idx in sorted(inserts, reverse=True):
            lines[idx + 1:idx + 1] = [""] + "\n\n".join(inserts[idx]).split("\n")
        out = re.sub(r"\n{3,}", "\n\n", "\n".join(lines))
        path.write_text(out, encoding="utf-8")
        print(f"{doc_rel}: {sum(len(v) for v in inserts.values())} sơ đồ")
    return 1 if missing else 0


if __name__ == "__main__":
    sys.exit(main())
