/**
 * 私有导航页面
 *
 * 职责：
 * - 顶部菜单区：按当前用户角色/权限注入系统级菜单（后台管理等），用户头像+姓名下拉（个人信息/退出），低强调主题切换。
 * - 按分组渲染当前用户的私有导航入口（基于共享 .hc-entry-card 舒展门户卡片）。
 * - 点击展示形态来自配置（openType），不硬编码入口类型：new_tab 新标签页打开；instruction 说明/命令小弹框（复制+关闭）；
 *   address_choice 公网/内网地址选择弹框。
 *   说明/命令类（ssh_rdp/note）默认走 instruction；其余默认走 new_tab。
 * - 可跳转入口展示轻量健康状态小圆点（不展示详细错误）。
 * - 空数据提示到配置界面维护，有配置权限时显示低强调“去配置”。
 *
 */
(function () {
    var state = {
        categories: [],
        links: [],
        internalSystems: [],
        query: ''
    };

    // 权限状态（顶部菜单与“去配置”按钮共用）
    var permState = {
        loaded: false,
        isSuperAdmin: false,
        codes: {}
    };

    // 入口类型元数据：badge 文案 + 是否为说明/命令类（默认点击形态）
    var ENTRY_META = {
        link: {label: '外链', note: false, badge: 'muted'},
        nginx_proxy: {label: '代理', note: false, badge: ''},
        intranet: {label: '内网', note: false, badge: 'success'},
        ssh_rdp: {label: '命令', note: true, badge: 'note'},
        note: {label: '说明', note: true, badge: 'note'}
    };

    // 顶部系统级权限菜单（有对应权限才显示）
    var PERM_MENUS = [
        {code: '/admin.html', label: '后台管理', href: '/admin.html'},
        {code: '/api/private-nav/**', label: '导航配置', href: '/admin/private-nav.html'},
        {code: '/device/pc/power.html', label: '设备控制', href: '/device/pc/power.html'}
    ];

    var CONFIG_PERM = '/api/private-nav/**';

    function normalizeText(value) {
        return value === null || value === undefined ? '' : String(value);
    }

    function escapeHtml(value) {
        return normalizeText(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function entryMeta(type) {
        return ENTRY_META[normalizeText(type)] || ENTRY_META.link;
    }

    // 点击展示形态：优先 openType 配置，回退到入口类型
    function isInstructionEntry(link, meta) {
        var openType = normalizeText(link.openType);
        if (openType === 'instruction') {
            return true;
        }
        if (openType === 'address_choice') {
            return false;
        }
        if (openType === 'new_tab') {
            return false;
        }
        return meta.note;
    }

    function isImageIcon(value) {
        var text = normalizeText(value).trim();
        return /^(https?:)?\/\//i.test(text) ||
            /^data:image\//i.test(text) ||
            /\.(png|jpe?g|gif|webp|svg)(\?.*)?$/i.test(text);
    }

    function hashColor(str) {
        var text = normalizeText(str) || 'HC';
        var h = 0;
        var palettes = [
            ['#d9ecff', '#8fc7ff'],
            ['#d8f5f2', '#80d8cf'],
            ['#e4edff', '#9bb8f4'],
            ['#ddf7e8', '#8bdcae'],
            ['#e7eef5', '#b9c9d8'],
            ['#e1f4ff', '#98d6ee']
        ];
        for (var i = 0; i < text.length; i++) {
            h = (h << 5) - h + text.charCodeAt(i);
            h = h & h;
        }
        var color = palettes[Math.abs(h) % palettes.length];
        return 'linear-gradient(135deg,' + color[0] + ',' + color[1] + ')';
    }

    function makeText(value) {
        var text = normalizeText(value).trim();
        if (!text) {
            return 'HC';
        }
        var chars = Array.prototype.slice.call(text);
        if (/[一-鿿]/.test(chars[0])) {
            return chars.slice(0, 2).join('');
        }
        return text.replace(/\s+/g, '').slice(0, 4).toUpperCase();
    }

    // ===== 权限匹配（与 admin 首页 index.js 保持一致的 ant 风格通配） =====
    function matchesAntPath(pattern, path) {
        if (!pattern || pattern.indexOf('*') === -1) {
            return pattern === path;
        }
        var regex = '^' + pattern
            .replace(/[.+^${}()|[\]\\]/g, '\\$&')
            .replace(/\*\*/g, '::DS::')
            .replace(/\*/g, '[^/]*')
            .replace(/::DS::/g, '.*') + '$';
        return new RegExp(regex).test(path);
    }

    function hasPermission(code) {
        if (!permState.loaded) {
            return false;
        }
        if (permState.isSuperAdmin) {
            return true;
        }
        if (!code) {
            return true;
        }
        if (permState.codes[code]) {
            return true;
        }
        var keys = Object.keys(permState.codes);
        for (var i = 0; i < keys.length; i++) {
            if (matchesAntPath(keys[i], code)) {
                return true;
            }
        }
        return false;
    }

    // ===== 顶部菜单区 =====
    function renderPermMenu() {
        var container = document.getElementById('pnPermMenu');
        if (!container) {
            return;
        }
        container.innerHTML = '';
        PERM_MENUS.forEach(function (item) {
            if (!hasPermission(item.code)) {
                return;
            }
            var link = document.createElement('a');
            link.className = 'pn-perm-link';
            link.href = item.href;
            link.textContent = item.label;
            container.appendChild(link);
        });
    }

    function loadUserInfo() {
        getRequest('/user/info', function (data) {
            if (!data) {
                return;
            }
            var nameEl = document.getElementById('pnUserName');
            var avatarEl = document.getElementById('pnUserAvatar');
            if (nameEl && data.nickname) {
                nameEl.textContent = data.nickname;
            }
            if (avatarEl && data.avatar) {
                avatarEl.src = data.avatar;
            }
        }, function () { /* 未登录或接口异常时保持默认 */ });
    }

    function loadPermissions() {
        getRequest('/user/role', function (role) {
            permState.isSuperAdmin = !!(role && Number(role.id) === 1);
            getRequest('/user/permission', function (permissions) {
                permState.codes = flattenPermissionUrls(permissions || []);
                permState.loaded = true;
                onPermissionReady();
            }, function () {
                permState.codes = {};
                permState.loaded = true;
                onPermissionReady();
            });
        }, function () {
            permState.loaded = true;
            onPermissionReady();
        });
    }

    function flattenPermissionUrls(permissions) {
        var set = {};
        permissions.forEach(function (p) {
            String(p.urls || '').split(';').forEach(function (part) {
                var code = part.trim();
                if (code) {
                    set[code] = true;
                }
            });
        });
        return set;
    }

    function onPermissionReady() {
        renderPermMenu();
        updateConfigButtons();
    }

    function bindUserMenu() {
        var btn = document.getElementById('pnUserBtn');
        var menu = document.getElementById('pnUserMenu');
        var userWrap = btn && btn.parentNode;
        if (!btn || !menu) {
            return;
        }
        btn.addEventListener('click', function (event) {
            event.stopPropagation();
            var open = menu.hasAttribute('hidden');
            if (open) {
                menu.removeAttribute('hidden');
            } else {
                menu.setAttribute('hidden', '');
            }
            btn.setAttribute('aria-expanded', open ? 'true' : 'false');
        });
        document.addEventListener('click', function (event) {
            if (userWrap && !userWrap.contains(event.target)) {
                menu.setAttribute('hidden', '');
                btn.setAttribute('aria-expanded', 'false');
            }
        });
        var logoutBtn = document.getElementById('pnLogoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', function () {
                window.location.href = '/logout';
            });
        }
    }

    // ===== 卡片渲染 =====
    function createIconEl(link) {
        var icon = document.createElement('span');
        icon.className = 'hc-entry-card-icon';
        icon.setAttribute('aria-hidden', 'true');
        var emojiText = normalizeText(link.iconEmoji);
        if (isImageIcon(link.icon)) {
            var img = document.createElement('img');
            img.src = normalizeText(link.icon);
            img.alt = '';
            icon.appendChild(img);
        } else {
            icon.style.background = hashColor(emojiText || link.title);
            icon.style.color = '#10384f';
            icon.textContent = makeText(emojiText || link.title);
        }
        return icon;
    }

    function createBadgeEl(meta) {
        var badge = document.createElement('span');
        badge.className = 'pn-badge ' + (meta.badge || '');
        badge.textContent = meta.label;
        return badge;
    }

    function createHealthDot(status) {
        var dot = document.createElement('span');
        dot.className = 'pn-health-dot ' + (normalizeText(status) || 'unknown');
        dot.setAttribute('aria-hidden', 'true');
        return dot;
    }

    function fillCard(tile, link, meta) {
        var titleText = normalizeText(link.title) || '未命名入口';
        var descText = normalizeText(link.description) || (meta.note ? '点击查看说明 / 复制命令' : '点击打开该入口');

        tile.appendChild(createBadgeEl(meta));
        tile.appendChild(createIconEl(link));

        var name = document.createElement('span');
        name.className = 'hc-entry-card-name';
        name.textContent = titleText;
        tile.appendChild(name);

        // 可跳转入口展示轻量健康状态点（不展示详细错误）
        if (!meta.note) {
            tile.appendChild(createHealthDot(link.checkStatus));
        }

        var desc = document.createElement('span');
        desc.className = 'hc-entry-card-desc';
        desc.textContent = descText;
        tile.appendChild(desc);

        tile.title = titleText;
        tile.setAttribute('aria-label', titleText + '：' + descText);
    }

    function createCard(link) {
        var meta = entryMeta(link.entryType);
        var tile;
        if (isInstructionEntry(link, meta)) {
            tile = document.createElement('button');
            tile.type = 'button';
            tile.className = 'hc-entry-card pn-card is-note';
            tile.addEventListener('click', function () {
                openInstruction(link, meta);
            });
        } else if (normalizeText(link.openType) === 'address_choice') {
            tile = document.createElement('button');
            tile.type = 'button';
            tile.className = 'hc-entry-card pn-card';
            tile.addEventListener('click', function () {
                openAddressChoice(link);
            });
        } else {
            tile = document.createElement('a');
            tile.className = 'hc-entry-card pn-card';
            tile.href = firstAvailableUrl(link) || '#';
            tile.target = '_blank';
            tile.rel = 'noopener noreferrer';
        }
        fillCard(tile, link, meta);
        return tile;
    }

    // ===== 内部系统分组渲染 =====
    function matchesInternalSystem(sys, query) {
        if (!query) { return true; }
        var haystack = [sys.sysName, sys.sysDescription, sys.sysDomain, sys.internetUrl]
            .map(function (v) { return normalizeText(v).toLowerCase(); }).join(' ');
        return haystack.indexOf(query) !== -1;
    }

    function createInternalSystemCard(sys) {
        var tile = document.createElement('a');
        tile.className = 'hc-entry-card pn-card';
        var url = normalizeText(sys.internetUrl) || normalizeText(sys.openInternetUrl) ||
                  normalizeText(sys.internalUrl);
        tile.href = url || '#';
        tile.target = '_blank';
        tile.rel = 'noopener noreferrer';

        // badge: 代理
        var meta = ENTRY_META.nginx_proxy;
        tile.appendChild(createBadgeEl(meta));

        // icon
        var icon = document.createElement('span');
        icon.className = 'hc-entry-card-icon';
        icon.setAttribute('aria-hidden', 'true');
        if (isImageIcon(sys.icon)) {
            var img = document.createElement('img');
            img.src = normalizeText(sys.icon);
            img.alt = '';
            icon.appendChild(img);
        } else {
            icon.style.background = hashColor(sys.sysName);
            icon.style.color = '#10384f';
            icon.textContent = makeText(sys.sysName);
        }
        tile.appendChild(icon);

        var name = document.createElement('span');
        name.className = 'hc-entry-card-name';
        name.textContent = normalizeText(sys.sysName) || '未命名系统';
        tile.appendChild(name);

        tile.appendChild(createHealthDot('ok'));

        var desc = document.createElement('span');
        desc.className = 'hc-entry-card-desc';
        desc.textContent = normalizeText(sys.sysDescription) || '点击打开内部系统';
        tile.appendChild(desc);

        tile.title = normalizeText(sys.sysName);
        tile.setAttribute('aria-label', normalizeText(sys.sysName) + '：' + (normalizeText(sys.sysDescription) || '内部系统'));
        return tile;
    }

    function createInternalSystemGroup(systems) {
        var group = document.createElement('section');
        group.className = 'pn-group';

        var title = document.createElement('h2');
        title.className = 'pn-group-title';
        var icon = document.createElement('span');
        icon.className = 'pn-group-icon';
        icon.textContent = '⚡';
        icon.setAttribute('aria-hidden', 'true');
        var name = document.createElement('span');
        name.textContent = '内部系统';
        title.appendChild(icon);
        title.appendChild(name);

        var grid = document.createElement('div');
        grid.className = 'pn-grid';
        systems.forEach(function (sys) {
            grid.appendChild(createInternalSystemCard(sys));
        });

        group.appendChild(title);
        group.appendChild(grid);
        return group;
    }

    function createGroup(category, links) {
        var group = document.createElement('section');
        group.className = 'pn-group';

        var title = document.createElement('h2');
        title.className = 'pn-group-title';
        var icon = document.createElement('span');
        icon.className = 'pn-group-icon';
        icon.textContent = normalizeText(category.icon) || '◆';
        icon.setAttribute('aria-hidden', 'true');
        var name = document.createElement('span');
        name.textContent = normalizeText(category.name) || '未命名分组';
        title.appendChild(icon);
        title.appendChild(name);

        var grid = document.createElement('div');
        grid.className = 'pn-grid';
        if (links.length) {
            links.forEach(function (link) {
                grid.appendChild(createCard(link));
            });
        } else {
            grid.appendChild(createEmpty('该分组暂无入口', false));
        }

        group.appendChild(title);
        group.appendChild(grid);
        return group;
    }

    function createEmpty(text, withConfig) {
        var empty = document.createElement('div');
        empty.className = 'pn-empty';
        empty.textContent = text;
        if (withConfig) {
            var actions = document.createElement('div');
            actions.className = 'pn-empty-actions';
            var configLink = document.createElement('a');
            configLink.className = 'hc-entry-link pn-config-link';
            configLink.href = '/admin/private-nav.html';
            configLink.textContent = '去配置';
            configLink.setAttribute('data-pn-config', '');
            if (!hasPermission(CONFIG_PERM)) {
                configLink.setAttribute('hidden', '');
            }
            actions.appendChild(configLink);
            empty.appendChild(actions);
        }
        return empty;
    }

    function updateConfigButtons() {
        var allowed = hasPermission(CONFIG_PERM);
        Array.prototype.forEach.call(document.querySelectorAll('[data-pn-config]'), function (el) {
            if (allowed) {
                el.removeAttribute('hidden');
            } else {
                el.setAttribute('hidden', '');
            }
        });
    }

    function matchesQuery(link, category, query) {
        if (!query) {
            return true;
        }
        var haystack = [
            link.title, link.description, link.url, link.publicUrl, link.intranetUrl, link.icon, link.iconEmoji,
            link.entryType, link.instruction,
            category && category.name, category && category.icon
        ].map(function (value) {
            return normalizeText(value).toLowerCase();
        }).join(' ');
        return haystack.indexOf(query) !== -1;
    }

    function compareSort(a, b) {
        var left = Number(a.sortOrder || 0);
        var right = Number(b.sortOrder || 0);
        if (left !== right) {
            return left - right;
        }
        return Number(a.id || 0) - Number(b.id || 0);
    }

    function render() {
        var content = document.getElementById('pnContent');
        if (!content) {
            return;
        }
        while (content.firstChild) {
            content.removeChild(content.firstChild);
        }

        var query = state.query.trim().toLowerCase();
        var categories = state.categories.slice().sort(compareSort);
        var links = state.links.slice().sort(compareSort);
        var systems = state.internalSystems.slice().sort(function (a, b) {
            var left = Number(a.sort || 0);
            var right = Number(b.sort || 0);
            if (left !== right) { return left - right; }
            return Number(a.id || 0) - Number(b.id || 0);
        });

        var rendered = 0;

        // 内部系统分组（置顶）
        var matchedSystems = systems.filter(function (sys) {
            return matchesInternalSystem(sys, query);
        });
        if (matchedSystems.length) {
            content.appendChild(createInternalSystemGroup(matchedSystems));
            rendered += matchedSystems.length;
        }

        if (!categories.length && !links.length && !systems.length) {
            content.appendChild(createEmpty('暂无内部导航入口，可在配置或管理界面维护私有导航。', true));
            return;
        }

        categories.forEach(function (category) {
            var groupLinks = links.filter(function (link) {
                return String(link.categoryId) === String(category.id) && matchesQuery(link, category, query);
            });
            if (query && !groupLinks.length) {
                return;
            }
            content.appendChild(createGroup(category, groupLinks));
            rendered += groupLinks.length;
        });

        if (query && rendered === 0) {
            content.appendChild(createEmpty('未找到匹配的私有导航入口', false));
        }
        updateConfigButtons();
    }

    function firstAvailableUrl(link) {
        return normalizeText(link.url) || normalizeText(link.publicUrl) || normalizeText(link.intranetUrl);
    }

    // ===== 说明 / 命令小弹框 =====
    function openInstruction(link, meta) {
        var instruction = normalizeText(link.instruction);
        var backdrop = document.createElement('div');
        backdrop.className = 'hc-dialog-backdrop show';
        backdrop.tabIndex = -1;
        backdrop.innerHTML =
            '<div class="hc-dialog">' +
            '<div class="hc-dialog-content">' +
            '<div class="hc-dialog-header">' +
            '<h6 class="hc-dialog-title">[i] ' + escapeHtml(normalizeText(link.title) || '说明') +
            ' <span class="pn-badge ' + (meta.badge || '') + '">' + escapeHtml(meta.label) + '</span></h6>' +
            '<button type="button" class="hc-close-button" aria-label="关闭">×</button>' +
            '</div>' +
            '<div class="hc-dialog-body">' +
            (instruction
                ? '<pre class="pn-instruction">' + escapeHtml(instruction) + '</pre>'
                : '<p class="hc-muted">该入口暂未填写说明或命令。</p>') +
            '</div>' +
            '<div class="hc-dialog-footer pn-dialog-buttons">' +
            '<button type="button" class="hc-button pn-copy-btn">复制命令</button>' +
            '<button type="button" class="hc-button hc-button-primary pn-close-btn">关 闭</button>' +
            '</div>' +
            '</div>' +
            '</div>';

        document.body.appendChild(backdrop);

        function close() {
            if (backdrop.parentNode) {
                backdrop.parentNode.removeChild(backdrop);
            }
        }

        backdrop.querySelector('.hc-close-button').addEventListener('click', close);
        backdrop.querySelector('.pn-close-btn').addEventListener('click', close);
        backdrop.addEventListener('click', function (event) {
            if (event.target === backdrop) {
                close();
            }
        });

        var copyBtn = backdrop.querySelector('.pn-copy-btn');
        copyBtn.addEventListener('click', function () {
            if (!instruction) {
                showToastSimple('该入口没有可复制的命令', MsgTypes.WARNING, Position.TopCenter);
                return;
            }
            copyText(instruction, function (ok) {
                showToastSimple(ok ? '已复制到剪贴板' : '复制失败，请手动选择复制',
                    ok ? MsgTypes.SUCCESS : MsgTypes.DANGER, Position.TopCenter);
            });
        });
    }

    // ===== 公网 / 内网地址选择弹框 =====
    function openAddressChoice(link) {
        var publicUrl = normalizeText(link.publicUrl) || normalizeText(link.url);
        var intranetUrl = normalizeText(link.intranetUrl);
        if (!publicUrl && !intranetUrl) {
            showToastSimple('该入口没有可打开的地址', MsgTypes.WARNING, Position.TopCenter);
            return;
        }

        var backdrop = document.createElement('div');
        backdrop.className = 'hc-dialog-backdrop show';
        backdrop.tabIndex = -1;
        backdrop.innerHTML =
            '<div class="hc-dialog">' +
            '<div class="hc-dialog-content">' +
            '<div class="hc-dialog-header">' +
            '<h6 class="hc-dialog-title">选择访问地址</h6>' +
            '<button type="button" class="hc-close-button" aria-label="关闭">×</button>' +
            '</div>' +
            '<div class="hc-dialog-body">' +
            '<p class="hc-muted">请选择要打开的访问地址。</p>' +
            '<div class="pn-dialog-buttons">' +
            (publicUrl ? '<a class="hc-button hc-button-primary" target="_blank" rel="noopener noreferrer" href="' + escapeHtml(publicUrl) + '">公网访问地址</a>' : '') +
            (intranetUrl ? '<a class="hc-button" target="_blank" rel="noopener noreferrer" href="' + escapeHtml(intranetUrl) + '">内网访问地址</a>' : '') +
            '</div>' +
            '</div>' +
            '<div class="hc-dialog-footer pn-dialog-buttons">' +
            '<button type="button" class="hc-button pn-close-btn">关 闭</button>' +
            '</div>' +
            '</div>' +
            '</div>';

        document.body.appendChild(backdrop);

        function close() {
            if (backdrop.parentNode) {
                backdrop.parentNode.removeChild(backdrop);
            }
        }

        backdrop.querySelector('.hc-close-button').addEventListener('click', close);
        backdrop.querySelector('.pn-close-btn').addEventListener('click', close);
        backdrop.addEventListener('click', function (event) {
            if (event.target === backdrop) {
                close();
            }
        });
    }

    function copyText(text, callback) {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(function () {
                callback(true);
            }, function () {
                legacyCopy(text, callback);
            });
            return;
        }
        legacyCopy(text, callback);
    }

    function legacyCopy(text, callback) {
        try {
            var ta = document.createElement('textarea');
            ta.value = text;
            ta.style.position = 'fixed';
            ta.style.opacity = '0';
            document.body.appendChild(ta);
            ta.select();
            var ok = document.execCommand('copy');
            document.body.removeChild(ta);
            callback(ok);
        } catch (e) {
            callback(false);
        }
    }

    // ===== 数据加载与搜索 =====
    function load() {
        // 并行加载私有导航 + 已分配的内部系统
        var navDone = false, sysDone = false;

        function tryRender() {
            if (navDone && sysDone) { render(); }
        }

        getRequest('/api/private-nav/all', function (data) {
            state.categories = (data && data.categories) || [];
            state.links = (data && data.links) || [];
            navDone = true;
            tryRender();
        }, function () {
            state.categories = [];
            state.links = [];
            navDone = true;
            tryRender();
        });

        getRequest('/user/internalSystem', function (list) {
            state.internalSystems = list || [];
            sysDone = true;
            tryRender();
        }, function () {
            state.internalSystems = [];
            sysDone = true;
            tryRender();
        });
    }

    function bindSearch() {
        var input = document.getElementById('pnSearch');
        if (!input) {
            return;
        }
        var timer = null;
        input.addEventListener('input', function () {
            window.clearTimeout(timer);
            timer = window.setTimeout(function () {
                state.query = input.value || '';
                render();
            }, 200);
        });
    }

    function init() {
        bindSearch();
        bindUserMenu();
        loadUserInfo();
        loadPermissions();
        load();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
