/**
 * 1. 发送请求
 * 2. 各种弹出框
 */

(function () {
    var navState = {
        categories: [],
        links: [],
        query: ''
    };

    function hasPublicNav() {
        return Boolean(document.getElementById('navContent'));
    }

    function syncBodyThemeClass() {
        var isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        if (document.body) {
            document.body.classList.toggle('dark-theme', isDark);
        }
    }

    function bindThemeSync() {
        syncBodyThemeClass();
        if (!window.MutationObserver) {
            return;
        }
        var observer = new MutationObserver(syncBodyThemeClass);
        observer.observe(document.documentElement, {
            attributes: true,
            attributeFilter: ['data-theme']
        });
        window.addEventListener('beforeunload', function () {
            observer.disconnect();
        }, {once: true});
    }

    function normalizeText(value) {
        return value === null || value === undefined ? '' : String(value);
    }

    function compareSort(a, b) {
        var left = Number(a.sortOrder || 0);
        var right = Number(b.sortOrder || 0);
        if (left !== right) {
            return left - right;
        }
        return Number(a.id || 0) - Number(b.id || 0);
    }

    function hashColor(str) {
        var text = normalizeText(str) || 'Home';
        var h = 0;
        var palettes = [
            ['#d9ecff', '#8fc7ff', '#0f4f82'],
            ['#d8f5f2', '#80d8cf', '#0a5a52'],
            ['#e4edff', '#9bb8f4', '#274a87'],
            ['#ddf7e8', '#8bdcae', '#145a35'],
            ['#e7eef5', '#b9c9d8', '#32485d'],
            ['#e1f4ff', '#98d6ee', '#155a73']
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
        if (/[\u4e00-\u9fff]/.test(chars[0])) {
            return chars.slice(0, 2).join('');
        }
        return text.replace(/\s+/g, '').slice(0, 4).toUpperCase();
    }

    function isImageIcon(value) {
        var text = normalizeText(value).trim();
        return /^(https?:)?\/\//i.test(text) ||
            /^data:image\//i.test(text) ||
            /\.(png|jpe?g|gif|webp|svg)(\?.*)?$/i.test(text);
    }

    function clearNode(node) {
        while (node.firstChild) {
            node.removeChild(node.firstChild);
        }
    }

    function createEmptyMessage(text) {
        var empty = document.createElement('div');
        empty.className = 'empty-message';
        empty.textContent = text;
        return empty;
    }

    function renderTile(link) {
        var titleText = normalizeText(link.title) || '未命名入口';
        var descText = normalizeText(link.description) || '点击打开该入口';
        var href = normalizeText(link.url) || '#';

        var tile = document.createElement('a');
        tile.className = 'tile';
        tile.href = href;
        tile.target = '_blank';
        tile.rel = 'noopener noreferrer';
        tile.setAttribute('aria-label', titleText + ': ' + descText);

        var front = document.createElement('div');
        front.className = 'tile-front';

        var icon = document.createElement('div');
        icon.className = 'icon';
        if (isImageIcon(link.icon)) {
            var img = document.createElement('img');
            img.src = normalizeText(link.icon);
            img.alt = '';
            icon.appendChild(img);
        } else {
            icon.style.background = hashColor(link.icon || titleText);
            icon.style.color = '#10384f';
            icon.textContent = makeText(link.icon || titleText);
        }
        icon.setAttribute('aria-hidden', 'true');

        var title = document.createElement('div');
        title.className = 'title';
        title.textContent = titleText;

        front.appendChild(icon);
        front.appendChild(title);

        var back = document.createElement('div');
        back.className = 'tile-back';

        var backTitle = document.createElement('div');
        backTitle.className = 'title';
        backTitle.textContent = titleText;

        var desc = document.createElement('div');
        desc.className = 'desc';
        desc.textContent = descText;

        back.appendChild(backTitle);
        back.appendChild(desc);
        tile.appendChild(front);
        tile.appendChild(back);
        return tile;
    }

    function renderGroup(category, links) {
        var group = document.createElement('section');
        group.className = 'group';

        var title = document.createElement('h2');
        title.className = 'group-title';

        var icon = document.createElement('span');
        icon.className = 'group-icon';
        icon.textContent = normalizeText(category.icon) || '◆';
        icon.setAttribute('aria-hidden', 'true');

        var name = document.createElement('span');
        name.textContent = normalizeText(category.name) || '未命名分组';

        title.appendChild(icon);
        title.appendChild(name);

        var grid = document.createElement('div');
        grid.className = 'grid';

        if (links.length) {
            links.forEach(function (link) {
                grid.appendChild(renderTile(link));
            });
        } else {
            grid.appendChild(createEmptyMessage('该分组暂无链接'));
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
            link.title,
            link.description,
            link.url,
            link.icon,
            category && category.name,
            category && category.icon
        ].map(function (value) {
            return normalizeText(value).toLowerCase();
        }).join(' ');
        return haystack.indexOf(query) !== -1;
    }

    function renderNavigation() {
        var content = document.getElementById('navContent');
        if (!content) {
            return;
        }

        clearNode(content);

        var query = navState.query.trim().toLowerCase();
        var categories = navState.categories.slice().sort(compareSort);
        var links = navState.links.slice().sort(compareSort);

        if (!categories.length) {
            content.appendChild(createEmptyMessage('暂无公开导航'));
            return;
        }

        var renderedCount = 0;
        categories.forEach(function (category) {
            var categoryLinks = links.filter(function (link) {
                return String(link.categoryId) === String(category.id) && matchesQuery(link, category, query);
            });

            if (query && !categoryLinks.length) {
                return;
            }

            content.appendChild(renderGroup(category, categoryLinks));
            renderedCount += categoryLinks.length;
        });

        if (query && renderedCount === 0) {
            content.appendChild(createEmptyMessage('未找到匹配的导航链接'));
        }
    }

    function loadNavigation() {
        var content = document.getElementById('navContent');
        fetch('/api/nav/all')
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('加载失败');
                }
                return response.json();
            })
            .then(function (result) {
                if (result.code !== 200) {
                    throw new Error(result.message || '加载失败');
                }
                var data = result.data || {};
                navState.categories = (data.categories || []).filter(function (category) {
                    return category.status === undefined || Number(category.status) === 1;
                });
                navState.links = (data.links || []).filter(function (link) {
                    return link.status === undefined || Number(link.status) === 1;
                });
                renderNavigation();
            })
            .catch(function () {
                if (content) {
                    clearNode(content);
                    content.appendChild(createEmptyMessage('导航数据暂时不可用'));
                }
            });
    }

    function bindSearch() {
        var input = document.getElementById('navSearch');
        if (!input) {
            return;
        }

        var timer = null;
        input.addEventListener('input', function () {
            window.clearTimeout(timer);
            timer = window.setTimeout(function () {
                navState.query = input.value || '';
                renderNavigation();
            }, 200);
        });
    }

    function initPublicNav() {
        bindThemeSync();
        if (!hasPublicNav()) {
            return;
        }
        bindSearch();
        loadNavigation();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initPublicNav);
    } else {
        initPublicNav();
    }
})();


