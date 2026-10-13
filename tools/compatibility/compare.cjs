'use strict';

// 仅执行语料中的结构化操作与 Formula.js 函数，不 eval 公式文本。
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const formula = require('@formulajs/formulajs');
const version = require('@formulajs/formulajs/package.json').version;
const args = process.argv.slice(2);
const option = name => args.includes(name) ? args[args.indexOf(name) + 1] : undefined;
if (!args[0]) throw new Error('Usage: node compare.cjs java-report.json [--corpus path] [--report path] [--catalog path] [--inspect]');
const corpusPath = option('--corpus') || path.resolve(__dirname, '../../src/test/resources/compatibility/formulajs-4.6.1.json');
const corpus = JSON.parse(fs.readFileSync(corpusPath, 'utf8'));
const java = JSON.parse(fs.readFileSync(args[0], 'utf8'));
assert.equal(version, corpus.formulajsVersion, '必须使用语料固定的 Formula.js 版本');
assert.equal(java.formulajsVersion, version);
process.env.TZ = 'UTC';

function evaluate(reference) {
  if (reference === null || typeof reference !== 'object') return reference;
  if (Array.isArray(reference)) return reference.map(evaluate);
  const values = reference.args.map(evaluate);
  if (reference.function) {
    const fn = formula[reference.function];
    if (!Object.hasOwn(formula, reference.function) || typeof fn !== 'function') {
      throw new Error(`Unknown oracle function: ${reference.function}`);
    }
    return fn(...values);
  }
  switch (reference.operator) {
    case '+': return values[0] + values[1];
    case '-': return values[0] - values[1];
    case '*': return values[0] * values[1];
    case '/': return values[0] / values[1];
    case '^': return values[0] ** values[1];
    default: throw new Error(`Unknown oracle operation: ${reference.operator}`);
  }
}

function capture(reference) {
  const value = evaluate(reference);
  if (value instanceof Error) return {type: 'error', code: value.message};
  if (value instanceof Date) return {type: 'date', value: value.toISOString()};
  if (value === null) return {type: 'null'};
  if (typeof value === 'number') {
    return Number.isFinite(value) ? {type: 'number', value: String(value)} : {type: 'error', code: 'NON_FINITE'};
  }
  if (typeof value === 'string' || typeof value === 'boolean') return {type: typeof value, value};
  throw new Error(`Unmodeled oracle result: ${typeof value}`);
}

// 使用 BigInt 比较十进制字符串，避免 Number 转换抹掉大整数差异。
function decimal(text) {
  const match = /^([+-]?)(\d+)(?:\.(\d*))?(?:e([+-]?\d+))?$/i.exec(text);
  assert.ok(match, `Invalid decimal: ${text}`);
  const fraction = match[3] || '';
  return {coefficient: BigInt((match[1] === '-' ? '-' : '') + match[2] + fraction),
    scale: fraction.length - Number(match[4] || 0)};
}

function close(left, right, tolerance) {
  const numbers = [decimal(left), decimal(right), decimal(tolerance)];
  const scale = Math.max(...numbers.map(value => value.scale));
  const integers = numbers.map(value => value.coefficient * 10n ** BigInt(scale - value.scale));
  const difference = integers[0] - integers[1];
  assert.ok(integers[2] >= 0n, 'Tolerance must be nonnegative');
  return (difference < 0n ? -difference : difference) <= integers[2];
}

function classify(actual, reference, tolerance) {
  if (actual.type === 'error') return actual.code === 'UNKNOWN_FUNCTION' ? 'unsupported' : 'error';
  if (actual.type !== reference.type) return 'difference';
  if (actual.type === 'number') return close(actual.value, reference.value, tolerance) ? 'match' : 'difference';
  return actual.value === reference.value ? 'match' : 'difference';
}

