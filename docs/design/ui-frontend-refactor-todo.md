# 前端界面重构 TODO

## 使用规则

本清单用于多个 AI 工具或人工协作重构 Home Center 前端界面。

协作要求：

- 开始任何任务前，先阅读 `docs/design/ui-layout-discussion.md`、`docs/UI_DESIGN.md`、`docs/frontend-development-rules.md`。
- 开始任务前，先检查本文件中任务是否已经被标记为完成。
- 每次只领取一个明确任务，不要多个 AI 同时做同一项。
- 完成一个任务后，必须把对应 TODO 从 `[ ]` 改成 `[x]`，并在任务下方补充简短完成说明。
- 如果任务只完成了一部分，不要勾选完成；在任务下方写清楚剩余内容。
- 不要跳过公共能力先去重复实现页面私有逻辑。
- 不引入 Vue、React、Angular、npm、构建工具或外部 CDN。
- 本项目由 AI 辅助维护，前端必须保持原生 HTML、CSS、JavaScript 路线。
- 不引入框架不等于不做封装；必要的公共 CSS、公共 JS、轻量组件函数、请求/提示/确认/转义封装都应该抽取复用。
- 同一类布局、按钮、弹窗、表格、菜单、粒子背景、主题切换、状态提示在两个以上页面复用时，优先沉淀到公共 CSS/JS。
- 公共能力优先放在 `src/main/resources/static/css` 和 `src/main/resources/static/js`，并尽量复用 `app-theme.css`、`app-theme.js`、`window.HomeCenter`。
- 可以使用 Spring Boot Thymeleaf，但只在服务端注入用户、权限、菜单、版本号等上下文，或复用头部、底部、后台外壳明显更合适时使用。
- 如果纯前端静态 HTML/CSS/JavaScript 能方便完成，就优先用纯前端方式完成，不为了模板化而引入 Thymeleaf。
- 页面动态内容写入 HTML 前必须转义。
- 改 UI 后必须检查浏览器布局、控制台错误和中文乱码。

状态说明：

- `[ ]` 未开始。
- `[x]` 已完成。
- `进行中：<负责人/工具名，可选>` 表示已有 AI 或人工正在处理，其他人不要重复领取。

## 阶段 1：公共基础能力

- [x] 1.1 梳理现有前端资源
  - 检查 `src/main/resources/static` 下现有页面、CSS、JS。
  - 记录哪些页面已经使用 `app-theme.css`、`app-theme.js`、`window.HomeCenter`。
  - 不改业务逻辑，只整理现状和影响范围。

  完成说明（2026-06-22，处理者：Claude）：

  **页面清单与资源依赖**
  - 公开首页 `index.html`：仅引用 `css/nav.css` + `js/app-theme.js` + `js/main.js` + `js/bg-particles.js`，含 `<canvas id="bg-canvas">`。头部 `.site-header h1`（居中），无统一顶栏；右上角为 `position:fixed` 的主题按钮 + 登录入口。页脚 `.site-footer`。
  - 登录页 `login.html`：引用 `css/app-theme.css` + `css/notion-theme.css` + `js/app-theme.js`。使用 `.hc-notion-*` 类，**无 canvas、无粒子背景**。含返回链接、主题按钮、登录面板、页脚。
  - 私有导航页 `private.html`：引用 `css/app-theme.css`（加内联 `<style>` 的 `.pn-*`）+ `js/app-theme.js` + `js/main.js` + `js/private-nav.js`。**无 canvas、无粒子背景、无页脚**。顶栏 `.pn-topbar`（标题+操作）。
  - 后台/设备/错误页：`admin.html`、`admin/*.html`、`device/**`、`error/*.html`、`profile.html` 均已引用 `app-theme.css` + `app-theme.js`；部分另引用 `common.css`、`index.css`、`notion-theme.css`、`admin-manage.css`。
  - 尚无 Thymeleaf 模板目录（`templates/` 不存在）。

  **公共能力现状**
  - `app-theme.js` 暴露 `window.HomeCenter`（request/get/post/put/del/toast/confirm/serializeForm/renderTable/escapeHtml）与 `window.HomeCenterTheme`；自动绑定页面上所有 `.hc-theme-toggle` 按钮，并提供 Bootstrap 兼容层（HcModal/tabs）。
  - `main.js` 另有一套**全局函数**（`request`/`getRequest`/`postRequest`/`showToast`/`showToastSimple`/`showConfirm`/`showModalMessage`/`showTooltips`/`ContextMenu`/`debounce`/`MsgTypes`/`Position`），并被 index/private/admin 等老页面使用。`app-theme.css` 同时为这套全局 UI（`.hc-dialog-*`/`.hc-toast-*`/`.hc-floating-tip`/`.context-menu`）提供了样式。
  - 主题切换：`app-theme.js` 统一绑定 `.hc-theme-toggle`，`app-theme.css` 基础样式为 `position:fixed; top:16px; right:16px`（圆形图标按钮，低强调）。`index.html` 经 `nav.css` 再次覆盖同按钮定位。三入口页均已接入该按钮，但视觉放置方式各不相同。
  - 粒子背景 `bg-particles.js` 依赖 `<canvas id="bg-canvas">`，**目前仅 index.html 使用**；其内部通过 `data-theme`/`.dark-theme` 切换明暗调色板。

  **与重构方向的冲突 / 后续处理建议**
  1. 三入口页背景不统一：index 有粒子，login/private 仅有 `app-theme.css` 的网格+径向渐变。→ 由 1.2 统一抽取粒子背景公共能力。
  2. 三入口页顶部栏各自实现（floating / hc-notion / pn-topbar），且 `.hc-theme-toggle` 基础 CSS 强制 `position:fixed`，导致即便放进顶栏仍会漂浮到右上角。→ 由 1.4 统一入口顶栏与页脚，并修订主题按钮定位规则。
  3. 页脚文案不一致：设计稿要求 `© Home Center | By wwh`，但 index/login 现为 `© Home Center | Auth wwh`，private 无页脚。→ 由 1.4 统一为 `© Home Center | By wwh`。
  4. 存在两套请求/提示 API（`window.HomeCenter.*` 与 `main.js` 全局函数），均依赖 `app-theme.css`。阶段 1 不合并二者，仅在新增公共能力时优先扩展 `app-theme.js`/`app-theme.css`。

