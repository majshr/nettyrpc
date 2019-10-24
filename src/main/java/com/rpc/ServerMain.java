package com.rpc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerMain {
    // 服务端启动
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("classpath:rpc-server-invoke.xml");
	}
}
