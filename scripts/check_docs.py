"""检查发布页面和 Wiki 中的本地文件链接、UTF-8 编码与 BOM。"""
from pathlib import Path
from html.parser import HTMLParser
from urllib.parse import urlsplit, unquote, parse_qs
import re

ROOT = Path(__file__).resolve().parents[1]


class Links(HTMLParser):
    def __init__(self):
        super().__init__()
        self.links = []

    def handle_starttag(self, tag, attrs):
        for key, value in attrs:
            if key in ('href', 'src', 'data-source') and value:
                self.links.append(value)


def main():
    files = [ROOT / name for name in ['index.html', 'CONTRIBUTING.html', 'markdown-viewer.html', 'docs/使用指南.html']]
    files += list((ROOT / 'docs/wiki').glob('*.md'))
    files += [ROOT / 'docs' / name for name in ('engine-contract.md', 'strict-engine.md',
                                              'formulajs-compatibility.md', 'benchmark-baseline.md')]
    files += [ROOT / 'assets/site.css', ROOT / 'assets/site.js', ROOT / 'docs/strict-functions.json']
    files += [ROOT / 'tools/benchmark/README.md']
    errors = []
    for file in files:
        raw = file.read_bytes()
        if raw.startswith(b'\xef\xbb\xbf'):
            errors.append(f'{file.relative_to(ROOT)}: BOM')
        text = raw.decode('utf-8')
        if file.suffix == '.html':
            parser = Links()
            parser.feed(text)
            links = parser.links
        else:
            # 代码块中的示例链接不是导航目标。
            text = re.sub(r'```.*?```', '', text, flags=re.S)
            links = re.findall(r'\]\(([^)\s]+)\)', text)
        for link in links:
            parts = urlsplit(link)
            if parts.scheme or parts.netloc or not parts.path:
                continue
            target = file.parent / unquote(parts.path)
            if not target.exists():
                errors.append(f'{file.relative_to(ROOT)}: missing {link}')
            if parts.path.endswith('markdown-viewer.html'):
                for document in parse_qs(parts.query).get('file', []):
                    if not (target.parent / document).is_file():
                        errors.append(f'{file.relative_to(ROOT)}: missing document {document}')
    if errors:
        raise SystemExit('\n'.join(errors))
    print(f'PASS: {len(files)} pages; local file links and UTF-8 without BOM')


if __name__ == '__main__':
    main()
