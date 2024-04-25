/**
 * 文件列表树
 */
function NoteListTree() {
    //当前选中的
    let currentSelectElement = null;
    let currentSelectPath = "/";
    let currentSelectFile = null;

    //右键菜单
    let fileContextMenu = null;
    let dirContextMenu = null;
    let otherContextMenu = null;
    // 右键菜单选中的元素
    let rightClickElement = null;

    const noteListTree = document.getElementById("noteListTree");

    this.getCurrentSelectElement = function () {
        return currentSelectElement;
    }
    this.getCurrentSelectPath = function () {
        return currentSelectPath;
    }
    this.getCurrentSelectFile = function () {
        return currentSelectFile;
    }

    this.init = function () {
        //控制按钮初始化
        ctrlBtnInit();

        // 基本事件处理
        noteListTreeEventHandle();

        //数据初始化
        treeDataInit();

        //右键菜单
        initContextMenu();
    }

    function initContextMenu() {
        const fileMenuItems = [
            {
                text: '复制文件名', onClick: function () {
                    if (rightClickElement == null) {
                        return;
                    }
                    navigator.clipboard.writeText(rightClickElement.textContent).catch(function (err) {
                        console.error('复制失败：', err);
                    });
                }
            },
            {
                text: '复制文件路径', onClick: function () {
                    if (rightClickElement == null) {
                        return;
                    }
                    navigator.clipboard.writeText(rightClickElement.dataset.fullPath).catch(function (err) {
                        console.error('复制失败：', err);
                    });
                }
            },
            {
                type: 'separator'
            },
            {
                text: '在新窗口打开', onClick: function () {
                    window.open("edit.html#" + rightClickElement.dataset.fullPath, "_blank");
                }
            },
            {
                text: '在新窗口查看', onClick: function () {
                    window.open("view.html#" + rightClickElement.dataset.fullPath, "_blank");
                }
            },
            {
                type: 'separator'
            },
            {
                text: '删除', onClick: function () {
                    const filePath = rightClickElement.dataset.fullPath;
                    showConfirm("确认删除？", "确认删除：" + filePath, MsgTypes.QUESTION, function () {
                        delFile(filePath, function () {
                            removeTreeNode(filePath);
                        });
                    }, null, null, {keyboard: true});
                }
            }
        ];

        fileContextMenu = new ContextMenu(fileMenuItems);
        fileContextMenu.init();

        const dirMenuItems = [
            {
                text: '新建文件', onClick: function () {

                    addNewFile();
                }
            },
            {
                text: '新建目录', onClick: function () {

                    addNewFolder();
                }
            },
            {
                type: 'separator'
            },
            {
                text: '复制目录', onClick: function (event) {
                    console.log(event);
                    debugger;
                    navigator.clipboard.writeText("文件名").catch(function (err) {
                        console.error('复制失败：', err);
                    });
                }
            },
            {
                text: '移动目录', onClick: function () {
                    alert('你点击了菜单项 2');
                }
            },
            {
                type: 'separator'
            },
            {
                text: '删除目录', onClick: function () {
                    alert('提示确认删除？还是直接移动到回收站');
                }
            }
        ];

        dirContextMenu = new ContextMenu(dirMenuItems);
        dirContextMenu.init();


    }

    this.selectPath = function (path) {
        if (path == null || path == '') {
            return;
        }
        let target = noteListTree.querySelector("[data-full-path='" + path + "']")

        if (target == null) {
            return;
        }

        /*// 取消其他的选中
        noteListTree.querySelectorAll("[data-select='true']").forEach(l => {
            l.dataset.select = "false";
        });
        //选中当前的
        target.dataset.select = "true";*/

        //展开全部上级
        getAncestorsUntil(target, noteListTree).forEach(i => {
            if (i.tagName === 'LI') {
                // 获取元素下的第一个A标签
                const a = i.querySelector('a')
                a.classList.remove("closed");
            }
        });

        //滚动到窗口的可见区域
        scrollElementIntoView(noteListTree, target);

        //点击
        target.click();
    }

    function addNewFile(path) {
        if (path === null || path === '') {
            path = "/";
        }
        let formData = new FormData();
        formData.append('path', path);

        postRequest('createFile', formData, pathVo => {
            console.log("创建一个文件：" + pathVo);
            console.log("需要刷新菜单树，定位，重命名");
        });
    }

    function delFile(path, callback) {
        let formData = new FormData();
        formData.append('filePath', path);
        console.debug("删除文件：" + path);
        postRequest('delete', formData, callback);
    }

    function addNewFolder(path) {
        if (path === null || path === '') {
            path = "/";
        }
        let formData = new FormData();
        formData.append('path', path);

        postRequest('createDir', formData, pathVo => {
            console.log("需要创建一个目录：" + pathVo);
            console.log("需要刷新菜单树，定位，重命名");
        });
    }

    function ctrlBtnInit() {
        const btnNewFile = document.getElementById("tltb-newFile");
        btnNewFile.onclick = function () {
            addNewFile(noteTree.getCurrentSelectPath());
        }
        const btnNewFolder = document.getElementById("tltb-newFolder");
        btnNewFolder.onclick = function () {
            addNewFolder(noteTree.getCurrentSelectPath());
        }

        const btnExpand = document.getElementById("tltb-expand");

        btnExpand.onclick = function () {
            const tooltip = bootstrap.Tooltip.getInstance('#tltb-expand')
            tooltip.hide();

            if (btnExpand.dataset.status == 'expand') {
                collapseAll()
                btnExpand.dataset.status = 'collapse';
                btnExpand.dataset.displayChild = '1';

                btnExpand.dataset.bsTitle = '全部展开';
                tooltip._config.title = '全部展开'

            } else {
                expandAll()
                btnExpand.dataset.status = 'expand';
                btnExpand.dataset.displayChild = '2';

                btnExpand.dataset.bsTitle = '全部折叠';
                tooltip._config.title = '全部折叠'
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

    this.refreshList = treeDataInit;

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

    function removeTreeNode(path) {
        if (path == null || path == '') {
            return;
        }
        let target = noteListTree.querySelector("[data-full-path='" + path + "']")

        if (target == null) {
            return;
        }
        const liElement = target.parentElement;
        liElement.parentElement.removeChild(liElement);
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
        //noteListTree.removeEventListener("contextmenu", labelRightClick);
        noteListTree.addEventListener("contextmenu", labelRightClick);
    }

    /**
     * 鼠标右键
     * @param event
     */
    function labelRightClick(event) {
        // 阻止默认的右键菜单行为
        event.preventDefault();

        rightClickElement = event.target;

        if (rightClickElement === noteListTree) {
            console.log("空白区域的右键菜单")
            rightClickElement = null;
            return;
        } else if (rightClickElement.dataset.type === 'DIR') {
            dirContextMenu.showMenu(event);
        } else {
            fileContextMenu.showMenu(event);
        }
    }

    /**
     * 列表被点击
     * @param event
     */
    function labelClick(event) {
        event.preventDefault();

        const lb = event.target;
        currentSelectElement = lb;
        if (lb.dataset.type == "DIR") {
            /*lb.classList.toggle('closed');*/
            if (lb.classList.contains("closed")) {
                lb.classList.remove('closed');
            } else if (lb.dataset.select == "true") {
                lb.classList.add("closed");
            }
            currentSelectPath = lb.dataset.fullPath;
            currentSelectFile = null;
        } else {
            currentSelectFile = lb.dataset.fullPath;

            const lastIndexOf = currentSelectFile.lastIndexOf("/");
            if (lastIndexOf === 0) {
                currentSelectPath = "/";
            } else {
                currentSelectPath = currentSelectFile.substring(0, lastIndexOf);
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
