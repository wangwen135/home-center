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
    // 使用 Toast 或其他方式弹出错误提示信息
    // 例如：
    // Toast.error(message);
    console.error(error);
    alert(error.message);
}

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


