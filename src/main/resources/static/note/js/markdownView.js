function MarkdownView(options) {

    //markdown文件内容
    let markdownText = null;

    //父路径
    let parentPath = null;
    //文件名
    let fileName = null;
    //文件原始内容
    let originContent = null;

    const converter = new showdown.Converter();
    // converter.setOption('moreStyling', 'true');
    converter.setFlavor('github');

    const previewWrapper = document.getElementById('previewWrapper');
    const previewToolbar = document.getElementById("previewToolbar");
    const markdownContainer = document.getElementById("markdownContainer");
    const markdownContent = document.getElementById('markdownContent');
    // 目录列表
    const markdownCatalogList = document.getElementById("markdownCatalogList");

    this.init = function () {
        // 工具栏按钮绑定事件
        previewToolbarBtnBind();

        //预览区域右键菜单
        previewContextMenuCtrl();

        //目录处理
        markdownCatalogHandle();

    }


    /**
     * 预览区域右键菜单
     */
    function previewContextMenuCtrl() {
        //右键菜单
        const menuItems = [
            {
                text: '宽度', onClick: function () {
                    alert('你点击了菜单项 宽度');
                }
            },
            {
                text: '样式', onClick: function () {
                    alert('你点击了菜单项 样式');
                }
            },
            {
                text: '颜色', onClick: function () {
                    alert('你点击了菜单项 颜色');
                }
            }
        ];

        const contextMenu = new ContextMenu(menuItems, markdownContainer);
        contextMenu.init();
    }

    /**
     * 文档中的工具按钮
     */
    function markdownCatalogHandle() {
        const btnCatalog = document.getElementById("btnCatalog");
        const markdownCatalogContainer = document.getElementById("markdownCatalogContainer");

        btnCatalog.onclick = function () {
            markdownCatalogContainer.classList.toggle("d-none");
        }

        //点击时滚动内容
        markdownCatalogList.onclick = function (e) {
            const hl = e.target;
            const index = hl.dataset.index;
            markdownContent.querySelectorAll('h1, h2, h3, h4, h5, h6')[index]
                .scrollIntoView({behavior: 'smooth', block: 'start'});
        }
    }

    /**
     * 预览工具栏的按钮绑定事件
     */
    function previewToolbarBtnBind() {

        // 宽度控制
        const small = document.getElementById("p-tb-small");
        small.onclick = function (event) {
            clearSelection();
            small.dataset.select = 'true';
            markdownContent.style.maxWidth = '680px';
        }

        const middle = document.getElementById("p-tb-middle");
        middle.onclick = function (event) {
            clearSelection();
            middle.dataset.select = 'true';
            markdownContent.style.maxWidth = '1020px';
        }

        const large = document.getElementById("p-tb-large");
        large.onclick = function (event) {
            clearSelection();
            large.dataset.select = 'true';
            markdownContent.style.maxWidth = '1360px';
        }

        const full = document.getElementById("p-tb-full");
        full.onclick = function (event) {
            clearSelection();
            full.dataset.select = 'true';
            markdownContent.style.maxWidth = '100%';
        }

        function clearSelection() {
            previewToolbar.querySelectorAll("[data-select='true']").forEach(i => {
                i.dataset.select = '';
            });
        }

        // 颜色控制
        const colorDropdown = document.getElementById("tb-color-dropdown");
        colorDropdown.onclick = function (e) {
            const color = e.target.dataset.color;
            markdownContent.style.backgroundColor = color;
        }

        //下载
        const btnDownload = document.getElementById("p-tb-download");
        btnDownload.onclick = function () {
            /*
            html2canvas(markdownContent, {scale: 2, useCORS: true}).then(canvas => {
                const imgData = canvas.toDataURL('image/png');
                const downloadLink = document.createElement('a');
                downloadLink.href = imgData;
                downloadLink.download = fileName + '.png';
                document.body.appendChild(downloadLink);
                downloadLink.click();
                document.body.removeChild(downloadLink);
            });
            */

        }

        //全屏
        const btnFullscreen = document.getElementById("p-tb-fullscreen");
        btnFullscreen.onclick = function () {
            markdownContainer.requestFullscreen();
        }
    }


    this.showToolbar = function () {
        previewToolbar.style.display = '';
    }

    this.hiddenToolbar = function () {
        previewToolbar.style.display = 'none';
    }


    // 防抖渲染
    const debouncedRenderer = debounce(function (text) {
        render(text);
    }, 300);


    function debouncedRender(text) {
        debouncedRenderer(text);
    }

    this.debouncedRenderMd = debouncedRender;

    /**
     * 实时内容渲染
     */
    function render(text) {
        markdownText = text;
        markdownContent.innerHTML = converter.makeHtml(text);

        if (!options.disableFootBar) {
            refreshPreviewFootBar();
        }

        updateCatalog();
    }

    this.renderMd = render;

    this.getMarkdownTest = function () {
        return markdownText;
    }

    /**
     * 刷新底部状态栏
     */
    function refreshPreviewFootBar() {
        document.getElementById('previewStatisticHeadline').textContent
            = markdownContent.querySelectorAll('h1, h2, h3, h4, h5, h6').length + '';
        document.getElementById('previewStatisticParagraph').textContent
            = markdownContent.querySelectorAll('p').length + '';

        document.getElementById('previewStatisticCode').textContent
            = markdownContent.querySelectorAll('code').length + '';

        document.getElementById('previewStatisticImage').textContent
            = markdownContent.querySelectorAll('img').length + '';
    }

    /**
     * 更新目录
     */
    function updateCatalog() {
        markdownCatalogList.innerHTML = '';
        const headers = markdownContent.querySelectorAll('h1, h2, h3, h4, h5, h6');
        for (let i = 0; i < headers.length; i++) {
            const h = headers[i];
            const catalogItem = document.createElement('div');
            catalogItem.className = 'markdown-catalog-item';
            catalogItem.textContent = h.textContent;
            catalogItem.dataset.headline = h.tagName;
            catalogItem.dataset.index = i;
            markdownCatalogList.appendChild(catalogItem);
        }
    }


}

