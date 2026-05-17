#!/usr/bin/env python3
"""Render docs/INTERVIEW_MVLChain.md to docs/INTERVIEW_MVLChain.pdf (lightweight Markdown)."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MD_PATH = ROOT / "docs" / "INTERVIEW_MVLChain.md"
PDF_PATH = ROOT / "docs" / "INTERVIEW_MVLChain.pdf"


def to_core_font_text(s: str) -> str:
    """fpdf core Helvetica supports Latin-1; normalize common Unicode punctuation."""
    s = s.replace("**", "")
    for a, b in (
        ("\u2014", "-"),  # em dash
        ("\u2013", "-"),  # en dash
        ("\u2192", "->"),
        ("\u2026", "..."),
        ("\u201c", '"'),
        ("\u201d", '"'),
        ("\u2018", "'"),
        ("\u2019", "'"),
        ("\u00a0", " "),
        ("`", "'"),
    ):
        s = s.replace(a, b)
    # Drop any remaining non latin-1 for safety
    out: list[str] = []
    for ch in s:
        o = ord(ch)
        if o < 32 and ch not in "\t":
            continue
        if o <= 255:
            out.append(ch)
        else:
            out.append("?")
    return "".join(out)


def is_table_separator(line: str) -> bool:
    t = line.strip()
    return bool(re.match(r"^\|?[\s\-:|]+\|?$", t)) and "-" in t


def main() -> None:
    try:
        from fpdf import FPDF
        from fpdf.enums import XPos, YPos
    except ImportError:
        print(
            "fpdf2 is required. Run: python3 -m pip install fpdf2 -t build/pdfgen_vendor",
            file=sys.stderr,
        )
        sys.exit(1)

    if not MD_PATH.is_file():
        print(f"Missing {MD_PATH}", file=sys.stderr)
        sys.exit(1)

    class DocPDF(FPDF):
        def footer(self) -> None:
            self.set_y(-15)
            self.set_font("Helvetica", "I", 9)
            self.cell(0, 10, to_core_font_text(f"Page {self.page_no()}"), align="C", new_x=XPos.LMARGIN, new_y=YPos.NEXT)

    pdf = DocPDF()
    pdf.set_auto_page_break(auto=True, margin=15)
    pdf.add_page()
    pdf.set_left_margin(18)
    pdf.set_right_margin(18)

    body = MD_PATH.read_text(encoding="utf-8")
    epw = pdf.epw

    def mcell(h: float, text: str, size: int | None = None, style: str = "") -> None:
        if size is not None:
            pdf.set_font("Helvetica", style, size)
        pdf.multi_cell(
            w=epw,
            h=h,
            text=text,
            new_x=XPos.LMARGIN,
            new_y=YPos.NEXT,
        )

    for raw in body.splitlines():
        line = to_core_font_text(raw.rstrip())

        if not line:
            pdf.ln(3)
            continue
        if line.strip() == "---":
            pdf.ln(3)
            continue
        if is_table_separator(line):
            continue

        if line.startswith("|") and line.endswith("|"):
            mcell(4, line, size=8)
            pdf.set_font("Helvetica", size=10)
            continue

        if line.startswith("# "):
            mcell(8, line[2:], size=18, style="B")
            pdf.ln(2)
            continue
        if line.startswith("## "):
            mcell(7, line[3:], size=14, style="B")
            pdf.ln(1)
            continue
        if line.startswith("### "):
            mcell(6, line[4:], size=11, style="B")
            pdf.ln(1)
            continue
        if line.startswith("- "):
            mcell(5, "  - " + line[2:], size=10)
            continue
        if re.match(r"^\d+\.\s", line):
            mcell(5, line, size=10)
            continue
        if line.startswith("*") and line.endswith("*") and len(line) > 2:
            mcell(5, line[1:-1], size=9, style="I")
            continue

        mcell(5, line, size=10)

    pdf.output(str(PDF_PATH))
    print(f"Wrote {PDF_PATH}")


if __name__ == "__main__":
    main()
