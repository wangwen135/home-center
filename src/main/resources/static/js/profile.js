/**
 * 个人信息页：加载资料、上传头像、保存修改
 *
 * 说明：鉴权走同源 cookie（home_center_token），fetch 默认携带，无需手动加头。
 */
(function () {
    'use strict';

    // 性别：实体 1=男 2=女；下拉框用中文文案
    var GENDER_TO_TEXT = {1: '男', 2: '女'};
    var GENDER_TO_CODE = {'男': 1, '女': 2};

    function $(id) {
        return document.getElementById(id);
    }

    /**
     * LocalDateTime("yyyy-MM-dd HH:mm:ss") -> datetime-local 需要的 "yyyy-MM-ddTHH:mm"
     */
    function toLocalValue(dt) {
        if (!dt) {
            return '';
        }
        return String(dt).replace(' ', 'T').substring(0, 16);
    }

    function setMsg(text, ok) {
        var el = $('profileMsg');
        if (!el) {
            return;
        }
        el.textContent = text || '';
        el.className = 'hc-help-text profile-msg ' + (ok ? 'profile-msg-ok' : 'profile-msg-err');
    }

    function api(url, options) {
        options = options || {};
        options.credentials = 'same-origin';
        // 对象体转 JSON，FormData 原样发送
        if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
            options.headers = Object.assign({'Content-Type': 'application/json'}, options.headers || {});
            options.body = JSON.stringify(options.body);
        }
        return fetch(url, options).then(function (resp) {
            return resp.json().then(function (data) {
                if (resp.status === 200 && data.code === 200) {
                    return data.data;
                }
                // 401 等需要重新登录
                if (resp.status === 401 || data.code === 401) {
                    location.href = '/login.html';
                    throw new Error('未登录');
                }
                throw new Error(data.message || ('请求失败：' + resp.status));
            });
        });
    }

    function showAvatar(url) {
        var wrap = $('avatarPreviewWrap');
        var img = $('avatarPreview');
        if (!wrap || !img) {
            return;
        }
        if (url) {
            img.src = url;
            wrap.style.display = '';
        } else {
            img.src = '';
            wrap.style.display = 'none';
        }
    }

    function fillForm(info) {
        if (!info) {
            return;
        }
        $('username').value = info.username || '';
        $('nickname').value = info.nickname || '';
        $('phone').value = info.phone || '';
        $('email').value = info.email || '';
        $('gender').value = GENDER_TO_TEXT[info.gender] || '其他';
        $('create-time').value = toLocalValue(info.createTime);
        $('modify-time').value = toLocalValue(info.updateTime);
        $('expire-time').value = toLocalValue(info.expirationTime);
        showAvatar(info.avatar);
    }

    function loadProfile() {
        api('/user/info').then(fillForm).catch(function (e) {
            setMsg('加载用户信息失败：' + e.message, false);
        });
        api('/user/role').then(function (role) {
            var el = $('role');
            if (el) {
                el.value = (role && role.name) ? role.name : '';
            }
        }).catch(function () {
            // 角色加载失败不阻塞
        });
    }

    function onAvatarChange() {
        var input = $('avatar');
        if (!input || !input.files || !input.files.length) {
            return;
        }
        var fd = new FormData();
        fd.append('file', input.files[0]);
        api('/user/avatar', {method: 'POST', body: fd}).then(function (url) {
            showAvatar(url);
            setMsg('头像已更新', true);
            input.value = '';
        }).catch(function (e) {
            setMsg('头像上传失败：' + e.message, false);
        });
    }

    function onSave(event) {
        event.preventDefault();
        var payload = {
            nickname: ($('nickname').value || '').trim(),
            gender: GENDER_TO_CODE[$('gender').value] || null,
            phone: ($('phone').value || '').trim(),
            email: ($('email').value || '').trim()
        };
        api('/user/profile', {method: 'PUT', body: payload}).then(function () {
            setMsg('保存成功', true);
        }).catch(function (e) {
            setMsg('保存失败：' + e.message, false);
        });
    }

    function init() {
        var form = document.querySelector('.profile-form');
        if (form) {
            form.addEventListener('submit', onSave);
        }
        var avatar = $('avatar');
        if (avatar) {
            avatar.addEventListener('change', onAvatarChange);
        }
        loadProfile();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