/**
 * 发送get请求
 * @param url 请求地址
 * @param callback 成功回调
 * @param errorCallback 失败回调
 * @returns {Promise<*> | undefined} 没有回调函数则返回Promise
 */
function getRequest(url, callback, errorCallback) {
    return request(url, null, callback, errorCallback);
}

/**
 * 发送post请求
 * @param url 请求地址
 * @param param 请求参数，FormData 或者对象（对象会转成JSON字符串）
 * @param callback 成功回调
 * @param errorCallback 失败回调
 * @returns {Promise<*> | undefined} 没有回调函数则返回Promise
 */
function postRequest(url, param, callback, errorCallback) {
    const options = {
        method: 'POST',
        body: param
    }
    return request(url, options, callback, errorCallback);
}

/**
 * 发送请求
 * @param url 请求地址
 * @param options 请求选项，请求方法、请求头、请求参数等
 * @param callback 成功回调
 * @param errorCallback 失败回调
 * @returns {Promise<*> | undefined} 没有回调函数则返回Promise
 */
function request(url, options, callback, errorCallback) {
    // 默认参数
    const defaultOptions = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
    };

    // 合并参数
    const opts = Object.assign({}, defaultOptions, options);

    if (opts.body instanceof FormData) {
        // 删除Content-Type，因为FormData会有正确的默认设置
        delete opts.headers['Content-Type'];
    } else if (opts.body && typeof opts.body === 'object') {
        // 如果body是一个对象，转换为JSON字符串
        opts.body = JSON.stringify(opts.body);
    }

    // 发送请求
    const rpPromise = fetch(url, opts)
        .then(response => {
            // 如果响应状态码不是200，则抛出错误
            if (response.status !== 200) {
                return Promise.reject({
                    type: 'global',
                    code: response.status,
                    message: `Error Response Code : ${response.status}`
                });
            }
            return response.json();
        }).then(data => {
            if (data.code == 200) {
                return data.data;
            } else {
                return Promise.reject({
                    type: 'api',
                    code: data.code,
                    message: data.message,
                    timestamp: data.timestamp
                });
            }
        });


    if (typeof callback === 'function') {
        rpPromise.then(data => {
            //执行回调函数
            callback(data);
        }).catch(error => {
            if (error.type == 'global') {
                globalErrorHandle(error);
            } else {
                if (!reLoginHandle(error)) {
                    if (typeof errorCallback === 'function') {
                        errorCallback(error);
                    } else {
                        apiErrorHandle(error);
                    }
                }
            }
        });
    } else {
        //没有回调函数，返回Promise
        return rpPromise.catch(error => {
            if (error.type == 'global') {
                globalErrorHandle(error);
            } else {
                reLoginHandle(error)
            }
            return Promise.reject(error);
        });
    }
}

