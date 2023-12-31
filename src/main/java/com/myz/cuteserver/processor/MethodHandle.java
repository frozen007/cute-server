package com.myz.cuteserver.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: zhaomingyu
 * @description:
 */
public class MethodHandle {

    private Method method;

    private Object object;

    private String[] params;

    public MethodHandle(Method method, Object object, String[] params) {
        this.method = method;
        this.object = object;
        this.params = params;
    }

    public Object doInvoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(object, args);
    }

    public String[] getParams() {
        return params;
    }
}
