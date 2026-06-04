package com.wwh.home.center.common.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathMatchUtilsTest {

    @Test
    void returnsFalseForEmptyPatterns() {
        assertFalse(PathMatchUtils.matchList("/user/info", null));
        assertFalse(PathMatchUtils.matchList("/user/info", Collections.emptyList()));
        assertFalse(PathMatchUtils.matchList("/user/info", Arrays.asList("", "  ")));
    }

    @Test
    void matchesExactAndAntPatterns() {
        assertTrue(PathMatchUtils.matchList("/user/info", Arrays.asList("/login", "/user/info")));
        assertTrue(PathMatchUtils.matchList("/backend/user/list", Arrays.asList("/backend/**")));
        assertTrue(PathMatchUtils.matchList("/smartScreen/a", Arrays.asList("/smartScreen/?")));
    }

    @Test
    void doesNotMatchDifferentPath() {
        assertFalse(PathMatchUtils.matchList("/backend/user/list", Arrays.asList("/backend/user")));
        assertFalse(PathMatchUtils.matchList("/smartScreen/abc", Arrays.asList("/smartScreen/?")));
    }
}
