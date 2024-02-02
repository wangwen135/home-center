// 页面加载完成后执行的函数
window.onload = function () {
    loadUserInfo();
}

function loadUserInfo() {

    request('/user/info1')
        .then(data => {
            console.log('Data received:', data);
            alert("这个触发了：" + JSON.stringify(data));
        });

}

function test() {
    request('/user/role')
        .then(data => {
            console.log('接收到数据:', data);
            alert("222 触发了：" + JSON.stringify(data));
        });
}