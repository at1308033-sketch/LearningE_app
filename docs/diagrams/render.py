"""Render toàn bộ sơ đồ PlantUML trong docs/diagrams thành PNG + SVG.

Cách dùng:
    set PLANTUML_JAR=D:\\tools\\plantuml.jar   (tải tại https://github.com/plantuml/plantuml/releases)
    python docs/diagrams/render.py            # render tất cả
    python docs/diagrams/render.py usecase    # chỉ một thư mục con

Yêu cầu: Java 17+. Không cần Graphviz (dùng bộ bố cục smetana tích hợp).
Ảnh xuất ra nằm cạnh tệp .puml cùng tên.
"""
import os
import pathlib
import shutil
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parent


def main() -> int:
    jar = os.environ.get("PLANTUML_JAR")
    if not jar or not pathlib.Path(jar).is_file():
        print("Chưa đặt PLANTUML_JAR trỏ tới plantuml.jar", file=sys.stderr)
        return 2
    java = shutil.which("java") or str(pathlib.Path(os.environ.get("JAVA_HOME", "")) / "bin" / "java")
    sub = sys.argv[1:] or [p.name for p in ROOT.iterdir() if p.is_dir()]
    files = [f for d in sub for f in sorted((ROOT / d).glob("*.puml"))]
    if not files:
        print("Không có tệp .puml nào")
        return 1
    failed = 0
    for fmt in ("png", "svg"):
        r = subprocess.run([java, "-jar", jar, "-charset", "UTF-8", f"-t{fmt}", "-failfast2", *map(str, files)],
                           capture_output=True, text=True, encoding="utf-8", errors="replace")
        if r.returncode != 0:
            failed += 1
            print(r.stdout[-3000:], r.stderr[-3000:], sep="\n")
    print(f"Đã render {len(files)} sơ đồ ({'lỗi' if failed else 'OK'})")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