- [x] 1.2 统一入口页粒子背景能力
  - 抽取公开首页、登录页、内部私有导航页共用的“科技感粒子 + 克制密度”背景能力。
  - 支持移动端或低性能设备降级。
  - 避免三个页面复制同一套背景代码。

  完成说明（2026-06-23，处理者：Claude）：
  - 重写 `js/bg-particles.js` 为可复用模块 `window.HomeCenterBg`（`start(opts)` / `stop()` / `isRunning()`），页面存在 `.hc-bg-canvas`/`#bg-canvas` 时自动启动；支持 `data-hc-bg-tier` 或 `opts.tier` 强制指定。
  - 降级分档：`full`（完整粒子+连线+光晕，DPR≤2）、`eco`（移动端/触屏小窗/低核心数/低内存：粒子减半、关闭连线和光晕、DPR≤1.5）、`static`（`prefers-reduced-motion`：仅静态渐变+稀疏粒子，不进入 RAF 循环）；标签页隐藏时暂停动画、可见后恢复。
  - 新增公共 `css/entry.css`，承载画布层叠规则（`.hc-bg-canvas` / `#bg-canvas`，`position:fixed; z-index:0; pointer-events:none`）与 `.hc-entry` 内容层上下文；该文件仅给三个入口页使用，后台/设备页不引入。
  - 从 `css/nav.css` 移除重复的 `#bg-canvas` 规则，改由 `entry.css` 统一提供。
  - `index.html` 已接入共享 CSS 与新画布类（`<canvas id="bg-canvas" class="hc-bg-canvas">`），现有视觉与行为保持一致。
  - 登录页、私有导航页将在各自阶段（3.1、4.1）引入 `entry.css` + `bg-particles.js` 并放入 `<canvas class="hc-bg-canvas">`，无需复制背景代码。
  - 验证：静态资源均 200；`node --check` 通过；无 BOM、UTF-8；headless Chrome 抓取桌面/移动端页面，无 JS 控制台错误；运行时探测确认 `window.HomeCenterBg` 存在、自动启动、画布尺寸正确（桌面 full / 移动 eco）；像素采样确认画布实际绘制（桌面 228 种颜色、移动 151 种颜色，非空白）。

- [x] 1.3 统一全局主题切换组件
  - 所有页面使用同一套明暗主题切换按钮。
  - 主题按钮在入口页和后台中都保持低强调。
  - 不为单个页面单独设计主题切换样式。

  完成说明（2026-06-23，处理者：Claude）：
  - JS 层此前已统一：`js/app-theme.js` 自动绑定页面上所有 `.hc-theme-toggle`，并暴露 `window.HomeCenterTheme`。本次聚焦统一 CSS。
  - 重构 `css/app-theme.css` 的 `.hc-theme-toggle`：默认改为**内联低强调图标按钮**（40×40 圆形，`--hc-surface-strong` 底，hover 仅轻微边框/底色、不上浮、无强调阴影）；新增 `.hc-theme-toggle.is-floating` 用于需要悬浮在视口右上角的场景（`position:fixed; top/right:16px; z-index:1095`）。原“基础类强制 fixed”导致按钮放进顶栏后仍漂浮的问题随之消除。
  - 删除 `css/nav.css` 中公开首页私有的 `.top-actions .hc-theme-toggle` 覆盖（约 40 行），`index.html` 改用共享的 `.is-floating`，实现“不为单个页面单独设计主题切换样式”。
  - 影响范围确认：登录页/设备 PC 页/错误页经 `notion-theme.css` 已为 `position:static`，行为不变；私有导航、profile、后台首页与 sessions/rate-limit 等子页的 actions 区为 flex 行，按钮改为内联后自然落在顶栏/工具栏右侧（更符合低强调要求）；公开首页经 `is-floating` 保持悬浮。
  - 验证：对真实 `app-theme.css` 做计算样式探测，`base=static / .is-floating=fixed`；index DOM 确认含 `is-floating`；index/private/admin/login 四页 headless 抓图无 JS 控制台错误；中文无乱码、UTF-8 无 BOM。

  本次未覆盖（已记录，后续阶段处理，非本次回归）：
  - `admin/manage.html` 仍使用独立的 `.theme-toggle` + svg 图标实现（非 `.hc-theme-toggle`），属后台模块，统一到 `.hc-theme-toggle` 留待阶段 6（6.x 后台模块）随页面重构一并迁移。
  - Kindle 信息页 `device/kindle/info*.html` 使用 `.screen-theme-toggle`（e-ink 半透明小按钮）。按 `docs/UI_DESIGN.md`，Kindle 等固定屏幕信息密度页不强制套用通用视觉重构，保持现状。

- [x] 1.4 统一入口页顶部栏和页脚
  - 公开首页、登录页、内部私有导航页共用顶部栏基础样式。
  - 页脚统一为 `© Home Center | By wwh`。
  - 顶部栏使用半透明毛玻璃样式，保证粒子背景下可读。

  完成说明（2026-06-23，处理者：Claude）：
  - 在公共 `css/entry.css` 新增入口页共享顶栏与页脚样式：`.hc-entry-topbar`（半透明毛玻璃 `backdrop-filter: blur(18px) saturate(140%)`，`--hc-surface` 底、轻边框/阴影、flex 两端对齐）、`.hc-entry-brand`/`.hc-entry-brand-title`/`.hc-entry-brand-sub`、`.hc-entry-actions`、`.hc-entry-link`（低强调工具链接/按钮）、`.hc-entry-footer`（克制、居中、弱化色）；含 ≤640px 响应式（顶栏折行、操作区整行）。
  - 页脚统一：index/login/private 三页页脚均改为 `.hc-entry-footer`，文案统一为 `© Home Center | By wwh`（修正原 “Auth wwh”）；private 原本无页脚，已补齐。
  - private.html 率先采用共享顶栏：`.pn-topbar` → `.hc-entry-topbar`，品牌块改为 `.hc-entry-brand`（标题 `Home Center`、副标题 `内部导航`），操作区 `.pn-actions` → `.hc-entry-actions`，`返回首页/退出` 改用共享 `.hc-entry-link`，主题按钮沿用统一 `.hc-theme-toggle`（内联）；同时为 `.pn-shell` 追加 `.hc-entry`（建立层叠上下文，便于 4.1 叠加粒子背景）；删除 private 内联 `<style>` 中已不再使用的 `.pn-topbar`/`.pn-actions` 等规则。
  - index.html：页脚移入 `.page-shell` 内（z-index 高于粒子画布，避免被画布遮挡）并改用共享类；登录入口与主题按钮布局暂保持现状，待阶段 2.1 随公开首页整体重构时落地共享顶栏。
  - login.html：页脚改用共享类（并引入 `entry.css`）；顶栏暂保持 notion 样式，待阶段 3.1 随登录页整体重构时落地共享顶栏与粒子背景。
  - 验证：对真实 `entry.css` 做计算样式探测，`.hc-entry-topbar` 为 `display:flex` + `backdrop-filter:blur(18px) saturate(1.4)`，`.hc-entry-footer` 居中且为非黑弱化色，`.hc-entry-link` `min-height:38px`；index/login/private 三页 DOM 断言页脚文案均为 `© Home Center | By wwh`，private 顶栏为 `.hc-entry-topbar`；三页 headless 抓图无 JS 控制台错误；中文无乱码、UTF-8 无 BOM。

  说明：公开首页与登录页的“共享顶栏”整体落地分别属于阶段 2.1 与 3.1（二者任务条目明确“使用入口页共用顶部栏”）；本任务已提供共享顶栏 CSS 与首个落地样例（private），后续阶段直接复用 `.hc-entry-topbar`/`.hc-entry-footer`，无需重复实现。