/**
 * 全局错误提示
 * @param error
 */
function globalErrorHandle(error) {
    const code = error.code === undefined ? '' : error.code;
    showModalMessage("错误信息：" + code, error.message, MsgTypes.DANGER);
}

/**
 * 重新登陆提示
 * @param error
 * @returns {boolean}
 */
function reLoginHandle(error) {
    if (error.code == 401) {
        showConfirm("请重新登录", error.message, MsgTypes.INFO, function () {
            location.href = '/login.html';
        });
        return true;
    }
    return false;
}

/**
 * 接口错误默认提示
 * @param error
 */
function apiErrorHandle(error) {
    showToast(error.message, "错误信息：" + error.code, error.timestamp, MsgTypes.DANGER, Position.TopCenter);
}


/*########################## 提示框 ##########################*/
/**
 * 消息类型
 * @type {{DANGER: string, SUCCESS: string, INFO: string, NONE: string, WARNING: string}}
 */
const MsgTypes = {
    SUCCESS: "success",
    WARNING: "warning",
    DANGER: "danger",
    INFO: "info",
    QUESTION: "question",
    NONE: "none"
};

/**
 * 创建消息类型对应的样式
 * @param type
 * @returns {{icon: string, titleStyle: string}}
 */
function createMsgStyle(type) {
    // 样式：
    let icon, titleStyle;
    switch (type) {
        case MsgTypes.SUCCESS:
            icon = "[OK]";
            titleStyle = "text-success";
            break;
        case MsgTypes.WARNING:
            icon = "[!]";
            titleStyle = "text-warning";
            break;
        case MsgTypes.DANGER:
            icon = "[X]";
            titleStyle = "text-danger";
            break;
        case MsgTypes.INFO:
            icon = "[i]";
            titleStyle = "text-info";
            break;
        case MsgTypes.QUESTION:
            icon = "[?]";
            titleStyle = "text-dark";
            break;
        default:
            icon = "";
            titleStyle = "";
            break;
    }
    return {
        icon: icon,
        titleStyle: titleStyle
    }
}

/**
 * 弹出模态消息框
 * @param title 标题
 * @param message 消息内容
 * @param type 消息类型 @see MsgTypes
 */
