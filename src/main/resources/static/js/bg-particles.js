/**
 * 入口页公共粒子背景（科技感粒子 + 克制密度）
 *
 * 用途：公开首页、登录页、内部私有导航页共用。后台与设备页不引入本脚本，
 * 以保持管理类页面纯净、无动态背景。
 *
 * 使用方式：
 *   1. 页面放入 <canvas class="hc-bg-canvas" aria-hidden="true"></canvas>；
 *   2. 引入本脚本后会自动启动；
 *   3. 如需手动控制：window.HomeCenterBg.start({ tier: 'full|eco|static' }) / stop()。
 *
 * 降级策略：
 *   - prefers-reduced-motion：static（仅绘制静态渐变与稀疏粒子，不进入动画循环）；
 *   - 移动端 / 触屏小窗 / 低核心数 / 低内存：eco（减少粒子、关闭粒子间连线、关闭光晕、降 DPR）；
 *   - 其它：full（完整效果）。
 *   - 也可通过 canvas 上的 data-hc-bg-tier="full|eco|static" 强制指定。
 *   - 标签页隐藏时暂停动画，可见后恢复，节省电量。
 */
(function () {
    var instance = null;

    function prefersReducedMotion() {
        try {
            return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        } catch (e) {
            return false;
        }
    }

    function isMobileLike() {
        try {
            var touch = (navigator.maxTouchPoints || 0) > 0;
            var narrow = window.matchMedia ? window.matchMedia('(max-width: 820px)').matches : window.innerWidth <= 820;
            return narrow || (touch && window.innerWidth < 1024);
        } catch (e) {
            return window.innerWidth <= 820;
        }
    }

    function detectTier(options, canvas) {
        var explicit = (options && options.tier) || (canvas && canvas.getAttribute('data-hc-bg-tier'));
        if (explicit === 'static' || explicit === 'eco' || explicit === 'full') {
            return explicit;
        }
        if (prefersReducedMotion()) {
            return 'static';
        }
        var cores = navigator.hardwareConcurrency || 0;
        var mem = navigator.deviceMemory || 0;
        if (isMobileLike() || (cores && cores <= 2) || (mem && mem <= 2)) {
            return 'eco';
        }
        return 'full';
    }

    function findCanvas(options) {
        if (options && options.canvas) {
            return typeof options.canvas === 'string' ? document.querySelector(options.canvas) : options.canvas;
        }
        return document.querySelector('.hc-bg-canvas') || document.getElementById('bg-canvas');
    }

    function isDarkTheme() {
        return (document.body && document.body.classList.contains('dark-theme')) ||
            document.documentElement.getAttribute('data-theme') === 'dark';
    }

    function getPalette() {
        if (isDarkTheme()) {
            return {
                bg0: '#021025',
                bg1: '#021226',
                bg2: '#00030a',
                particle: 'rgba(255,255,255,',
                line: '102,217,255',
                nebula: '102,217,255',
                clear: 'rgba(2,6,14,0)'
            };
        }
        return {
            bg0: '#f8fbff',
            bg1: '#edf4fb',
            bg2: '#f0f2f5',
            particle: 'rgba(0,66,130,',
            line: '0,102,204',
            nebula: '0,102,204',
            clear: 'rgba(248,251,255,0)'
        };
    }

    function getRandom(min, max) {
        return Math.random() * (max - min) + min;
    }

    function start(options) {
        stop();
        var canvas = findCanvas(options);
        if (!canvas || !canvas.getContext) {
            return null;
        }
        var ctx = canvas.getContext('2d');
        var tier = detectTier(options, canvas);
        var cfg = {
            full: { divisor: 80000, min: 30, dprCap: 2, lines: true, mouse: true, glow: true, nebula: 12 },
            eco: { divisor: 160000, min: 18, dprCap: 1.5, lines: false, mouse: false, glow: false, nebula: 6 },
            static: { divisor: 60000, min: 14, dprCap: 1.5, lines: false, mouse: false, glow: false, nebula: 8 }
        }[tier];

        var W = 0;
        var H = 0;
        var particles = [];
        var mouse = { x: -9999, y: -9999 };
        var lastInteractTime = 0;
        var animationFrameId = null;
        var running = true;
        var visible = !document.hidden;

        function resizeCanvas() {
            var ratio = Math.min(cfg.dprCap, window.devicePixelRatio || 1);
            W = window.innerWidth;
            H = window.innerHeight;
            canvas.width = Math.max(1, Math.floor(W * ratio));
            canvas.height = Math.max(1, Math.floor(H * ratio));
            canvas.style.width = W + 'px';
            canvas.style.height = H + 'px';
            ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
            initParticles();
        }

        function initParticles() {
            particles = [];
            var count = Math.max(cfg.min, Math.floor((W * H) / cfg.divisor));
            for (var i = 0; i < count; i++) {
                particles.push({
                    x: Math.random() * W,
                    y: Math.random() * H,
                    vx: getRandom(-0.2, 0.2),
                    vy: getRandom(-0.2, 0.2),
                    r: getRandom(0.5, 1.8),
                    alpha: getRandom(0.15, 0.8)
                });
            }
        }

        var resizeTimer = null;
        function handleResize() {
            window.clearTimeout(resizeTimer);
            resizeTimer = window.setTimeout(resizeCanvas, 120);
        }

        function handleMouseMove(event) {
            mouse.x = event.clientX;
            mouse.y = event.clientY;
        }

        function handleMouseOut() {
            mouse.x = -9999;
            mouse.y = -9999;
        }

        function handleVisibility() {
            visible = !document.hidden;
            if (visible && running && tier !== 'static' && animationFrameId === null) {
                animationFrameId = requestAnimationFrame(draw);
            }
        }

        function updateInteraction(now) {
            if (now - lastInteractTime < 16) {
                return;
            }
            lastInteractTime = now;
            var threshold = 160 * 160;
            for (var i = 0; i < particles.length; i++) {
                var p = particles[i];
                var dx = mouse.x - p.x;
                var dy = mouse.y - p.y;
                var distSquared = dx * dx + dy * dy;
                if (distSquared < threshold && distSquared > 0) {
                    var dist = Math.sqrt(distSquared);
                    var force = -0.0012 * (1 - dist / 160);
                    p.vx += (dx / dist) * force;
                    p.vy += (dy / dist) * force;
                }
                p.vx = Math.max(-0.3, Math.min(0.3, p.vx));
                p.vy = Math.max(-0.3, Math.min(0.3, p.vy));
            }
        }

        function drawBackground(palette) {
            var g = ctx.createLinearGradient(0, 0, W, H);
            g.addColorStop(0, palette.bg0);
            g.addColorStop(0.5, palette.bg1);
            g.addColorStop(1, palette.bg2);
            ctx.fillStyle = g;
            ctx.fillRect(0, 0, W, H);

            for (var i = 0; i < cfg.nebula; i++) {
                var cx = W * (((i * 0.17) % 1) + 0.02);
                var cy = H * (((i * 0.29) % 1) + 0.01);
                var rad = Math.max(1, Math.min(W, H) * (0.14 + 0.02 * Math.sin(i)));
                var grad = ctx.createRadialGradient(cx, cy, rad * 0.1, cx, cy, rad);
                grad.addColorStop(0, 'rgba(' + palette.nebula + ',0.018)');
                grad.addColorStop(1, palette.clear);
                ctx.fillStyle = grad;
                ctx.beginPath();
                ctx.arc(cx, cy, rad, 0, Math.PI * 2);
                ctx.fill();
            }
        }

        function drawParticles(palette, dark, now, animate) {
            for (var i = 0; i < particles.length; i++) {
                var p = particles[i];
                if (animate) {
                    p.x += p.vx * (0.6 + Math.sin(now * 0.0002));
                    p.y += p.vy * (0.6 + Math.cos(now * 0.0001));
                    if (p.x < -20) p.x = W + 20;
                    if (p.x > W + 20) p.x = -20;
                    if (p.y < -20) p.y = H + 20;
                    if (p.y > H + 20) p.y = -20;
                }
                ctx.beginPath();
                ctx.fillStyle = palette.particle + (p.alpha * (dark ? 0.9 : 0.55)) + ')';
                ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
                ctx.fill();
            }

            if (cfg.lines) {
                var maxDist = Math.min(W, H) * 0.12;
                var maxDistSquared = maxDist * maxDist;
                ctx.strokeStyle = 'rgba(' + palette.line + ',' + (dark ? 0.12 : 0.08) + ')';
                ctx.lineWidth = dark ? 0.8 : 0.7;
                for (var a = 0; a < particles.length; a++) {
                    for (var b = a + 1; b < particles.length; b++) {
                        var pa = particles[a];
                        var pb = particles[b];
                        var dx = pa.x - pb.x;
                        var dy = pa.y - pb.y;
                        var dSquared = dx * dx + dy * dy;
                        if (dSquared < maxDistSquared) {
                            ctx.globalAlpha = (dark ? 0.12 : 0.08) * (1 - Math.sqrt(dSquared) / maxDist);
                            ctx.beginPath();
                            ctx.moveTo(pa.x, pa.y);
                            ctx.lineTo(pb.x, pb.y);
                            ctx.stroke();
                        }
                    }
                }
                ctx.globalAlpha = 1;
            }
        }

        function drawGlow(palette, now) {
            ctx.globalCompositeOperation = isDarkTheme() ? 'lighter' : 'source-over';
            for (var i = 0; i < 3; i++) {
                var gx = (W * 0.2) * (i + 1) + 60 * Math.sin(now * 0.0004 * (i + 3));
                var gy = H * (0.2 + 0.25 * i) + 40 * Math.cos(now * 0.0005 * (i + 2));
                var gr = Math.max(1, Math.min(W, H) * (0.25 - i * 0.05));
                var glow = ctx.createRadialGradient(gx, gy, gr * 0.1, gx, gy, gr);
                glow.addColorStop(0, 'rgba(' + palette.nebula + ',0.015)');
                glow.addColorStop(1, 'rgba(' + palette.nebula + ',0)');
                ctx.fillStyle = glow;
                ctx.beginPath();
                ctx.arc(gx, gy, gr, 0, Math.PI * 2);
                ctx.fill();
            }
            ctx.globalCompositeOperation = 'source-over';
        }

        function draw() {
            if (!running || !visible) {
                animationFrameId = null;
                return;
            }
            var now = performance.now();
            var palette = getPalette();
            var dark = isDarkTheme();

            if (cfg.mouse) {
                updateInteraction(now);
            }

            drawBackground(palette);
            drawParticles(palette, dark, now, true);
            if (cfg.glow) {
                drawGlow(palette, now);
            }

            animationFrameId = requestAnimationFrame(draw);
        }

        function drawStatic() {
            var palette = getPalette();
            var dark = isDarkTheme();
            drawBackground(palette);
            drawParticles(palette, dark, 0, false);
        }

        function stopInternal() {
            running = false;
            window.clearTimeout(resizeTimer);
            window.removeEventListener('resize', handleResize);
            window.removeEventListener('mousemove', handleMouseMove);
            window.removeEventListener('mouseout', handleMouseOut);
            document.removeEventListener('visibilitychange', handleVisibility);
            if (animationFrameId !== null) {
                cancelAnimationFrame(animationFrameId);
                animationFrameId = null;
            }
        }

        running = true;
        window.addEventListener('resize', handleResize);
        document.addEventListener('visibilitychange', handleVisibility);
        if (cfg.mouse) {
            window.addEventListener('mousemove', handleMouseMove);
            window.addEventListener('mouseout', handleMouseOut);
        }

        resizeCanvas();
        if (tier === 'static') {
            drawStatic();
        } else {
            animationFrameId = requestAnimationFrame(draw);
        }

        instance = {
            tier: tier,
            stop: stopInternal,
            canvas: canvas
        };
        return instance;
    }

    function stop() {
        if (instance) {
            instance.stop();
            instance = null;
        }
    }

    function isRunning() {
        return instance !== null;
    }

    function autoStart() {
        if (instance) {
            return;
        }
        var canvas = findCanvas();
        if (!canvas) {
            return;
        }
        start();
    }

    window.HomeCenterBg = {
        start: start,
        stop: stop,
        isRunning: isRunning
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', autoStart);
    } else {
        autoStart();
    }

    window.addEventListener('beforeunload', stop, { once: true });
})();
