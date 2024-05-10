/**
 * 文件列表树
 */
function NoteListTree() {
    const noteListTree = document.getElementById("noteListTree");

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
                    navigator.clipboard.writeText(rightClickElement.textContent).catch(function (err) {
                        console.error('复制失败：', err);
                    });
                }
            },
            {
                text: '复制文件路径', onClick: function () {
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
                text: '重命名文件', onClick: function () {
                    renameTreeNode(rightClickElement);
                }
            },
            {
                text: '移动到目录', onClick: function () {
                    alert('待实现');
                }
            },
            {
                type: 'separator'
            },
            {
                text: '删除', onClick: function () {
                    const filePath = rightClickElement.dataset.fullPath;
                    showConfirm("确认删除？", "确认删除：" + filePath, MsgTypes.QUESTION,
                        () => delFile(filePath), null, null, {keyboard: true});
                }
            },
            {
                text: '下载文件', onClick: function () {
                    const fileName = rightClickElement.dataset.fullPath; // 需要下载的文件名
                    let downloadUrl = 'download?filePath=' + encodeURIComponent(fileName);
                    // 创建一个a标签
                    const a = document.createElement('a');
                    a.href = downloadUrl;
                    a.style.display = 'none';
                    document.body.appendChild(a);
                    // 模拟点击下载
                    a.click();
                    // 移除a标签
                    document.body.removeChild(a);
                }
            }
        ];

        fileContextMenu = new ContextMenu(fileMenuItems);
        fileContextMenu.init();

        const dirMenuItems = [
            {
                text: '新建文件', onClick: function () {
                    addNewFile(rightClickElement.dataset.fullPath);
                }
            },
            {
                text: '新建目录', onClick: function () {
                    addNewFolder(rightClickElement.dataset.fullPath);
                }
            },
            {
                type: 'separator'
            },
            {
                text: '复制目录', onClick: function (event) {
                    navigator.clipboard.writeText(rightClickElement.dataset.fullPath).catch(function (err) {
                        console.error('复制失败：', err);
                    });
                }
            },
            {
                text: '重命名目录', onClick: function () {
                    alert('待实现');
                }
            },
            {
                text: '移动目录', onClick: function () {
                    alert('待实现');
                }
            },
            {
                type: 'separator'
            },
            {
                text: '删除目录', onClick: function () {
                    const dirPath = rightClickElement.dataset.fullPath;

                    if (hasChildNodes(dirPath)) {
                        showModalMessage("无法删除！", "请先删除目录：" + dirPath + " 下的其他文件和目录");
                        return;
                    }

                    showConfirm("确认删除？", "确认删除目录：" + dirPath, MsgTypes.QUESTION,
                        () => delDir(dirPath), null, null, {keyboard: true});
                }
            }
        ];

        dirContextMenu = new ContextMenu(dirMenuItems);
        dirContextMenu.init();
    }

    /**
     * 选中并打开指定路径
     * @type {selectPathClick}
     */
    this.selectPath = selectPathClick;

    /**
     * 选中并点击路径
     * @param path
     */
    function selectPathClick(path) {
        const target = expandAndSelected(path);
        if (target != null) {
            //点击
            target.click();
        }
    }

    /**
     * 展开指定路径
     * @type {function(*=): (null|Element | null)}
     */
    this.expandPath = expandAndSelected;

    /**
     * 展开并选中
     * @param path
     */
    function expandAndSelected(path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        let target = noteListTree.querySelector("[data-full-path='" + path + "']")

        if (target == null) {
            return null;
        }

        // 取消其他的选中
        noteListTree.querySelectorAll("[data-select='true']").forEach(l => {
            l.dataset.select = "false";
        });
        //选中当前的
        target.dataset.select = "true";

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
        return target;
    }

    function addNewFile(path) {
        if (path === null || path === '') {
            path = "/";
        }
        let formData = new FormData();
        formData.append('path', path);

        postRequest('createFile', formData, pathVo => {
            addNode(pathVo)
            selectPathClick(pathVo.fullPath);
        });
    }

    function delFile(path) {
        let formData = new FormData();
        formData.append('filePath', path);
        console.debug("删除文件：" + path);
        postRequest('delFile', formData, r => {
            if (r) {
                removeTreeNode(path);
            } else {
                showModalMessage("系统错误", "删除文件失败，请刷新页面", MsgTypes.WARNING);
            }
        });
    }

    function delDir(path) {
        let formData = new FormData();
        formData.append('dirPath', path);
        console.debug("删除目录：" + path);
        postRequest('delDir', formData, r => {
            if (r) {
                removeTreeNode(path);
            } else {
                showModalMessage("系统错误", "删除目录失败，请刷新页面", MsgTypes.WARNING);
            }
        });
    }

    function addNewFolder(path) {
        if (path === null || path === '') {
            path = "/";
        }
        let formData = new FormData();
        formData.append('path', path);

        postRequest('createDir', formData, pathVo => {
            addNode(pathVo)
            selectPathClick(pathVo.fullPath);
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

    /**
     * 刷新列表，全部重新加载
     * @type {treeDataInit}
     */
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
                const li = createLiElement(item);
                ul.appendChild(li);
                if (item.children && item.children.length > 0) {
                    recursive(item.children, li);
                }
            });
            //全部折叠
            ul.querySelectorAll("[data-type='DIR']").forEach(i => {
                i.classList.add("closed");
            });
            parent.appendChild(ul);
        }
    }

    function createLiElement(item) {
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
        return li;
    }


    //更新节点，比如重命名之后

    /**
     * 在编辑区域修改文件名之后 需要同步修改列表中的文件名
     * @param parentPath
     * @param oldName
     * @param newName
     */
    this.renameFile = function (parentPath, oldName, newName) {

        let filePath = parentPath + oldName;
        const aElement = noteListTree.querySelector("[data-full-path='" + filePath + "']")
        if (aElement == null) {
            console.error("路径不存在：" + path);
            return;
        }
        aElement.textContent = newName;
        aElement.dataset.fullPath = parentPath + newName;
    }

    /**
     * 获取目录UL元素
     * @param path
     * @returns {ChildNode|HTMLUListElement|null|Element}
     */
    function getDirUlElement(path) {
        if (path == null || path == '' || path == '/') {
            return noteListTree.firstChild;
        }
        const aElement = noteListTree.querySelector("[data-full-path='" + path + "']")
        if (aElement == null) {
            console.log("目录不存在：" + path);
            return null;
        }
        if (aElement.dataset.type != "DIR") {
            console.log(path + " 不是目录")
            return null;
        }
        const parentLi = aElement.parentElement;
        const ulElement = aElement.nextElementSibling;
        if (ulElement == null || ulElement.tagName != "UL") {
            const ul = document.createElement('ul');
            parentLi.appendChild(ul)
            return ul;
        }
        return ulElement;
    }

    /**
     * 新增节点
     * @param notePathVo
     */
    function addNode(notePathVo) {
        if (notePathVo == null) {
            return;
        }
        const parentUl = getDirUlElement(notePathVo.parentPath);
        const li = createLiElement(notePathVo);
        parentUl.appendChild(li);

        listFootBarRefresh();
    }

    /**
     * 判断路径下是否还有子节点
     * @param path
     * @returns {boolean}
     */
    function hasChildNodes(path) {
        const nodeList = getChildNodes(path);
        if (nodeList == null) {
            return false;
        }
        return nodeList.length > 0;
    }

    /**
     * 获取子节点
     * @param path
     * @returns {null|NodeListOf<ChildNode>}
     */
    function getChildNodes(path) {
        if (path == null || path == '') {
            return null;
        }
        const aElement = noteListTree.querySelector("[data-full-path='" + path + "']")
        if (aElement == null) {
            return null;
        }
        const ulElement = aElement.nextElementSibling;
        if (ulElement == null) {
            return null;
        }
        if (ulElement.tagName != 'UL') {
            return null;
        }
        return ulElement.childNodes;
    }

    /**
     * 移除树节点
     * @param path
     */
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

        listFootBarRefresh();
    }


    /**
     * 内部的重命名动作
     * @param targetNode
     */
    function renameTreeNode(targetNode) {
        const oldName = targetNode.textContent;
        const parentLi = targetNode.parentElement;
        const textInput = document.createElement("INPUT");
        textInput.type = 'text';
        textInput.value = oldName;
        targetNode.style.display = 'none';
        parentLi.insertBefore(textInput, targetNode);
        textInput.focus();
        textInput.onblur = function () {
            const newName = textInput.value;
            if (newName === oldName) {
                resetNode();
                return;
            }
            showConfirm("确定将文件名修改为：", newName, MsgTypes.QUESTION, confirm, resetNode, resetNode, {keyboard: true});
        }


        function confirm() {
            const oldName = targetNode.textContent;
            const newName = textInput.value;
            const fullPath = targetNode.dataset.fullPath;
            //修改文件名
            let formData = new FormData();
            formData.append('filePath', fullPath);
            formData.append('newName', newName);

            postRequest('/note/rename', formData).then(pathVo => {
                targetNode.textContent = newName;
                targetNode.dataset.fullPath = fullPath.replace(oldName, newName);
                showToastSimple("文件名修改成功！", MsgTypes.INFO, Position.TopCenter);
            }).catch(error => {
                apiErrorHandle(error);
            });
            resetNode();
        }

        function resetNode() {
            targetNode.style.display = '';
            parentLi.removeChild(textInput);
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
        //noteListTree.removeEventListener("contextmenu", labelRightClick);
        noteListTree.addEventListener("contextmenu", treeRightClick);
    }

    /**
     * 鼠标右键
     * @param event
     */
    function treeRightClick(event) {
        // 阻止默认的右键菜单行为
        event.preventDefault();

        const tmp = event.target;

        if (tmp === noteListTree) {
            console.log("空白区域的右键菜单待定义");
            return;
        }
        if (tmp.tagName !== 'A') {
            console.log("点击了其他位置");
            return;
        }

        rightClickElement = event.target;

        labelClick(event, false);

        if (rightClickElement.dataset.type === 'DIR') {
            dirContextMenu.showMenu(event);
        } else {
            fileContextMenu.showMenu(event);
        }
    }

    /**
     * 列表被点击
     * @param event
     */
    function labelClick(event, toggleDirState = true) {
        event.preventDefault();

        const lbA = event.target;
        currentSelectElement = lbA;
        if (lbA.dataset.type == "DIR") {

            if (toggleDirState) {
                if (lbA.classList.contains("closed")) {
                    lbA.classList.remove('closed');
                } else if (lbA.dataset.select == "true") {
                    lbA.classList.add("closed");
                }
            }
            currentSelectPath = lbA.dataset.fullPath;
            currentSelectFile = null;
        } else {
            currentSelectFile = lbA.dataset.fullPath;

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
        lbA.dataset.select = "true";

        //加载文件

        const fileType = lbA.dataset.type;
        if (fileType == "md") {
            const path = lbA.dataset.fullPath;
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