function showModalMessage(title, message, type = MsgTypes.INFO) {
    // 样式：
    const style = createMsgStyle(type);

    let modal = document.createElement('div');
    modal.className = 'hc-dialog-backdrop';
    modal.tabIndex = -1;
    modal.innerHTML = `
    <div class="hc-dialog">
      <div class="hc-dialog-content">
        <div class="hc-dialog-header">
          <h6 class="hc-dialog-title ${style.titleStyle}">${style.icon} ${title}</h6>
          <button type="button" class="hc-close-button" aria-label="Close">×</button>
        </div>
        <div class="hc-dialog-body">
          ${message}
        </div>
      </div>
    </div>
  `;

    // 将 Modal 元素添加到 body 中
    document.body.appendChild(modal);
    modal.classList.add('show');
    modal.querySelector('.hc-close-button').addEventListener('click', function () {
        closeHcDialog(modal);
    });
    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            closeHcDialog(modal);
        }
    });
}


/**
 * 弹出确认框
 * @param title 标题
 * @param message 内容
 * @param type 类型
 * @param callbackTrue 确认回调函数
 * @param callbackFalse 取消回调函数
 * @param hiddenCallback 隐藏回调，不会和上面的回调同时触发
 * @param options 参数
 */
function showConfirm(title, message, type = MsgTypes.QUESTION, callbackTrue, callbackFalse, hiddenCallback, options) {
    // 样式：
    const style = createMsgStyle(type);

    let modal = document.createElement('div');
    modal.className = 'hc-dialog-backdrop';
    modal.tabIndex = -1;

    const defOptions = {
        backdrop: "static",
        focus: true,
        keyboard: false
    };
    options = Object.assign({}, defOptions, options || {});

    modal.innerHTML = `
    <div class="hc-dialog">
      <div class="hc-dialog-content">
        <div class="hc-dialog-header">
          <h6 class="hc-dialog-title ${style.titleStyle}">${style.icon} ${title}</h6>
          <button type="button" class="hc-close-button cancel-button" aria-label="Close">×</button>
        </div>
        <div class="hc-dialog-body">
          ${message}
        </div>
        <div class="hc-dialog-footer">
          <button type="button" class="hc-button hc-button-primary confirm-button">确  定</button>
          <button type="button" class="hc-button cancel-button">取  消</button>
        </div>
      </div>
    </div>
  `;

    // 将 Modal 元素添加到 body 中
    document.body.appendChild(modal);
    modal.classList.add('show');
    if (options.focus) {
        modal.focus();
    }

    let executed = false;

    // 为“确定”按钮添加事件监听器
    if (typeof callbackTrue === 'function') {
        modal.querySelector('.confirm-button').addEventListener('click', function () {
            executed = true;
            callbackTrue();
            closeHcDialog(modal, executed, hiddenCallback);
        });
    } else {
        modal.querySelector('.confirm-button').addEventListener('click', function () {
            executed = true;
            closeHcDialog(modal, executed, hiddenCallback);
        });
    }

    //为“取消”按钮添加事件监听
    Array.prototype.forEach.call(modal.querySelectorAll('.cancel-button'), function (button) {
        button.addEventListener('click', function () {
            if (typeof callbackFalse === 'function') {
                executed = true;
                callbackFalse();
            }
            closeHcDialog(modal, executed, hiddenCallback);
        });
    });

    modal.addEventListener('click', function (event) {
        if (event.target === modal && options.backdrop !== 'static') {
            closeHcDialog(modal, executed, hiddenCallback);
        }
    });

    if (options.keyboard !== false) {
        modal.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeHcDialog(modal, executed, hiddenCallback);
            }
        });
    }
}

function closeHcDialog(modal, executed, hiddenCallback) {
    if (!modal || !modal.parentNode) {
        return;
    }
    modal.classList.remove('show');
    setTimeout(function () {
        if (modal.parentNode) {
            modal.parentNode.removeChild(modal);
        }
        if (!executed && typeof hiddenCallback === 'function') {
            hiddenCallback();
        }
    }, 120);
}

/**
 * 位置
 */
