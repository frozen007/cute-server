package com.myz.cuteserver.mapping;

import com.myz.cuteserver.annotation.UriMapping;

/**
 * @author: zhaomingyumt
 * @date: 2023/11/24 8:20 PM
 * @description:
 */
public class RootMapping {

    @UriMapping("/")
    public String root() {
        return "cute http server";
    }
}
