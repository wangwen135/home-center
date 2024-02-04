// 封装一个全局发送请求的函数
function request(url, options) {
    // 默认参数
    const defaultOptions = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // 合并参数
    const opts = Object.assign({}, defaultOptions, options);
    // 如果body是一个对象，转换为JSON字符串
    if (opts.body && typeof opts.body === 'object') {
        opts.body = JSON.stringify(mergedOptions.body);
    }

    // 发送请求
    return fetch(url, opts)
        .then(response => {
            // 如果响应状态码不是200，则抛出错误
            if (response.status !== 200) {
                throw new Error(`Error Response Code : ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            // 处理不同状态码
            switch (data.code) {
                case 200:
                    // 返回 data
                    return data.data;
                case 401:
                    // 跳转到登录界面
                    location.href = '/login.html';
                    throw new Error("请重新登录");
                default:
                    // 弹出错误提示信息
                    throw new Error(data.message);
            }
        }).catch(error => {
            showError(error);
            throw error;
        });
}

// 弹出错误提示信息
function showError(error) {
    console.error(error);
    //showToastSimple(error.message);
    //alert(error.message);
    showModalMessage("错误信息", error.message, MsgTypes.DANGER);
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
        <div class="modal-body">
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
 * 创建Toast容器，如果已经存在就使用现有的
 * @returns {HTMLElement}
 */
function createToastContainer() {
    let toastContainer = document.getElementById("toast-container");
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3 text-black';
        toastContainer.id = "toast-container";
        document.body.appendChild(toastContainer);
    }
    return toastContainer;
}

function showToast(message, title = '提示', smallTitle = '', type = MsgTypes.NONE) {
    let toastContainer = createToastContainer();
    // 样式
    const style = createMsgStyle(type);

    // 创建 Toast 元素
    var toastElement = document.createElement('div');
    toastElement.className = 'toast';
    toastElement.innerHTML = `
    <div class="toast-header">
      <strong class="me-auto ${style.titleStyle}">${style.icon} ${title}</strong>
      <small>${smallTitle}</small>
      <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
    </div>
    <div class="toast-body">
      ${message}
    </div>
  `;

    toastContainer.appendChild(toastElement);

    // 初始化 Bootstrap Toast
    var toast = new bootstrap.Toast(toastElement);

    // 显示 Toast
    toast.show();

    // 监听 Toast 隐藏事件，确保在隐藏后移除 Toast 元素
    toastElement.addEventListener('hidden.bs.toast', function () {
        toastContainer.removeChild(toastElement);
    });
}

function showToastSimple(message, type = MsgTypes.NONE) {
    let toastContainer = createToastContainer();
    // 样式
    const style = createMsgStyle(type);

    // 创建 Toast 元素
    var toastElement = document.createElement('div');
    toastElement.className = 'toast';
    toastElement.innerHTML = `
      <div class="d-flex">
        <div class="toast-body ${style.titleStyle}">
          ${style.icon} ${message}
        </div>
        <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
      </div>
  `;

    toastContainer.appendChild(toastElement);

    // 初始化 Bootstrap Toast
    var toast = new bootstrap.Toast(toastElement);

    // 显示 Toast
    toast.show();

    // 监听 Toast 隐藏事件，确保在隐藏后移除 Toast 元素
    toastElement.addEventListener('hidden.bs.toast', function () {
        toastContainer.removeChild(toastElement);
    });
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