const Position = {
    TopLeft: {id: 1, style: "top-0 start-0"},
    TopCenter: {id: 2, style: "top-0 start-50 translate-middle-x"},
    TopRight: {id: 3, style: "top-0 end-0"},
    MiddleLeft: {id: 4, style: "top-50 start-0 translate-middle-y"},
    MiddleCenter: {id: 5, style: "top-50 start-50 translate-middle"},
    MiddleRight: {id: 6, style: "top-50 end-0 translate-middle-y"},
    BottomLeft: {id: 7, style: "bottom-0 start-0"},
    BottomCenter: {id: 8, style: "bottom-0 start-50 translate-middle-x"},
    BottomRight: {id: 9, style: "bottom-0 end-0"}
};

/**
 * 根据编号获取对应位置
 * @param num 编号为1到9
 * @returns {{style: string, id: number}|*}
 */
function getPositionById(num) {
    for (const key in Position) {
        if (Position.hasOwnProperty(key) && Position[key].id === num) {
            return Position[key];
        }
    }
    return Position.BottomRight;
}

/**
 * 创建Toast容器，如果已经存在就使用现有的
 * @returns {HTMLElement}
 */
function createToastContainer(position = Position.BottomRight) {
    if (position == null || position == '') {
        position = Position.BottomRight;
    } else if (typeof position === 'number') {
        position = getPositionById(position);
    }

    let containerId = "toast-container-" + position.id

    let toastContainer = document.getElementById(containerId);
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'hc-toast-container hc-toast-position-' + position.id;
        toastContainer.id = containerId;
        document.body.appendChild(toastContainer);
    }
    return toastContainer;
}

/**
 * 弹出吐司框
 * @param message 消息内容
 * @param title 标题
 * @param smallTitle 副标题
 * @param type 类型
 */
function showToast(message, title = '提示', smallTitle = '', type = MsgTypes.NONE, position = Position.BottomRight) {
    let toastContainer = createToastContainer(position);
    // 样式
    const style = createMsgStyle(type);

    // 创建 Toast 元素
    const toastElement = document.createElement('div');
    toastElement.className = 'hc-toast';

    let htmlContent;

    if ((title == null || title == '') && (smallTitle == null || smallTitle == '')) {
        htmlContent = `
          <div class="hc-toast-line">
            <div class="hc-toast-body ${style.titleStyle}">
              ${style.icon} ${message}
            </div>
            <button type="button" class="hc-toast-close" aria-label="Close">×</button>
          </div>
          `;
    } else {
        htmlContent = `
            <div class="hc-toast-header">
              <strong class="${style.titleStyle}">${style.icon} ${title}</strong>
              <small>${smallTitle}</small>
              <button type="button" class="hc-toast-close" aria-label="Close">×</button>
            </div>
            <div class="hc-toast-body">
              ${message}
            </div>
          `;
    }

    toastElement.innerHTML = htmlContent;

    toastContainer.appendChild(toastElement);
    setTimeout(function () {
        toastElement.classList.add('show');
    }, 0);

    function closeToast() {
        toastElement.classList.remove('show');
        setTimeout(function () {
            if (toastElement.parentNode) {
                toastElement.parentNode.removeChild(toastElement);
            }
        }, 160);
    }

    const closeButton = toastElement.querySelector('.hc-toast-close');
    if (closeButton) {
        closeButton.addEventListener('click', closeToast);
    }
    setTimeout(closeToast, 5200);
}

function removeFloatingTip(element) {
    const id = element && element.getAttribute('data-hc-tip-id');
    if (!id) {
        return;
    }
    const oldTip = document.getElementById(id);
    if (oldTip && oldTip.parentNode) {
        oldTip.parentNode.removeChild(oldTip);
    }
    element.removeAttribute('data-hc-tip-id');
}

function positionFloatingTip(tip, element, placement) {
    const rect = element.getBoundingClientRect();
    const tipRect = tip.getBoundingClientRect();
    const gap = 8;
    let left = rect.left + (rect.width - tipRect.width) / 2;
    let top = rect.bottom + gap;
    if (placement === 'top') {
        top = rect.top - tipRect.height - gap;
    } else if (placement === 'left') {
        left = rect.left - tipRect.width - gap;
        top = rect.top + (rect.height - tipRect.height) / 2;
    } else if (placement === 'right') {
        left = rect.right + gap;
        top = rect.top + (rect.height - tipRect.height) / 2;
    }
    tip.style.left = Math.max(8, Math.min(left, window.innerWidth - tipRect.width - 8)) + 'px';
    tip.style.top = Math.max(8, Math.min(top, window.innerHeight - tipRect.height - 8)) + 'px';
}

