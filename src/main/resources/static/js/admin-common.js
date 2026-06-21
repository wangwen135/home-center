(function (window) {
    'use strict';

    function escapeHtml(value) {
        return window.HomeCenter.escapeHtml(value);
    }

    function renderEmptyRow(colspan, text) {
        return '<tr><td colspan="' + colspan + '" class="text-center text-white-50">' +
            escapeHtml(text || '暂无数据') + '</td></tr>';
    }

    function renderStatusBadge(text, type) {
        var safeType = type || 'info';
        return '<span class="manage-badge manage-badge-' + safeType + '">' + escapeHtml(text) + '</span>';
    }

    function setBusy(element, busy, pendingText) {
        if (!element) {
            return;
        }
        if (busy) {
            if (!element.getAttribute('data-origin-text')) {
                element.setAttribute('data-origin-text', element.textContent || '');
            }
            element.disabled = true;
            if (pendingText) {
                element.textContent = pendingText;
            }
            return;
        }
        element.disabled = false;
        if (element.getAttribute('data-origin-text')) {
            element.textContent = element.getAttribute('data-origin-text');
            element.removeAttribute('data-origin-text');
        }
    }

    function withConfirm(title, message, callback) {
        if (window.showConfirm) {
            window.showConfirm(title, message, window.MsgTypes.WARNING, callback);
            return;
        }
        window.HomeCenter.confirm(message || title, {
            title: title || '确认操作',
            okText: '确认',
            cancelText: '取消'
        }).then(function (confirmed) {
            if (confirmed) {
                callback();
            }
        });
    }

    function toDatetimeLocalValue(value) {
        if (!value) {
            return '';
        }
        if (typeof value === 'number' || /^\d+$/.test(String(value))) {
            var date = new Date(Number(value));
            var pad = function (number) {
                return number < 10 ? '0' + number : String(number);
            };
            return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) +
                'T' + pad(date.getHours()) + ':' + pad(date.getMinutes());
        }
        return String(value).replace(' ', 'T').substring(0, 16);
    }

    window.AdminCommon = {
        escapeHtml: escapeHtml,
        renderEmptyRow: renderEmptyRow,
        renderStatusBadge: renderStatusBadge,
        setBusy: setBusy,
        withConfirm: withConfirm,
        toDatetimeLocalValue: toDatetimeLocalValue
    };
})(window);
