window.onload = function () {
    init();
    dragControl();
    menuInit();
    markdownInit();
}

function init() {

}

// ===========================================================
// 编辑器初始化
let converter;
let editor, content, preview;
// *** 模式切换 ***
let isEditMode = true;

function markdownInit() {

    converter = new showdown.Converter();
    // converter.setOption('moreStyling', 'true');
    converter.setFlavor('github');


    editor = document.getElementById('editor');
    preview = document.getElementById('preview');
    content = document.getElementById('markdownContent');

    // 滚动同步 (开关控制)
    editor.addEventListener('scroll', () => {
        const height = editor.scrollHeight;
        const top = editor.scrollTop;
        const pHeight = preview.scrollHeight;

        //直接滚动到底部？

        //窗口百分比同步
        preview.scrollTop = pHeight * top / height;

    });

    // 中间部分
    document.getElementById('editorDivider').onclick = function () {
        if (preview.style.display != 'none') {
            preview.style.display = 'none';
        } else {
            preview.style.display = '';
        }
    };

    // 工具栏位
    document.getElementById("t-table").onclick = function () {
        insertText("| header1 | header2 |\n" +
            "|---|---|\n" +
            "| row1-1 | row1-2 |\n" +
            "| row2-1 | row2-2 |");
    }

    // *** 模式切换 ***

    const editPreviewModelBtn = document.getElementById('edit-preview-model');
    editPreviewModelBtn.onclick = function () {
        if (isEditMode) {
            previewModel();
        } else {
            editModel();
        }
    }
    markdownLoad();

    // 输入内容实时渲染
    editor.addEventListener('propertychange', render);
    editor.addEventListener('input', render);

    // 键盘监听
    editor.addEventListener('keydown', editorKeyBoardAction);
}


function markdownLoad() {
    //颜色模式
    if (localStorage.lightDark == 'dark') {
        darkModel();
    }
    //编辑内容
    if (localStorage.editContents != null) {
        editor.value = localStorage.editContents;
        render();
    }
}

/**
 * 渲染内容
 */
function render() {
    const text = editor.value;
    localStorage.editContents = text;
    content.innerHTML = converter.makeHtml(text);
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
        wrapText("**", "**");
    } else if (ctrl && key == 'i') { //斜体
        wrapText("*", "*");
    } else if (ctrl && key == 'u') {// 下划线
        event.preventDefault();
        wrapText("<u>", "</u>");
    } else if (ctrl && shift && (key == 's') || key == 'S') { //删除线
        wrapText("~~", "~~");
    } else if (ctrl && key == 't') {
        event.preventDefault();
        insertText("| header1 | header2 |\n" +
            "|---|---|\n" +
            "| row1-1 | row1-2 |\n" +
            "| row2-1 | row2-2 |");
    } else if (ctrl && key == 's') {
        event.preventDefault();
        console.log("保存文档");
    }

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

// 编辑模式
function editModel() {
    editor.style.display = '';
    divider.style.display = '';
    document.getElementById('edit-preview-model').innerText = "预览";
    isEditMode = true;
}

// 预览模式
function previewModel() {
    editor.style.display = 'none';
    document.getElementById("editorDivider").style.display = 'none';
    preview.style.display = '';
    document.getElementById('edit-preview-model').innerText = "编辑";
    isEditMode = false;
}

// ===========================================================


/*
 * 菜单初始化
 *
 */
function menuInit() {
    const allLabels = document.querySelectorAll(".tree li > label");

    allLabels.forEach((li) => {
        console.log("给【" + li.textContent + "】添加点击事件")
        li.addEventListener('click', labelClick);
    });

}

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
    document.querySelectorAll(".tree li >label[data-select='true']").forEach(l => {
        l.dataset.select = "false";
    });
    lb.dataset.select = "true";

    //加载文件
    console.log('lable 被点击', event.target);
    console.log(event.target.textContent)
}

function showOrHideNoteList() {
    const noteList = document.querySelector("#noteList");
    const divider = document.querySelector("#divider");

    // btnNoteListToggle
    // 图标样式
    // 事件注册 怎么统一

    let d = 'none';
    if (noteList.style.display == 'none') {
        d = '';
    }
    noteList.style.display = d;
    divider.style.display = d;
}


let startX;
let startWidth;

function dragControl() {
    const noteList = document.querySelector("#noteList");
    const divider = document.querySelector("#divider");
    const content = document.querySelector("#content");

    divider.addEventListener("mousedown", function (e) {
        startX = e.clientX;
        startWidth = noteList.clientWidth;
        document.documentElement.style.cursor = "col-resize";
        document.addEventListener("mousemove", mousemove);
        document.addEventListener("mouseup", mouseup);
    });
}

function mousemove(e) {
    const delta = e.clientX - startX;
    const newWidth = startWidth + delta;

    // if (newWidth >= 100 && newWidth <= 680) {
    if (newWidth >= 10 && newWidth <= 2680) {
        document.documentElement.style.cursor = "col-resize";
        document.querySelector("#noteList").style.width = newWidth + "px";
    } else {
        document.documentElement.style.cursor = "not-allowed";
    }
}

function mouseup() {
    document.documentElement.style.cursor = "initial";
    document.removeEventListener("mousemove", mousemove);
    document.removeEventListener("mouseup", mouseup);
}