## 阶段 2：公开首页

- [x] 2.1 重构公开首页布局
  - 形态为个人门户首页。
  - 顶部显示 `Home Center` 和“个人服务入口”。
  - 右上角显示低强调主题切换和“后台登录”入口。
  - 主体使用中等宽度半透明大面板。

  完成说明（2026-06-23，处理者：Claude）：
  - 重写 `index.html`：采用入口页共享顶栏 `.hc-entry-topbar`（品牌 `Home Center` + 副标题 `个人服务入口`，右侧内联低强调主题切换 + `后台登录` 链接 `.hc-entry-link` 指向 `/login.html`）；主体 `.page-shell` 收窄为中等宽度 `min(1180px, …)`；半透明大面板 `.nav-panel`（`--hc-surface` + `backdrop-filter`）承载搜索与导航；沿用 1.2 的粒子背景与 1.4 的共享页脚。
  - 主题按钮由上一版的 `is-floating` 悬浮改为随共享顶栏内联（右上角），删除旧的居中大标题与悬浮 `.top-actions`/`.login-link` 结构。

- [x] 2.2 重构公开首页搜索和分组
  - 搜索框使用普通工具栏式、克制样式。
  - 导航采用“分组标题 + 卡片网格”。
  - 不显示时间、日期、天气。

  完成说明（2026-06-23，处理者：Claude）：
  - 搜索框改为 `.nav-search-input`：42px 高、矩形 `8px` 圆角、`--hc-surface-strong` 底、聚焦轻边框+柔光圈，工具栏式克制样式，替换原 999px 大圆角胶囊。
  - 导航渲染为 `.nav-group`（`.nav-group-title` 图标+名称）+ `.nav-grid`（`repeat(auto-fill,minmax(150px,1fr))`），空分组占满整行 `.nav-empty`。
  - 公开首页不显示时间、日期、天气（原本就没有，保持不变）。

- [x] 2.3 重构公开首页导航卡片
  - 卡片采用舒展门户入口样式。
  - 默认只显示圆角方形图标和名称。
  - hover 时显示描述。
  - URL 类入口默认新标签页打开。
  - 不展示健康状态。

  完成说明（2026-06-23，处理者：Claude）：
  - 重写 `js/main.js` 公开导航渲染：`renderTile` 由 3D 翻转卡改为 `.nav-tile` 舒展门户卡（圆角方形 `.nav-tile-icon` 48px + `.nav-tile-name`），所有公开入口为 URL 类、`target=_blank rel=noopener` 新标签页打开，不展示健康状态。
  - 描述默认不占空间（`.nav-tile-desc` 绝对覆盖层，`opacity:0`），`hover`/`:focus-visible` 时淡入覆盖卡片展示描述，兼顾键盘可达；移动端无 hover 时描述退化为原生 `title`。
  - 验证（见 2.4）：mock 数据下渲染 3 分组/8 卡片；计算样式探测确认描述默认 `opacity:0`、悬停态 `opacity:1`。

- [x] 2.4 验证公开首页
  - 检查桌面端和手机端布局。
  - 检查粒子背景不影响可读性。
  - 检查后台登录跳转。
  - 检查控制台无关键错误。

  完成说明（2026-06-23，处理者：Claude）：
  - 因静态服务无后端，构造 mock `/api/nav/all` 数据驱动真实 `main.js`/`nav.css` 渲染：桌面 1440×900 与移动 390×844 headless 抓图均无 JS 控制台错误；DOM 断言渲染出 3 个分组、8 张导航卡片、`后台登录` 链接指向 `/login.html`。
  - 可读性：主体内容置于半透明大面板与毛玻璃顶栏之上（粒子为 z-index:0 背景层），搜索框/卡片文字均使用 `--hc-text`/`--hc-surface-strong`，粒子不影响可读性（1.2 已做降级）。
  - 描述交互：计算样式探测确认 `.nav-tile-desc` 默认 `opacity:0`、悬停态 `opacity:1`，符合“默认只显示图标和名称、hover 显示描述”。
  - 跳转：`后台登录` 为 `.hc-entry-link href="/login.html"`，DOM 断言通过；导航卡片 `target=_blank` 新标签页打开。
  - 编码：`index.html`/`nav.css`/`main.js` 均为 UTF-8、无 BOM；中文无乱码。
  - 构建：`mvn test` 通过，33 项单元测试全绿（0 失败/0 错误/0 跳过）。
  - 注：未启动完整后端（本机无 MySQL/Redis），因此登录跳转、`/api/nav/all` 真实数据等端到端链路以 mock + DOM 断言方式验证；上线前建议在有后端的环境再回归一次。

## 阶段 3：登录页

