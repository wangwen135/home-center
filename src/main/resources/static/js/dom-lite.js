(function (window, document) {
    'use strict';

    function toArray(value) {
        if (!value) {
            return [];
        }
        if (value instanceof DomLite) {
            return value.elements.slice();
        }
        if (value.nodeType || value === window || value === document) {
            return [value];
        }
        if (typeof value.length === 'number' && typeof value !== 'string') {
            return Array.prototype.slice.call(value);
        }
        return [];
    }

    function DomLite(elements) {
        this.elements = elements || [];
        this.length = this.elements.length;
        for (var i = 0; i < this.elements.length; i += 1) {
            this[i] = this.elements[i];
        }
    }

    DomLite.prototype.each = function (callback) {
        this.elements.forEach(function (element, index) {
            callback.call(element, index, element);
        });
        return this;
    };

    DomLite.prototype.on = function (eventName, selector, handler) {
        if (typeof selector === 'function') {
            handler = selector;
            selector = null;
        }
        return this.each(function () {
            var root = this;
            root.addEventListener(eventName, function (event) {
                if (!selector) {
                    handler.call(root, event);
                    return;
                }
                var target = event.target.closest(selector);
                if (target && root.contains(target)) {
                    handler.call(target, event);
                }
            });
        });
    };

    DomLite.prototype.val = function (value) {
        if (value === undefined) {
            return this.length ? this[0].value : undefined;
        }
        return this.each(function () {
            this.value = value;
        });
    };

    DomLite.prototype.text = function (value) {
        if (value === undefined) {
            return this.length ? this[0].textContent : undefined;
        }
        return this.each(function () {
            this.textContent = value;
        });
    };

    DomLite.prototype.html = function (value) {
        if (value === undefined) {
            return this.length ? this[0].innerHTML : undefined;
        }
        return this.each(function () {
            this.innerHTML = value;
        });
    };

    DomLite.prototype.empty = function () {
        return this.html('');
    };

    DomLite.prototype.show = function () {
        return this.each(function () {
            this.style.display = '';
        });
    };

    DomLite.prototype.hide = function () {
        return this.each(function () {
            this.style.display = 'none';
        });
    };

    DomLite.prototype.addClass = function (className) {
        var names = String(className || '').split(/\s+/).filter(Boolean);
        return this.each(function () {
            this.classList.add.apply(this.classList, names);
        });
    };

    DomLite.prototype.removeClass = function (className) {
        var names = String(className || '').split(/\s+/).filter(Boolean);
        return this.each(function () {
            this.classList.remove.apply(this.classList, names);
        });
    };

    DomLite.prototype.prop = function (name, value) {
        if (value === undefined) {
            return this.length ? this[0][name] : undefined;
        }
        return this.each(function () {
            this[name] = value;
        });
    };

    DomLite.prototype.data = function (name) {
        if (!this.length) {
            return undefined;
        }
        var key = String(name || '').replace(/-([a-z])/g, function (_, letter) {
            return letter.toUpperCase();
        });
        return this[0].dataset ? this[0].dataset[key] : undefined;
    };

    DomLite.prototype.blur = function () {
        return this.each(function () {
            this.blur();
        });
    };

    DomLite.prototype.is = function (selector) {
        if (!this.length) {
            return false;
        }
        if (selector === ':checked') {
            return Boolean(this[0].checked);
        }
        return this[0].matches(selector);
    };

    DomLite.prototype.map = function (callback) {
        var result = this.elements.map(function (element, index) {
            return callback.call(element, index, element);
        });
        return {
            get: function (index) {
                return index === undefined ? result : result[index];
            }
        };
    };

    DomLite.prototype.get = function (index) {
        return index === undefined ? this.elements.slice() : this.elements[index];
    };

    function $(selector, context) {
        if (typeof selector === 'function') {
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', selector);
            } else {
                selector();
            }
            return new DomLite([]);
        }
        if (typeof selector === 'string') {
            return new DomLite(Array.prototype.slice.call((context || document).querySelectorAll(selector)));
        }
        return new DomLite(toArray(selector));
    }

    $.trim = function (value) {
        return value == null ? '' : String(value).trim();
    };

    $.get = function (url, callback) {
        return fetch(url)
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('request failed');
                }
                var contentType = response.headers.get('content-type') || '';
                return contentType.indexOf('application/json') >= 0 ? response.json() : response.text();
            })
            .then(callback);
    };

    window.$ = $;
    window.DomLite = DomLite;
})(window, document);
