package com.rpc.nettyrpc.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rpc.exception.RejectResponeException;
import com.rpc.model.MessageResponse;

/**
 * 类似于Future，根据请求，设置响应；获取时，如果还没收到响应，等待；设置好后，唤醒
 * 
 * @author maj
 *
 */
public class MessageCallback {
	static final Logger LOG = LoggerFactory.getLogger(MessageCallback.class);
	
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
    
    /**
     * 回填响应；回填后signal一下get阻塞的线程
     * 
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
        if (isFinish) {
            return getResponseData();
        }

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
            if (isInvokeResultSuccess()) {
                return this.response.getResult();
            } else {
                throw new RejectResponeException(response.getError());
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