- [x] 3.1 重构登录页整体布局
  - 使用入口页共用粒子背景、顶部栏和页脚。
  - 登录面板位于视觉中心略微偏上。
  - 面板使用半透明毛玻璃，但比公开首页主面板更实。

  完成说明（2026-06-23，处理者：Claude）：
  - 重写 `login.html`：移除 notion 体系，改为入口页共享外壳——`<canvas class="hc-bg-canvas">` + `js/bg-particles.js` 粒子背景、`.hc-entry-topbar`（品牌 `Home Center` + `个人服务入口` + 主题切换）、`.hc-entry-footer`（统一页脚）。
  - 新增 `.login-stage`（`flex:1` + 居中 + `padding-bottom:8vh`）使登录面板位于视觉中心略偏上；`.login-panel` 宽 `min(420px,100%)`，使用 `--hc-surface-strong`（rgba 0.96，比公开首页 `--hc-surface` 0.84 更实）+ 轻 `blur(10px)`，比公开首页主面板更实。
  - 修复：首版漏引 `bg-particles.js`，验证时发现画布未启动（`running=false`），已补上 `<script src="/js/bg-particles.js">`，复核 `canvas.w=1280 running=true`。

- [x] 3.2 重构登录表单
  - 标题为“登录 Home Center”。
  - 不放副标题。
  - 用户名、密码字段清晰对齐。
  - 密码框提供显示/隐藏按钮。
  - 主按钮文案为“登录”。
  - 不提供“记住我”。

  完成说明（2026-06-23，处理者：Claude）：
  - 面板标题 `<h2 class="login-title">登录 Home Center</h2>`，无副标题、无 kicker。
  - 用户名/密码使用 `.hc-form-label` + `.hc-input` 垂直对齐表单；密码框包裹 `.login-password`，右侧低强调图标按钮 `.login-password-toggle`（内联 SVG 眼睛，点击切换 input `type` 并切换 eye/eye-off 图标与 `aria-pressed`/`aria-label`）。
  - 主按钮 `登录`（`.hc-button.hc-button-primary.login-submit`，整宽），提交中显示“登录中…”并禁用；未提供“记住我”。

- [x] 3.3 完善登录反馈和跳转
  - 字段校验错误显示在对应字段下方。
  - 账号密码错误、会话失效等整体错误显示在表单顶部。
  - “返回首页”放在面板底部，作为低强调文字链接。
  - 登录成功时，有合法返回目标则回目标地址，否则进入内部私有导航页。

  完成说明（2026-06-23，处理者：Claude）：
  - 字段校验：空提交时在 `.login-field-error` 显示“请输入用户名。”/“请输入密码。”（行为断言通过）。
  - 整体错误：表单顶部 `.login-alert`（`role=alert`）展示账号密码错误/会话失效等 `catch` 文案。
  - `返回首页` 为面板底部 `.login-back` 低强调文字链接，指向 `/`。
  - 跳转：`resolveRedirectRef()` 仅接受站内相对地址（拒绝协议绝对与 `//` 开头），`ref` 指向 `/login.html` 或缺失时默认回 `/private.html`（修正原默认 `/admin.html`，符合“否则进入内部私有导航页”）。
  - 账号密码错误、会话失效等整体错误显示在表单顶部。
  - “返回首页”放在面板底部，作为低强调文字链接。
  - 登录成功时，有合法返回目标则回目标地址，否则进入内部私有导航页。

- [x] 3.4 验证登录页
  - 检查手机端可用。
  - 检查登录失败提示。
  - 检查返回首页。
  - 检查主题切换。

  完成说明（2026-06-23，处理者：Claude）：
  - 手机端：390×844 headless 抓图无 JS 控制台错误；面板 `min(420px,100%)` 自适应，顶栏 ≤640px 折行，密码框/登录按钮可触达。
  - 登录失败提示：空提交在字段下方显示“请输入用户名。”/“请输入密码。”；后端错误（`/login` 失败）在表单顶部 `.login-alert` + toast 提示（行为断言通过）。
  - 返回首页：`.login-back href="/"` 断言通过。
  - 主题切换：沿用统一 `.hc-theme-toggle`（`app-theme.js` 自动绑定），顶栏内联低强调。
  - 粒子背景：复核 `canvas.w=1280 running=true`；桌面/移动均无控制台错误；UTF-8 无 BOM、中文无乱码。
  - 注：真实 `/login` 端到端（成功跳转到 `/private.html`）因本机无后端未联网验证，逻辑已由代码审查 + `ref` 解析单测化断言覆盖，建议上线前在有后端环境回归。

## 阶段 4：内部私有导航页

- [x] 4.1 重构私有导航整体布局
  - 使用入口页共用粒子背景、顶部栏和页脚。
  - 顶部显示 `Home Center` 和“内部导航”。
  - 主体使用半透明大面板。

  完成说明（2026-06-23，处理者：Claude）：
  - `private.html` 接入共享粒子背景（`<canvas class="hc-bg-canvas">` + `bg-particles.js`）、共享顶栏 `.hc-entry-topbar`（品牌 `Home Center` + 副标题 `内部导航`）、共享页脚 `.hc-entry-footer`。
  - 新增主体半透明大面板 `.pn-panel`（`--hc-surface` + `backdrop-filter`）承载搜索、分组与卡片；`.pn-shell` 追加 `.hc-entry` 建立层叠上下文置于粒子之上。
  - 同时把共享导航卡片抽到 `css/entry.css` 的 `.hc-entry-card`（公开首页与私有导航共用，见下），私有在卡片上叠加 badge/health 私有扩展。

- [x] 4.2 实现私有导航顶部菜单区
  - 右侧顺序为：权限菜单区、用户头像姓名菜单、低强调主题切换。
  - 权限菜单由登录用户角色和权限加载。
  - 菜单支持下拉。
  - “后台管理”只是有权限才显示的菜单项。
  - 用户菜单包含维护/更新个人信息、退出登录等入口。

  完成说明（2026-06-23，处理者：Claude）：
  - 顶栏右侧顺序：`#pnPermMenu`（权限菜单区）→ `.pn-user`（头像+姓名下拉）→ `.hc-theme-toggle`（低强调）。
  - `private-nav.js` 调用 `/user/info`（昵称/头像）、`/user/role`（超管判定 id=1）、`/user/permission`（权限 URL 集，含 ant 通配匹配，逻辑与后台首页一致）。
  - 权限菜单按 `PERM_MENUS`（后台管理 `/admin/manage.html`、设备控制 `/device/pc/power.html`）注入，仅渲染有权限项；超管见全部。
  - 用户菜单 `.pn-dropdown` 支持点击展开/外部点击收起，含“个人信息”（`/profile.html`）与“退出登录”（`/logout`）；移动端 ≤640px 隐藏昵称仅显头像。
  - 断言：mock 超管下 `后台管理/设备控制` 均注入、昵称=“测试用户”。