function showFloatingTip(element, content, title, placement, timeout, className) {
    if (!element) {
        return;
    }
    removeFloatingTip(element);
    const tip = document.createElement('div');
    const id = 'hc-tip-' + Date.now() + '-' + Math.floor(Math.random() * 1000);
    tip.id = id;
    tip.className = className;
    tip.innerHTML = (title ? '<strong>' + title + '</strong>' : '') + '<div>' + content + '</div>';
    document.body.appendChild(tip);
    element.setAttribute('data-hc-tip-id', id);
    positionFloatingTip(tip, element, placement);
    setTimeout(function () {
        removeFloatingTip(element);
    }, timeout);
    return tip;
}

/**
 * 弹出简单吐司框
 * @param message 消息内容
 * @param type 消息类型
 * @param position 弹出位置
 */
function showToastSimple(message, type = MsgTypes.NONE, position = Position.BottomRight) {
    showToast(message, null, null, type, position);
}

/**
 * 展示工具提示
 * @param element 提示元素
 * @param content 提示内容
 * @param placement 位置，默认底部：auto top right bottom left
 * @param timeout 消失时间，默认3秒
 */
function showTooltips(element, content, placement = 'bottom', timeout = 3000) {
    showFloatingTip(element, content, '', placement, timeout, 'hc-floating-tip hc-tooltip');
}

/**
 * 显示pop提示，指定时间后自动消失
 * @param element 提示元素
 * @param content 提示内容
 * @param title 提示标题
 * @param placement 位置，默认右侧：auto top right bottom left
 * @param timeout 消失时间，默认3秒
 */
function showPopover(element, content, title = '', placement = 'right', timeout = 3000) {
    showFloatingTip(element, content, title, placement, timeout, 'hc-floating-tip hc-popover');
}

// ###############################################################
// ##################            右键菜单         ##################
// ###############################################################

/**
 * 创建右键菜单
 * @param menuItems 菜单项 和 对应处理函数
 * @param targetElement 目标对象
 * @constructor
 */
function ContextMenu(menuItems, targetElement) {
    this.menuItems = menuItems;
    this.targetElement = targetElement;
    this.menuElement = null;

    // 静态属性，用于存储所有实例的引用
    // 确保同时只会显示一个右键菜单
    if (typeof ContextMenu.instances === 'undefined') {
        ContextMenu.instances = [];
    }

    this.init = function () {
        this.createMenu();
        this.attachEvents();
        ContextMenu.instances.push(this);
    };

    this.createMenu = function () {
        const menu = document.createElement('div');
        menu.className = 'context-menu';
        const ul = document.createElement('ul');
        this.menuItems.forEach(function (item) {
            const li = document.createElement('li');
            if (item.type == 'separator') {
                li.className = 'menu-separator';
            } else {
                li.textContent = item.text;
                li.onclick = item.onClick;
                /*li.addEventListener('click', item.onClick);*/
            }
            ul.appendChild(li);
        });
        menu.appendChild(ul);
        document.body.appendChild(menu);
        this.menuElement = menu;
    };
    this.attachEvents = function () {
        if (targetElement) {
            targetElement.addEventListener('contextmenu', this.showMenu.bind(this));
        }
        document.addEventListener('click', this.hideMenu.bind(this));
        document.addEventListener('keydown', this.handleKeyDown.bind(this));
    };
    this.handleKeyDown = function (e) {
        if (e.keyCode === 27) { // 27 是 Esc 键的键码
            this.hideMenu();
        }
    };

    /**
     * 展示右键菜单
     * @param e
     */
    this.showMenu = function (e) {
        e.preventDefault();

        ContextMenu.instances.forEach((instance) => {
            instance.hideMenu();
        });

        // 获取菜单元素的大小
        this.menuElement.style.display = 'block';
        const menuWidth = this.menuElement.offsetWidth;
        const menuHeight = this.menuElement.offsetHeight;

        // 获取可视区域的大小
        const viewportWidth = window.innerWidth;
        const viewportHeight = window.innerHeight;
        //不考虑滚动条

        let left = e.pageX;
        let top = e.pageY;

        if ((left + menuWidth) > viewportWidth) {
            left = left - menuWidth;
            if (left < 0) {
                left = 0;
            }
        }

        if ((top + menuHeight) > viewportHeight) {
            top = viewportHeight - menuHeight;
            if (top < 0) {
                top = 0;
            }
        }

        this.menuElement.style.display = 'block';
        this.menuElement.style.left = left + 'px';
        this.menuElement.style.top = top + 'px';
    };

    /**
     * 隐藏右键菜单
     */
    this.hideMenu = function () {
        this.menuElement.style.display = 'none';
    };

    /**
     * 销毁右键菜单
     */
    this.destroy = function () {
        // 移除事件监听器
        if (targetElement) {
            targetElement.removeEventListener('contextmenu', this.showMenu.bind(this));
        }
        document.removeEventListener('click', this.hideMenu.bind(this));
        document.removeEventListener('keydown', this.handleKeyDown.bind(this));

        // 移除菜单元素
        if (this.menuElement && this.menuElement.parentNode) {
            this.menuElement.parentNode.removeChild(this.menuElement);
        }

        // 从数组中移除当前实例
        const index = ContextMenu.instances.indexOf(this);
        if (index !== -1) {
            ContextMenu.instances.splice(index, 1);
        }

        // 清空菜单元素的引用
        this.menuElement = null;
    };
}

