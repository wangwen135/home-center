(function () {
    var canvas = document.getElementById('bg-canvas');
    if (!canvas || !canvas.getContext) {
        return;
    }

    var ctx = canvas.getContext('2d');
    var W = 0;
    var H = 0;
    var particles = [];
    var mouse = {x: -9999, y: -9999};
    var lastInteractTime = 0;
    var animationFrameId = null;
    var running = true;

    function isDarkTheme() {
        return document.body.classList.contains('dark-theme') ||
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

    function resizeCanvas() {
        var ratio = window.devicePixelRatio || 1;
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
        var particleCount = Math.max(30, Math.floor((W * H) / 80000));
        for (var i = 0; i < particleCount; i++) {
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

    window.addEventListener('resize', handleResize);
    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseout', handleMouseOut);

    function updateInteraction(now) {
        if (now - lastInteractTime < 16) {
            return;
        }
        lastInteractTime = now;

        var mouseThresholdSquared = 160 * 160;
        for (var i = 0; i < particles.length; i++) {
            var p = particles[i];
            var dx = mouse.x - p.x;
            var dy = mouse.y - p.y;
            var distSquared = dx * dx + dy * dy;

            if (distSquared < mouseThresholdSquared && distSquared > 0) {
                var dist = Math.sqrt(distSquared);
                var force = -0.0012 * (1 - dist / 160);
                p.vx += (dx / dist) * force;
                p.vy += (dy / dist) * force;
            }

            p.vx = Math.max(-0.3, Math.min(0.3, p.vx));
            p.vy = Math.max(-0.3, Math.min(0.3, p.vy));
        }
    }

    function draw() {
        if (!running) {
            return;
        }
        var now = performance.now();
        var palette = getPalette();
        var dark = isDarkTheme();

        updateInteraction(now);

        var g = ctx.createLinearGradient(0, 0, W, H);
        g.addColorStop(0, palette.bg0);
        g.addColorStop(0.5, palette.bg1);
        g.addColorStop(1, palette.bg2);
        ctx.fillStyle = g;
        ctx.fillRect(0, 0, W, H);

        for (var i = 0; i < 12; i++) {
            var cx = W * (((i * 0.17) % 1) + 0.02);
            var cy = H * (((i * 0.29) % 1) + 0.01);
            var rad = Math.min(W, H) * (0.14 + 0.02 * Math.sin(now * 0.0007 + i));
            var grad = ctx.createRadialGradient(cx, cy, rad * 0.1, cx, cy, rad);
            var nebulaAlpha = dark ? 0.02 : 0.012;
            grad.addColorStop(0, 'rgba(' + palette.nebula + ',' + (nebulaAlpha + nebulaAlpha * Math.sin(now * 0.0008 + i)) + ')');
            grad.addColorStop(1, palette.clear);
            ctx.fillStyle = grad;
            ctx.beginPath();
            ctx.arc(cx, cy, rad, 0, Math.PI * 2);
            ctx.fill();
        }

        for (var pIndex = 0; pIndex < particles.length; pIndex++) {
            var p = particles[pIndex];
            p.x += p.vx * (0.6 + Math.sin(now * 0.0002));
            p.y += p.vy * (0.6 + Math.cos(now * 0.0001));

            if (p.x < -20) p.x = W + 20;
            if (p.x > W + 20) p.x = -20;
            if (p.y < -20) p.y = H + 20;
            if (p.y > H + 20) p.y = -20;

            ctx.beginPath();
            ctx.fillStyle = palette.particle + (p.alpha * (dark ? 0.9 : 0.55)) + ')';
            ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
            ctx.fill();
        }

        var maxDist = Math.min(W, H) * 0.12;
        var maxDistSquared = maxDist * maxDist;
        ctx.strokeStyle = 'rgba(' + palette.line + ',' + (dark ? 0.12 : 0.08) + ')';
        ctx.lineWidth = dark ? 0.8 : 0.7;

        for (var aIndex = 0; aIndex < particles.length; aIndex++) {
            for (var bIndex = aIndex + 1; bIndex < particles.length; bIndex++) {
                var a = particles[aIndex];
                var b = particles[bIndex];
                var dx = a.x - b.x;
                var dy = a.y - b.y;
                var dSquared = dx * dx + dy * dy;

                if (dSquared < maxDistSquared) {
                    ctx.globalAlpha = (dark ? 0.12 : 0.08) * (1 - Math.sqrt(dSquared) / maxDist);
                    ctx.beginPath();
                    ctx.moveTo(a.x, a.y);
                    ctx.lineTo(b.x, b.y);
                    ctx.stroke();
                }
            }
        }
        ctx.globalAlpha = 1;

        ctx.globalCompositeOperation = dark ? 'lighter' : 'source-over';
        for (var glowIndex = 0; glowIndex < 3; glowIndex++) {
            var gx = (W * 0.2) * (glowIndex + 1) + 60 * Math.sin(now * 0.0004 * (glowIndex + 3));
            var gy = H * (0.2 + 0.25 * glowIndex) + 40 * Math.cos(now * 0.0005 * (glowIndex + 2));
            var gr = Math.min(W, H) * (0.25 - glowIndex * 0.05 + 0.02 * Math.sin(now * 0.0006 * (glowIndex + 1)));
            var glow = ctx.createRadialGradient(gx, gy, gr * 0.1, gx, gy, gr);
            glow.addColorStop(0, 'rgba(' + palette.nebula + ',' + (dark ? 0.02 : 0.014) + ')');
            glow.addColorStop(1, 'rgba(' + palette.nebula + ',0)');
            ctx.fillStyle = glow;
            ctx.beginPath();
            ctx.arc(gx, gy, gr, 0, Math.PI * 2);
            ctx.fill();
        }
        ctx.globalCompositeOperation = 'source-over';

        animationFrameId = requestAnimationFrame(draw);
    }

    function stop() {
        running = false;
        window.clearTimeout(resizeTimer);
        window.removeEventListener('resize', handleResize);
        window.removeEventListener('mousemove', handleMouseMove);
        window.removeEventListener('mouseout', handleMouseOut);
        if (animationFrameId !== null) {
            cancelAnimationFrame(animationFrameId);
        }
    }

    resizeCanvas();
    animationFrameId = requestAnimationFrame(draw);
    window.addEventListener('beforeunload', stop, {once: true});
})();