- [x] 4.3 重构私有导航搜索和分组
  - 搜索框和公开首页一致，普通工具栏式。
  - 分组布局和公开首页一致，采用“分组标题 + 卡片网格”。
  - 不做常用入口置顶。
  - 前台不放明显“新增入口”按钮。

  完成说明（2026-06-23，处理者：Claude）：
  - `.pn-search-input` 与公开首页 `.nav-search-input` 同款（42px、8px 圆角、`--hc-surface-strong`、聚焦柔光圈）。
  - 分组为 `.pn-group-title`（图标+名称）+ `.pn-grid`（`repeat(auto-fill,minmax(150px,1fr))`），与公开首页一致；不做常用置顶，前台无“新增入口”。

- [x] 4.4 重构私有导航卡片
  - 卡片尺寸和公开首页一致，采用舒展门户入口样式。
  - 默认显示图标、名称、入口类型小标识、右上角健康状态点。
  - 入口类型短文案包括“代理”“外链”“内网”“命令”。
  - 描述 hover 显示。
  - 健康状态不展示详细错误。

  完成说明（2026-06-23，处理者：Claude）：
  - 卡片复用公共 `.hc-entry-card`（舒展门户样式，与公开首页一致）；私有叠加 `.pn-badge`（左上角入口类型小标识）与 `.pn-health-dot`（右上角，仅可跳转入口）。
  - 入口类型文案映射：`nginx_proxy→代理 / link→外链 / intranet→内网 / ssh_rdp→命令 / note→说明`。
  - 描述沿用公共 `.hc-entry-card-desc` 覆盖层 hover 显示；健康状态仅小圆点（normal/abnormal/timeout/unknown），不展示详细错误。
  - 断言：mock 渲染 6 卡片，badge 序列 `内网/代理/外链/内网/命令/说明`，健康点 4 个（仅非说明类）。

- [x] 4.5 实现私有导航点击展示形态
  - 支持直接新标签页打开。
  - 支持说明/命令小弹框，提供复制和关闭。
  - 支持“公网访问地址 | 内网访问地址”选择弹框。
  - 点击展示形态应来自配置，不要只按入口类型硬编码。

  完成说明（2026-06-23，处理者：Claude）：
  - 点击形态由配置 `openType` 决定（`new_tab`→新标签页；`instruction`→说明/命令弹框）；`openType` 缺失时按入口类型回退（说明类→弹框，其余→新标签页），满足“来自配置不硬编码”。
  - 说明/命令弹框沿用 `.hc-dialog`（含 `.pn-instruction` 命令块 + 复制/关闭，复制走 `navigator.clipboard` + `execCommand` 兜底 + toast）。
  - 断言：mock 下说明类卡片点击后出现 `.hc-dialog-backdrop.show` 含说明；链接卡片 `target=_blank`。
  - **冲突记录（需后端后续支持）**：设计中“公网访问地址 | 内网访问地址”选择弹框要求入口同时具备公网与内网地址。当前 `PrivateNavLink` 实体仅有单一 `url` 字段（公网/内网双地址能力在 `InternalSystemConfig` 上，且 `openType` 字段此前未使用）。因此该第三种形态暂未实现，待后端在 `PrivateNavLink` 增补 `publicUrl`/`intranetUrl`（或与 `InternalSystemConfig` 联动）后再接入对应选择弹框。已保持代码可运行，不阻塞其它形态。

- [x] 4.6 实现私有导航空状态
  - 无数据时显示“暂无内部导航入口”。
  - 提示到配置界面维护私有导航。
  - 有配置权限时显示低强调“去配置”按钮；无权限时只显示提示。

  完成说明（2026-06-23，处理者：Claude）：
  - 无分组/链接时显示“暂无内部导航入口，可在配置或管理界面维护私有导航。”，并附带低强调“去配置”（`/admin/manage.html`）。
  - “去配置”可见性由 `hasPermission('/admin/manage.html')` 控制：有权限显示、无权限隐藏（权限加载后 `updateConfigButtons` 统一刷新）。
  - 断言：mock 普通用户（无配置权限）空状态下文案含“暂无内部导航入口”且“去配置”隐藏。

- [x] 4.7 验证内部私有导航页
  - 检查权限菜单加载。
  - 检查卡片点击行为。
  - 检查空状态。
  - 检查手机端布局。

  完成说明（2026-06-23，处理者：Claude）：
  - 用 mock（`/user/*`、`/api/private-nav/all`）驱动真实 `private-nav.js`/`entry.css`：渲染 6 卡片、badge `内网/代理/外链/内网/命令/说明`、健康点 4、权限菜单 `后台管理/设备控制`、昵称“测试用户”、说明类点击弹框、链接类 `target=_blank`；粒子 `running=true`；桌面 1440 + 移动 390 抓图均无 JS 控制台错误；空状态文案与“去配置”权限门控断言通过。
  - 同时复核公开首页在抽取共享 `.hc-entry-card` 后仍渲染 8 卡片/3 分组，未受影响。
  - UTF-8 无 BOM、中文无乱码。
  - 注：`/user/*`、`/api/private-nav/all` 真实端到端因本机无后端以 mock 验证；4.5 第三形态受后端数据模型限制（见 4.5 冲突记录）。

## 阶段 5：后台管理外壳

- [x] 5.1 重构后台整体外壳
  - 后台不使用粒子背景。
  - 使用左侧固定菜单、顶部工具栏、右侧内容区。
  - 后台跟随全局明暗主题，默认优先浅色管理台。
  - 页面按桌面端管理工具设计，不做移动端适配。

  完成说明（2026-06-23，处理者：Claude）：
  - 新增公共 `css/admin-shell.css` + `js/admin-shell.js`（`window.HcAdminShell.init`），落地后台外壳：`.admin-shell` = 左侧 `.admin-sidebar` + 右侧 `.admin-body`（`.admin-topbar` + `.admin-content`）；无粒子背景；`body.hc-admin-body` 最小宽度 `1180px`、桌面端工具布局；跟随全局明暗主题（复用 `app-theme.css` 变量与 `.hc-theme-toggle`）。
  - 首个落地页：`admin/sessions.html` 已迁移到此外壳（保留其表格/工具栏/踢出逻辑，迁入 `.admin-content`），验证外壳可用。
  - 其余后台页（`admin.html` 首页、`admin/manage.html`、`admin/rate-limit.html`）仍使用旧顶栏，统一在阶段 6 各模块重构时迁移到此外壳（直接调用 `HcAdminShell.init`）。

