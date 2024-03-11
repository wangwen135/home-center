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

    //标题/路径 修改时间等
    const noteTitle = document.getElementById("noteTitle");
    const filePath = document.getElementById("filePath");
    const createTime = document.getElementById("createTime");
    const updateTime = document.getElementById("updateTime");

    // 编辑和预览区域
    const editorWrapper = document.getElementById('editorWrapper');
    const editor = document.getElementById('editor');
    const editorDivider = document.getElementById('editorDivider')
    const preview = document.getElementById('preview');
    const markdownContent = document.getElementById('markdownContent');

    //编辑器工具栏控制
    const btnToggleToolbar = document.getElementById("btnToggleToolbar");
    const toolbarContainer = document.getElementById("toolbarContainer");
    const editorToolbar = document.getElementById("editorToolbar");
    const previewToolbar = document.getElementById("previewToolbar");
    // 编辑/预览 按钮
    const btnEditOrPreview = document.getElementById('btnEditOrPreview');

    this.init = function () {

        //笔记标题控制
        noteTitleCtrl();

        //编辑器控制
        editorCtrl();
        //滚动控制
        scrollCtrl();
        // 点击中间隐藏预览
        hiddenPreviewCtrl();
        // 展示或隐藏编辑器的工具栏
        toggleToolbar();

        // 工具栏按钮绑定事件
        editorToolbarBtnBind();
        previewToolbarBtnBind();

        // 切换编辑和查看模式
        toggleEditOrPreview();

        //加载上一次配置
        loadLastConf();

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

            render();

            editorFootBarRefresh()
        });
    }

    /**
     * 预览工具栏的按钮绑定事件
     */
    function previewToolbarBtnBind() {

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
                // 编辑模式工具栏
                switchEditorToolbar();

                editorWrapper.style.display = '';
                editorDivider.style.display = '';

                // 修改图标
                btnEditOrPreview.querySelector("i").className = "bi bi-filetype-md";
                // 更改按钮文本
                btnEditOrPreview.childNodes[2].textContent = " 预览 ";

            } else {
                // 预览模式工具栏
                switchPreviewToolbar();

                editorWrapper.style.display = 'none';
                editorDivider.style.display = 'none';

                // 修改图标
                btnEditOrPreview.querySelector("i").className = "bi bi-pencil-square";
                // 更改按钮文本
                btnEditOrPreview.childNodes[2].textContent = " 编辑 ";

                /*预览区域总是需要显示*/
                preview.style.display = '';
            }
        }
    }

    function switchEditorToolbar() {
        editorToolbar.classList.remove("d-none");
        previewToolbar.classList.add("d-none");
    }

    function switchPreviewToolbar() {
        editorToolbar.classList.add("d-none");
        previewToolbar.classList.remove("d-none");
    }

    /**
     * 切换工具条
     */
    function toggleToolbar() {

        btnToggleToolbar.onclick = function () {
            if (toolbarContainer.style.display == 'none') {
                /*展示工具栏*/
                localStorage.styleHiddenToolbar = 'false';
                toolbarContainer.style.display = '';
                btnToggleToolbar.children[0].classList.remove("rotate-180");
            } else {
                localStorage.styleHiddenToolbar = 'true';
                hiddenToolbar()
            }
        }
    }

    function hiddenToolbar() {
        if (toolbarContainer.style.display == 'none') {
            return;
        }
        toolbarContainer.style.display = 'none';
        btnToggleToolbar.children[0].classList.add("rotate-180");
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


    //两个窗口滚动条同步
    function scrollCtrl() {
        editor.addEventListener('scroll', () => {
            // 滚动同步 (开关控制)
            if (localStorage.synchronizeScroller == 'false') {
                return;
            }
            const height = editor.scrollHeight;
            const top = editor.scrollTop;
            const pHeight = preview.scrollHeight;
            //窗口百分比同步
            preview.scrollTop = pHeight * top / height;

        });

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

    //点击中间隐藏预览
    function hiddenPreviewCtrl() {
        editorDivider.onclick = function () {
            if (preview.style.display != 'none') {
                preview.style.display = 'none';
            } else {
                preview.style.display = '';
            }
        };
    }


    function editorCtrl() {
        // 输入内容实时渲染
        editor.addEventListener('propertychange', render);
        editor.addEventListener('input', render);

        // 键盘监听
        editor.addEventListener('keydown', editorKeyboardAction);

        //刷新光标位置记录等
        editor.addEventListener('keyup', editorFootBarRefresh);
        editor.addEventListener('mouseup', editorFootBarRefresh);

    }

    /**
     * 渲染内容
     */
    function render() {
        const text = editor.value;
        markdownContent.innerHTML = converter.makeHtml(text);
        previewFootBarRefresh();

        /*editorFootBarRefresh();*/
    }

    this.renderMd = render;


    function previewFootBarRefresh() {
        document.getElementById('previewStatisticHeadline').textContent
            = markdownContent.querySelectorAll('h1, h2, h3, h4, h5, h6').length + '';
        document.getElementById('previewStatisticParagraph').textContent
            = markdownContent.querySelectorAll('p').length + '';

        document.getElementById('previewStatisticCode').textContent
            = markdownContent.querySelectorAll('code').length + '';

        document.getElementById('previewStatisticImage').textContent
            = markdownContent.querySelectorAll('img').length + '';
    }

    function editorFootBarRefresh() {
        const value = editor.value;
        document.getElementById('editorStatisticChars').textContent = value.length + '';
        const rows = value.split('\n').length;
        document.getElementById('editorStatisticRows').textContent = rows + '';

        const cursorPos = editor.selectionStart;
        // 计算光标所在的行和列
        let cursorRow = 0, cursorCol = 0;
        if (cursorPos > 0) {
            cursorRow = value.substring(0, cursorPos).split('\n').length;
            cursorCol = cursorPos - value.lastIndexOf('\n', cursorPos - 1) - 1;
        }
        document.getElementById('editorCursorRow').textContent = cursorRow + '';
        document.getElementById('editorCursorCol').textContent = cursorCol + '';
    }

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
        } else if (ctrl && shift && (key == 's') || key == 'S') { //删除线
            strikethroughAction();
        } else if (ctrl && key == 't') {
            event.preventDefault();
            tableAction();
        } else if (ctrl && key == 's') {
            event.preventDefault();
            saveDoc();
        }
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

        render();
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

        render();
    }


}

