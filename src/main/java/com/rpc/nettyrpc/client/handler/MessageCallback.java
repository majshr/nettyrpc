package com.rpc.nettyrpc.client.handler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpc.config.RpcSystemConfig;
import com.rpc.exception.InvokeErrorException;
import com.rpc.exception.RejectResponeException;
import com.rpc.model.MessageRequest;
import com.rpc.model.MessageResponse;

/**
 * 消息回调方法；传入请求，等待请求完成后，会回填响应
 * @author maj
 *
 */
public class MessageCallback {
	static final Logger LOG = LoggerFactory.getLogger(MessageCallback.class);
	
	/**
	 * 请求
	 */
    private MessageRequest request;
    /**
     * 响应
     */
    private MessageResponse response;
    volatile boolean isFinish = false;
    
    /**
     * 锁
     */
    private Lock lock = new ReentrantLock();
    /**
     *  请求完成通知信号
     */
    private Condition finishCondition = lock.newCondition();
    
    public MessageCallback(MessageRequest request) {
        this.request = request;
    }
    
    /**
     * 回填响应
     * @param response
     */
    public void backfillResponse(MessageResponse response){
    	// 在没有响应前，尝试获取该值，会阻塞住（wait），设置好后再唤醒
    	try {
    		lock.lock();
    		this.response = response;
    		isFinish = true;
    		finishCondition.signalAll();
    	}finally {
			lock.unlock();
		}
    }
    
    /**
     * 获取响应信息，如果还没有返回，会阻塞等待
     */
    public Object getResponseDataSync() {
//    	if(isFinish) {
//    		System.out.println("响应还未返回");
//    		return getResponseData();
//    	}
    	
    	try {
    		lock.lock();
    		// 等待响应完成
    		while(!isFinish) {
    			try {
    				// 等待超时后
					if(!finishCondition.await(10000, TimeUnit.SECONDS)) {
						LOG.error("等到结果超时！");
						break;
					}
				} catch (InterruptedException e) {
					LOG.error("等待响应结果被中断!", e);
				}
    		}
    		
    		// 响应结果
			return getResponseData();
    	}finally {
			lock.unlock();
		}
    }
    
    /**
     * 根据response信息返回结果
     * @return
     */
    private Object getResponseData() {
    	if (this.response != null) {
            boolean isInvokeSucc = isInvokeResultSuccess();
            if (isInvokeSucc) {
                return this.response.getResult();
            } else {
                throw new RejectResponeException(RpcSystemConfig.FILTER_RESPONSE_MSG);
            }
        } else {
            return null;
        }
    }
    
    /**
     * 是否正确响应
     * @return
     */
    private boolean isInvokeResultSuccess() {
        return (this.response.getError() == null);
    }
}
