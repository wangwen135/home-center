// 页面加载完成后执行的函数
window.onload = function () {
    init();
}

function init() {
    loadUserInfo();
    loadRoleInfo();
    loadSystemLab();
}

function loadUserInfo() {
    getRequest('/user/info', data => {
        document.getElementById("userName").textContent = data.nickname;
        const avatar = data.avatar;
        if (avatar !== null && avatar !== '') {
            document.getElementById("avatar").src = avatar;
        }

        sessionStorage.setItem('userInfo', JSON.stringify(data));
    });
}

function loadRoleInfo() {
    getRequest('/user/role', data => {
        document.getElementById("roleName").textContent = data.name;

        sessionStorage.setItem('roleInfo', JSON.stringify(data));
    });
}

function loadSystemLab() {
    getRequest("/user/internalSystem", data => {

        sessionStorage.setItem('sysInfo', JSON.stringify(data));

        const container = document.getElementById("systemLabContainer")

        data.forEach(s => {
            // 创建系统div
            const labDiv = document.createElement('div');
            labDiv.className = "sys-lab status-dot";
            labDiv.id = s.id;
            labDiv.tabIndex = 0;
            labDiv.dataset.status = s.sysStatus;

            // 创建img元素
            var sysImg = document.createElement('img');
            sysImg.src = s.icon;
            sysImg.alt = s.sysName;
            sysImg.className = "sys-icon";

            // 创建span元素
            var sysSpan = document.createElement('span');
            sysSpan.textContent = s.sysName;
            sysSpan.className = "sys-text";

            // 将img和span添加到系统div中
            labDiv.appendChild(sysImg);
            labDiv.appendChild(sysSpan);
            // 将系统div添加到容器中
            container.appendChild(labDiv);
            //添加事件
            labDiv.addEventListener("click", function () {
                sysLabClick(s);
            });
        });

    });
}

function sysLabClick(sysInfo) {
    showToast(sysInfo.sysDescription, sysInfo.sysName);
    const hostname = window.location.hostname;
    let url = sysInfo.internetUrl;
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
        //内网地址
        url = sysInfo.internalUrl;
    }
    window.open(url, url);

}

function test2() {
    request('/user/role2')
        .then(data => {
            console.log('接收到数据:', data);
            alert("222 触发了：" + JSON.stringify(data));
        }).catch();
}