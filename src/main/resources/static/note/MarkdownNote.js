/**
 * Markdown 笔记编辑器
 */
function MarkdownNote() {
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

    this.openFile = openFileInner;

    function openFileInner(path) {
        if (path == null || path == '') {
            showToastSimple("要打开的文件路径不能为空", MsgTypes.WARNING);
            return;
        }
        getRequest("/note/getNote?path=" + path, data => {
            editor.value = data.content;

            noteTitle.textContent = data.name;
            filePath.textContent = data.parentPath;
            createTime.textContent = data.createTime;
            updateTime.textContent = data.updateTime;
            // 收藏和标星

            //记录最后打开的文件名
            localStorage.openFilePath = path;
            render();
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
        editor.addEventListener('keydown', editorKeyBoardAction);
    }

    /**
     * 渲染内容
     */
    function render() {
        const text = editor.value;
        markdownContent.innerHTML = converter.makeHtml(text);
        footBarRefresh();
    }

    this.renderMd = render;

    function footBarRefresh() {
        document.getElementById('previewStatisticHeadline').textContent
            = markdownContent.querySelectorAll('h1, h2, h3, h4, h5, h6').length + '';
        document.getElementById('previewStatisticParagraph').textContent
            = markdownContent.querySelectorAll('p').length + '';

        document.getElementById('previewStatisticCode').textContent
            = markdownContent.querySelectorAll('code').length + '';

        document.getElementById('previewStatisticImage').textContent
            = markdownContent.querySelectorAll('img').length + '';
    }

    function editorKeyBoardAction(event) {
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
        const name = document.getElementById("noteTitle").textContent;
        const parentPath = document.getElementById("filePath").textContent;

        postRequest("/note/save", {
            name: name,
            parentPath: parentPath,
            content: content
        }).then(data => {
            // 更新文档日期
            showToastSimple("保存成功", MsgTypes.SUCCESS)
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

