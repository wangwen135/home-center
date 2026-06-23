/**
 * 后台管理外壳（admin shell）公共脚本
 *
 * 提供 window.HcAdminShell.init(options)，负责：
 * - 渲染左侧分组菜单（默认配置，按当前用户权限过滤，高亮当前模块）。
 * - 渲染顶部工具栏标题/面包屑、用户头像+姓名下拉（个人信息 / 退出）。
 * - 左侧菜单折叠/展开（状态持久化到 localStorage），折叠态仅图标 + title tooltip。
 *
 * 依赖：app-theme.js（主题切换自动绑定）、main.js（getRequest 全局）。
 * 主题切换按钮沿用统一 .hc-theme-toggle，由本脚本之外自动绑定，无需单独处理。
 */
(function () {
    var COLLAPSE_KEY = 'hc-admin-collapsed';

    // 默认菜单分组（阶段 6 各后台模块落地后再细化 href/图标）
    var DEFAULT_MENU = [
        {
            group: '概览', items: [
                {code: '/admin.html', label: '概览', href: '/admin.html', icon: '◈'}
            ]
        },
        {
            group: '用户与权限', items: [
                {code: '/admin/manage.html', label: '用户与角色', href: '/admin/manage.html', icon: '👤'},
                {code: '/admin/sessions.html', label: '在线会话', href: '/admin/sessions.html', icon: '🟢'}
            ]
        },
        {
            group: '导航管理', items: [
                {code: '/admin/manage.html', label: '导航管理', href: '/admin/manage.html', icon: '🧭'}
            ]
        },
        {
            group: '设备', items: [
                {code: '/device/pc/power.html', label: '设备控制', href: '/device/pc/power.html', icon: '🖥'}
            ]
        },
        {
            group: '应用接入', items: [
                {code: '/admin/manage.html', label: 'SSO 应用', href: '/admin/manage.html', icon: '🔐'}
            ]
        },
        {
            group: '系统', items: [
                {code: '/admin/rate-limit.html', label: '限流管理', href: '/admin/rate-limit.html', icon: '⏱'},
                {code: '/admin/manage.html', label: '系统配置', href: '/admin/manage.html', icon: '⚙'}
            ]
        }
    ];

    var permState = {loaded: false, isSuperAdmin: false, codes: {}};

    function normalizeText(value) {
        return value === null || value === undefined ? '' : String(value);
    }

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

    function flattenPermissionUrls(permissions) {
        var set = {};
        (permissions || []).forEach(function (p) {
            String(p.urls || '').split(';').forEach(function (part) {
                var code = part.trim();
                if (code) {
                    set[code] = true;
                }
            });
        });
        return set;
    }

    function currentPath() {
        return window.location.pathname || '';
    }

    function isActive(item) {
        var path = currentPath();
        if (item.href && path === item.href) {
            return true;
        }
        return false;
    }

    function renderMenu(container, menu) {
        if (!container) {
            return;
        }
        container.innerHTML = '';
        menu.forEach(function (group) {
            var visibleItems = (group.items || []).filter(function (item) {
                return hasPermission(item.code);
            });
            if (!visibleItems.length) {
                return;
            }
            var groupWrap = document.createElement('div');
            groupWrap.className = 'admin-menu-group';

            var label = document.createElement('div');
            label.className = 'admin-menu-group-label';
            label.textContent = group.group;
            groupWrap.appendChild(label);

            visibleItems.forEach(function (item) {
                var a = document.createElement('a');
                a.className = 'admin-menu-item' + (isActive(item) ? ' is-active' : '');
                a.href = item.href;
                a.title = item.label;
                var icon = document.createElement('span');
                icon.className = 'admin-menu-item-icon';
                icon.textContent = normalizeText(item.icon) || '•';
                icon.setAttribute('aria-hidden', 'true');
                var text = document.createElement('span');
                text.className = 'admin-menu-item-text';
                text.textContent = item.label;
                a.appendChild(icon);
                a.appendChild(text);
                groupWrap.appendChild(a);
            });

            container.appendChild(groupWrap);
        });
    }

    function applyCollapse(shell, collapsed) {
        if (!shell) {
            return;
        }
        if (collapsed) {
            shell.classList.add('is-collapsed');
        } else {
            shell.classList.remove('is-collapsed');
        }
        try {
            localStorage.setItem(COLLAPSE_KEY, collapsed ? '1' : '0');
        } catch (e) {
            /* 忽略存储异常 */
        }
    }

    function bindCollapse(shell) {
        var btn = document.getElementById('adminCollapseBtn');
        if (!btn || !shell) {
            return;
        }
        btn.addEventListener('click', function () {
            applyCollapse(shell, !shell.classList.contains('is-collapsed'));
        });
    }

    function setTitle(options) {
        var el = document.getElementById('adminTitle');
        if (!el) {
            return;
        }
        var title = (options && options.title) || document.title || '';
        if (options && options.breadcrumb) {
            el.innerHTML = '<span class="admin-crumb-muted">' + escapeHtml(options.breadcrumb) + '</span> <span>/</span> ' + escapeHtml(title);
        } else {
            el.textContent = title;
        }
    }

    function escapeHtml(value) {
        return normalizeText(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function bindUserMenu() {
        var btn = document.getElementById('adminUserBtn');
        var menu = document.getElementById('adminUserMenu');
        var wrap = btn && btn.parentNode;
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
            if (wrap && !wrap.contains(event.target)) {
                menu.setAttribute('hidden', '');
                btn.setAttribute('aria-expanded', 'false');
            }
        });
        var logout = document.getElementById('adminLogoutBtn');
        if (logout) {
            logout.addEventListener('click', function () {
                window.location.href = '/logout';
            });
        }
    }

    function loadUserInfo() {
        if (typeof getRequest !== 'function') {
            return;
        }
        getRequest('/user/info', function (data) {
            if (!data) {
                return;
            }
            var nameEl = document.getElementById('adminUserName');
            var avatarEl = document.getElementById('adminUserAvatar');
            if (nameEl && data.nickname) {
                nameEl.textContent = data.nickname;
            }
            if (avatarEl && data.avatar) {
                avatarEl.src = data.avatar;
            }
        }, function () { /* 未登录或接口异常保持默认 */ });
    }

    function loadPermissionsThenRender(menuContainer, menu) {
        if (typeof getRequest !== 'function') {
            permState.loaded = true;
            renderMenu(menuContainer, menu);
            return;
        }
        getRequest('/user/role', function (role) {
            permState.isSuperAdmin = !!(role && Number(role.id) === 1);
            getRequest('/user/permission', function (permissions) {
                permState.codes = flattenPermissionUrls(permissions);
                permState.loaded = true;
                renderMenu(menuContainer, menu);
            }, function () {
                permState.codes = {};
                permState.loaded = true;
                renderMenu(menuContainer, menu);
            });
        }, function () {
            permState.loaded = true;
            renderMenu(menuContainer, menu);
        });
    }

    function init(options) {
        var shell = document.getElementById('adminShell');
        var menuContainer = document.getElementById('adminMenu');
        var menu = (options && options.menu) || DEFAULT_MENU;

        setTitle(options);

        var collapsed = false;
        try {
            collapsed = localStorage.getItem(COLLAPSE_KEY) === '1';
        } catch (e) {
            collapsed = false;
        }
        applyCollapse(shell, collapsed);

        bindCollapse(shell);
        bindUserMenu();
        loadUserInfo();
        loadPermissionsThenRender(menuContainer, menu);
    }

    window.HcAdminShell = {
        init: init,
        hasPermission: function (code) {
            return hasPermission(code);
        },
        refreshMenu: function () {
            renderMenu(document.getElementById('adminMenu'), DEFAULT_MENU);
        }
    };
})();
