(function () {
    var storageKey = 'homeCenterTheme';

    function readStorage(key) {
        try {
            return localStorage.getItem(key);
        } catch (error) {
            return null;
        }
    }

    function writeStorage(key, value) {
        try {
            localStorage.setItem(key, value);
        } catch (error) {
            // Ignore storage failures in restricted browsers.
        }
    }

    function getSavedTheme() {
        var theme = readStorage(storageKey) || readStorage('publicNavTheme');
        return theme === 'dark' ? 'dark' : 'light';
    }

    function applyTheme(theme) {
        var normalized = theme === 'dark' ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', normalized);
        writeStorage(storageKey, normalized);
        writeStorage('publicNavTheme', normalized);
        updateToggleText(normalized);
    }

    function updateToggleText(theme) {
        var text = theme === 'dark' ? '浅色' : '深色';
        var label = theme === 'dark' ? '切换为浅色主题' : '切换为深色主题';
        Array.prototype.forEach.call(document.querySelectorAll('[data-theme-toggle-text]'), function (node) {
            node.textContent = text;
        });
        Array.prototype.forEach.call(document.querySelectorAll('.hc-theme-toggle'), function (button) {
            button.setAttribute('title', label);
            button.setAttribute('aria-label', label);
        });
    }
    function bindToggles() {
        Array.prototype.forEach.call(document.querySelectorAll('.hc-theme-toggle'), function (button) {
            if (button.getAttribute('data-theme-bound') === 'true') {
                return;
            }
            button.setAttribute('data-theme-bound', 'true');
            button.addEventListener('click', function () {
                var current = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
                applyTheme(current === 'dark' ? 'light' : 'dark');
            });
        });
        updateToggleText(document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light');
    }

    function toJson(response) {
        var contentType = response.headers.get('content-type') || '';
        if (contentType.indexOf('application/json') >= 0) {
            return response.json();
        }
        return response.text();
    }

    function request(url, options) {
        var config = options || {};
        var headers = config.headers || {};
        if (config.body !== undefined && typeof config.body !== 'string' && !(config.body instanceof FormData)) {
            config.body = JSON.stringify(config.body);
            headers['Content-Type'] = headers['Content-Type'] || 'application/json';
        }
        config.headers = headers;
        return fetch(url, config).then(function (response) {
            return toJson(response).then(function (body) {
                if (!response.ok) {
                    var message = body && body.message ? body.message : '请求失败';
                    throw new Error(message);
                }
                return body;
            });
        });
    }

    function showToast(message, type) {
        var root = document.querySelector('.hc-toast-root');
        if (!root) {
            root = document.createElement('div');
            root.className = 'hc-toast-root';
            document.body.appendChild(root);
        }
        var toast = document.createElement('div');
        toast.className = 'hc-toast hc-toast-' + (type || 'info');
        toast.textContent = message || '';
        root.appendChild(toast);
        window.setTimeout(function () {
            toast.classList.add('is-leaving');
            window.setTimeout(function () {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 180);
        }, 2600);
    }

    function showConfirm(message, options) {
        var config = options || {};
        return new Promise(function (resolve) {
            var backdrop = document.createElement('div');
            backdrop.className = 'hc-dialog-backdrop';
            var dialog = document.createElement('div');
            dialog.className = 'hc-dialog';
            dialog.innerHTML = '<div class="hc-dialog-header">' +
                '<strong>' + escapeHtml(config.title || '确认操作') + '</strong>' +
                '</div>' +
                '<div class="hc-dialog-body">' + escapeHtml(message || '') + '</div>' +
                '<div class="hc-dialog-footer">' +
                '<button type="button" class="hc-button" data-hc-cancel>' + escapeHtml(config.cancelText || '取消') + '</button>' +
                '<button type="button" class="hc-button hc-button-primary" data-hc-ok>' + escapeHtml(config.okText || '确认') + '</button>' +
                '</div>';
            backdrop.appendChild(dialog);
            document.body.appendChild(backdrop);

            function close(value) {
                if (backdrop.parentNode) {
                    backdrop.parentNode.removeChild(backdrop);
                }
                resolve(value);
            }

            dialog.querySelector('[data-hc-cancel]').addEventListener('click', function () {
                close(false);
            });
            dialog.querySelector('[data-hc-ok]').addEventListener('click', function () {
                close(true);
            });
            backdrop.addEventListener('click', function (event) {
                if (event.target === backdrop) {
                    close(false);
                }
            });
        });
    }

    function serializeForm(form) {
        var data = {};
        Array.prototype.forEach.call(new FormData(form).entries(), function (entry) {
            data[entry[0]] = entry[1];
        });
        return data;
    }

    function renderTable(tbody, rows, columns, emptyText) {
        var target = typeof tbody === 'string' ? document.querySelector(tbody) : tbody;
        if (!target) {
            return;
        }
        if (!rows || !rows.length) {
            target.innerHTML = '<tr><td class="text-center hc-muted" colspan="' + columns.length + '">' +
                escapeHtml(emptyText || '暂无数据') + '</td></tr>';
            return;
        }
        target.innerHTML = rows.map(function (row, index) {
            return '<tr>' + columns.map(function (column) {
                var value = typeof column.render === 'function' ? column.render(row, index) : row[column.key];
                return '<td>' + (column.html ? value : escapeHtml(value)) + '</td>';
            }).join('') + '</tr>';
        }).join('');
    }

    function escapeHtml(value) {
        if (value === null || value === undefined) {
            return '';
        }
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function initDismissButtons() {
        document.addEventListener('click', function (event) {
            var button = event.target.closest('[data-bs-dismiss="modal"]');
            if (!button) {
                return;
            }
            var modal = button.closest('.modal');
            if (modal && window.HcModal) {
                window.HcModal.getOrCreateInstance(modal).hide();
            }
        });
    }

    function initTabs() {
        Array.prototype.forEach.call(document.querySelectorAll('[data-bs-toggle="pill"]'), function (button) {
            if (button.getAttribute('data-hc-tab-bound') === 'true') {
                return;
            }
            button.setAttribute('data-hc-tab-bound', 'true');
            button.addEventListener('click', function () {
                var targetSelector = button.getAttribute('data-bs-target');
                var target = targetSelector ? document.querySelector(targetSelector) : null;
                if (!target) {
                    return;
                }
                var tabRoot = button.closest('[role="tablist"]');
                if (tabRoot) {
                    Array.prototype.forEach.call(tabRoot.querySelectorAll('[data-bs-toggle="pill"]'), function (tab) {
                        tab.classList.remove('active');
                        tab.setAttribute('aria-selected', 'false');
                    });
                }
                Array.prototype.forEach.call(document.querySelectorAll('.tab-pane'), function (pane) {
                    pane.classList.remove('show', 'active');
                });
                button.classList.add('active');
                button.setAttribute('aria-selected', 'true');
                target.classList.add('show', 'active');
            });
        });
    }

    function ensureModalApi() {
        if (window.HcModal) {
            return;
        }

        function HcModal(element) {
            this.element = element;
            this.backdrop = null;
            element.__hcModal = this;
        }

        HcModal.prototype.show = function () {
            if (!this.element) {
                return;
            }
            this.backdrop = document.createElement('div');
            this.backdrop.className = 'modal-backdrop show';
            document.body.appendChild(this.backdrop);
            this.element.style.display = 'block';
            this.element.removeAttribute('aria-hidden');
            this.element.setAttribute('aria-modal', 'true');
            this.element.classList.add('show');
            document.body.classList.add('modal-open');
        };

        HcModal.prototype.hide = function () {
            if (!this.element) {
                return;
            }
            this.element.classList.remove('show');
            this.element.style.display = 'none';
            this.element.setAttribute('aria-hidden', 'true');
            this.element.removeAttribute('aria-modal');
            document.body.classList.remove('modal-open');
            if (this.backdrop && this.backdrop.parentNode) {
                this.backdrop.parentNode.removeChild(this.backdrop);
            }
            this.backdrop = null;
            var event;
            if (typeof Event === 'function') {
                event = new Event('hidden.bs.modal');
            } else {
                event = document.createEvent('Event');
                event.initEvent('hidden.bs.modal', true, true);
            }
            this.element.dispatchEvent(event);
        };

        HcModal.prototype.dispose = function () {
            this.hide();
            if (this.element) {
                this.element.__hcModal = null;
            }
        };

        HcModal.getOrCreateInstance = function (element) {
            return element.__hcModal || new HcModal(element);
        };

        window.HcModal = HcModal;
    }

    function initCompatLayer() {
        ensureModalApi();
        initDismissButtons();
        initTabs();
    }

    applyTheme(getSavedTheme());
    ensureModalApi();

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () {
            bindToggles();
            initDismissButtons();
            initTabs();
        });
    } else {
        bindToggles();
        initDismissButtons();
        initTabs();
    }

    window.HomeCenterTheme = {
        get: function () {
            return document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
        },
        set: applyTheme,
        toggle: function () {
            applyTheme(this.get() === 'dark' ? 'light' : 'dark');
        },
        bind: bindToggles
    };

    window.HomeCenter = {
        request: request,
        get: function (url) {
            return request(url);
        },
        post: function (url, body) {
            return request(url, {
                method: 'POST',
                body: body
            });
        },
        put: function (url, body) {
            return request(url, {
                method: 'PUT',
                body: body
            });
        },
        del: function (url) {
            return request(url, {
                method: 'DELETE'
            });
        },
        toast: showToast,
        confirm: showConfirm,
        serializeForm: serializeForm,
        renderTable: renderTable,
        escapeHtml: escapeHtml
    };
})();
