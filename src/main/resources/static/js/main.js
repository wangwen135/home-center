/*
 * 全局发送请求的函数封装
*/

function getRequest(url, callback, errorCallback) {
    return request(url, null, callback, errorCallback);
}

function postRequest(url, param, callback, errorCallback) {
    const options = {
        method: 'POST',
        body: param
    }
    return request(url, options, callback, errorCallback);
}

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


function globalErrorHandle(error) {
    const code = error.code === undefined ? '' : error.code;
    showModalMessage("错误信息：" + code, error.message, MsgTypes.DANGER);
}

function reLoginHandle(error) {
    if (error.code == 401) {
        showConfirm("请重新登录", error.message, MsgTypes.INFO, function () {
            location.href = '/login.html';
        });
        return true;
    }
    return false;
}

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
            icon = "<i class='bi bi-check-circle'></i>";
            titleStyle = "text-success";
            break;
        case MsgTypes.WARNING:
            icon = "<i class='bi bi-exclamation-circle'></i>";
            titleStyle = "text-warning";
            break;
        case MsgTypes.DANGER:
            icon = "<i class='bi bi-x-circle'></i>";
            titleStyle = "text-danger";
            break;
        case MsgTypes.INFO:
            icon = "<i class='bi bi-info-circle'></i>";
            titleStyle = "text-info";
            break;
        case MsgTypes.QUESTION:
            icon = "<i class='bi bi-question-circle'></i>";
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
    modal.className = 'modal fade text-black';
    modal.tabIndex = -1;
    modal.innerHTML = `
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header py-2">
          <h6 class="modal-title ${style.titleStyle}">${style.icon} ${title}</h6>
          <button type="button" class="btn-close btn-sm" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body text-break">
          ${message}
        </div>
      </div>
    </div>
  `;

    // 将 Modal 元素添加到 body 中
    document.body.appendChild(modal);

    // 初始化 Bootstrap Modal
    var modalInstance = new bootstrap.Modal(modal);

    // 显示 Modal
    modalInstance.show();

    // 监听 Modal 关闭事件，确保在关闭后移除 Modal 元素
    modal.addEventListener('hidden.bs.modal', function () {
        document.body.removeChild(modal);
    });
}

/**
 * 弹出确认框
 * @param title 标题
 * @param message 内容
 * @param type 类型
 * @param callbackTrue 确认回调函数
 * @param callbackFalse 取消回调函数
 */
function showConfirm(title, message, type = MsgTypes.QUESTION, callbackTrue, callbackFalse) {
    // 样式：
    const style = createMsgStyle(type);

    let modal = document.createElement('div');
    modal.className = 'modal fade text-black';
    modal.tabIndex = -1;
    //设置data-bs-backdrop属性为static
    modal.dataset.bsBackdrop = 'static';
    //设置data-bs-keyboard属性为false
    modal.dataset.bsKeyboard = 'false';

    modal.innerHTML = `
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header py-2">
          <h6 class="modal-title ${style.titleStyle}">${style.icon} ${title}</h6>
          <button type="button" class="btn-close btn-sm cancel-button" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body text-break">
          ${message}
        </div>
        <div class="modal-footer py-2">
          <button type="button" class="btn btn-primary confirm-button" data-bs-dismiss="modal">确  定</button>
          <button type="button" class="btn btn-secondary cancel-button" data-bs-dismiss="modal">取  消</button>
        </div>
      </div>
    </div>
  `;

    // 将 Modal 元素添加到 body 中
    document.body.appendChild(modal);

    // 初始化 Bootstrap Modal
    var modalInstance = new bootstrap.Modal(modal);

    // 显示 Modal
    modalInstance.show();

    // 为“确定”按钮添加事件监听器
    if (typeof callbackTrue === 'undefined') {

    } else if (typeof callbackTrue !== 'function') {
        console.error('true callback is not a function');
    } else {
        modal.querySelector('.confirm-button').addEventListener('click', function () {
            callbackTrue();
        });
    }

    //为“取消”按钮添加事件监听
    if (typeof callbackFalse === 'undefined') {

    } else if (typeof callbackFalse !== 'function') {
        console.error('false callback is not a function');
    } else {
        modal.querySelectorAll('.cancel-button').forEach(function (button) {
            button.addEventListener('click', function () {
                callbackFalse();
            });
        });
    }


    // 监听 Modal 关闭事件，确保在关闭后移除 Modal 元素
    modal.addEventListener('hidden.bs.modal', function () {
        document.body.removeChild(modal);
    });
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
        toastContainer.className = 'toast-container position-fixed p-3 text-black ' + position.style;
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
    toastElement.className = 'toast';

    let htmlContent;

    if ((title == null || title == '') && (smallTitle == null || smallTitle == '')) {
        htmlContent = `
          <div class="d-flex">
            <div class="toast-body ${style.titleStyle}">
              ${style.icon} ${message}
            </div>
            <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
          </div>
          `;
    } else {
        htmlContent = `
            <div class="toast-header">
              <strong class="me-auto ${style.titleStyle}">${style.icon} ${title}</strong>
              <small>${smallTitle}</small>
              <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
            <div class="toast-body">
              ${message}
            </div>
          `;
    }

    toastElement.innerHTML = htmlContent;

    toastContainer.appendChild(toastElement);

    // 初始化 Bootstrap Toast
    const toast = new bootstrap.Toast(toastElement);

    // 显示 Toast
    toast.show();

    // 监听 Toast 隐藏事件，确保在隐藏后移除 Toast 元素
    toastElement.addEventListener('hidden.bs.toast', function () {
        toastContainer.removeChild(toastElement);
    });
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
    const tooltip = new bootstrap.Tooltip(element, {
        title: content,
        placement: placement,
        trigger: 'manual',
        customClass: 'note-title-tooltips',
        html: true
    });
    tooltip.show();
    setTimeout(() => {
        // tooltip.hide();
        // 销毁
        tooltip.dispose();
    }, timeout);
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
    const popover = new bootstrap.Popover(element, {
        title: title,
        content: content,
        placement: placement,
        trigger: 'manual',
        html: true
    });
    popover.show()
    setTimeout(() => {
        //popover.hide();
        // 销毁 直接隐藏没有动画
        popover.dispose();
    }, timeout);
}


/*################ 下面是测试用的 ####################*/

function fetchRequest(url, options) {
    // 设置默认的请求选项
    const defaultOptions = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
        body: null
    };

    // 合并用户提供的选项和默认选项
    const mergedOptions = {...defaultOptions, ...options};

    // 如果body是一个对象，转换为JSON字符串
    if (mergedOptions.body && typeof mergedOptions.body === 'object') {
        mergedOptions.body = JSON.stringify(mergedOptions.body);
    }

    // 发送fetch请求
    return fetch(url, mergedOptions)
        .then(response => {
            // 如果响应状态码不是200，则抛出错误
            if (response.status !== 200) {
                alert("错误码：" + response.status + "错误消息：" + response.message)
                throw new Error(`Error: ${response.status}`);
            }
            return response.json(); // 解析JSON响应
        })
        .catch(error => {
            // 处理请求错误
            console.error('Request failed:', error);
            throw error; // 重新抛出错误，以便调用者处理
        });
}

// 使用封装的fetchRequest函数
/*
fetchRequest('/your/java/service', {
    method: 'POST',
    body: {key: 'value'}
})
    .then(data => {
        // 请求成功的处理逻辑
        console.log('Data received:', data);
    })
    .catch(error => {
        // 错误处理逻辑
        console.error('An error occurred:', error);
    });
*/


