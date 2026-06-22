// 页面加载完成后执行的函数
window.onload = function () {
    init();
}

function init() {
    loadUserInfo();
    // 角色加载完成后再加载菜单权限，避免读取 sessionStorage 时的竞态
    loadRoleInfo(function () {
        loadMenuPermissions();
    });
    loadSystemLab();

    initProgressBar();
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

function loadRoleInfo(onDone) {
    getRequest('/user/role', data => {
        document.getElementById("roleName").textContent = data.name;

        sessionStorage.setItem('roleInfo', JSON.stringify(data));
        if (typeof onDone === 'function') {
            onDone();
        }
    });
}

/**
 * 根据当前用户角色与权限动态展示顶部功能菜单。
 * 超管展示全部；普通用户仅展示其拥有对应权限编码的菜单（后端仍会做权限校验）。
 */
function loadMenuPermissions() {
    const roleRaw = sessionStorage.getItem('roleInfo');
    const role = roleRaw ? JSON.parse(roleRaw) : null;
    const isSuperAdmin = role && role.id === 1;

    getRequest('/user/permission', function (permissions) {
        applyMenuVisibility(isSuperAdmin, flattenPermissionUrls(permissions || []));
    }, function () {
        // 权限接口不可用时降级：超管展示全部，普通用户隐藏需权限的菜单
        applyMenuVisibility(isSuperAdmin, []);
    });
}

function flattenPermissionUrls(permissions) {
    const set = {};
    permissions.forEach(function (p) {
        String(p.urls || '').split(';').forEach(function (part) {
            const code = part.trim();
            if (code) {
                set[code] = true;
            }
        });
    });
    return set;
}

function applyMenuVisibility(isSuperAdmin, permittedCodes) {
    const nodes = document.querySelectorAll('.admin-home-nav [data-perm]');
    Array.prototype.forEach.call(nodes, function (node) {
        const code = node.getAttribute('data-perm');
        const visible = isSuperAdmin || hasMenuPermission(code, permittedCodes);
        node.style.display = visible ? '' : 'none';
    });
}

function hasMenuPermission(code, permittedCodes) {
    if (!code) {
        return true;
    }
    if (permittedCodes[code]) {
        return true;
    }
    // 支持通配符权限编码匹配，例如 /device/pc/** 命中 /device/pc/power.html
    const keys = Object.keys(permittedCodes);
    for (let i = 0; i < keys.length; i++) {
        if (matchesAntPath(keys[i], code)) {
            return true;
        }
    }
    return false;
}

function matchesAntPath(pattern, path) {
    if (!pattern || pattern.indexOf('*') === -1) {
        return pattern === path;
    }
    // 将 ant 风格通配符转为正则：** 匹配任意（含 /），* 匹配不含 / 的段
    const regex = '^' + pattern
        .replace(/[.+^${}()|[\]\\]/g, '\\$&')
        .replace(/\*\*/g, '::DS::')
        .replace(/\*/g, '[^/]*')
        .replace(/::DS::/g, '.*') + '$';
    return new RegExp(regex).test(path);
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
            labDiv.addEventListener("contextmenu", function (event) {
                sysLabRightClick(s);
                // 阻止默认的右键菜单行为
                event.preventDefault();
            });
        });

    });
}

function sysLabClick(sysInfo) {
    const hostname = window.location.hostname;
    let url = sysInfo.internetUrl;
    if (isPrivateIP(hostname)) {
        //内网地址
        url = sysInfo.internalUrl;
    }
    window.open(url, url);
}

function isPrivateIP(ipAddress) {
    if (ipAddress === 'localhost' || ipAddress === '127.0.0.1') {
        return true;
    }

    // 使用正则表达式匹配内网IP地址段
    const privateIPPatterns = [
        /^10\.\d{1,3}\.\d{1,3}\.\d{1,3}$/, // 10.0.0.0 - 10.255.255.255
        /^172\.(1[6-9]|2\d|3[0-1])\.\d{1,3}\.\d{1,3}$/, // 172.16.0.0 - 172.31.255.255
        /^192\.168\.\d{1,3}\.\d{1,3}$/, // localhost - localhost
    ];
    // 检查ipAddress是否与上述任意一个模式匹配
    return privateIPPatterns.some(pattern => pattern.test(ipAddress));
}


function sysLabRightClick(sysInfo) {

    const container = document.getElementById("sysDescContainer");
    container.style.display = "none";
    document.getElementById("sysIcon").src = sysInfo.icon;
    document.getElementById("sysName").textContent = sysInfo.sysName;
    document.getElementById("sysDesc").textContent = sysInfo.sysDescription;
    document.getElementById("sysRemark").textContent = sysInfo.remark;
    document.getElementById("sysInternalAddr-A").href = sysInfo.internalUrl;
    document.getElementById("sysInternalAddr").textContent = sysInfo.internalUrl;
    document.getElementById("sysInternetAddr-A").href = sysInfo.internetUrl;
    document.getElementById("sysInternetAddr").textContent = sysInfo.internetUrl;

    // 通过读取元素的offsetWidth属性来强制DOM更新。可以确保类移除后DOM能够立即反映出来。
    // 然后动画就会重新开始
    container.offsetWidth;

    container.style.display = "block";
}

function closeSysDesc() {
    document.getElementById("sysDescContainer").style.display = "none";
}

function initProgressBar() {
    const animatedElement = document.querySelector('.progress-bar');
    // 其父元素就是容器
    const parentElement = animatedElement.parentElement;
    // 添加动画结束事件监听器
    animatedElement.addEventListener('animationend', function () {
        // 动画结束后，设置父元素的样式使其消失
        parentElement.style.display = 'none';
    });
}