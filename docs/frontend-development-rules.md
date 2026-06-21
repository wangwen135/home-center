# 前端页面开发规则

## 目标

本项目的前端页面以 AI 辅助开发为主，规则目标是让页面实现保持简单、稳定、可复用。

前端默认使用原生 HTML、CSS、JavaScript，不引入 Vue、React、Angular、前端构建工具或复杂 SPA 架构。需要组件化和复用时，优先使用项目现有的公共 CSS、公共 JS、函数封装和轻量 DOM 片段。

## 技术栈原则

- 只使用浏览器原生能力：HTML、CSS、JavaScript、Fetch、DOM API。
- 不新增前端工程化构建步骤，不引入 npm、webpack、vite、babel、typescript 等前端构建链路。
- 不引入 Vue、React、Angular 等前端框架。
- 不随意添加外部 CDN。确需新增第三方库时，必须说明原因，并优先使用项目已有的本地静态资源。
- 页面行为优先复用 `window.HomeCenter`、`window.HomeCenterTheme`、`window.DomLite` 或已有公共函数。
- 新页面要能在 Spring Boot 静态资源路径下直接访问，不依赖额外前端服务。

## 目录与文件组织

- 静态页面放在 `src/main/resources/static`。
- 设备相关页面继续放在 `src/main/resources/static/device/...`，例如 PC 页面放在 `device/pc`。
- 公共样式放在 `src/main/resources/static/css`。
- 公共脚本放在 `src/main/resources/static/js`。
- 页面专属资源优先和页面放在相近目录，避免把单页面私有逻辑放进公共目录。
- 通用能力超过两个页面复用时，再提取到公共 CSS 或 JS。
- 不在 HTML 中写大量重复结构；重复卡片、列表项、表格行应抽成渲染函数。

## HTML 页面结构

新页面默认使用下面的基础结构：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>页面标题</title>
    <link rel="stylesheet" href="/css/app-theme.css">
    <script src="/js/app-theme.js"></script>
</head>
<body class="hc-app">
<main class="hc-shell hc-stack">
    <header class="hc-topbar">
        <div class="hc-title-block">
            <p class="hc-eyebrow">Module</p>
            <h1 class="hc-title">页面标题</h1>
            <p class="hc-subtitle">页面说明。</p>
        </div>
        <div class="hc-actions">
            <button class="hc-theme-toggle" type="button" aria-label="切换深浅色主题">
                <span class="hc-theme-toggle-icon" aria-hidden="true"></span>
                <span data-theme-toggle-text>深色</span>
            </button>
        </div>
    </header>
</main>
</body>
</html>
```

页面结构规则：

- 必须声明 `<!DOCTYPE html>`、`lang="zh-CN"`、`UTF-8` 和 `viewport`。
- 页面主体默认使用 `body.hc-app`。
- 管理类、设备类页面优先使用 `main.hc-shell.hc-stack`。
- 顶部区域优先使用 `hc-topbar`、`hc-title-block`、`hc-actions`。
- 可交互元素使用语义化标签：按钮用 `button`，跳转用 `a`，表单用 `form`。
- 动态更新区域需要根据场景添加 `aria-live="polite"`。
- 只在确有必要时使用内联 `style`；可复用样式应写成 class。

## CSS 规则

- 优先复用 `/css/app-theme.css` 中已有的 CSS 变量和 `.hc-*` 类。
- 颜色、边框、阴影、背景、状态色优先使用项目变量，例如：
  - `--hc-bg`
  - `--hc-surface`
  - `--hc-surface-strong`
  - `--hc-text`
  - `--hc-muted`
  - `--hc-line`
  - `--hc-accent`
  - `--hc-danger`
  - `--hc-success`
  - `--hc-warning`
- 常用容器和控件优先使用：
  - `.hc-shell`
  - `.hc-stack`
  - `.hc-topbar`
  - `.hc-panel`
  - `.hc-panel-pad`
  - `.hc-actions`
  - `.hc-button`
  - `.hc-button-primary`
  - `.hc-theme-toggle`
  - `.hc-muted`
- 页面私有样式可以写在当前 HTML 的 `<style>` 中；当样式明显可复用或页面变大时，再提取到页面专属 CSS 文件。
- 页面私有 class 使用清晰业务前缀，避免和公共 `.hc-*` 组件类冲突。
- 不覆盖公共 `.hc-*` 类的基础行为；需要变化时添加页面私有 class 组合使用。
- 布局优先使用 Flex 和 Grid，避免浮动布局。
- 移动端必须可用，容器要设置合理的 `min-width`、`minmax`、`flex-wrap` 或媒体查询。
- 文本要能换行或截断，不能在窄屏下撑破布局。

## JavaScript 规则

页面脚本默认使用 IIFE 包裹，避免污染全局作用域：

```html
<script>
    (function () {
        var state = {
            rows: [],
            loading: false
        };

        function init() {
            loadData();
        }

        function loadData() {
            state.loading = true;
            renderLoading();

            window.HomeCenter.get('/api/example')
                .then(function (result) {
                    state.rows = result.data || [];
                    render();
                })
                .catch(function (error) {
                    renderError(error);
                    window.HomeCenter.toast(error.message || '加载失败', 'error');
                })
                .finally(function () {
                    state.loading = false;
                });
        }

        init();
    })();
