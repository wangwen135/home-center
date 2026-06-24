/**
 * 站点公开信息注入（站点名 / Logo / 描述）
 *
 * 在入口页（公开首页 / 登录页 / 私有导航页）加载，从免登录接口 /api/site/info 读取
 * 后台配置的站点品牌信息，填充到带 data-site-* 属性的元素：
 *   [data-site-name]     站点名称（标题）
 *   [data-site-tagline]  站点描述（标题下一行小字）
 *   [data-site-logo]     站点 Logo：图片 URL 显示图片；emoji/文字显示文字；空则用默认 HC
 * 接口不可用（如纯静态预览）时保持页面内默认值，不影响展示。
 *
 * 依赖：app-theme.js 的 window.HomeCenter.get（登录页仅加载 app-theme.js，故不依赖 main.js）。
 */
(function () {
    function isImage(value) {
        var text = String(value || '').trim();
        return /^(https?:)?\/\//i.test(text) ||
            /^data:image\//i.test(text) ||
            /\.(png|jpe?g|gif|webp|svg)(\?.*)?$/i.test(text);
    }

    function applySiteInfo(info) {
        if (!info) {
            return;
        }
        Array.prototype.forEach.call(document.querySelectorAll('[data-site-name]'), function (el) {
            if (info.name) {
                el.textContent = info.name;
            }
        });
        Array.prototype.forEach.call(document.querySelectorAll('[data-site-tagline]'), function (el) {
            if (info.tagline != null && info.tagline !== '') {
                el.textContent = info.tagline;
            }
        });
        Array.prototype.forEach.call(document.querySelectorAll('[data-site-logo]'), function (el) {
            var logo = String(info.logo || '').trim();
            el.innerHTML = '';
            if (isImage(logo)) {
                var img = document.createElement('img');
                img.src = logo;
                img.alt = '';
                el.appendChild(img);
            } else if (logo) {
                el.textContent = logo;
            } else {
                el.textContent = 'HC';
            }
        });
    }

    function load() {
        if (!window.HomeCenter || typeof window.HomeCenter.get !== 'function') {
            return;
        }
        window.HomeCenter.get('/api/site/info').then(function (result) {
            // HomeCenter.get 返回 Result 封装，站点信息在 .data
            applySiteInfo(result && result.data);
        }).catch(function () {
            /* 接口不可用：保持页面内默认值 */
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', load);
    } else {
        load();
    }
})();
