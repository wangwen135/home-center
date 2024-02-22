window.onload = function () {
    init();
    dragControl();
    menuInit();
}

function init() {

}

function menuInit() {
    const allLabels = document.querySelectorAll(".tree li > label");

    allLabels.forEach((li) => {
        console.log("给【" + li.textContent + "】添加点击事件")
        li.addEventListener('click', labelClick);
    });

}

function labelClick(event) {
    event.preventDefault();
    console.log('lable 被点击', event.target);
    console.log(event.target.textContent)
    const li = event.target.parentNode;

    if (li.dataset.type == "dir") {
        li.classList.toggle('closed');
    }

    // console.log("点击了：" + folder);
    // folder.classList.toggle('closed');
}

let startX;
let startWidth;

function dragControl() {
    const menu = document.querySelector("#menu");
    const divider = document.querySelector("#divider");
    const content = document.querySelector("#content");

    divider.addEventListener("mousedown", function (e) {
        startX = e.clientX;
        startWidth = menu.clientWidth;
        document.documentElement.style.cursor = "col-resize";
        document.addEventListener("mousemove", mousemove);
        document.addEventListener("mouseup", mouseup);
    });
}

function mousemove(e) {
    const delta = e.clientX - startX;
    const newWidth = startWidth + delta;

    if (newWidth >= 100 && newWidth <= 680) {
        document.documentElement.style.cursor = "col-resize";
        menu.style.width = newWidth + "px";
    } else {
        document.documentElement.style.cursor = "not-allowed";
    }
}

function mouseup() {
    document.documentElement.style.cursor = "initial";
    document.removeEventListener("mousemove", mousemove);
    document.removeEventListener("mouseup", mouseup);
}

