let noteTree;
let mdNote;


window.onload = function () {
    frameworkInit();
    noteListInit();
    mdNote = new MarkdownNote();
    mdNote.init();
    // openLastFile();
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
        mdNote.openFile(path);
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


/**
 * 文件列表树初始化
 */
function NoteListTree() {
    const noteListTree = document.getElementById("noteListTree");


    this.init = function () {
        //控制按钮初始化
        ctrlBtnInit();

        // 基本事件处理
        noteListTreeEventHandle();

        //数据初始化
        treeDataInit();
    }

    this.selectPath = function (path) {
        if (path == null || path == '') {
            return;
        }
        let target = noteListTree.querySelector("[data-full-path='" + path + "']")

        if (target == null) {
            console.log("目标不存在", path);
            return;
        }

        // 取消其他的选中
        noteListTree.querySelectorAll("[data-select='true']").forEach(l => {
            l.dataset.select = "false";
        });
        //选中当前的
        target.dataset.select = "true";

        getAncestorsUntil(target, noteListTree).forEach(i => {
            console.log(i);
            // 获取元素下的第一个A标签
            const a = i.querySelector('a')
            console.log(a);
            a.classList.remove("closed");
        });

        //滚动到窗口的可见区域
        scrollElementIntoView(noteListTree, target);
    }

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

            /*统计文件数量*/
            listFootBarRefresh();

            openLastFile();
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
            //全部折叠
            ul.querySelectorAll("[data-type='DIR']").forEach(i => {
                i.classList.add("closed");
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
            openMdFile(path);
        } else if (fileType == 'DIR') {

        } else {
            showToast("暂时不支持直接打开【" + fileType + "】类型的文件");
        }

    }

    /**
     * 统计文件数量
     */
    function listFootBarRefresh() {

        const listStatisticFolder = document.getElementById("listStatisticFolder");
        const listStatisticFiles = document.getElementById("listStatisticFiles");
        const lestStatisticMd = document.getElementById("lestStatisticMd");

        listStatisticFolder.textContent = noteListTree.querySelectorAll('[data-type="DIR"]').length + "";
        listStatisticFiles.textContent = noteListTree.querySelectorAll('[data-type]:not([data-type="DIR"])').length + "";
        lestStatisticMd.textContent = noteListTree.querySelectorAll('[data-type="md"]').length + "";
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


// ###############################################################
// ##################            工具方法         ##################
// ###############################################################
/**
 * 获取祖先元素，直到为某个元素为止
 * @param element
 * @param untilElement
 * @returns {[]|*[]}
 */
function getAncestorsUntil(element, untilElement) {
    let currentElement = element.parentElement;
    const ancestors = [];
    while (currentElement !== untilElement && currentElement !== document) {
        ancestors.push(currentElement);
        currentElement = currentElement.parentElement; // 或者使用 currentElement.parentNode
    }
    // 如果直到文档根元素都没有找到 untilElement，则返回空数组
    if (currentElement !== untilElement) {
        return [];
    }
    // 否则，返回包含所有祖先元素的数组
    return ancestors;
}

/**
 * 滚动元素到窗口的可见区域
 * @param container position属性为relative, absolute, fixed或sticky
 * @param element
 */
function scrollElementIntoView(container, element) {
    // 获取选中元素到容器顶部的距离
    const elementTop = element.offsetTop;
    // 获取选中元素的尺寸
    const elementHeight = element.offsetHeight;
    // 获取容器的尺寸
    const containerHeight = container.offsetHeight;
    // 获取容器的当前滚动位置
    const containerScrollTop = container.scrollTop;
    // 计算元素底部到容器顶部的距离
    const elementBottom = elementTop + elementHeight;


    // 检查元素是否在容器的可见范围内
    if (elementTop < containerScrollTop) {
        // 元素在容器上方
        container.scrollTop = elementTop;
    } else if (elementBottom > containerScrollTop + containerHeight) {
        // 元素在容器下方
        container.scrollTop = elementBottom - containerHeight;
    }
    // 如果元素已经在可见范围内，则不需要滚动
}