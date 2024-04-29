/**
 * Markdown 笔记
 * 整个右侧，包括标题区域，编辑器，查看器
 */
function MarkdownNote(options) {

    const markdownViewer = new MarkdownViewer();
    const markdownEditor = new MarkdownEditor(markdownViewer, {
            saveCallBack: onSaveCallback
        }
    );

    //父路径
    let parentPath = null;
    //文件名
    let fileName = null;
    //文件原始内容
    let originContent = null;

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

    //############## 分割器 ##############
    const editorPreviewDivider = document.getElementById('editorPreviewDivider')

    //############## 编辑区域 ##############
    const editorWrapper = document.getElementById('editorWrapper');
    const editorToolbar = document.getElementById("editorToolbar");

    //############## 预览区域 ##############
    const previewWrapper = document.getElementById('previewWrapper');
    const previewToolbar = document.getElementById("previewToolbar");

    this.init = function () {
        markdownViewer.init()
        markdownEditor.init();

        //笔记标题控制
        noteTitleCtrl();

        // 切换编辑和查看模式
        toggleEditOrPreview();

        //滚动控制
        scrollCtrl();

        // 隐藏和显示预览控制
        hiddenPreviewCtrl();

        // 展示或隐藏编辑器的工具栏
        toggleToolbar();

        //加载上一次配置
        loadLastConf();
    }


    this.getFileFullPath = function () {
        if (parentPath == null) {
            return null;
        }
        return parentPath + fileName;
    }

    this.getFileName = function () {
        return fileName;
    }

    this.openFile = function (path, callback) {

        if (path == null || path == '') {
            showToastSimple("要打开的文件路径不能为空", MsgTypes.WARNING);
            return;
        }

        markdownEditor.openFile(path, data => {
            noteTitle.value = data.name;
            filePath.textContent = data.parentPath;
            createTime.textContent = data.createTime;
            updateTime.textContent = data.updateTime;
            // 收藏和标星

            //设置变量值
            parentPath = data.parentPath;
            fileName = data.name;
            originContent = data.content;

            if (typeof callback === 'function') {
                callback(data);
            }
        });
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
                btnToggleToolbar.dataset.bsOriginalTitle = "隐藏工具栏";
            } else {
                localStorage.styleHiddenToolbar = 'true';
                editorToolbar.style.display = 'none';
                previewToolbar.style.display = 'none';
                btnToggleToolbar.children[0].classList.add("rotate-180");
                editorPreviewDivider.classList.remove("mt-36px")
                btnToggleToolbar.dataset.bsOriginalTitle = "展开工具栏";
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

    /**
     * 两个窗口滚动条同步
     */
    function scrollCtrl() {
        // 编辑窗口
        const editor = document.getElementById('editor');
        // 预览窗口
        const markdownScrollbar = document.getElementById("markdownScrollbar");

        editor.addEventListener('scroll', syncScrolling);

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
                btnTogglePreview.dataset.bsOriginalTitle = '展开预览区域';
            } else {
                previewWrapper.style.display = '';
                btnTogglePreview.children[0].classList.remove("rotate-180");
                btnTogglePreview.dataset.bsOriginalTitle = '隐藏预览区域';
            }
        }
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
            if (!newName) {
                noteTitle.value = fileName;
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
        formData.append('filePath', parentPath + fileName);
        formData.append('newName', newName);

        let oldName = fileName;

        postRequest('/note/rename', formData, pathVo => {
            noteTitle.value = newName;
            fileName = newName;

            titleChangeSync();

            markdownEditor.setFileName(newName)

            //抛出事件
            //通知菜单树修改文件名称
            if (options != null && typeof options.renameCallback === 'function') {
                options.renameCallback(parentPath, oldName, newName);
            }

            showToastSimple("文件名修改成功！", MsgTypes.INFO, Position.TopCenter);
        });
    }

    /**
     * 保存时修改文件时间
     * @param data
     */
    function onSaveCallback(data) {
        // 更新文档日期
        createTime.textContent = data.createTime;
        updateTime.textContent = data.updateTime;
    }


}

