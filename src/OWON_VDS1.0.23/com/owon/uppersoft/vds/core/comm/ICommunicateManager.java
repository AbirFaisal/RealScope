package com.owon.uppersoft.vds.core.comm;

/**
 * ICommunicateManager，将比较底层的通用数据传输接口分离为单独的类
 * 
 */
public interface ICommunicateManager {
	/**
	 * 对远程端在出现传输出错后，可以用来尝试恢复并重新连接
	 * 
	 * 使用基于通信类的该方法可以避免涉及通信方式的细节信息如端口ip等， 也不需要在重新连接中设置这些，方便使用
	 */
	boolean tryRescue();

	int retryTimes();

	/**
	 * 在具体的实现中，根据实现的连接状态判断；
	 * 
	 * 在统一的SourceManager/BufferredSourceManager中，使用特定变量判断
	 * 
	 * @return
	 */
	boolean isConnected();

	/**
	 * 写指令
	 */
	int write(byte[] arr, int len);

	/**
	 * 接收单独指令
	 */
	int acceptResponse(byte[] arr, int len);

}