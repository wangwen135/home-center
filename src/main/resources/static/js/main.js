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


