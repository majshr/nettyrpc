package com.rpc.model;

import java.util.Map;

/**
 * 
 * @author mengaijun
 * @Description: MessageKeyVal功能模块；实现绑定关系
 * @date: 2019年10月23日 下午5:30:36
 */
public class MessageKeyVal {

    /**
     * 会在启动的时候，根据配置，接口类名，对象配置到map中
     */
    private Map<String, Object> messageKeyVal;

    public void setMessageKeyVal(Map<String, Object> messageKeyVal) {
        this.messageKeyVal = messageKeyVal;
    }

    public Map<String, Object> getMessageKeyVal() {
        return messageKeyVal;
    }
}


