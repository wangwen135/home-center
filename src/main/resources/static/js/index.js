// 页面加载完成后执行的函数
window.onload = function () {
    loadUserInfo();
}

function loadUserInfo() {
    getRequest('/user/info', data => {
        showModalMessage("接收到数据", JSON.stringify(data))

        document.getElementById("userName").textContent = data.username;

    });
}

function test() {
    getRequest('/user/role', data => {
        console.log('接收到数据:', data);
        showModalMessage("接收到数据", JSON.stringify(data))
    });

    /* .then(data => {
         console.log('接收到数据:', data);

         showModalMessage("接收到数据", JSON.stringify(data))
         // alert("222 触发了：" + JSON.stringify(data));
     });*/
}

function test2() {
    request('/user/role2')
        .then(data => {
            console.log('接收到数据:', data);
            alert("222 触发了：" + JSON.stringify(data));
        }).catch();
}