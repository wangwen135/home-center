let noteTree;
let mdNote;


window.onload = function () {
    frameworkInit();
    noteListInit();
    mdNote = new MarkdownNote();
    mdNote.init();
    // openLastFile();
    initBootstrap();

    blockingSystemBehavior();
}

function blockingSystemBehavior() {
    document.addEventListener('contextmenu', e => {
        e.preventDefault();
    });
}

function initBootstrap() {
    // 初始化Tooltips
    document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(t => {
        new bootstrap.Tooltip(t, {
            delay: {"show": 1000, "hide": 100},
            customClass: 'note-title-tooltips',
            trigger: 'hover',
            html: true
        });

    })
}

function frameworkInit() {
    // 最左侧的控制区域
    controlAreaInit();
    //笔记列表宽度调整
    noteListWidthCtrl();
}

function openMdFile(path) {
    localStorage.lastOpenFilePath = path;
    mdNote.openFile(path);
}

function openLastFile() {

    if (localStorage.lastOpenFilePath != null) {
        //打开上次的文件
        const path = localStorage.lastOpenFilePath;
        //mdNote.openFile(path);
        noteTree.selectPath(path);
    }
}


function controlAreaInit() {
    /*显示与隐藏列表栏*/
    const btnToggleNoteList = document.getElementById("btnToggleNoteList");
    btnToggleNoteList.onclick = function () {
        const noteList = document.getElementById("noteList");
        const divider = document.getElementById("divider");

        if (noteList.style.display == 'none') {
            noteList.style.display = '';
            divider.style.display = '';
            btnToggleNoteList.dataset.select = 'false';
        } else {
            noteList.style.display = 'none';
            divider.style.display = 'none';
            btnToggleNoteList.dataset.select = 'true';
        }
    }

    // 刷新笔记列表
    const btnRefreshNoteList = document.getElementById("btnRefreshNoteList");
    btnRefreshNoteList.onclick = function () {
        alert("TODO 刷新笔记列表")
    }

    /*显示与隐藏底部栏*/
    const btnToggleFootBar = document.getElementById("btnToggleFootBar");
    btnToggleFootBar.onclick = function () {
        let d = '';
        if (btnToggleFootBar.dataset.select == 'true') {
            btnToggleFootBar.dataset.select = 'false';
            d = 'none';
        } else {
            btnToggleFootBar.dataset.select = 'true';
        }
        document.getElementById('listFootBar').style.display = d;
        document.getElementById('editorFootBar').style.display = d;
        document.getElementById('previewFootBar').style.display = d;
    }
}


/**
 * 笔记列表宽度控制
 */
function noteListWidthCtrl() {
    const noteList = document.getElementById("noteList");
    const divider = document.getElementById("divider");

    // 拖拽
    dragControl();
    // 双击还原
    divider.ondblclick = resetNoteListWidth;

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


/**
 * 笔记列表初始化
 */
function noteListInit() {
    //初始化树
    noteTree = new NoteListTree();
    noteTree.init();

}


