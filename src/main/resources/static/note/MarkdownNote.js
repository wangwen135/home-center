/**
 * Markdown 笔记编辑器
 */
function MarkdownNote(options) {

    //父路径
    let parentPath = null;
    //文件名
    let fileName = null;
    //文件原始内容
    let originContent = null;

    const converter = new showdown.Converter();
    // converter.setOption('moreStyling', 'true');
    converter.setFlavor('github');

    //############## 标题区域 ##############
    //标题，路径，修改时间等
    const noteTitle = document.getElementById("noteTitle");
    const filePath = document.getElementById("filePath");
    const createTime = document.getElementById("createTime");
    const updateTime = document.getElementById("updateTime");

    //标题区域工具栏
    //编辑/预览 按钮
    const btnEditOrPreview = document.getElementById('btnEditOrPreview');
    //展示或隐藏工具栏
    const btnToggleToolbar = document.getElementById("btnToggleToolbar");

    //############## 编辑区域 ##############
    const editorWrapper = document.getElementById('editorWrapper');
    const editorToolbar = document.getElementById("editorToolbar");
    const lineNumbers = document.getElementById('lineNumbers');
    const editor = document.getElementById('editor');

    //############## 分割器 ##############
    const editorPreviewDivider = document.getElementById('editorPreviewDivider')

    //############## 预览区域 ##############
    const previewWrapper = document.getElementById('previewWrapper');
    const previewToolbar = document.getElementById("previewToolbar");
    const markdownContainer = document.getElementById("markdownContainer");
    const markdownScrollbar = document.getElementById("markdownScrollbar");
    const markdownContent = document.getElementById('markdownContent');
    // 目录列表
    const markdownCatalogList = document.getElementById("markdownCatalogList");

    this.init = function () {

        //笔记标题控制
        noteTitleCtrl();

        // 切换编辑和查看模式
        toggleEditOrPreview();

        //编辑器控制
        editorCtrl();

        //滚动控制
        scrollCtrl();

        // 隐藏和显示预览控制
        hiddenPreviewCtrl();

        // 展示或隐藏编辑器的工具栏
        toggleToolbar();

        // 工具栏按钮绑定事件
        editorToolbarBtnBind();
        previewToolbarBtnBind();

        //预览区域右键菜单
        previewContextMenuCtrl();
        //目录处理
        markdownCatalogHandle();

        //加载上一次配置
        loadLastConf();

        //全局快捷键
        registerGlobalShortcutKeys();
    }

    this.getOpenFileFullPath = getFileFullPath;

    function getFileFullPath() {
        if (parentPath == null) {
            return null;
        }
        return parentPath + fileName;
    }

    this.openFile = openFileInner;

    function openFileInner(path) {
        if (path == null || path == '') {
            showToastSimple("要打开的文件路径不能为空", MsgTypes.WARNING);
            return;
        }
        getRequest("/note/getNote?path=" + path, data => {
            editor.value = data.content;

            noteTitle.value = data.name;
            filePath.textContent = data.parentPath;
            createTime.textContent = data.createTime;
            updateTime.textContent = data.updateTime;
            // 收藏和标星


            //设置变量值
            parentPath = data.parentPath;
            fileName = data.name;
            originContent = data.content;


            textareaValueChanged();
        });
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

        const contextMenu = new ContextMenu(menuItems, previewWrapper);
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

        const auto = document.getElementById("p-tb-auto");
        auto.onclick = function (event) {
            clearSelection();
            auto.dataset.select = 'true';
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

        //全屏
        const btnFullscreen = document.getElementById("p-tb-fullscreen");
        btnFullscreen.onclick = function (){
            markdownContainer.requestFullscreen();
        }
    }

    /**
     * 编辑工具栏的按钮绑定事件
     */
    function editorToolbarBtnBind() {
        // 工具栏位
        document.getElementById("tb-bold").onclick = boldAction;
        document.getElementById("tb-italic").onclick = italicAction;
        document.getElementById("tb-underline").onclick = underlineAction;
        document.getElementById("tb-strikethrough").onclick = strikethroughAction;
        document.getElementById("tb-table").onclick = tableAction;
    }

    /**
     * 切换编辑或预览模式
     */
    function toggleEditOrPreview() {
        btnEditOrPreview.onclick = function () {
            if (editorWrapper.style.display == 'none') {

                noteTitle.disabled = false;
                editorWrapper.style.display = '';
                editorPreviewDivider.style.display = '';

                // 修改图标
                btnEditOrPreview.querySelector("i").className = "bi bi-filetype-md";
                // 更改按钮文本
                btnEditOrPreview.childNodes[2].textContent = " 预览 ";

            } else {
                noteTitle.disabled = true;
                editorWrapper.style.display = 'none';
                editorPreviewDivider.style.display = 'none';

                // 修改图标
                btnEditOrPreview.querySelector("i").className = "bi bi-pencil-square";
                // 更改按钮文本
                btnEditOrPreview.childNodes[2].textContent = " 编辑 ";

                /*预览区域总是需要显示*/
                previewWrapper.style.display = '';
            }
        }
    }

    /**
     * 切换工具条
     */
    function toggleToolbar() {

        btnToggleToolbar.onclick = function () {
            if (editorToolbar.style.display == 'none') {
                /*展示工具栏*/
                localStorage.styleHiddenToolbar = 'false';
                editorToolbar.style.display = '';
                previewToolbar.style.display = '';
                btnToggleToolbar.children[0].classList.remove("rotate-180");
                editorPreviewDivider.classList.add("mt-36px")
            } else {
                localStorage.styleHiddenToolbar = 'true';
                editorToolbar.style.display = 'none';
                previewToolbar.style.display = 'none';
                btnToggleToolbar.children[0].classList.add("rotate-180");
                editorPreviewDivider.classList.remove("mt-36px")
            }
        }
    }

    function hiddenToolbar() {
        if (editorToolbar.style.display == 'none') {
            return;
        }
        editorToolbar.style.display = 'none';
        previewToolbar.style.display = 'none';
        btnToggleToolbar.children[0].classList.add("rotate-180");
        editorPreviewDivider.classList.remove("mt-36px")
    }


    /**
     * 加载上一次的配置
     */
    function loadLastConf() {
        // 从本地存储中获得上一次工具栏的状态
        if (localStorage.styleHiddenToolbar == 'true') {
            hiddenToolbar();
        }

        //颜色模式
        if (localStorage.lightDark == 'dark') {
            // darkModel();
        }
    }


    function syncScrolling() {
        // 滚动同步 (开关控制)
        if (localStorage.synchronizeScroller == 'false') {
            return;
        }
        const height = editor.scrollHeight;
        const top = editor.scrollTop;
        const pHeight = markdownScrollbar.scrollHeight;
        //窗口百分比同步
        markdownScrollbar.scrollTop = pHeight * top / height;
    }

    //两个窗口滚动条同步
    function scrollCtrl() {
        editor.addEventListener('scroll', syncScrolling);

        const btnSyncScroll = document.getElementById("tb-syncScroll");
        if (localStorage.synchronizeScroller == 'false') {
            btnSyncScroll.dataset.select = 'false';
        }
        btnSyncScroll.onclick = function () {
            if (localStorage.synchronizeScroller == 'false') {
                localStorage.synchronizeScroller = 'true';
                btnSyncScroll.dataset.select = 'true';
            } else {
                localStorage.synchronizeScroller = 'false';
                btnSyncScroll.dataset.select = 'false';
            }
        }

    }

    //隐藏和显示预览控制
    function hiddenPreviewCtrl() {
        const btnTogglePreview = document.getElementById('tb-togglePreview');

        btnTogglePreview.onclick = togglePreview;
        editorPreviewDivider.onclick = togglePreview;

        function togglePreview() {
            if (previewWrapper.style.display != 'none') {
                previewWrapper.style.display = 'none';
                btnTogglePreview.children[0].classList.add("rotate-180");
            } else {
                previewWrapper.style.display = '';
                btnTogglePreview.children[0].classList.remove("rotate-180");
            }
        }
    }


    function editorCtrl() {
        //行号控制
        // 纵向滚动条与行号同步
        editor.addEventListener('scroll', function (event) {
            //滚动条同步
            lineNumbers.scrollTop = editor.scrollTop;
        });


        // 输入内容
        editor.addEventListener('input', textareaValueChanged);

        // 键盘监听
        editor.addEventListener('keydown', editorKeyboardAction);

        //刷新光标位置记录等
        editor.addEventListener('keyup', debouncedRefreshEditorFootBarCursor);
        editor.addEventListener('mouseup', debouncedRefreshEditorFootBarCursor);

        //右键菜单
        const menuItems = [
            {
                text: '复制', onClick: function () {
                    alert('你点击了菜单项 复制');
                }
            },
            {
                text: '粘贴', onClick: function () {
                    alert('你点击了菜单项 粘贴');
                }
            },
            {
                text: '菜单项 3', onClick: function () {
                    alert('你点击了菜单项 3');
                }
            }
        ];

        const contextMenu = new ContextMenu(menuItems, editor);
        contextMenu.init();
    }

    /**
     * 编辑框内容改变
     */
    function textareaValueChanged() {
        const value = editor.value;
        const lineCount = value.split('\n').length;

        updateEditorLineNumbers(lineCount);

        refreshEditorFootBar(lineCount);

        // 异步渲染，300毫秒之后渲染
        debouncedRender();
    }

    /**
     * 光标所在的行
     */
    let selectedLineNumber = 1;

    function setSelectedLineNumber(line) {
        if (line === selectedLineNumber) {
            return;
        }
        if (line == null) {
            return;
        }
        const selectSpan = lineNumbers.querySelector(".selected");
        if (selectSpan != null) {
            selectSpan.classList.remove("selected");
        }
        selectedLineNumber = line;
        lineNumbers.children[line - 1].classList.add('selected');
    }

    /**
     * 总行数
     */
    let lineNumberCounter = 1;

    function updateEditorLineNumbers(lineCount) {

        if (lineCount == null) {
            lineCount = editor.value.split('\n').length;
        }

        if (lineCount == lineNumberCounter) {
            return;
        }
        while (lineCount != lineNumberCounter) {
            if (lineCount > lineNumberCounter) {
                //增加行数
                lineNumberCounter++;
                const s = document.createElement("span");
                s.textContent = lineNumberCounter + '';
                lineNumbers.appendChild(s);
            } else if (lineCount < lineNumberCounter) {
                lineNumberCounter--;
                // 移除最后一个子元素
                lineNumbers.removeChild(lineNumbers.lastChild);
            }
        }
    }

    // 防抖渲染
    const debouncedRender = debounce(function (event) {
        render();
    }, 300);

    /**
     * 实时内容渲染
     */
    function render() {
        const text = editor.value;
        markdownContent.innerHTML = converter.makeHtml(text);
        refreshPreviewFootBar();

        updateCatalog();
    }

    this.renderMd = render;

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

    function refreshEditorFootBar(rows) {
        const value = editor.value;
        document.getElementById('editorStatisticChars').textContent = value.length + '';
        if (rows == null) {
            rows = value.split('\n').length;
        }
        document.getElementById('editorStatisticRows').textContent = rows + '';

        //刷新光标位置
        refreshEditorFootBarCursor();
    }

    // 相当于异步刷新
    const debouncedRefreshEditorFootBarCursor = debounce(function (event) {
        refreshEditorFootBarCursor();
    }, 1);

    function refreshEditorFootBarCursor() {
        const value = editor.value;
        const cursorPos = editor.selectionStart;
        // 计算光标所在的行和列
        let cursorRow = 1, cursorCol = 0;
        if (cursorPos > 0) {
            cursorRow = value.substring(0, cursorPos).split('\n').length;
            cursorCol = cursorPos - value.lastIndexOf('\n', cursorPos - 1) - 1;
        }
        document.getElementById('editorCursorRow').textContent = cursorRow + '';
        setSelectedLineNumber(cursorRow);
        document.getElementById('editorCursorCol').textContent = cursorCol + '';
    }

    /**
     * 快捷键处理
     * @param event
     */
    function editorKeyboardAction(event) {
        const key = event.key;
        const ctrl = event.ctrlKey;
        const shift = event.shiftKey;
        // console.log(key);
        // console.log(ctrl);
        // console.log(shift);

        // tab按键变成4个空格 (开关控制)
        if (key == 'Tab') {
            event.preventDefault();
            insertText("    ");
        } else if (ctrl && key == 'b') { //加粗
            boldAction();
        } else if (ctrl && key == 'i') { //斜体
            italicAction();
        } else if (ctrl && key == 'u') {// 下划线
            event.preventDefault();
            underlineAction()
        } else if (ctrl && shift && (key == 's' || key == 'S')) { //删除线
            strikethroughAction();
        } else if (ctrl && key == 't') {
            event.preventDefault();
            tableAction();
        } else if (ctrl && key == 's') {
            event.preventDefault();
            saveDoc();
        }
    }

    function registerGlobalShortcutKeys() {
        function handleKeyDown(event) {
            //保存
            if (event.ctrlKey && event.key === 's') {
                // 阻止默认的保存事件
                event.preventDefault();
                saveDoc();
            }
        }

        document.addEventListener('keydown', handleKeyDown);
    }


    function saveDoc() {

        const content = editor.value;
        const name = noteTitle.value;
        const parentPath = filePath.textContent;

        postRequest("/note/save", {
            name: name,
            parentPath: parentPath,
            content: content
        }).then(data => {
            // 更新文档日期
            createTime.textContent = data.createTime;
            updateTime.textContent = data.updateTime;

            showToastSimple("保存成功", MsgTypes.SUCCESS, Position.TopCenter);
        });

    }

    /**
     * 标题改变同步
     */
    function titleChangeSync() {
        const noteTitleSpan = document.getElementById("noteTileSpan");
        noteTitleSpan.textContent = noteTitle.value;
    }


    function checkInvalidFileName(text) {
        // Windows文件名中禁止的字符正则表达式
        const invalidCharsRegex = /[<>\/\\|:\?\*\"\x00-\x1F]/;
        return invalidCharsRegex.test(text);
    }

    function replaceInvalidFileName(text) {
        const invalidCharsRegex = /[<>\/\\|:\?\*\"\x00-\x1F]+/g;
        return text.replace(invalidCharsRegex, '');
    }

    function noteTitleCtrl() {
        //禁止输入特殊字符
        noteTitle.onkeypress = handleKeyPress;
        //粘贴时过滤特殊字符
        noteTitle.onpaste = handlePaste;
        //拖拽时过滤特殊字符
        noteTitle.ondrop = handleDrop;

        //标题修改同步，这个是为了宽度自动填充
        noteTitle.addEventListener("input", titleChangeSync)

        //修改文件名称
        noteTitle.onblur = onBlurCheck;

        //提示信息
        const tooltip = new bootstrap.Tooltip(noteTitle, {
            title: '文件名中禁止出现 < > : " / \\ | ? * 或控制字符',
            animation: false,
            placement: 'bottom',
            trigger: 'manual',
            customClass: 'note-title-tooltips'
        });

        //保留提示状态
        let tipsShow = false;
        let timerId;

        function showTips(content) {
            const defaultTitle = '文件名中禁止出现 < > : " / \\ | ? * 或控制字符';

            if (content == null || content == '') {
                tooltip._config.title = defaultTitle;
            } else {
                tooltip._config.title = content;
            }

            if (tipsShow) {
                // 取消定时器
                clearTimeout(timerId);
                tooltip.hide();
            }

            // tooltip.hide();
            tooltip.show();
            tipsShow = true;
            timerId = setTimeout(() => {
                tooltip.hide();
                tipsShow = false;
            }, 2000);
        }

        function onBlurCheck() {
            const newName = noteTitle.value;
            if (newName == fileName) {
                return;
            }
            showConfirm("确定将文件名修改为：", newName, MsgTypes.QUESTION, callbackTrue, callbackFalse, callbackFalse, {keyboard: true});

            function callbackFalse() {
                noteTitle.value = fileName;
            }

            function callbackTrue() {
                //修改文件名
                renameNote(newName);
            }
        }

        function handleKeyPress(e) {
            // 获取用户输入的内容
            const char = e.key;
            // 检查输入内容中是否包含禁止字符
            if (checkInvalidFileName(char)) {
                e.preventDefault();
                showTips("文件名中禁止包含：" + char);
            }
        }

        // 处理粘贴事件
        function handlePaste(e) {
            // 获取用户粘贴的内容
            let text = e.clipboardData.getData('text/plain');
            if (checkInvalidFileName(text)) {
                // 阻止默认的粘贴行为
                e.preventDefault();

                text = replaceInvalidFileName(text);

                //将过滤后的文本插入到输入框中
                document.execCommand('insertText', false, text);
                showTips();
            }

        }

        //处理拖拽事件
        function handleDrop(e) {
            // 获取拖拽的文本数据
            let text = e.dataTransfer.getData('text/plain');
            if (checkInvalidFileName(text)) {
                e.preventDefault();

                text = replaceInvalidFileName(text);
                //将过滤后的文本插入到输入框中
                e.target.focus();
                document.execCommand('insertText', false, text);
                showTips();
            }
        }

    }

    /**
     * 修改笔记名称
     * @param newName
     */
    function renameNote(newName) {

        let formData = new FormData();
        formData.append('filePath', getFileFullPath());
        formData.append('newName', newName);

        let oldName = fileName;

        postRequest('rename', formData, pathVo => {
            noteTitle.value = newName;
            fileName = newName;

            titleChangeSync();
            //抛出事件
            //通知菜单树修改文件名称
            if (options != null && typeof options.renameCallback === 'function') {
                options.renameCallback(parentPath, oldName, newName);
            }

            showToastSimple("文件名修改成功！", MsgTypes.INFO, Position.TopCenter);
        });
    }

    //删除线
    function strikethroughAction() {
        wrapText("~~", "~~");
    }

    //下划线
    function underlineAction() {
        wrapText("<u>", "</u>");
    }

    function italicAction() {//斜体
        wrapText("*", "*");
    }

    function boldAction() {
        wrapText("**", "**");
    }

    function tableAction() {
        insertText("| header1 | header2 |\n" +
            "|---|---|\n" +
            "| row1-1 | row1-2 |\n" +
            "| row2-1 | row2-2 |");
    }

    /**
     * 插入内容
     * @param str
     */
    function insertText(str) {
        const start = editor.selectionStart;
        const end = editor.selectionEnd;
        const oldValue = editor.value;
        editor.value = oldValue.substring(0, start) + str + oldValue.substring(end);
        editor.selectionStart = editor.selectionEnd = start + str.length;

        textareaValueChanged();
    }

    /**
     * 包围内容
     * @param strStart
     * @param strEnd
     */
    function wrapText(strStart, strEnd) {
        const start = editor.selectionStart;
        const end = editor.selectionEnd;
        const oldValue = editor.value;
        const selectedText = oldValue.substring(start, end);
        editor.value = oldValue.substring(0, start) + strStart + selectedText + strEnd + oldValue.substring(end);
        editor.selectionStart = editor.selectionEnd = start + strStart.length + selectedText.length + strEnd.length;

        textareaValueChanged();
    }


}