</script>
```

通用规则：

- 页面代码使用 `var` 和普通函数，保持和现有代码风格一致。
- 不创建新的全局变量。确需新增项目级全局能力时，只能挂到明确命名空间，例如 `window.HomeCenter`。
- 接口请求优先使用：
  - `window.HomeCenter.get(url)`
  - `window.HomeCenter.post(url, body)`
  - `window.HomeCenter.put(url, body)`
  - `window.HomeCenter.del(url)`
- 用户提示优先使用 `window.HomeCenter.toast(message, type)`。
- 确认操作优先使用 `window.HomeCenter.confirm(message, options)`。
- 表单序列化优先使用 `window.HomeCenter.serializeForm(form)`。
- 表格渲染优先使用 `window.HomeCenter.renderTable(tbody, rows, columns, emptyText)`。
- HTML 转义必须使用 `window.HomeCenter.escapeHtml(value)`。
- 可以使用原生 DOM API；只有当前页面已经加载 `dom-lite.js` 时，才使用 `$` 或 `DomLite`。
- 事件绑定优先使用事件委托，尤其是列表、表格、卡片中的动态按钮。

## 轻量组件化规则

本项目不使用框架组件。组件化通过函数实现，常见方式有两种：

1. 函数返回 HTML 字符串。
2. 函数创建并返回 DOM 节点。

示例：

```javascript
function renderDeviceCard(device) {
    return '<article class="hc-panel device-card" data-device-id="' + window.HomeCenter.escapeHtml(device.id) + '">' +
        '<h2>' + window.HomeCenter.escapeHtml(device.name || '未命名设备') + '</h2>' +
        '<p class="hc-muted">' + window.HomeCenter.escapeHtml(device.description || '暂无描述') + '</p>' +
        '</article>';
}
```

组件化规则：

- 组件函数只接收数据并输出视图，不在组件函数里发请求。
- 组件函数不直接读取全局状态，所需数据通过参数传入。
- 组件函数内部必须处理空值兜底。
- 多处复用的组件函数应提取到页面级公共函数；超过两个页面复用时，再提取到公共 JS。
- 组件 HTML 中的动态文本必须转义。
- 组件内部不要绑定事件；事件统一在页面初始化或父容器上绑定。

## 接口与数据渲染

- 接口调用统一通过 `window.HomeCenter` 请求方法。
- 页面中不要重复封装 `fetch`，除非需要处理下载、上传进度或特殊响应类型。
- 接口返回数据进入 HTML 前必须转义。
- 只有确认安全、来源可信、并且明确需要渲染 HTML 片段时，才能使用未转义的 `innerHTML`。
- 普通文本优先使用 `textContent`。
- 表格数据优先使用 `HomeCenter.renderTable`；如果需要复杂操作列，列渲染函数中仍要对动态值做转义。
- 请求失败时必须给用户明确反馈，不能只写入控制台。
- 删除、关机、重启、覆盖保存等危险操作必须有确认步骤。

## 交互状态与错误处理

每个有接口请求的页面至少考虑这些状态：

- 加载中：显示“加载中...”或骨架区域。
- 空数据：显示明确的空状态文案。
- 成功：正常渲染数据，并在必要时 toast 提示。
- 错误：显示失败文案，并通过 toast 或页面状态提示用户。
- 提交中：禁用相关按钮，避免重复提交。
- 完成后：恢复按钮状态，刷新必要数据。

按钮和表单规则：

- 提交期间必须禁用触发按钮。
- 批量操作期间必须禁用相关批量按钮。
- `finally` 中恢复按钮状态，避免异常后按钮一直不可用。
- 表单提交前做必要的前端校验，但最终以后端校验为准。
- 错误文案要能指导用户下一步操作，避免只显示“失败”。

## 复用与提取标准

满足以下任一条件时，应考虑提取公共能力：

- 同一段 JS 逻辑在两个以上页面出现。
- 同一套卡片、表格、筛选栏、弹窗结构在两个以上页面出现。
- 同一类接口错误处理、按钮 loading、状态渲染反复出现。
- 同一套 CSS 组件样式在两个以上页面出现。

提取规则：

- 通用 JS 放入 `src/main/resources/static/js`。
- 通用 CSS 放入 `src/main/resources/static/css`。
- 项目级能力优先扩展 `app-theme.js` 或新增明确命名的公共 JS 文件。
- 项目级样式优先扩展 `app-theme.css` 或新增明确命名的公共 CSS 文件。
- 不把页面私有逻辑提前抽象成复杂框架。
- 不为了“看起来组件化”引入额外层级；只有真实复用或降低复杂度时才提取。

## AI 开发检查清单

AI 新增或修改前端页面前，必须检查：

- 是否先查看了现有相似页面。
- 是否复用了 `/css/app-theme.css`。
- 是否复用了 `window.HomeCenter` 的请求、提示、确认、转义等能力。
- 是否避免引入 Vue、React、Angular、npm、构建工具或外部 CDN。
- 是否使用 `body.hc-app`、`hc-shell`、`hc-topbar`、`hc-panel` 等项目级结构。
- 是否处理了加载、空数据、成功、错误、提交中状态。
- 是否对所有接口返回的动态内容做了 HTML 转义。
- 是否对危险操作增加了确认。
- 是否避免新增无意义的全局变量。
- 是否避免复制大段重复 HTML、CSS 或 JS。
- 是否保持移动端可用。
- 是否保持页面可直接通过 Spring Boot 静态资源访问。

完成后必须检查：

- 页面在浏览器中没有明显布局溢出、遮挡、重叠。
- 控制台没有关键 JavaScript 错误。
- 主要交互按钮可以正常点击。
- 接口失败时用户能看到错误提示。
- 文档、样式和脚本路径使用项目内的绝对静态路径，例如 `/css/app-theme.css`、`/js/app-theme.js`。
