/**
 * 各种工具方法
 */

/**
 * 判断是否为空
 * @param value
 * @returns {boolean}
 */
function isEmpty(value) {
    if (value == null || value == '') {
        return true;
    }

    if (value.trim() == '') {
        return true;
    }
    return false;
}