"""Xuất tài liệu Markdown trong docs/ thành Word (.docx) có kèm sơ đồ.

    python docs/tools/build_docx.py                 # xuất bộ tài liệu mặc định
    python docs/tools/build_docx.py project/SRS.md  # xuất một tệp

Kết quả: docs/export/<Tên>.docx. Hỗ trợ: tiêu đề, đoạn văn, **đậm**, *nghiêng*, `mã`,
liên kết (giữ chữ), danh sách, bảng, trích dẫn, khối mã, ảnh ![chú thích](đường/dẫn.png).
Khối ```mermaid bị bỏ qua vì sơ đồ đã được vẽ lại bằng PlantUML và chèn dưới dạng ảnh.
Cần: pip install python-docx
"""
import datetime
import pathlib
import re
import sys

from docx import Document
from docx.enum.section import WD_ORIENT  # noqa: F401  (giữ cho người sửa sau)
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

DOCS = pathlib.Path(__file__).resolve().parent.parent
OUT = DOCS / "export"
DEFAULT = [
    "project/PRD.md", "project/SRS.md", "project/Use-Cases.md", "project/Business-Rules.md",
    "project/Glossary.md", "project/PROJECT-PLAN.md",
    "engineering/SYSTEM-BLUEPRINT.md", "engineering/DATABASE-DESIGN.md", "engineering/API-SPECIFICATION.md", "engineering/GRPC-API.md", "engineering/SECURITY-THREAT-MODEL.md",
    "engineering/ARCHITECTURE-DECISIONS.md", "engineering/UI-UX-SPECIFICATION.md",
    "engineering/INTEGRATION-DOCUMENTATION.md", "engineering/CODING-STANDARDS.md",
    "quality/TESTING-STRATEGY.md", "quality/TEST-CASES.md",
]
FONT = "Times New Roman"
INK = RGBColor(0x1E, 0x1B, 0x3A)
BRAND = RGBColor(0x2E, 0x28, 0x70)
MAX_W = Cm(16)

BAD_XML = re.compile("[\x00-\x08\x0b\x0c\x0e-\x1f￾￿]")  # ký tự không hợp lệ trong XML
INLINE = re.compile(r"(\*\*[^*]+\*\*|`[^`]+`|\[[^\]]+\]\([^)]+\)|(?<![*\w])\*[^*\s][^*]*\*(?!\w))")


def set_cell_bg(cell, hex_color):
    tc = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), hex_color)
    tc.append(shd)


def add_runs(par, text, size=None, bold=False):
    text = text.replace("<br>", "\n").replace("<br/>", "\n")
    text = BAD_XML.sub("", text)
    for part in INLINE.split(text):
        if not part:
            continue
        if part.startswith("**") and part.endswith("**"):
            r = par.add_run(part[2:-2]); r.bold = True
        elif part.startswith("`") and part.endswith("`"):
            r = par.add_run(part[1:-1]); r.font.name = "Consolas"
            r._element.rPr.rFonts.set(qn("w:eastAsia"), "Consolas")
        elif re.fullmatch(r"\[([^\]]+)\]\(([^)]+)\)", part):
            m = re.fullmatch(r"\[([^\]]+)\]\(([^)]+)\)", part)
            r = par.add_run(m.group(1)); r.font.color.rgb = BRAND; r.underline = True
        elif part.startswith("*") and part.endswith("*") and len(part) > 2:
            r = par.add_run(part[1:-1]); r.italic = True
        else:
            r = par.add_run(part)
        if bold:
            r.bold = True
        if size:
            r.font.size = size


def new_document(title, src):
    doc = Document()
    sec = doc.sections[0]
    sec.page_height, sec.page_width = Cm(29.7), Cm(21)
    sec.left_margin, sec.right_margin = Cm(3), Cm(2)
    sec.top_margin, sec.bottom_margin = Cm(2), Cm(2)
    st = doc.styles["Normal"]
    st.font.name = FONT; st.font.size = Pt(12)
    st.element.rPr.rFonts.set(qn("w:eastAsia"), FONT)
    st.paragraph_format.space_after = Pt(4)
    st.paragraph_format.line_spacing = 1.2
    for lvl, size in ((1, 18), (2, 15), (3, 13), (4, 12)):
        h = doc.styles[f"Heading {lvl}"]
        h.font.name = FONT; h.font.size = Pt(size); h.font.bold = True; h.font.color.rgb = BRAND
        h.element.rPr.rFonts.set(qn("w:eastAsia"), FONT)
        h.paragraph_format.space_before = Pt(12 if lvl > 1 else 18); h.paragraph_format.space_after = Pt(6)
    # trang bìa gọn
    for _ in range(6):
        doc.add_paragraph()
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    add_runs(p, "LearningE — ỨNG DỤNG HỖ TRỢ HỌC TIẾNG ANH", Pt(14), True)
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(title); r.bold = True; r.font.size = Pt(24); r.font.color.rgb = BRAND
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    add_runs(p, "Đồ án môn Lập trình di động (MOPR331279) — HK1 2026–2027", Pt(12))
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    add_runs(p, "Nhóm: Phan Anh Tuấn · Nguyễn Đức Quang Vinh · Nguyễn Tấn Thành · Lê Văn Tấn Lực", Pt(12))
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    add_runs(p, f"Nguồn: docs/{src} · Xuất ngày {datetime.date.today():%d/%m/%Y}", Pt(10))
    doc.add_page_break()
    return doc


