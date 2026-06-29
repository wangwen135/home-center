/*
 * 待办事项前端逻辑（仅 todo.html 引用）
 * 依赖：js/app-theme.js → window.HomeCenter（request/get/post/put/del/toast/confirm/escapeHtml）
 *                   window.HcModal（弹窗）。数据全部走 /api/todo/**，按当前登录用户隔离。
 */
(function () {
    'use strict';

    var HC = window.HomeCenter;
    var esc = HC.escapeHtml;

    var PRIORITY_LABEL = { high: '高', medium: '中', low: '低' };
    var MAX_IMPORT_FILE_SIZE = 1024 * 1024;

    var state = {
        todos: [],
        view: 'list',        // list | board | calendar
        status: 'all',       // all | pending | done
        priority: 'all',     // all | high | medium | low
        tags: [],            // 选中的标签筛选
        keyword: '',
        multi: false,        // 多选模式
        selected: {},        // id -> true
        detailId: null,
        cal: null,           // {y, m}
        calBuckets: {}       // 日历按日分组缓存
    };

    var els = {};

    // ==================== 工具 ====================
    function $(id) { return document.getElementById(id); }
    function on(el, ev, fn) { if (el) { el.addEventListener(ev, fn); } }

    function tagList(t) {
        return t && t.tags ? String(t.tags).split(',').map(function (s) { return s.trim(); }).filter(Boolean) : [];
    }

    function parseDate(s) {
        if (!s) { return null; }
        var d = new Date(String(s).replace(' ', 'T'));
        return isNaN(d.getTime()) ? null : d;
    }

    function fmtDate(s) {
        return s ? String(s).slice(0, 16).replace('T', ' ') : '';
    }

    function dayKey(s) {
        return s ? String(s).slice(0, 10) : '';
    }

    function pad(n) { return n < 10 ? '0' + n : '' + n; }

    function fileStamp() {
        var d = new Date();
        return '' + d.getFullYear() + pad(d.getMonth() + 1) + pad(d.getDate());
    }

    function formatRelative(s) {
        var d = parseDate(s);
        if (!d) { return ''; }
        var diff = Date.now() - d.getTime();
        if (diff < 0) { diff = 0; }
        var m = Math.floor(diff / 60000);
        if (m < 1) { return '刚刚'; }
        if (m < 60) { return m + ' 分钟前'; }
        var h = Math.floor(m / 60);
        if (h < 24) { return h + ' 小时前'; }
        var day = Math.floor(h / 24);
        if (day < 30) { return day + ' 天前'; }
        return fmtDate(s);
    }

    function findTodo(id) {
        for (var i = 0; i < state.todos.length; i++) {
            if (Number(state.todos[i].id) === Number(id)) { return state.todos[i]; }
        }
        return null;
    }

    function selectedIds() {
        return Object.keys(state.selected).filter(function (id) { return state.selected[id]; });
    }

    function activate(btn) {
        if (!btn) { return; }
        Array.prototype.forEach.call(btn.parentElement.children, function (c) {
            c.classList.remove('is-active');
        });
        btn.classList.add('is-active');
    }

    // ==================== 筛选 ====================
    function filterTodos(ignorePriority) {
        var kw = state.keyword.trim().toLowerCase();
        return state.todos.filter(function (t) {
            if (state.status === 'pending' && t.completed) { return false; }
            if (state.status === 'done' && !t.completed) { return false; }
            if (!ignorePriority && state.priority !== 'all' && t.priority !== state.priority) { return false; }
            if (state.tags.length) {
                var tl = tagList(t);
                var match = false;
                for (var i = 0; i < state.tags.length; i++) {
                    if (tl.indexOf(state.tags[i]) >= 0) { match = true; break; }
                }
                if (!match) { return false; }
            }
            if (kw) {
                var hay = (t.content + ' ' + (t.tags || '')).toLowerCase();
                if (hay.indexOf(kw) < 0) { return false; }
            }
            return true;
        });
    }

    // ==================== 渲染 ====================
    function render() {
        renderCount();
        renderTagCloud();
        els.batchBar.hidden = !state.multi;
        if (state.view === 'list') { renderList(); }
        else if (state.view === 'board') { renderBoard(); }
        else { renderCalendar(); }
    }

    function renderCount() {
        var total = state.todos.length;
        var pending = state.todos.filter(function (t) { return !t.completed; }).length;
        els.todoCount.textContent = total ? ('共 ' + total + ' 项 · ' + pending + ' 项未完成') : '还没有待办';
    }

    function renderTagCloud() {
        var counts = {};
        state.todos.forEach(function (t) {
            tagList(t).forEach(function (tg) { counts[tg] = (counts[tg] || 0) + 1; });
        });
        var keys = Object.keys(counts).sort(function (a, b) { return counts[b] - counts[a]; });
        var html = '<div class="todo-tags-title">标签</div>';
        if (state.tags.length) {
            html += '<button type="button" class="todo-tag is-active" data-tag-clear>✕ 清除筛选</button>';
        }
        if (!keys.length && !state.tags.length) {
            html += '<div class="todo-muted">暂无标签</div>';
        }
        html += keys.map(function (k) {
            var on = state.tags.indexOf(k) >= 0;
            return '<button type="button" class="todo-tag' + (on ? ' is-active' : '') + '" data-tag="' + esc(k) + '">'
                + '<span>#' + esc(k) + '</span><span class="todo-tag-count">' + counts[k] + '</span></button>';
        }).join('');
        els.tagCloud.innerHTML = html;
    }

    function cardHtml(t) {
        var tags = tagList(t);
        var tagHtml = tags.length
            ? '<div class="todo-card-tags">' + tags.map(function (tg) {
                return '<span class="todo-pill todo-tagchip">#' + esc(tg) + '</span>';
            }).join('') + '</div>'
            : '';
        var check = state.multi
            ? '<input type="checkbox" class="todo-check"' + (state.selected[t.id] ? ' checked' : '') + ' data-act="select">'
            : '';
        return '<article class="todo-card prio-' + t.priority + (t.completed ? ' is-done' : '') + (state.selected[t.id] ? ' is-selected' : '') + '" data-id="' + t.id + '">'
            + '<div class="todo-card-top">'
            + check
            + '<span class="todo-badge prio-' + t.priority + '"><span class="todo-prio-dot"></span>' + PRIORITY_LABEL[t.priority] + '</span>'
            + '<div class="todo-acts">'
            + '<button type="button" class="todo-iconbtn is-done-toggle" data-act="toggle" title="' + (t.completed ? '标记未完成' : '标记完成') + '">' + (t.completed ? '↩' : '✓') + '</button>'
            + '<button type="button" class="todo-iconbtn" data-act="edit" title="编辑">✎</button>'
            + '<button type="button" class="todo-iconbtn is-danger" data-act="delete" title="删除">✕</button>'
            + '</div>'
            + '</div>'
            + '<div class="todo-card-content" data-act="detail">' + esc(t.content) + '</div>'
            + tagHtml
            + '<div class="todo-card-foot"><span class="todo-card-time">' + formatRelative(t.updateTime || t.createTime) + '</span></div>'
            + '</article>';
    }

    function emptyHtml() {
        if (!state.todos.length) {
            return '<div class="todo-empty">还没有待办，点击右上角「+ 新建」开始记录吧</div>';
        }
        return '<div class="todo-empty">没有匹配的待办</div>';
    }

    function renderList() {
        var list = filterTodos(false);
        if (!list.length) { els.view.innerHTML = emptyHtml(); return; }
        els.view.innerHTML = '<div class="todo-grid">' + list.map(cardHtml).join('') + '</div>';
    }

    function renderBoard() {
        var list = filterTodos(true);
        var buckets = { high: [], medium: [], low: [], done: [] };
        list.forEach(function (t) {
            if (t.completed) { buckets.done.push(t); } else { (buckets[t.priority] || buckets.low).push(t); }
        });
        var lanes = [
            { key: 'high', name: '高', cls: 'prio-high' },
            { key: 'medium', name: '中', cls: 'prio-medium' },
            { key: 'low', name: '低', cls: 'prio-low' },
            { key: 'done', name: '已完成', cls: '' }
        ];
        els.view.innerHTML = '<div class="todo-board">' + lanes.map(function (lane) {
            var items = buckets[lane.key];
            var body = items.length ? items.map(function (t) {
                return '<div class="todo-mini prio-' + t.priority + (t.completed ? ' is-done' : '') + '" data-act="detail" data-id="' + t.id + '">' + esc(t.content) + '</div>';
            }).join('') : '<div class="todo-muted" style="padding:4px 2px;font-size:12px;">无</div>';
            return '<div class="todo-lane"><div class="todo-lane-head">'
                + '<span class="' + lane.cls + '">' + lane.name + '</span>'
                + '<span class="todo-lane-count">' + items.length + '</span></div>'
                + '<div class="todo-lane-body">' + body + '</div></div>';
        }).join('') + '</div>';
    }

    function renderCalendar() {
        if (!state.cal) {
            var d = new Date();
            state.cal = { y: d.getFullYear(), m: d.getMonth() };
        }
        var y = state.cal.y, m = state.cal.m;
        var buckets = {};
        state.todos.forEach(function (t) {
            var k = dayKey(t.createTime);
            if (k) { (buckets[k] = buckets[k] || []).push(t); }
        });
        state.calBuckets = buckets;

        var today = new Date();
        var todayKey = today.getFullYear() + '-' + pad(today.getMonth() + 1) + '-' + pad(today.getDate());

        var startWd = new Date(y, m, 1).getDay();
        var daysInMonth = new Date(y, m + 1, 0).getDate();
        var prevDays = new Date(y, m, 0).getDate();

        var cells = [];
        for (var i = startWd - 1; i >= 0; i--) {
            cells.push({ day: prevDays - i, out: true,
                key: (m === 0 ? (y - 1) + '-12-' : y + '-' + pad(m) + '-') + pad(prevDays - i) });
        }
        for (var dd = 1; dd <= daysInMonth; dd++) {
            cells.push({ day: dd, out: false, key: y + '-' + pad(m + 1) + '-' + pad(dd) });
        }
        var next = 1;
        while (cells.length % 7 !== 0) {
            cells.push({ day: next, out: true,
                key: (m === 11 ? (y + 1) + '-01-' : y + '-' + pad(m + 2) + '-') + pad(next) });
            next++;
        }

        var wds = ['日', '一', '二', '三', '四', '五', '六'];
        var html = '<div class="todo-cal-head">'
            + '<button type="button" class="hc-button" data-cal="prev">‹</button>'
            + '<span class="todo-cal-title">' + y + '年' + (m + 1) + '月</span>'
            + '<button type="button" class="hc-button" data-cal="next">›</button>'
            + '<button type="button" class="hc-button" data-cal="today" style="margin-left:auto">今天</button>'
            + '</div>';
        html += '<div class="todo-cal">' + wds.map(function (w) { return '<div class="todo-cal-wd">' + w + '</div>'; }).join('');
        html += cells.map(function (c) {
            var items = buckets[c.key] || [];
            var dots = items.slice(0, 4).map(function (t) {
                return '<span class="todo-cal-dot ' + t.priority + '"></span>';
            }).join('');
            if (items.length > 4) { dots += '<span class="todo-cal-more">+' + (items.length - 4) + '</span>'; }
            var cls = 'todo-cal-cell' + (c.out ? ' is-out' : '') + (c.key === todayKey ? ' is-today' : '');
            return '<div class="' + cls + '" data-day="' + c.key + '">'
                + '<div class="todo-cal-day">' + c.day + '</div>'
                + '<div class="todo-cal-dots">' + dots + '</div></div>';
        }).join('') + '</div>';

        els.view.innerHTML = html;
    }

    // ==================== API ====================
    function load() {
        HC.get('/api/todo/list').then(function (res) {
            state.todos = (res && res.data) || [];
            render();
        }).catch(function (e) {
            HC.toast(e.message || '加载失败', 'danger');
            els.view.innerHTML = '<div class="todo-empty">加载失败，请刷新重试</div>';
        });
    }

    function toggleDone(id) {
        HC.put('/api/todo/' + id + '/toggle').then(function () {
            HC.toast('已更新', 'success');
            load();
        }).catch(function (e) { HC.toast(e.message || '操作失败', 'danger'); });
    }

    function removeOne(id) {
        HC.confirm('确定删除这条待办吗？', { title: '删除待办', okText: '删除' }).then(function (ok) {
            if (!ok) { return; }
            HC.del('/api/todo/' + id).then(function () {
                HC.toast('已删除', 'success');
                load();
            }).catch(function (e) { HC.toast(e.message || '删除失败', 'danger'); });
        });
    }

    // ==================== 弹窗 ====================
    function openModal(id) {
        window.HcModal.getOrCreateInstance($(id)).show();
    }

    function closeModal(id) {
        var el = $(id);
        if (el && el.__hcModal) { el.__hcModal.hide(); }
    }

    function openCreate() {
        els.editTitle.textContent = '新建待办';
        els.editContent.value = '';
        els.editTags.value = '';
        els.editCounter.textContent = '0';
        state.editingId = null;
        activate(els.editPriority.querySelector('[data-p="medium"]'));
        openModal('editModal');
        setTimeout(function () { els.editContent.focus(); }, 50);
    }

    function openEdit(id) {
        var t = findTodo(id);
        if (!t) { return; }
        els.editTitle.textContent = '编辑待办';
        els.editContent.value = t.content;
        els.editTags.value = (tagList(t).join(', '));
        els.editCounter.textContent = String(t.content.length);
        state.editingId = id;
        activate(els.editPriority.querySelector('[data-p="' + (t.priority || 'medium') + '"]'));
        openModal('editModal');
        setTimeout(function () { els.editContent.focus(); }, 50);
    }

    function submitEdit(e) {
        e.preventDefault();
        var content = els.editContent.value.trim();
        if (!content) { HC.toast('内容不能为空', 'warning'); return; }
        var active = els.editPriority.querySelector('.is-active');
        var priority = active ? active.getAttribute('data-p') : 'medium';
        var payload = { content: content, priority: priority, tags: els.editTags.value };
        var p;
        if (state.editingId) {
            payload.id = state.editingId;
            p = HC.put('/api/todo', payload);
        } else {
            p = HC.post('/api/todo', payload);
        }
        p.then(function () {
            closeModal('editModal');
            HC.toast(state.editingId ? '已保存' : '已新增', 'success');
            load();
        }).catch(function (err) { HC.toast(err.message || '保存失败', 'danger'); });
    }

    function detailOf(id) {
        var t = findTodo(id);
        if (!t) { return; }
        state.detailId = id;
        var tags = tagList(t);
        els.detailContent.innerHTML =
            '<div class="modal-header"><h6 class="modal-title">待办详情</h6>'
            + '<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="关闭"></button></div>'
            + '<div class="modal-body">'
            + '<div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;margin-bottom:10px;">'
            + '<span class="todo-badge prio-' + t.priority + '"><span class="todo-prio-dot"></span>' + PRIORITY_LABEL[t.priority] + '优先级</span>'
            + (tags.length ? tags.map(function (tg) { return '<span class="todo-pill todo-tagchip">#' + esc(tg) + '</span>'; }).join('') : '')
            + '</div>'
            + '<div class="todo-detail-content' + (t.completed ? ' is-done' : '') + '">' + esc(t.content) + '</div>'
            + '<div class="todo-detail-meta">'
            + '<span>状态：' + (t.completed ? '已完成' : '未完成') + '</span>'
            + '<span>创建：' + fmtDate(t.createTime) + '</span>'
            + '<span>更新：' + formatRelative(t.updateTime || t.createTime) + '</span>'
            + '</div>'
            + '</div>'
            + '<div class="modal-footer">'
            + '<button type="button" class="hc-button" data-dact="edit">编辑</button>'
            + '<button type="button" class="hc-button" data-dact="toggle">' + (t.completed ? '标记未完成' : '标记完成') + '</button>'
            + '<button type="button" class="hc-button hc-button-danger" data-dact="delete">删除</button>'
            + '<button type="button" class="hc-button" data-bs-dismiss="modal">关闭</button>'
            + '</div>';
        openModal('detailModal');
    }

    function openDayModal(key) {
        var items = state.calBuckets[key] || [];
        $('dayTitle').textContent = key.replace(/-/g, ' / ') + '（' + items.length + ' 项）';
        $('dayBody').innerHTML = items.length ? items.map(function (t) {
            return '<div class="todo-mini prio-' + t.priority + (t.completed ? ' is-done' : '') + '" data-act="detail" data-id="' + t.id + '">'
                + '<span class="todo-badge prio-' + t.priority + '" style="margin-right:6px;">' + PRIORITY_LABEL[t.priority] + '</span>'
                + esc(t.content) + '</div>';
        }).join('') : '<p class="todo-muted">这天没有待办</p>';
        openModal('dayModal');
    }

    // ==================== 多选 / 批量 ====================
    function toggleSelect(id) {
        if (state.selected[id]) { delete state.selected[id]; } else { state.selected[id] = true; }
        els.batchInfo.textContent = '已选 ' + selectedIds().length + ' 项';
        render();
    }

    function toggleMulti() {
        state.multi = !state.multi;
        if (state.multi) {
            state.view = 'list';
            activate(document.querySelector('[data-view="list"]'));
        }
        state.selected = {};
        els.btnMulti.textContent = state.multi ? '完成多选' : '多选';
        render();
    }

    function exitMulti() {
        state.multi = false;
        state.selected = {};
        els.btnMulti.textContent = '多选';
        render();
    }

    function batchDelete() {
        var ids = selectedIds();
        if (!ids.length) { HC.toast('请先选择待办', 'warning'); return; }
        HC.confirm('删除选中的 ' + ids.length + ' 条待办？', { title: '批量删除', okText: '删除' }).then(function (ok) {
            if (!ok) { return; }
            HC.post('/api/todo/batch/delete', { ids: ids.map(Number) }).then(function () {
                HC.toast('已删除', 'success');
                exitMulti();
                load();
            }).catch(function (e) { HC.toast(e.message || '删除失败', 'danger'); });
        });
    }

    function batchPriority(p) {
        var ids = selectedIds();
        if (!ids.length) { HC.toast('请先选择待办', 'warning'); return; }
        HC.put('/api/todo/batch/priority', { ids: ids.map(Number), priority: p }).then(function () {
            HC.toast('已更新优先级', 'success');
            load();
        }).catch(function (e) { HC.toast(e.message || '更新失败', 'danger'); });
    }

    function batchDone() {
        var ids = selectedIds();
        if (!ids.length) { HC.toast('请先选择待办', 'warning'); return; }
        var pending = state.todos.filter(function (t) {
            return ids.indexOf(String(t.id)) >= 0 && !t.completed;
        });
        if (!pending.length) { HC.toast('选中项均已完成', 'info'); return; }
        Promise.all(pending.map(function (t) { return HC.put('/api/todo/' + t.id + '/toggle'); })).then(function () {
            HC.toast('已标记完成', 'success');
            load();
        }).catch(function (e) { HC.toast(e.message || '操作失败', 'danger'); });
    }

    function clearCompleted() {
        HC.confirm('清除全部已完成的待办？', { title: '清除已完成', okText: '清除' }).then(function (ok) {
            if (!ok) { return; }
            HC.post('/api/todo/clear-completed').then(function (res) {
                HC.toast('已清除 ' + (res && res.data != null ? res.data : 0) + ' 条', 'success');
                load();
            }).catch(function (e) { HC.toast(e.message || '清除失败', 'danger'); });
        });
    }

    // ==================== 导入 / 导出 ====================
    function exportData() {
        HC.get('/api/todo/export').then(function (res) {
            var data = JSON.stringify((res && res.data) || [], null, 2);
            var blob = new Blob([data], { type: 'application/json' });
            var url = URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = url;
            a.download = 'todo-' + fileStamp() + '.json';
            document.body.appendChild(a);
            a.click();
            a.remove();
            URL.revokeObjectURL(url);
            HC.toast('已导出', 'success');
        }).catch(function (e) { HC.toast(e.message || '导出失败', 'danger'); });
    }

    function doImport() {
        var f = els.importFile.files[0];
        if (!f) { HC.toast('请选择 JSON 文件', 'warning'); return; }
        if (f.size > MAX_IMPORT_FILE_SIZE) {
            HC.toast('导入文件不能超过 1MB', 'warning');
            return;
        }
        var reader = new FileReader();
        reader.onload = function () {
            var arr;
            try {
                arr = JSON.parse(reader.result);
                if (!Array.isArray(arr)) { throw new Error('内容不是数组'); }
            } catch (err) {
                HC.toast('JSON 解析失败：' + err.message, 'danger');
                return;
            }
            HC.post('/api/todo/import', arr).then(function (res) {
                closeModal('importModal');
                HC.toast('已导入 ' + (res && res.data != null ? res.data : 0) + ' 条', 'success');
                els.importFile.value = '';
                load();
            }).catch(function (e) { HC.toast(e.message || '导入失败', 'danger'); });
        };
        reader.readAsText(f);
    }

    // ==================== 事件绑定 ====================
    function onViewClick(e) {
        var card = e.target.closest('.todo-card');
        if (card) {
            var id = Number(card.getAttribute('data-id'));
            var actEl = e.target.closest('[data-act]');
            var act = actEl ? actEl.getAttribute('data-act') : '';
            if (act === 'toggle') { toggleDone(id); return; }
            if (act === 'edit') { openEdit(id); return; }
            if (act === 'delete') { removeOne(id); return; }
            if (act === 'select') { toggleSelect(id); return; }
            if (state.multi) { toggleSelect(id); } else { detailOf(id); }
            return;
        }
        var mini = e.target.closest('.todo-mini[data-act="detail"]');
        if (mini) { detailOf(Number(mini.getAttribute('data-id'))); return; }
        var cell = e.target.closest('.todo-cal-cell');
        if (cell) { openDayModal(cell.getAttribute('data-day')); return; }
        var calNav = e.target.closest('[data-cal]');
        if (calNav) {
            if (!state.cal) { state.cal = { y: new Date().getFullYear(), m: new Date().getMonth() }; }
            if (calNav.getAttribute('data-cal') === 'prev') {
                state.cal.m--; if (state.cal.m < 0) { state.cal.m = 11; state.cal.y--; }
            } else if (calNav.getAttribute('data-cal') === 'next') {
                state.cal.m++; if (state.cal.m > 11) { state.cal.m = 0; state.cal.y++; }
            } else {
                var t = new Date(); state.cal = { y: t.getFullYear(), m: t.getMonth() };
            }
            renderCalendar();
            return;
        }
    }

    function bindEvents() {
        on(els.btnNew, 'click', openCreate);
        on(els.btnMulti, 'click', toggleMulti);
        on(els.btnClear, 'click', clearCompleted);
        on(els.btnImport, 'click', function () { openModal('importModal'); });
        on(els.btnExport, 'click', exportData);

        on(els.search, 'input', function () {
            clearTimeout(els.search._t);
            els.search._t = setTimeout(function () { state.keyword = els.search.value; render(); }, 250);
        });

        document.addEventListener('click', function (e) {
            var b;
            if ((b = e.target.closest('[data-view]'))) { state.view = b.getAttribute('data-view'); activate(b); render(); }
            else if ((b = e.target.closest('[data-status]'))) { state.status = b.getAttribute('data-status'); activate(b); render(); }
            else if ((b = e.target.closest('[data-priority]'))) { state.priority = b.getAttribute('data-priority'); activate(b); render(); }
            var tagBtn = e.target.closest('[data-tag]');
            if (tagBtn) {
                var tg = tagBtn.getAttribute('data-tag');
                var idx = state.tags.indexOf(tg);
                if (idx >= 0) { state.tags.splice(idx, 1); } else { state.tags.push(tg); }
                render();
            }
            if (e.target.closest('[data-tag-clear]')) { state.tags = []; render(); }
        });

        on(els.view, 'click', onViewClick);

        on(els.btnBatchDelete, 'click', batchDelete);
        on(els.btnBatchDone, 'click', batchDone);
        on(els.btnBatchCancel, 'click', exitMulti);
        Array.prototype.forEach.call(document.querySelectorAll('[data-batch-priority]'), function (b) {
            on(b, 'click', function () { batchPriority(b.getAttribute('data-batch-priority')); });
        });

        on(els.editForm, 'submit', submitEdit);
        on(els.editPriority, 'click', function (e) {
            var b = e.target.closest('[data-p]');
            if (b) { activate(b); }
        });
        on(els.editContent, 'input', function () { els.editCounter.textContent = String(els.editContent.value.length); });

        on(els.btnImportDo, 'click', doImport);

        on(els.detailContent, 'click', function (e) {
            var b = e.target.closest('[data-dact]');
            if (!b || state.detailId == null) { return; }
            var act = b.getAttribute('data-dact');
            var id = state.detailId;
            if (act === 'edit') { closeModal('detailModal'); openEdit(id); }
            else if (act === 'toggle') { closeModal('detailModal'); toggleDone(id); }
            else if (act === 'delete') { closeModal('detailModal'); removeOne(id); }
        });

        on($('dayBody'), 'click', function (e) {
            var m = e.target.closest('[data-act="detail"]');
            if (m) { closeModal('dayModal'); detailOf(Number(m.getAttribute('data-id'))); }
        });
    }

    // ==================== 启动 ====================
    function init() {
        els.btnNew = $('btnNew');
        els.btnMulti = $('btnMulti');
        els.btnClear = $('btnClear');
        els.btnImport = $('btnImport');
        els.btnExport = $('btnExport');
        els.search = $('todoSearch');
        els.view = $('todoView');
        els.tagCloud = $('tagCloud');
        els.todoCount = $('todoCount');
        els.batchBar = $('batchBar');
        els.batchInfo = $('batchInfo');
        els.btnBatchDelete = $('btnBatchDelete');
        els.btnBatchDone = $('btnBatchDone');
        els.btnBatchCancel = $('btnBatchCancel');
        els.editForm = $('editForm');
        els.editTitle = $('editTitle');
        els.editContent = $('editContent');
        els.editTags = $('editTags');
        els.editCounter = $('editCounter');
        els.editPriority = $('editPriority');
        els.detailContent = $('detailContent');
        els.importFile = $('importFile');
        els.btnImportDo = $('btnImportDo');

        bindEvents();
        load();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