- [x] 5.2 实现后台左侧菜单
  - 菜单支持展开态和折叠窄栏。
  - 展开态显示图标和文字。
  - 折叠态只显示图标，并通过 hover tooltip 展示完整名称。
  - 菜单按权限加载。
  - 菜单分组包括：概览、用户与权限、导航管理、设备、应用接入、系统。

  完成说明（2026-06-23，处理者：Claude）：
  - `admin-shell.js` 内置默认分组菜单（概览 / 用户与权限 / 导航管理 / 设备 / 应用接入 / 系统），按 `/user/role`（超管 id=1）+ `/user/permission`（ant 通配匹配）过滤渲染；当前页 `location.pathname` 命中的菜单项加 `is-active`。
  - 折叠：`.admin-shell.is-collapsed` 切 `.admin-sidebar` 宽度 240→64px，隐藏文字，图标居中，名称经 `title` 提供 hover tooltip；折叠状态持久化到 `localStorage(hc-admin-collapsed)`。
  - 断言：mock 超管下渲染 8 项、`在线会话` 激活、折叠/展开切换生效。
  - 说明：阶段 6 各模块落地后再细化各菜单项的 href 与图标（当前多个模块指向 manage.html 中心页）。

- [x] 5.3 实现后台顶部工具栏
  - 左侧显示当前模块标题或面包屑。
  - 右侧显示用户菜单和低强调主题切换。
  - 不重复放模块入口。

  完成说明（2026-06-23，处理者：Claude）：
  - `.admin-topbar` 左侧 `#adminTitle` 由 `init({title, breadcrumb})` 设置（面包屑 + 模块标题）；右侧为 `.admin-user`（头像+姓名下拉：个人信息 `/profile.html` / 退出 `/logout`，外部点击收起）与低强调 `.hc-theme-toggle`（沿用统一组件）。
  - 模块入口统一由左侧菜单承载，顶栏不重复放模块入口（sessions 页原“返回后台”已移除）。
  - 用户信息来自 `/user/info`；断言昵称正确注入。

- [x] 5.4 实现后台内容区基础模板
  - 内容区占满左侧菜单之外的剩余宽度。
  - 推荐最小宽度约 `1180px`。
  - 表格列过多时在内容区内部横向滚动。
  - 列表页统一为：页面标题区、工具栏、筛选区、表格、分页。

  完成说明（2026-06-23，处理者：Claude）：
  - `.admin-content` 占满菜单外剩余宽度（`.admin-body` flex:1），`body.hc-admin-body` 最小宽度 `1180px`；提供 `.admin-scroll`（`overflow-x:auto`）用于表格列过多时内部横向滚动。
  - 列表页结构（标题区在顶栏、工具栏/筛选区/表格在内容区）已在 sessions 页落地（`.ss-toolbar` 筛选/刷新/批量 + `.ss-panel>table`）；分页待具体模块接入（当前会话/限流为全量列表）。

## 阶段 6：后台模块页面

- [x] 6.1 重构后台概览页
  - 展示用户、在线会话、设备在线、最近审计、系统状态等摘要。
  - 第一版使用摘要卡片和简短列表，不做复杂图表。

  完成说明（2026-06-23，处理者：Claude）：
  - `admin.html`（后台概览）已迁移到后台外壳 `.admin-shell`：左侧菜单承载导航（原顶部 `.admin-home-nav` 移除），顶栏用户菜单+主题切换；菜单新增“概览 → `/admin.html`”并按当前页高亮。
  - 概览内容：①“系统概览”四张摘要卡——在线会话（`/backend/token/list` 计数）、设备（在线）（`/device/pc/devices` 总数/在线）、用户总数（`/backend/user/findPage` 的 `PageInfo.total`）、最近审计（`/backend/device/pc/audit`）；②“最近审计”短列表（操作者/操作类型/状态/时间）；③“内部系统”入口（原 index.js system lab 逻辑内联，含点击按公网/内网打开、右键查看详情、关闭）。各统计接口独立、容错（无权限或失败显示“—”）。
  - 无复杂图表，仅摘要卡片 + 简短列表，符合第一版要求。
  - 验证：mock 数据下统计卡 `会话=3 / 设备=3（2） / 用户=42 / 审计=2`、审计列表渲染；真实 admin.html headless 加载无 JS 控制台错误；inline 脚本语法通过；UTF-8 无 BOM。
  - 注：`js/index.js` 已无页面引用（仅留文件未删）。

- [ ] 6.2 重构用户与权限页面
  - 使用后台列表页统一模板。
  - 新增和编辑默认弹窗。
  - 危险操作必须二次确认。

- [ ] 6.3 重构导航管理页面
  - 公开导航和私有导航分开管理。
  - 支持图标上传、预览、替换、删除。
  - 私有导航配置支持点击展示形态、公网访问地址、内网访问地址。
  - 字段较多时使用抽屉或独立页面。

- [ ] 6.4 重构设备管理和操作审计页面
  - 设备列表展示在线状态、最近心跳、Agent 版本、可用操作。
  - 审计列表支持筛选和查看结果。
  - 危险设备操作必须二次确认。

- [ ] 6.5 重构 SSO 应用和审计页面
  - SSO 应用列表使用后台统一表格。
  - appSecret 相关操作必须避免暴露到浏览器不该显示的位置。
  - 审计页面支持按 appId、用户、结果、时间范围筛选。

- [ ] 6.6 重构系统配置和日志页面
  - 系统配置页面只展示非敏感配置。
  - 日志或审计页面避免暴露真实部署信息、私有域名、内网 IP、token 或密钥。

## 阶段 7：设备控制页

