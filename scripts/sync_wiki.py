"""将主仓库维护的 Wiki 章节同步到相邻文档目录，不复制历史模板和许可证。"""
from pathlib import Path
import argparse


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--destination', type=Path, default=Path(__file__).resolve().parents[2] / 'formula-wiki')
    parser.add_argument('--check', action='store_true', help='只检查差异，不写入文件')
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    destination = args.destination.resolve()
    if not destination.is_dir():
        parser.error('目标目录不存在，请先确认配套 Wiki 路径')
    changed = []
    for source in sorted((root / 'docs/wiki').glob('*.md')):
        if source.name == 'README.md':
            continue
        text = source.read_text(encoding='utf-8')
        text = text.replace('(README.md)', '(../README.md)')
        text = text.replace('(../../', '(../../Formula4j-Plus/')
        for document in ('engine-contract.md', 'strict-engine.md', 'formulajs-compatibility.md', 'benchmark-baseline.md'):
            text = text.replace(f'(../{document})', f'(../../Formula4j-Plus/docs/{document})')
        target = destination / 'wiki' / source.name
        existing = target.read_text(encoding='utf-8-sig') if target.exists() else None
        if existing != text:
            changed.append(source.name)
            if not args.check:
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text(text, encoding='utf-8', newline='\n')
    print(('待同步' if args.check else '已同步') + ': ' + (', '.join(changed) or '无差异'))
    if args.check and changed:
        raise SystemExit(1)


if __name__ == '__main__':
    main()