// ###############################################################
// ##################            工具方法         ##################
// ###############################################################

/**
 * 防抖函数，在一定的时间内只会执行一次
 * @param func
 * @param delay
 * @returns {function(): void}
 */
function debounce(func, delay) {
    let timer;
    return function () {
        const context = this;
        const args = arguments;
        clearTimeout(timer);
        timer = setTimeout(() => func.apply(context, args), delay);
    };
}

/**
 * 延时执行，在真正执行之前多次调用无效
 * @param func
 * @param delay
 * @returns {function(): (undefined)}
 */
function executeWithDelay(func, delay) {
    let isTimerRunning = false;

    return function () {
        if (isTimerRunning) {
            // 如果定时器正在运行，忽略后续调用
            return;
        }

        isTimerRunning = true;  // 标记定时器已启动
        const context = this;
        const args = arguments;

        setTimeout(() => {
            func.apply(context, args);  // 执行函数
            isTimerRunning = false;  // 重置标记，允许再次启动
        }, delay);  // 设置延迟时间
    };
}

/**
 * 获取祖先元素，直到为某个元素为止
 * @param element
 * @param untilElement
 * @returns {[]|*[]}
 */
function getAncestorsUntil(element, untilElement) {
    let parentElement = element.parentElement;
    const ancestors = [];
    while (parentElement !== untilElement && parentElement !== document) {
        ancestors.push(parentElement);
        parentElement = parentElement.parentElement; // 或者使用 currentElement.parentNode
    }
    // 如果直到文档根元素都没有找到 untilElement，则返回空数组
    if (parentElement !== untilElement) {
        return [];
    }
    // 否则，返回包含所有祖先元素的数组
    return ancestors;
}

/**
 * 滚动元素到窗口的可见区域
 * @param container position属性为relative, absolute, fixed或sticky
 * @param element
 */
function scrollElementIntoView(container, element) {
    // 获取选中元素到容器顶部的距离
    const elementTop = element.offsetTop;
    // 获取选中元素的尺寸
    const elementHeight = element.offsetHeight;
    // 获取容器的尺寸
    const containerHeight = container.offsetHeight;
    // 获取容器的当前滚动位置
    const containerScrollTop = container.scrollTop;
    // 计算元素底部到容器顶部的距离
    const elementBottom = elementTop + elementHeight;

    // 检查元素是否在容器的可见范围内
    if (elementTop < containerScrollTop) {
        // 元素在容器上方
        container.scrollTop = elementTop;
    } else if (elementBottom > containerScrollTop + containerHeight) {
        // 元素在容器下方
        container.scrollTop = elementBottom - containerHeight;
    }
    // 如果元素已经在可见范围内，则不需要滚动
}