const results = [];
for (const fixture of corpus.cases) {
  const reference = capture(fixture.reference);
  const engines = java.cases[fixture.id];
  assert.ok(engines, `Missing Java case: ${fixture.id}`);
  const status = Object.fromEntries(['legacy', 'strict'].map(engine =>
    [engine, classify(engines[engine], reference, fixture.tolerance || '0')]));
  if (!args.includes('--inspect')) {
    assert.deepEqual(reference, fixture.referenceExpected, `${fixture.id}: Formula.js result changed`);
    assert.deepEqual(status, fixture.expectedStatus, `${fixture.id}: compatibility classification changed`);
    assert.deepEqual(engines, fixture.javaExpected, `${fixture.id}: Java contract changed`);
  }
  results.push({id: fixture.id, reference, ...engines, status});
}
assert.deepEqual(Object.keys(java.cases).sort(), corpus.cases.map(fixture => fixture.id).sort());
if (args.includes('--inspect')) {
  process.stdout.write(JSON.stringify(results, null, 2) + '\n');
} else {
  const counts = {};
  for (const engine of ['legacy', 'strict']) {
    counts[engine] = {};
    for (const result of results) counts[engine][result.status[engine]] = (counts[engine][result.status[engine]] || 0) + 1;
  }
  console.log(`PASS: Formula.js ${version}; ${results.length} reviewed cases; ${JSON.stringify(counts)}`);
  if (option('--catalog')) {
    assert.equal(java.strictFunctions.length, 28, '严格函数元数据发生变化，需要更新文档');
    fs.writeFileSync(option('--catalog'), JSON.stringify(java.strictFunctions, null, 2) + '\n', 'utf8');
  }
  if (option('--report')) {
    const labels = {match: '一致', difference: '差异', error: '报错', unsupported: '未支持'};
    const report = [
      '# Formula.js 差分兼容矩阵', '',
      `对照版本：\`@formulajs/formulajs@${version}\`，由 package-lock.json 固定依赖树。共 ${results.length} 个已审核用例。`, '',
      '这是有限语料的实测结果，不代表全部函数或 Excel 完全兼容。默认要求数值精确一致；少量浮点用例在表中列出绝对误差。', '',
      'Java 结果与 Formula.js 结果分别固定为契约；CI 重新执行两端，任何结果或分类变化都会失败。', '',
      '严格入口返回 BigDecimal、String、Boolean 或 null；兼容入口保留历史返回类型。', '',
      '| 用例 | Java 公式 | 兼容入口 | 严格入口 | 绝对误差 | 说明 |',
      '| --- | --- | --- | --- | --- | --- |'
    ];
    const cell = text => String(text).replaceAll('|', '&#124;').replaceAll('\n', ' ');
    for (let i = 0; i < results.length; i++) {
      const result = results[i];
      const fixture = corpus.cases[i];
      report.push(`| ${cell(result.id)} | \`${cell(fixture.formula)}\` | ${labels[result.status.legacy]} | ${labels[result.status.strict]} | ${fixture.tolerance || '0'} | ${cell(fixture.note)} |`);
    }
    report.push('', '## 复现', '',
      '```sh',
      'mvn -B -Dtest=FormulaJsDifferentialTest -Duser.timezone=UTC -Dformula.differential.output=java-report.json test',
      'npm --prefix tools/compatibility ci --ignore-scripts',
      'node tools/compatibility/compare.cjs java-report.json --report docs/formulajs-compatibility.md --catalog docs/strict-functions.json',
      '```', '',
      'Formula.js 是函数库。参考侧直接执行结构化函数调用和明确的算术操作，不使用 eval，也不将其当成与 Java 同构的表达式解析器。', '',
      '日期对照固定 UTC：Formula.js 返回 Date，兼容入口的历史日期表示存在差异；严格入口当前不支持日期函数。', '',
      '报错和未支持不计入一致。预期差异被明确记录，不能通过扩大误差或忽略异常来标记为兼容。', '',
      '[严格模式接入与边界](strict-engine.md) · [引擎行为契约](engine-contract.md)', ''
    );
    fs.writeFileSync(option('--report'), report.join('\n'), 'utf8');
  }
}
