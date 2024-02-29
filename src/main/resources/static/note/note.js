window.onload = function () {
    noteListInit();
    markdownInit();
}


// =====================================================================================
// =====================================================================================
// =====================================================================================

/**
 * Markdown编辑器初始化
 */
function markdownInit() {
    const converter = new showdown.Converter();
    // converter.setOption('moreStyling', 'true');
    converter.setFlavor('github');

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

    //加载之前的Md文档内容
    markdownLoad();

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
                btnEditOrPreview.querySelector("i").className = "bi bi-eye";
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


    function markdownLoad() {

        //编辑内容
        if (localStorage.editContents != null) {
            editor.value = localStorage.editContents;
            render();
        }
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
        localStorage.editContents = text;
        markdownContent.innerHTML = converter.makeHtml(text);
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
        showToastSimple("保存文档", MsgTypes.SUCCESS)
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


// =====================================================================================
// =====================================================================================
// =====================================================================================

/**
 * 笔记列表初始化
 */
function noteListInit() {

    //控件初始化
    noteListDisplayCtrl();


    /**
     * 笔记列表展示控制
     */
    function noteListDisplayCtrl() {
        const btn = document.getElementById("btnToggleNoteList");
        const noteList = document.getElementById("noteList");
        const divider = document.getElementById("divider");

        btn.onclick = toggleNoteList;

        // 拖拽
        dragControl();
        // 双击还原
        divider.ondblclick = resetNoteListWidth;

        /**
         * 展示或隐藏文件列表
         */
        function toggleNoteList() {
            if (noteList.style.display == 'none') {
                noteList.style.display = '';
                divider.style.display = '';
                //btn.children[0].classList.remove("rotate-180");
                btn.style.color = '';
            } else {
                noteList.style.display = 'none';
                divider.style.display = 'none';
                // btn.children[0].classList.add("rotate-180");
                btn.style.color = 'blue';
            }
        }

        function resetNoteListWidth() {
            noteList.style.width = '280px';
        }

        /**
         * 拖拽方式调整文件列表宽度
         */
        function dragControl() {
            let startX;
            let startWidth;

            divider.addEventListener("mousedown", function (e) {
                startX = e.clientX;
                startWidth = noteList.clientWidth;
                document.documentElement.style.cursor = "col-resize";
                document.addEventListener("mousemove", mousemove);
                document.addEventListener("mouseup", mouseup);
            });

            function mousemove(e) {
                const delta = e.clientX - startX;
                const newWidth = startWidth + delta;

                // 小于窗口的一半
                if (newWidth >= 120 && newWidth <= document.body.clientWidth / 2) {
                    document.documentElement.style.cursor = "col-resize";
                    noteList.style.width = newWidth + "px";
                } else {
                    document.documentElement.style.cursor = "not-allowed";
                }
            }

            function mouseup() {
                document.documentElement.style.cursor = "initial";
                document.removeEventListener("mousemove", mousemove);
                document.removeEventListener("mouseup", mouseup);
            }
        }

    }


    noteListTreeInit();


}

/**
 * 文件列表树初始化
 */
function noteListTreeInit() {
    const noteListTree = document.getElementById("noteListTree");

    // 基本事件处理
    noteListTreeEventHandle();

    //数据初始化
    treeDataInit();

    //控制按钮初始化
    ctrlBtnInit();

    function ctrlBtnInit() {
        const expand = document.getElementById("nlt-expand");
        expand.onclick = function () {
            if (expand.dataset.status == 'expand') {
                collapseAll()
                expand.dataset.status = 'collapse';
                expand.dataset.displayChild = '1';
            } else {
                expandAll()
                expand.dataset.status = 'expand';
                expand.dataset.displayChild = '2';
            }
        }
    }

    function expandAll() {
        noteListTree.querySelectorAll("[data-type='DIR'].closed").forEach(d => {
            d.classList.remove("closed");
        });
    }

    function collapseAll() {
        noteListTree.querySelectorAll("[data-type='DIR']:not(.closed)").forEach(d => {
            d.classList.add("closed");
        });
    }

    /*
    * 菜单初始化
    */
    function treeDataInit() {
        noteListTree.innerHTML = '';

        getRequest("/note/listAll", receive);

        function receive(data) {
            recursive(data, noteListTree);
        }

        function recursive(data, parent) {
            if (!Array.isArray(data)) {
                return;
            }
            const ul = document.createElement('ul');
            data.forEach(item => {
                const li = document.createElement('li');
                const a = document.createElement('a');
                a.textContent = item.name;
                a.dataset.type = item.fileType;
                a.dataset.fullPath = item.fullPath;
                if (item.favorite) {
                    a.dataset.favorite = 'true';
                }
                if (item.asterisk) {
                    a.asterisk = 'true';
                }
                li.appendChild(a);
                if (item.children && item.children.length > 0) {
                    recursive(item.children, li);
                }
                ul.appendChild(li);
            });
            parent.appendChild(ul);
        }
    }

    /**
     * 笔记列表树事件处理
     */
    function noteListTreeEventHandle() {
        noteListTree.onclick = function (event) {
            if (event.target.tagName === 'A') {
                labelClick(event);
            }
        }
        noteListTree.removeEventListener("contextmenu", labelRightClick);
        noteListTree.addEventListener("contextmenu", labelRightClick);
    }

    /**
     * 鼠标右键
     * @param event
     */
    function labelRightClick(event) {
        // 阻止默认的右键菜单行为
        event.preventDefault();
        showToast("鼠标右键触发");
    }

    /**
     * 列表被点击
     * @param event
     */
    function labelClick(event) {
        event.preventDefault();

        const lb = event.target;

        if (lb.dataset.type == "DIR") {
            /*lb.classList.toggle('closed');*/
            if (lb.classList.contains("closed")) {
                lb.classList.remove('closed');
            } else if (lb.dataset.select == "true") {
                lb.classList.add("closed");
            }
        }

        // 取消其他的选中
        noteListTree.querySelectorAll("[data-select='true']").forEach(l => {
            l.dataset.select = "false";
        });
        lb.dataset.select = "true";

        //加载文件
        console.log('lable 被点击', event.target);
        console.log(event.target.textContent)

        const fileType = lb.dataset.type;
        if (fileType == "md") {
            const path = lb.dataset.fullPath;
            getRequest("/note/getNote?path=" + path, data => {
                const editor = document.getElementById("editor");
                editor.value = data.content;
                // 这里先用事件通知的方式使其正常渲染
                editor.dispatchEvent(new Event('input'));

                document.getElementById("noteTitle").textContent = data.name;
                document.getElementById("filePath").textContent = data.parentPath;
                document.getElementById("createTime").textContent = data.createTime;
                document.getElementById("updateTime").textContent = data.updateTime;
            });
        } else {
            showToast("暂时不支持直接打开【" + fileType + "】类型的文件");
        }

    }
}

function noteListSearchInit() {

}

function noteListLatest() {

}

function noteListFavorite() {

}

function noteListAsterisk() {

}








