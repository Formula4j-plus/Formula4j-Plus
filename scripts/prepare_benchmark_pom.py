"""为本地 JMH 别名生成保留主库依赖的临时 POM，不修改主项目版本。"""
from pathlib import Path
import argparse
import xml.etree.ElementTree as ET


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('source', type=Path, help='构建副本中的原 pom.xml')
    parser.add_argument('destination', type=Path, help='临时 benchmark-core.pom 路径')
    args = parser.parse_args()
    source = args.source.resolve()
    destination = args.destination.resolve()
    if source == destination:
        parser.error('临时 POM 不能覆盖原 POM')
    namespace = 'http://maven.apache.org/POM/4.0.0'
    ET.register_namespace('', namespace)
    ET.register_namespace('xsi', 'http://www.w3.org/2001/XMLSchema-instance')
    root = ET.fromstring(source.read_text(encoding='utf-8'))
    version = root.find(f'{{{namespace}}}version')
    if version is None:
        parser.error('原 POM 缺少项目 version')
    version.text = '3.0.0-benchmark'
    xml = ET.tostring(root, encoding='unicode')
    destination.write_text('<?xml version="1.0" encoding="UTF-8"?>\n' + xml + '\n',
                           encoding='utf-8', newline='\n')
    print(f'Generated local benchmark POM: {destination}')


if __name__ == '__main__':
    main()
