package com.centit.support.json;

public class JSONTransformer {

    /**
     * value | key:value
     * value : 非字符串常量 | string 常量  "@" + 常量 | 引用 ref
     * ref : /rootPath | .currentPath | ..ParentPath | path == currentPath
     * key : @ noKey | # loop | key 常量
     *
     * @param object 模板
     * @param dataSupport 元数据
     * @return 结果数据
     */
    public static Object transformer(Object object,
                                     JSONTransformDataSupport dataSupport){

        return object;
    }

    public static Object transformer(Object object,
                                     Object dataSupport){
        return transformer(object, new DefaultJSONTransformDataSupport(dataSupport));
    }
}
