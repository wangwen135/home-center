/**
 * 私有导航页面
 *
 * - 按分组渲染当前用户的私有导航入口
 * - 区分入口类型：nginx_proxy/external/intranet 为可跳转链接；note/ssh/rdp 为说明/命令类入口
 * - 说明/命令类入口点击后弹出说明并提供复制命令，不直接跳转
 * - 未上传图标时使用默认占位图标
 */
(function () {
    var state = {
        categories: [],
        links: [],
        query: ''
    };

    // 入口类型元数据：badge 文案 + 是否为说明/命令类
    var ENTRY_META = {
        nginx_proxy: {label: '代理', note: false, badge: ''},
        external: {label: '外链', note: false, badge: 'muted'},
        intranet: {label: '内网', note: false, badge: 'success'},
        note: {label: '说明', note: true, badge: 'note'},
        ssh: {label: 'SSH', note: true, badge: 'note'},
        rdp: {label: 'RDP', note: true, badge: 'note'}
    };

    function normalizeText(value) {
        return value === null || value === undefined ? '' : String(value);
    }

    function entryMeta(type) {
        return ENTRY_META[normalizeText(type)] || ENTRY_META.external;
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

    function createIcon(link) {
        var icon = document.createElement('div');
        icon.className = 'pn-icon';
        icon.setAttribute('aria-hidden', 'true');
        var emojiText = normalizeText(link.iconEmoji);
        if (isImageIcon(link.icon)) {
            var img = document.createElement('img');
            img.src = normalizeText(link.icon);
            img.alt = '';
            icon.style.background = '#fff';
            icon.appendChild(img);
        } else {
            icon.style.background = hashColor(emojiText || link.title);
            icon.style.color = '#10384f';
            icon.textContent = makeText(emojiText || link.title);
        }
        return icon;
    }

    function createBadge(meta) {
        var badge = document.createElement('span');
        badge.className = 'pn-badge ' + (meta.badge || '');
        badge.textContent = meta.label;
        return badge;
    }

    function createLinkTile(link, meta) {
        var tile = document.createElement('a');
        tile.className = 'pn-tile';
        tile.href = normalizeText(link.url) || '#';
        tile.target = '_blank';
        tile.rel = 'noopener noreferrer';
        tile.setAttribute('aria-label', normalizeText(link.title) + '：在新窗口打开');
        fillTile(tile, link, meta);
        return tile;
    }

    function createNoteTile(link, meta) {
        var tile = document.createElement('button');
        tile.type = 'button';
        tile.className = 'pn-tile is-note';
        tile.setAttribute('aria-label', normalizeText(link.title) + '：查看说明或复制命令');
        tile.addEventListener('click', function () {
            openInstruction(link, meta);
        });
        fillTile(tile, link, meta);
        return tile;
    }

    function fillTile(tile, link, meta) {
        var body = document.createElement('div');
        body.className = 'pn-tile-body';

        var title = document.createElement('div');
        title.className = 'pn-tile-title';
        var titleText = document.createElement('span');
        titleText.textContent = normalizeText(link.title) || '未命名入口';
        title.appendChild(titleText);
        title.appendChild(createBadge(meta));

        var desc = document.createElement('div');
        desc.className = 'pn-tile-desc';
        desc.textContent = normalizeText(link.description) || (meta.note ? '点击查看说明 / 复制命令' : '点击打开该入口');

        body.appendChild(title);
        body.appendChild(desc);

        tile.appendChild(createIcon(link));
        tile.appendChild(body);

        // 可跳转入口展示轻量健康状态小圆点（正常/异常/超时/未知），不展示详细错误
        if (!meta.note) {
            tile.appendChild(createHealthDot(link.checkStatus));
        }
    }

    function createHealthDot(status) {
        var dot = document.createElement('span');
        dot.className = 'pn-health-dot ' + (normalizeText(status) || 'unknown');
        dot.setAttribute('aria-hidden', 'true');
        return dot;
    }

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
            ' <span class="pn-badge ' + (meta.badge || '') + '">' + meta.label + '</span></h6>' +
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

    function escapeHtml(value) {
        return normalizeText(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function createGroup(category, links) {
        var group = document.createElement('section');
        group.className = 'pn-group';

        var title = document.createElement('h2');
        title.className = 'pn-group-title';
        var icon = document.createElement('span');
        icon.className = 'pn-group-icon';
        icon.textContent = normalizeText(category.icon) || '◆';
        var name = document.createElement('span');
        name.textContent = normalizeText(category.name) || '未命名分组';
        title.appendChild(icon);
        title.appendChild(name);

        var grid = document.createElement('div');
        grid.className = 'pn-grid';
        if (links.length) {
            links.forEach(function (link) {
                var meta = entryMeta(link.entryType);
                grid.appendChild(meta.note ? createNoteTile(link, meta) : createLinkTile(link, meta));
            });
        } else {
            var empty = document.createElement('div');
            empty.className = 'pn-empty';
            empty.textContent = '该分组暂无入口';
            grid.appendChild(empty);
        }

        group.appendChild(title);
        group.appendChild(grid);
        return group;
    }

    function matchesQuery(link, category, query) {
        if (!query) {
            return true;
        }
        var haystack = [
            link.title, link.description, link.url, link.icon, link.iconEmoji,
            link.entryType, link.instruction,
            category && category.name, category && category.icon
        ].map(function (value) {
            return normalizeText(value).toLowerCase();
        }).join(' ');
        return haystack.indexOf(query) !== -1;
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

        if (!categories.length) {
            content.appendChild(createEmpty('暂无私有导航分组，可在管理中新增'));
            return;
        }

        var rendered = 0;
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
            content.appendChild(createEmpty('未找到匹配的私有导航入口'));
        }
    }

    function compareSort(a, b) {
        var left = Number(a.sortOrder || 0);
        var right = Number(b.sortOrder || 0);
        if (left !== right) {
            return left - right;
        }
        return Number(a.id || 0) - Number(b.id || 0);
    }

    function createEmpty(text) {
        var empty = document.createElement('div');
        empty.className = 'pn-empty';
        empty.textContent = text;
        return empty;
    }

    function load() {
        getRequest('/api/private-nav/all', function (data) {
            state.categories = (data && data.categories) || [];
            state.links = (data && data.links) || [];
            render();
        }, function () {
            var content = document.getElementById('pnContent');
            if (content) {
                while (content.firstChild) {
                    content.removeChild(content.firstChild);
                }
                content.appendChild(createEmpty('私有导航加载失败，请刷新重试'));
            }
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
        load();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
