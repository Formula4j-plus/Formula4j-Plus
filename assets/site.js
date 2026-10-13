/* 共用站点导航与阅读目录；页面既有的语言、主题和代码复制逻辑继续负责原内容。 */
(() => {
    'use strict';
    const words = {
        'zh-CN': {
            skip: '跳到正文', guide: '使用指南', overview: '项目概览', changes: '更新日志', contribute: '参与贡献',
            menu: '打开导航', contents: '文档目录', onPage: '本页内容', docs: '开发文档', search: '筛选本页章节…',
            empty: '没有匹配的章节', lang: '选择语言', theme: '选择主题',
            eyebrow: 'JAVA 公式计算引擎', line1: '让每一次计算，', line2: '都有据可依。',
            subtitle: '为动态表单与业务结果提供后端复算。让前后端共用公式，让计算过程清晰可追溯。',
            start: '快速开始', read: '阅读文档', example: '计算示例', result: '预期结果',
            caption: '静态示例 · 在 Java 项目中执行', java: 'Java 8 编译目标', version: 'v3.0.0',
            mapTitle: '字段映射', mapDesc: '连接表单里的字段名称与业务数据，让公式更贴近业务。',
            fnTitle: '函数扩展', fnDesc: '复用内置函数，通过 Java Lambda 扩展自己的计算逻辑。',
            verifyTitle: '结果校验', verifyDesc: '后端复算提交的数据，按指定误差核对数值结果。',
            strictTitle: '严格模式 · 当前源码', strictIntro: '显式启用十进制计算、惰性 IF 与错误定位。新增能力尚未发布，旧入口继续使用兼容语义。',
            strictGuide: '严格模式接入与边界', compatibilityMatrix: 'Formula.js 4.6.1 实测矩阵',
            functionTitle: '严格函数目录', functionIntro: '28 个首批函数。按名称筛选，查看参数与结果类型；完整边界见严格模式文档。',
            functionSearch: '筛选函数名称…', functionResults: '匹配函数', functionParameters: '参数',
            functionReturns: '返回', functionLazy: '惰性求值', yes: '是', no: '否',
            functionLoadError: '目录加载失败，请打开严格模式文档查看完整列表。'
        },
        'zh-TW': {
            skip: '跳到正文', guide: '使用指南', overview: '專案概覽', changes: '更新日誌', contribute: '參與貢獻',
            menu: '開啟導覽', contents: '文檔目錄', onPage: '本頁內容', docs: '開發文檔', search: '篩選本頁章節…',
            empty: '沒有符合的章節', lang: '選擇語言', theme: '選擇主題',
            eyebrow: 'JAVA 公式計算引擎', line1: '讓每一次計算，', line2: '都有據可依。',
            subtitle: '為動態表單與業務結果提供後端複算。讓前後端共用公式，讓計算過程清晰可追溯。',
            start: '快速開始', read: '閱讀文檔', example: '計算示例', result: '預期結果',
            caption: '靜態示例 · 在 Java 專案中執行', java: 'Java 8 編譯目標', version: 'v3.0.0',
            mapTitle: '欄位映射', mapDesc: '連接表單裡的欄位名稱與業務資料，讓公式更貼近業務。',
            fnTitle: '函數擴展', fnDesc: '複用內建函數，透過 Java Lambda 擴展自己的計算邏輯。',
            verifyTitle: '結果校驗', verifyDesc: '後端複算提交的資料，依指定誤差核對數值結果。',
            strictTitle: '嚴格模式 · 目前原始碼', strictIntro: '明確啟用十進位計算、惰性 IF 與錯誤定位。新增能力尚未發布，舊入口繼續使用相容語義。',
            strictGuide: '嚴格模式接入與邊界', compatibilityMatrix: 'Formula.js 4.6.1 實測矩陣',
            functionTitle: '嚴格函數目錄', functionIntro: '28 個首批函數。依名稱篩選，查看參數與結果類型；完整邊界見嚴格模式文檔。',
            functionSearch: '篩選函數名稱…', functionResults: '符合函數', functionParameters: '參數',
            functionReturns: '返回', functionLazy: '惰性求值', yes: '是', no: '否',
            functionLoadError: '目錄載入失敗，請開啟嚴格模式文檔查看完整列表。'
        },
        en: {
            skip: 'Skip to content', guide: 'User guide', overview: 'Overview', changes: 'Changelog', contribute: 'Contribute',
            menu: 'Open navigation', contents: 'Documentation', onPage: 'On this page', docs: 'Documentation',
            search: 'Filter page sections…', empty: 'No matching sections', lang: 'Choose language', theme: 'Choose theme',
            eyebrow: 'JAVA FORMULA ENGINE', line1: 'Every calculation.', line2: 'A reliable result.',
            subtitle: 'Recalculate dynamic forms and business results on the backend. Share formulas across layers and keep your calculations clear.',
            start: 'Get started', read: 'Read the docs', example: 'Formula example', result: 'Expected result',
            caption: 'Static example · Execute in your Java project', java: 'Java 8 compile target', version: 'v3.0.0',
            mapTitle: 'Field mapping', mapDesc: 'Connect form labels to business data. Keep formulas close to the way your team works.',
            fnTitle: 'Custom functions', fnDesc: 'Use built-in functions and extend calculation logic with Java lambdas.',
            verifyTitle: 'Result validation', verifyDesc: 'Recalculate submitted data and compare numeric results within a chosen tolerance.',
            strictTitle: 'Strict mode · Current source', strictIntro: 'Opt into decimal arithmetic, lazy IF and error locations. These additions are unreleased; existing entry points retain compatibility semantics.',
            strictGuide: 'Strict mode guide and limits', compatibilityMatrix: 'Formula.js 4.6.1 comparison matrix',
            functionTitle: 'Strict function catalog', functionIntro: '28 initial functions. Filter by name and inspect parameter and result types. See the strict mode guide for limits.',
            functionSearch: 'Filter function names…', functionResults: 'Matching functions', functionParameters: 'Parameters',
            functionReturns: 'Returns', functionLazy: 'Lazy evaluation', yes: 'Yes', no: 'No',
            functionLoadError: 'Catalog could not load. Open the strict mode guide for the full list.'
        }
    };
    const language = () => words[document.documentElement.lang] || words['zh-CN'];
    const controls = document.querySelector('.control-bar');
    document.querySelector('.site-controls-slot').appendChild(controls);
    const menuButton = document.querySelector('.mobile-menu-toggle');
    const menu = document.querySelector('.site-links');
    const sidebar = document.querySelector('.doc-sidebar');
    const sidebarButton = document.querySelector('.doc-mobile-toggle');
    const toc = document.querySelector('.toc');
    const search = document.querySelector('.doc-sidebar input');
    const functionSearch = document.getElementById('function-filter');
    const functionList = document.getElementById('function-list');
    const functionStatus = document.getElementById('function-status');
    let functions = [];
    let functionLoadFailed = false;

    function renderFunctions() {
        if (!functionList) return;
        const text = language();
        const query = functionSearch.value.trim().toUpperCase();
        const selected = functions.filter(fn => fn.name.includes(query));
        functionSearch.placeholder = text.functionSearch;
        functionSearch.setAttribute('aria-label', text.functionSearch);
        functionList.replaceChildren();
        selected.forEach(fn => {
            const card = document.createElement('article');
            card.className = 'function-card';
            const heading = document.createElement('h3');
            heading.textContent = fn.name;
            const parameters = document.createElement('p');
            const max = fn.maxArguments === 2147483647 ? '…' : fn.maxArguments;
            parameters.textContent = `${text.functionParameters}: ${fn.minArguments}–${max} · ${fn.parameterTypes}`;
            const returns = document.createElement('p');
            returns.textContent = `${text.functionReturns}: ${fn.resultType} · ${text.functionLazy}: ${fn.lazy ? text.yes : text.no}`;
            card.append(heading, parameters, returns);
            functionList.appendChild(card);
        });
        functionStatus.textContent = functionLoadFailed ? text.functionLoadError
            : `${text.functionResults}: ${selected.length} / ${functions.length}`;
    }

    function enhanceCode() {
        document.querySelectorAll('pre').forEach(pre => {
            if (pre.dataset.copyReady) return;
            pre.dataset.copyReady = 'true';
            let block = pre.parentElement;
            if (!block.classList.contains('code-block')) {
                block = document.createElement('div');
                block.className = 'code-block';
                pre.replaceWith(block);
                block.appendChild(pre);
            }
            const toolbar = document.createElement('div');
            toolbar.className = 'code-toolbar';
            const label = document.createElement('span');
            const raw = pre.textContent.trim();
            const code = pre.querySelector('code');
            if (code) {
                // 单次分词后创建节点，避免高亮替换再次匹配已生成的 HTML 或占位符。
                const source = code.textContent;
                const tokens = /("(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*')|(\/\/[^\n]*)|\b(import|package|public|private|class|interface|static|final|void|new|return|if|else|for|while|boolean|int|long|double|null|true|false)\b|\b(\d+(?:\.\d+)?)\b/g;
                const fragment = document.createDocumentFragment();
                let position = 0;
                let token;
                while ((token = tokens.exec(source))) {
                    fragment.appendChild(document.createTextNode(source.slice(position, token.index)));
                    const span = document.createElement('span');
                    span.className = token[1] ? 'ch-string' : token[2] ? 'ch-comment'
                        : token[3] ? 'ch-keyword' : 'ch-number';
                    span.textContent = token[0];
                    fragment.appendChild(span);
                    position = tokens.lastIndex;
                }
                fragment.appendChild(document.createTextNode(source.slice(position)));
                code.replaceChildren(fragment);
            }
            label.textContent = raw.startsWith('<') ? 'XML' : raw.startsWith('{') ? 'JSON' : 'CODE';
            const copy = document.createElement('button');
            copy.type = 'button';
            copy.textContent = 'Copy';
            copy.setAttribute('aria-label', 'Copy code');
            copy.addEventListener('click', async () => {
                try {
                    await navigator.clipboard.writeText(pre.textContent);
                    copy.textContent = 'Copied ✓';
                } catch (error) {
                    // 不支持剪贴板的环境仍可手动复制。
                    const range = document.createRange();
                    range.selectNodeContents(pre);
                    const selection = window.getSelection();
                    selection.removeAllRanges();
                    selection.addRange(range);
                    copy.textContent = 'Ctrl/Cmd+C';
                }
                setTimeout(() => { copy.textContent = 'Copy'; }, 1800);
            });
            toolbar.append(label, copy);
            block.insertBefore(toolbar, pre);
        });
    }

    function closeMenus() {
        menu.classList.remove('is-open');
        menuButton.setAttribute('aria-expanded', 'false');
        if (sidebar) {
            sidebar.classList.remove('is-open');
            sidebarButton.setAttribute('aria-expanded', 'false');
        }
    }
    menuButton.addEventListener('click', () => {
        const expanded = menuButton.getAttribute('aria-expanded') !== 'true';
        closeMenus();
        menu.classList.toggle('is-open', expanded);
        menuButton.setAttribute('aria-expanded', String(expanded));
    });
    if (sidebarButton) sidebarButton.addEventListener('click', () => {
        const expanded = sidebarButton.getAttribute('aria-expanded') !== 'true';
        closeMenus();
        sidebar.classList.toggle('is-open', expanded);
        sidebarButton.setAttribute('aria-expanded', String(expanded));
    });
    document.addEventListener('click', event => {
        if (event.target.closest('.site-links a, .doc-sidebar a')) closeMenus();
        if (!event.target.closest('.site-topbar, .doc-sidebar, .doc-mobile-toggle')) closeMenus();
    });
    document.addEventListener('keydown', event => {
        if (event.key !== 'Escape') return;
        if (menu.classList.contains('is-open')) menuButton.focus();
        if (sidebar && sidebar.classList.contains('is-open')) sidebarButton.focus();
        closeMenus();
        document.querySelectorAll('.dropdown-menu.show').forEach(item => item.classList.remove('show'));
        document.querySelectorAll('.control-btn').forEach(button => button.setAttribute('aria-expanded', 'false'));
    });

    function filterSections() {
        if (!toc) return;
        const query = search.value.trim().toLocaleLowerCase();
        let shown = 0;
        toc.querySelectorAll('a').forEach(link => {
            link.hidden = !link.textContent.toLocaleLowerCase().includes(query);
            if (!link.hidden) shown++;
        });
        document.querySelector('.toc-empty').hidden = shown > 0;
    }
    function refreshToc() {
        if (!toc) return;
        const headings = document.querySelectorAll('.doc-shell h2');
        toc.replaceChildren();
        headings.forEach((heading, index) => {
            if (!heading.id) heading.id = heading.dataset.i18n || 'section-' + index;
            const link = document.createElement('a');
            link.href = '#' + encodeURIComponent(heading.id);
            link.textContent = heading.textContent;
            toc.appendChild(link);
        });
        filterSections();
    }
    function translate() {
        const text = language();
        document.querySelectorAll('[data-site-i18n]').forEach(el => {
            el.textContent = text[el.dataset.siteI18n];
        });
        document.getElementById('langBtn').setAttribute('aria-label', text.lang);
        document.getElementById('themeBtn').setAttribute('aria-label', text.theme);
        menuButton.setAttribute('aria-label', text.menu);
        if (search) {
            search.placeholder = text.search;
            search.setAttribute('aria-label', text.search);
        }
        refreshToc();
        renderFunctions();
    }
    document.querySelectorAll('.site-links a, .doc-pages a').forEach(link => {
        const url = new URL(link.href);
        if (url.pathname === location.pathname && url.search === location.search) {
            link.setAttribute('aria-current', 'page');
        }
    });
    if (search) search.addEventListener('input', filterSections);
    if (functionSearch) {
        functionSearch.addEventListener('input', renderFunctions);
        fetch(functionList.dataset.source).then(response => {
            if (!response.ok) throw new Error('Catalog request failed');
            return response.json();
        }).then(catalog => {
            functions = catalog;
            renderFunctions();
        }).catch(() => {
            functionLoadFailed = true;
            renderFunctions();
        });
    }
    // Markdown 异步载入后通知目录更新；不观察目录本身，避免重复渲染循环。
    document.addEventListener('formula:document-loaded', () => { refreshToc(); enhanceCode(); });
    new MutationObserver(translate).observe(document.documentElement, {attributes: true, attributeFilter: ['lang']});
    translate();
    enhanceCode();
})();
