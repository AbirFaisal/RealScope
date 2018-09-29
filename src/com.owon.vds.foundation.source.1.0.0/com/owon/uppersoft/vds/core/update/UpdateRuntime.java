package com.owon.uppersoft.vds.core.update;

import static com.owon.uppersoft.vds.util.StringPool.UTF8EncodingString;

import java.io.File;
import java.io.FileInputStream;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.owon.uppersoft.vds.util.FileUtil;
import com.sun.java.swing.plaf.motif.MotifLookAndFeel;

/**
 * UpdateRuntime, which updates the runtime environment and is created at the beginning of the
 * program to clean up the junk files left by the last update and to support the update service at any time.
 * 
 */
public class UpdateRuntime {
	public CheckUpdateFrame chUpFrame;

	public static void main(String[] args) {
		try {
			String lafn = MotifLookAndFeel.class.getName();
			UIManager.setLookAndFeel(lafn);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		// Locale.setDefault(Locale.CHINA);
		IUpdatable iu = new DefaultUpdatable();
		UpdateRuntime ur = new UpdateRuntime(iu);

		ur.checkUpdate();
		// ur.clearUp();
	}

	private IUpdatable iu;

	public UpdateRuntime(IUpdatable iu) {
		this.iu = iu;
	}

	/**
	 * 从xml中获取旧的jar列表，清理删除。程序运行时默认载入新的jar，故而清理过程可以随后再开启一个线程运行。
	 * 
	 * @param path
	 *            version.xml文件所在路径
	 */
	public void clearUp() {
		File clearupXML = new File(iu.getRelativePath());
		if (!clearupXML.exists())
			return;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new FileInputStream(clearupXML));
			is.setEncoding(UTF8EncodingString);
			Document doc = builder.parse(is);
			NodeList files = doc
					.getElementsByTagName(UpdateDetection.String_delFile);
			int l = files.getLength();
			for (int i = 0; i < l; i++) {
				Element file = (Element) files.item(i);
				String s = file.getTextContent().trim();
				FileUtil.deleteFile(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		clearupXML.delete();
	}

	/**
	 * 检查更新
	 */
	public void checkUpdate() {
			chUpFrame = new CheckUpdateFrame(iu);
			Thread checkTd = new Thread(chUpFrame);
			checkTd.start();
	}

	public CheckUpdateFrame getCheckUpdateFrame() {
		return chUpFrame;
	}
}