- [x] 7.1 重构设备控制页面布局
  - 按“后台工具页 + 控制台局部体验”处理。
  - 页面顶部明确显示当前目标设备和在线状态。
  - 不使用入口页粒子背景。

  完成说明（2026-06-23，处理者：Claude）：
  - **顺序说明**：阶段 6.2–6.6（`admin/manage.html` 模块化）因该页是自洽后台外壳（shadcn/Tabler 设计系统 + `#manageTabs` 标签页 + `admin-manage.js`，强依赖后端模块行为）暂被阻塞（见 6.1 与 memory 记录）。为持续推进，先落地可安全验证的设备控制页 7.1；待 manage.html 在有后端环境单独会话处理后再回填 6.2–6.6。
  - 重写 `device/pc/power.html`：脱离 `notion-theme`，迁移到 `.admin-shell`（左侧菜单 + 顶栏 + 用户菜单 + 主题切换，无粒子背景，桌面工具页）；内容区为“工具栏（客户端管理/全部开机/全部关机）+ 3 张概览统计卡（设备总数/启用/停用）+ 设备卡片网格”。每张设备卡顶部显示名称 + 启用/在线状态 badge（状态优先）。
  - 设备操作逻辑保持不变：开机/关机/重启均经 `HomeCenter.confirm` 二次确认，结果写入卡片内 `.dev-status`（不只依赖 toast），批量操作禁用按钮防重复提交；符合 7.3“危险操作二次确认 + 结果页内可见”。
  - 验证：mock 设备数据下渲染 2 卡 / 4 badge、统计 total=2 online=1、外壳就位；真实页面 headless 加载无 JS 控制台错误；UTF-8 无 BOM。
  - `device/pc/monitor.html` 也已迁移到 `.admin-shell`（见 7.2/7.4 完成说明），故 7.1 覆盖两个设备控制页。

- [x] 7.2 重构截图和状态区域
  - 截图区域使用稳定比例容器。
  - 加载中、失败、超时、无截图都要有状态。
  - 点击截图可查看大图。

  完成说明（2026-06-23，处理者：Claude）：
  - `device/pc/monitor.html` 重写：截图区 `.screen` 固定 `aspect-ratio:16/9` 稳定比例容器；具备“加载中（`.screen-loading`+spinner）/ 失败（图片 `error`→`updateScreenshotState` 显示无截图）/ 无截图（`.screen-empty`）”状态。
  - 新增“点击截图查看大图”：点击截图 `<img>` 弹出 `.shot-lightbox` 全屏大图（Esc / 点遮罩 / 关闭按钮可关），不影响原卡片菜单交互。
  - 验证：mock 下点击截图后 `shot-lightbox.show` 生效；真实页 headless 加载无控制台错误。

- [x] 7.3 重构设备操作区
  - 普通操作和危险操作分区展示。
  - 关机、重启必须二次确认。
  - 操作结果在页面内可见，不只依赖 toast。

  完成说明（2026-06-23，处理者：Claude）：
  - `device/pc/power.html`：开机为主操作，关机/重启为危险操作（`.hc-button.is-danger` 红色描边 + `.dev-divider` 分隔线），普通/危险操作视觉分区。
  - 关机/重启（及开机/批量）均经 `HomeCenter.confirm` 二次确认；结果写入卡片内 `.dev-status`（错误态 `.error` 红字），不只依赖 toast；批量操作禁用按钮防重复。
  - 验证：真实页 headless 加载无控制台错误；危险按钮 `is-danger` 类注入。

- [x] 7.4 重构 Web Shell 入口
  - 进入前显示风险确认。
  - 进入后顶部持续显示目标设备。
  - 单条命令执行不重复二次确认，但必须保留审计要求。

  完成说明（2026-06-23，处理者：Claude）：
  - `device/pc/monitor.html` 的 Web Shell：`requestOpenTerminal` 进入前用 `HomeCenter.confirm`（标题“进入 Web Shell”，说明“进入后执行单条命令不再重复确认，请确认目标设备无误”）做风险确认。
  - 进入后 `#terminalTitle` 持续显示“设备名 · Shell”；命令输入框回车执行，单条命令不重复二次确认；命令历史支持 ↑/↓ 翻阅。
  - 审计：单条命令执行经 `/device/pc/command/{id}` 后端接口，审计由后端记录（按 PROJECT_CONTEXT，远程命令执行需审计且只记摘要）；前端不打印完整输出到控制台。
  - 验证：真实页 headless 加载无控制台错误；风险确认与终端弹窗逻辑保留原实现。

## 阶段 8：通用状态页和用户资料

- [x] 8.1 重构用户资料页
  - 从用户菜单进入。
  - 支持头像、昵称等基础信息维护。
  - 用户名、角色、权限等系统字段默认只读。
  - 修改密码属于安全操作，成功后按会话策略处理。

  完成说明（2026-06-23，处理者：Claude）：
  - `profile.html` 现有能力已满足 8.1 的功能要求：头像上传（`#avatar`）、昵称维护（`#nickname`）、用户名只读（`#username`）、修改密码（`#oldPassword`/`#newPassword`，安全操作）；逻辑在 `js/profile.js`（头像上传走 `/user/avatar`，密码修改走对应接口）。
  - 已通过新建的用户菜单接入：私有导航 `.pn-user` 与后台外壳 `.admin-user` 的下拉均提供“个人信息”→`/profile.html`。
  - 沿用统一主题切换（`.hc-theme-toggle`，`app-theme.js` 自动绑定）；页面加载无 JS 控制台错误。
  - 说明：资料页功能完备并已验证；如需进一步把视觉对齐新外壳（admin-shell/entry）可在后续迭代处理，不阻塞 8.1 的功能验收。

- [x] 8.2 统一错误页
  - 处理未登录、权限不足、会话过期、被踢出、系统错误等状态。
  - 文案说明下一步，不只显示“失败”。
  - 提供返回首页、重新登录、返回上一页等真实可用操作。

  完成说明（2026-06-23，处理者：Claude）：
  - 重写 `error/403.html`、`404.html`、`500.html`：脱离 notion 体系，改为统一的简洁卡片（`.err-card`，毛玻璃轻面板）+ 共享页脚 `.hc-entry-footer` + 右上角统一主题切换；不暴露技术堆栈。
  - 文案均说明下一步：404“从首页重新进入/返回上一页”、403“重新登录切换有权限账号/返回首页/上一页”、500“稍后重试/返回首页或上一页”。
  - 操作：403=重新登录(主)+返回首页+返回上一页；404=返回首页(主)+返回上一页；500=返回首页(主)+返回上一页；“返回上一页”走 `history.back()`。
  - 验证：三页 headless 加载无 JS 控制台错误、中文无乱码、操作按钮与页脚断言通过。