def add_table(doc, rows):
    cols = max(len(r) for r in rows)
    t = doc.add_table(rows=len(rows), cols=cols)
    t.style = "Table Grid"; t.alignment = WD_TABLE_ALIGNMENT.CENTER
    for i, row in enumerate(rows):
        for j in range(cols):
            cell = t.cell(i, j)
            cell.text = ""
            par = cell.paragraphs[0]
            add_runs(par, row[j] if j < len(row) else "", Pt(10), bold=(i == 0))
            par.paragraph_format.space_after = Pt(0)
            if i == 0:
                set_cell_bg(cell, "ECEBFF")
    doc.add_paragraph()


def add_image(doc, md_dir, alt, rel, fig_no):
    path = (md_dir / rel).resolve()
    if path.suffix.lower() == ".svg":
        path = path.with_suffix(".png")
    if not path.is_file():
        p = doc.add_paragraph(); add_runs(p, f"[Thiếu ảnh: {rel}]")
        return fig_no
    try:
        from PIL import Image
        with Image.open(path) as im:
            w, h = im.size
        width = min(MAX_W, Cm(w / 150 * 2.54))  # ảnh render ở 150 dpi
        if h / w * width > Cm(22):  # ảnh quá cao: giới hạn theo chiều cao trang
            width = int(Cm(22) * w / h)
    except Exception:
        width = MAX_W
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run().add_picture(str(path), width=width)
    fig_no += 1
    c = doc.add_paragraph(); c.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = c.add_run(f"Hình {fig_no}: {alt}"); r.italic = True; r.font.size = Pt(11)
    return fig_no


def convert(rel):
    src = DOCS / rel
    lines = src.read_text(encoding="utf-8").splitlines()
    title = next((l[2:].strip() for l in lines if l.startswith("# ")), src.stem)
    doc = new_document(title, rel)
    i, fig, in_code, code_lang, code_buf, table = 0, 0, False, "", [], []
    skipped_first_h1 = False

    def flush_table():
        nonlocal table
        if table:
            rows = [r for r in table if not set(r.replace("|", "").strip()) <= set("-: ")]
            parsed = [[c.strip() for c in r.strip().strip("|").split("|")] for r in rows]
            add_table(doc, parsed)
            table = []

    while i < len(lines):
        line = lines[i]
        if line.startswith("```"):
            if not in_code:
                flush_table()
                in_code, code_lang, code_buf = True, line[3:].strip(), []
            else:
                in_code = False
                if code_lang != "mermaid" and code_buf:
                    p = doc.add_paragraph()
                    r = p.add_run(BAD_XML.sub("", "\n".join(code_buf))); r.font.name = "Consolas"; r.font.size = Pt(9)
            i += 1; continue
        if in_code:
            code_buf.append(line); i += 1; continue
        if line.strip().startswith("|"):
            table.append(line); i += 1; continue
        flush_table()
        s = line.strip()
        if not s or s.startswith("<!--"):
            i += 1; continue
        m = re.match(r"^(#{1,4})\s+(.*)", line)
        if m:
            if len(m.group(1)) == 1 and not skipped_first_h1:
                skipped_first_h1 = True  # đã có trên trang bìa
            else:
                doc.add_heading(re.sub(r"[*`]", "", m.group(2)), level=min(len(m.group(1)), 4))
            i += 1; continue
        img = re.match(r"^!\[([^\]]*)\]\(([^)]+)\)", s)
        if img:
            fig = add_image(doc, src.parent, img.group(1), img.group(2), fig); i += 1; continue
        if s.startswith(">"):
            if "TỰ SINH" in s:
                i += 1; continue
            p = doc.add_paragraph(); p.paragraph_format.left_indent = Cm(0.8)
            add_runs(p, s.lstrip("> ").strip()); p.runs and setattr(p.runs[0], "italic", True)
            i += 1; continue
        lm = re.match(r"^(\s*)([-*+]|\d+\.)\s+(.*)", line)
        if lm:
            style = "List Number" if lm.group(2)[0].isdigit() else "List Bullet"
            if len(lm.group(1)) >= 2:
                style += " 2"
            add_runs(doc.add_paragraph(style=style), lm.group(3)); i += 1; continue
        add_runs(doc.add_paragraph(), s); i += 1
    flush_table()
    OUT.mkdir(exist_ok=True)
    out = OUT / f"{src.stem}.docx"
    doc.save(out)
    return out, fig


def main():
    targets = sys.argv[1:] or DEFAULT
    for rel in targets:
        out, figs = convert(rel)
        print(f"{rel} -> {out.relative_to(DOCS.parent)} ({figs} hình)")


if __name__ == "__main__":
    main()
