package com.owon.uppersoft.vds.core.update;

import java.awt.Window;
import java.util.List;

import com.owon.uppersoft.vds.core.aspect.base.ResourceBundleProvider;

/**
 * 可更新的对象
 * 
 */
public interface IUpdatable extends ResourceBundleProvider {

	/**
	 * @return 对象的主窗口
	 */
//	Shell getMainShell();
//	void getMainFrame();//Platform.getMainWindow().getFrame()
	Window getWindow();
	/**
	 * 通知程序释放正在使用的资源
	 */
	void notifyDestroy();

	/**
	 * 关闭
	 */
	void close();

	/**
	 * 重启
	 */
	void startAgain();

	/**
	 * @return 所有可用的更新服务网址。更新服务网址，需是与RelativePath匹配的上一级网络路径
	 */
	List<String> getUpdatableServers();

	/**
	 * @return 相对路径，因基本的URL可能是相同的，每个可运行程序要有自己对应且不与其它程序冲突的相对路径
	 */
	String getRelativePath();

	/**
	 * @return 配置文件夹，用于存放升级信息及下载的临时区域，应使用其子目录
	 */
	String getConfigurationDir();

	/**
	 * @return 产品外部版本号
	 */
	String getProductVersion();
	
}