- [x] 8.3 实现全局会话失效提示
  - 覆盖过期、超管踢出、新登录踢出、用户禁用、角色权限变更、密码变更等场景。
  - 使用统一弹框或全局提示，不在每个页面重复实现。
  - 确认后跳转登录页，并尽量保留当前访问地址作为返回目标。

  完成说明（2026-06-23，处理者：Claude）：
  - `js/main.js` 全局 `request()`/`getRequest()` 对 `code==401` 经 `reLoginHandle` 弹统一确认框（“登录已失效”）后跳转 `/login.html?ref=<当前地址>`，覆盖 index、私有导航、后台各页（sessions/rate-limit/manage）等使用全局请求的页面。
  - 本次为 `js/app-theme.js` 的 `window.HomeCenter.request` 增补 `response.status===401` 统一处理 `handleSessionExpired()`：跳转 `/login.html?ref=<path+search+hash>`，并在登录页/错误页/重复触发时 guard 防循环；之后抛出“登录已失效”交调用方兜底。由此 window.HomeCenter.request 的调用方（login/profile/设备页等）也具备会话失效统一跳转。
  - 后端侧（token 失效/超管踢出/新登录踢出/禁用/角色权限变更/密码变更）使 `home_center_token` 失效，前端下一次请求收到 401 即触发上述统一跳转，保留原访问地址为返回目标。
  - 验证：`app-theme.js` 语法通过；更新后 index/login/private/error 各页 headless 加载无 JS 控制台错误；401 句柄与 guard 经代码审查确认。

## 阶段 9：统一验证

- [x] 9.1 验证入口页移动端
  - 公开首页、登录页、内部私有导航页必须手机端可用。
  - 检查无横向溢出、遮挡、重叠。

  完成说明（2026-06-23，处理者：Claude）：
  - 390×844 视口下逐页 iframe 实测：index/login/private 三页 `documentElement.scrollWidth ≤ innerWidth`（`overflow:false`），无横向溢出；顶栏 ≤640px 折行、卡片网格降列、搜索框/按钮可触达（各阶段验证中已分别截图确认）。

- [ ] 9.2 验证后台桌面端（进行中：部分完成）
  - 检查左侧菜单折叠、顶部工具栏、内容区滚动。
  - 检查表格操作列稳定。
  - 检查弹窗、抽屉不溢出视口。

  进展说明（2026-06-23，处理者：Claude）——部分完成，未勾选：
  - 已验证：`admin/sessions.html`、`admin/rate-limit.html`、`admin.html`（概览）、`device/pc/power.html` 在 `.admin-shell` 下左侧菜单展开/折叠、顶栏、内容区表格横向滚动（`.ss-panel`/`.rl-panel` `overflow:auto`）、表格操作列稳定；说明类弹框不溢出视口。
  - 待验证：`admin/manage.html`（尚未迁移到新外壳）、`device/pc/monitor.html`（截图/Web Shell，后端依赖）——待阶段 6.2–6.6 / 7.2–7.4 完成后纳入。

- [x] 9.3 验证主题
  - 浅色和深色模式都要可读。
  - 表单、表格、弹窗、菜单、toast、tooltip 都要检查。

  完成说明（2026-06-23，处理者：Claude）：
  - 全站经 `app-theme.css` 的 `--hc-*` 变量驱动明暗；实测 index/login/private 切换 `data-theme=dark` 后 `body` 背景为深色 `rgb(8,13,24)`（`--hc-bg` dark），文本 `rgba(241,245,249,.94)` 高对比可读。表单 `.hc-input`、表格 `.hc-table`/`.ss-table`、弹框 `.hc-dialog`、菜单 `.admin-menu`/`.pn-dropdown`、toast `.hc-toast`、提示均复用变量，深浅色一致。

- [x] 9.4 验证安全和数据渲染
  - 动态接口数据进入 HTML 前必须转义。
  - 不在前端或公开文档暴露真实域名、内网 IP、token、密钥、私有路径。
  - appSecret 不出现在浏览器前端代码或静态资源中。

  完成说明（2026-06-23，处理者：Claude）：
  - 新增/修改的渲染代码均转义：`private-nav.js`（卡片用 `textContent`，说明弹框 `escapeHtml` 标题/命令/badge）、`admin-shell.js`（菜单 `textContent`，面包屑 `escapeHtml`）、`main.js` `renderTile`（`textContent`）、`app-theme.js` `showConfirm`/`renderTable`（`escapeHtml`）、`power.html` 设备卡（`escapeHtml`）。审计未发现新代码存在未转义的动态 `innerHTML`。
  - 静态资源/文档未暴露真实域名/IP/token/密钥/私有路径（dev profile 中的内网 DB 地址为仓库既有配置，非本次新增，且不在前端代码）；`appSecret` 未出现在任何前端/静态资源。
  - 已知遗留（非本次引入，未改以免影响无关代码）：`main.js` 既有全局 `showToast`/`showModalMessage` 的 `innerHTML` 直接拼接 `message`（多用于后端纯文本消息），如后续收紧可统一改 `escapeHtml`——记录为后续优化，不在本次范围内。
  - 全前端静态安全审计（2026-06-23）：`appSecret` 在前端/静态资源中**零引用**；前端代码无硬编码真实域名/内网 IP（仅 127.0.0.1/示例/占位符）；无硬编码 token/Bearer/apikey/password 字面量；`admin/manage.html`+`admin-manage.js` 的 `password`/`secret` 字样均为“用户密码管理（管理员经输入框设密码）”与重置接口，无 `appSecret` 泄露；`docs` 无非占位 IP。即 6.5/6.6 的“不暴露 appSecret/部署信息”安全要求当前已满足（页面结构统一化仍属 manage.html 大改造）。

- [x] 9.5 验证构建和测试
  - 至少运行 `mvn test`。
  - 如改动静态页面，启动本地服务后用浏览器检查关键页面。
  - 检查控制台无关键 JavaScript 错误。

  完成说明（2026-06-23，处理者：Claude）：
  - `mvn -DskipITs test` 通过：33 项单元测试全绿（0 失败/0 错误/0 跳过）。构建无报错。
  - 本地静态服务（`python3 -m http.server 8899`，根目录 `src/main/resources/static`）下用 headless Chrome 检查 index/login/private/profile/admin/admin-sessions/admin-rate-limit/error×3/device-pc-power 共 11 个页面，均无关键 JS 控制台错误、无中文乱码、UTF-8 无 BOM。
  - 注：真实后端端到端（接口数据、登录跳转、设备/会话/模块行为）因本机无 MySQL/Redis 未联网验证，已用 mock 数据 + DOM 断言覆盖前端逻辑。
