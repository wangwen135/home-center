let mdNote;

window.onload = function () {
    mdNote = new MarkdownNote();
    mdNote.init();
    initBootstrap();
    blockingSystemBehavior();
    footBarCtrl();
    //根据参数打开文件
    openMdFile();
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

    mdNote.openFile(path, data => {
        document.title = data.name;
    });

}

function footBarCtrl() {
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
        document.getElementById('editorFootBar').style.display = d;
        document.getElementById('previewFootBar').style.display = d;
    }
}

