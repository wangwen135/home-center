/**
 * Markdown 编辑器
 * @param markdownViewer
 * @param options
 * @constructor
 */
function MarkdownEditor(markdownViewer, options) {

    //父路径
    let parentPath = null;
    //文件名
    let fileName = null;
    //文件原始内容
    let originContent = null;

    //############## 编辑区域 ##############
    const editorWrapper = document.getElementById('editorWrapper');
    const editorToolbar = document.getElementById("editorToolbar");
    const lineNumbers = document.getElementById('lineNumbers');
    const editor = document.getElementById('editor');

    this.getFileName = function () {
        return fileName;
    }
    this.setFileName = function (name) {
        fileName = name;
    }
    this.getParentPath = function () {
        return parentPath;
    }
    this.setParentPath = function (path) {
        parentPath = path;
    }

    this.getFileFullPath = function () {
        if (parentPath == null) {
            return null;
        }
        return parentPath + fileName;
    }
    this.showToolbar = function () {
        localStorage.style.display = '';
        editorToolbar.style.display = '';
    }

    this.hiddenToolbar = function () {
        localStorage.styleHiddenToolbar = 'true';
        editorToolbar.style.display = 'none';
    }


    this.init = function () {
        //编辑器控制
        editorCtrl();

        // 工具栏按钮绑定事件
        editorToolbarBtnBind();

        //编辑器右键菜单
        editorContextMenuInit();

        //全局快捷键
        registerGlobalShortcutKeys();
    }


    this.openFile = function (path, callback) {
        if (path == null || path == '') {
            showToastSimple("要打开的文件路径不能为空", MsgTypes.WARNING);
            return;
        }
        getRequest("/note/getNote?path=" + path, data => {
            editor.value = data.content;

            //设置变量值
            parentPath = data.parentPath;
            fileName = data.name;
            originContent = data.content;

            textareaValueChanged();

            if (typeof callback === 'function') {
                callback(data);
            }
        });
    }

    function saveDoc() {
        const content = editor.value;

        if (!fileName) {
            showToastSimple("文件名错误", MsgTypes.DANGER, Position.TopCenter);
            return;
        }
        if (!parentPath) {
            showToastSimple("文件路径错误", MsgTypes.DANGER, Position.TopCenter);
            return;
        }

        postRequest("/note/save", {
            name: fileName,
            parentPath: parentPath,
            content: content
        }, data => {
            showToastSimple("保存成功", MsgTypes.SUCCESS, Position.TopCenter);

            if (options != null && typeof options.saveCallBack === 'function') {
                options.saveCallBack(data);
            }
        });
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
    }

    function editorContextMenuInit() {

        //右键菜单
        const menuItems = [
            {
                text: '复制', onClick: function () {
                    const text = getSelectedText();
                    navigator.clipboard.writeText(text).catch(function (err) {
                        console.error('复制失败：', err);
                    });
                }
            },
            {
                text: '粘贴', onClick: function () {
                    navigator.clipboard.readText().then(text => {
                        insertText(text);
                        console.log('剪贴板中的文本：', text);
                    }).catch(err => {
                        console.error('读取剪贴板失败：', err);
                    });

                    editor.focus();

                }
            },
            {
                type: 'separator'
            },
            {
                text: '菜单项 3', onClick: function () {
                    alert('你点击了菜单项 3');
                }
            },
            {
                type: 'separator'
            },
            {
                text: '在新窗口打开',
                onClick: function () {
                    window.open("edit.html#" + parentPath + fileName, "_blank");
                }
            },
            {
                text: '在新窗口查看',
                onClick: function () {
                    window.open("view.html#" + parentPath + fileName, "_blank");
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

        markdownViewer.debouncedRenderMd(value);
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

    //####################### 编辑快捷键和工具栏 #################################

    /**
     * 编辑工具栏的按钮绑定事件
     */
    function editorToolbarBtnBind() {
        // 工具栏位
        document.getElementById("tb-bold").onclick = boldAction;
        document.getElementById("tb-italic").onclick = italicAction;
        document.getElementById("tb-underline").onclick = underlineAction;
        document.getElementById("tb-strikethrough").onclick = strikethroughAction;
        //标题1~6
        document.getElementById("tb-headline-ul").onclick = function (event) {
            const level = event.target.dataset.level;
            if (level) {
                insertHeadline(level);
            }
        };
        // 水平线
        document.getElementById('tb-horizontal-line').onclick = horizontalLineAction;
        //引用
        document.getElementById("tb-quote").onclick = quoteAction;
        //无序列表
        document.getElementById("tb-unordered-list").onclick = unorderedListAction;
        //有序列表
        document.getElementById("tb-ordered-list").onclick = orderedListAction;
        //任务列表-未勾选
        document.getElementById("tb-task-uncheck").onclick = taskUncheckAction;
        //任务列表-已勾选
        document.getElementById("tb-task-check").onclick = taskCheckAction;

        //表格
        document.getElementById("tb-table").onclick = tableAction;
        //图片
        document.getElementById("tb-image").onclick = imageAction;
        //链接
        document.getElementById("tb-link").onclick = linkAction;
        //内嵌代码
        document.getElementById("tb-inner-code").onclick = innerCodeAction;
        //代码块
        document.getElementById("tb-code").onclick = codeAction;
    }

    /**
     * 快捷键处理
     * @param event
     */
    function editorKeyboardAction(event) {
        const key = event.key;
        const ctrl = event.ctrlKey;
        const shift = event.shiftKey;

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
        } else if (ctrl && shift && key == 'S') { //删除线
            strikethroughAction();
        } else if (ctrl && key >= '1' && key <= '6') { //标题1~6
            event.preventDefault();
            insertHeadline(key);
        } else if (ctrl && shift && key == 'Q') {//引用
            quoteAction();
        } else if (ctrl && shift && key == '{') {//无序列表
            underlineAction();
        } else if (ctrl && shift && key == '}') {//有序列表
            orderedListAction();
        } else if (ctrl && shift && key == 'T') {//表格
            event.preventDefault();
            tableAction();
        } else if (ctrl && key == 's') {//保存
            //阻止默认行为
            event.preventDefault();
            //阻止事件传播
            event.stopPropagation();

            saveDoc();
        }
    }

    /**
     * 代码块
     */
    function codeAction() {
        insertTextAtLineStart("```\n```\n");
    }

    /**
     * 内嵌代码
     */
    function innerCodeAction() {
        insertText("`code`");
    }

    /**
     * 图片
     */
    function imageAction() {
        insertTextAtLineStart("![说明](https://img.wangwen135.top:23456/default.png)\n");
    }

    /**
     * 链接
     */
    function linkAction() {
        insertText("[链接](https://wangwen135.top)");
    }

    /**
     * 表格
     */
    function tableAction() {
        insertTextAtLineStart(
            "| header1 | header2 |\n" +
            "|---|---|\n" +
            "| row1-1 | row1-2 |\n" +
            "| row2-1 | row2-2 |\n");
    }


    /**
     * 任务未勾选
     */
    function taskUncheckAction() {
        insertTextAtLineStart("- [ ] ")
    }

    /**
     * 任务已勾选
     */
    function taskCheckAction() {
        insertTextAtLineStart("- [x] ")
    }

    /**
     * 无序列表
     */
    function unorderedListAction() {
        insertTextAtLineStart("- ")
    }

    /**
     * 有序列表
     */
    function orderedListAction() {
        let number = 1;
        //获取上一行的序号

        insertTextAtLineStart(number + ". ");
    }

    /**
     * 引用
     */
    function quoteAction() {
        insertTextAtLineStart("> ")
    }

    /**
     * 水平线
     */
    function horizontalLineAction() {
        insertTextAtLineStart("\n---\n");
    }

    /**
     * 插入不同级别的标题
     * @param level
     */
    function insertHeadline(level) {
        const prefix = '#'.repeat(level) + ' ';
        insertTextAtLineStart(prefix);
    }

    /**
     * 删除线
     */
    function strikethroughAction() {
        wrapText("~~", "~~");
    }

    /**
     * 下划线
     */
    function underlineAction() {
        wrapText("<u>", "</u>");
    }

    /**
     * 斜体
     */
    function italicAction() {
        wrapText("*", "*");
    }

    /**
     * 粗体
     */
    function boldAction() {
        wrapText("**", "**");
    }

    /**
     * 在光标所在行的开头的插入内容
     * @param str
     */
    function insertTextAtLineStart(str) {
        const oldValue = editor.value;
        // 获取光标所在行的起始位置
        let startOfLine = editor.selectionStart;
        while (startOfLine > 0 && oldValue[startOfLine - 1] !== '\n') {
            startOfLine--;
        }
        // 插入内容
        editor.value = oldValue.substring(0, startOfLine) + str + oldValue.substring(startOfLine);
        // 更新光标位置
        editor.selectionStart = editor.selectionEnd = startOfLine + str.length;

        textareaValueChanged();
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

    function getSelectedText() {
        return editor.value.substring(editor.selectionStart, editor.selectionEnd);
    }

}

