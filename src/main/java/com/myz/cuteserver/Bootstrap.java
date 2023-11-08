package com.myz.cuteserver;

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
        CuteServer cuteServer = new CuteServer(port);
        cuteServer.start();
    }

}