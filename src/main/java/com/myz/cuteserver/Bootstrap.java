package com.myz.cuteserver;

import com.myz.cuteserver.mapping.SimpleMapping;
import com.myz.cuteserver.processor.UriMappingProcessor;

/**
 * @author: zhaomingyu
 * @date: 2023/11/8 11:21 AM
 * @description:
 */
public class Bootstrap {

    public static void main(String[] args) {
        int port = 9091;
        if (args != null && args.length > 0) {
            port = Integer.getInteger(args[0], 9091);
        }

        // create and start a CuteServer
        CuteServer cuteServer = new CuteServer.Builder()
                .withPort(port)
                .withHttpRequestProcessor(new UriMappingProcessor().withMapping(new SimpleMapping()))
                .build();
        cuteServer.start();
    }

}