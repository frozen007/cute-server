package com.myz.cuteserver.processor;

import com.myz.cuteserver.annotation.UriMapping;
import com.myz.cuteserver.annotation.UriVariable;
import com.myz.cuteserver.mapping.RootMapping;
import com.myz.cuteserver.util.StrUtil;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: zhaomingyu
 * @description:
 */
public class UriMappingProcessor implements HttpRequestProcessor {

    private static Logger logger = LoggerFactory.getLogger(UriMappingProcessor.class);

    private List mappings = new ArrayList<>();

    private Map<String, MethodHandle> methodHandleMap = new HashMap<>();

    public UriMappingProcessor() {

    }

    public UriMappingProcessor withMapping(Object mappingInstance) {
        mappings.add(mappingInstance);

        return this;
    }

    @Override
    public void config() {
        for (Object mapping : mappings) {
            findMappingMethods(mapping);
        }
        if (!methodHandleMap.containsKey("/")) {
            findMappingMethods(new RootMapping());
        }
    }

    @Override
    public Object processHttpRequest(HttpRequest request) throws Exception {
        String uri = request.uri();
        if (StrUtil.isEmpty(uri)) {
            logger.info("bad request: uri is empty");
            return null;
        }
        logger.info("incoming request: uri={}", uri);

        Object result = executeWithUri(uri);

        return result;
    }

    public Object executeWithUri(String uri) throws Exception {
        int tag = uri.indexOf("?");
        String path;
        if (tag < 0) {
            path = uri;
        } else {
            path = uri.substring(0, tag);
        }

        MethodHandle methodHandle = methodHandleMap.get(path);
        if (methodHandle == null) {
            throw new NoSuchMethodException("can not find path");
        }

        String[] params = methodHandle.getParams();
        Object[] args = new Object[0];
        if (params != null) {
            args = new Object[params.length];
            if (uri.length() > tag + 1) {
                String varStr = uri.substring(tag + 1);
                Map<String, Object> paraMap = StrUtil.parseQueryString(varStr, "utf-8");
                for (int i = 0; i < params.length; i++) {
                    String[] values = (String[]) paraMap.get(params[i]);
                    if (values == null) {
                        args[i] = null;
                    } else {
                        args[i] = values[0];
                    }
                }
            }
        }

        Object result = methodHandle.doInvoke(args);
        return result;
    }

    private void findMappingMethods(Object mappingObj) {
        Method[] methods = mappingObj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            UriMapping uriMapping = method.getAnnotation(UriMapping.class);
            if (uriMapping == null) {
                continue;
            }

            Parameter[] parameters = method.getParameters();
            String[] params = null;
            if (parameters != null) {
                params = new String[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    UriVariable uriVariable = parameter.getAnnotation(UriVariable.class);
                    String paramName;
                    if (uriVariable == null || StrUtil.isEmpty(uriVariable.value())) {
                        paramName = parameter.getName();
                    } else {
                        paramName = uriVariable.value();
                    }

                    params[i] = paramName;
                }
            }

            String path = uriMapping.value();
            if (!StrUtil.isEmpty(path)) {
                methodHandleMap.put(path, new MethodHandle(method, mappingObj, params));
            }
        }
    }
}
