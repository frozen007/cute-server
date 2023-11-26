package com.myz.cuteserver.mapping;

import com.myz.cuteserver.annotation.UriMapping;

/**
 * @author: zhaomingyu
 * @description:
 */
public class RootMapping {

    @UriMapping("/")
    public String root() {
        return "cute http server";
    }
}
