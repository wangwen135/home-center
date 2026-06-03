let mdView;


window.onload = function () {

    mdView = new MarkdownViewer({
        disableFootBar: true
    });
    mdView.init();
    initBootstrap();
    blockingSystemBehavior();

    toggleToolbar();

    //根据参数打开文件
    openMdFile();
}

function toggleToolbar() {
    const btnToggleToolbar = document.getElementById("btnToggleToolbar");
    const previewToolbar = document.getElementById("previewToolbar");

    btnToggleToolbar.onclick = function () {
        if (previewToolbar.style.display == 'none') {
            /*展示工具栏*/
            localStorage.viewStyleHiddenToolbar = 'false';
            previewToolbar.style.display = '';
            btnToggleToolbar.children[0].classList.remove("rotate-180");
        } else {
            localStorage.viewStyleHiddenToolbar = 'true';
            previewToolbar.style.display = 'none';
            btnToggleToolbar.children[0].classList.add("rotate-180");
        }
    }
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

function openMdFile() {
    let path = window.location.hash;
    if (path) {
        // 去掉#号
        path = path.slice(1);
    } else {
        showModalMessage("文件为空", "要打开的文件不能为空", MsgTypes.DANGER);
        return;
    }
    console.log("打开文件：" + path)

    getRequest("/note/getNote?path=" + path, data => {
        document.title = data.name;
        mdView.setFileName(data.name);
        document.getElementById("noteTitle").textContent = data.name;

        document.getElementById("filePath").textContent = data.parentPath;
        mdView.setParentPath(data.parentPath);

        document.getElementById("createTime").textContent = data.createTime;
        document.getElementById("updateTime").textContent = data.updateTime;
        mdView.renderMd(data.content);
    });

}




