package com.owon.uppersoft.vds.core.update;

import static com.owon.uppersoft.vds.util.StringPool.DotString;
import static com.owon.uppersoft.vds.util.StringPool.EmptyString;
import static com.owon.uppersoft.vds.util.StringPool.Slash;
import static com.owon.uppersoft.vds.util.StringPool.UTF8EncodingString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.owon.uppersoft.vds.core.update.download.DownloadFile;
import com.owon.uppersoft.vds.util.FileUtil;
import com.owon.uppersoft.vds.util.ZipUtil;

public class UpdateDetection {
	private IUpdatable iu;
	private File localUpdateXML;
	private byte[] downloadBuffer = new byte[IUpdateReference.BUFFER_SIZE];
	private File downloadTempDir;

	public UpdateDetection(IUpdatable iu) {
		this.iu = iu;
		downloadTempDir = new File(iu.getConfigurationDir(),
				IUpdateReference.TempDir);
		FileUtil.deleteFile(downloadTempDir);
		downloadTempDir.mkdirs();// Creates the directory named by this
		// abstract pathname
		localUpdateXML = new File(downloadTempDir, iu.getRelativePath());
		FileUtil.checkPath(localUpdateXML);

	}

	public File getLocalUpdateXML() {
		return localUpdateXML;
	}

	public IUpdatable getUpdatable() {
		return iu;
	}

	public File getDownloadTempDir() {
		return downloadTempDir;
	}

	public void filesUpdate(List<DownloadFile> files) {
		for (DownloadFile df : files) {
			File lf = df.getLocalTempFile();
			String destFile = df.getDestFile();

			if (df.isZip()) {
				/* 使用zip压缩包名作为解压后的文件夹名，直接解压zip包根目录下的内容到dir中即可 */
				File f = new File(destFile);
				if (destFile.length() == 0) {
					try {
						// Creates a new instance of a File object representing
						// the file
						// located at the absolute path of the current File
						// object.
						f = f.getCanonicalFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					/* 删除可能存在的同名文件夹 */
					FileUtil.deleteFile(f);
				}

				ZipUtil.unzip(lf, f);

				FileUtil.deleteFile(lf);
			} else {
				FileUtil.replaceFile(lf, new File(destFile));
			}
		}

		File updateXml = new File(iu.getRelativePath());
		FileUtil.replaceFile(localUpdateXML, updateXml);
		/* 不去删除downloadTempDir */
	}

	public String detectServers() {
		List<String> servers = iu.getUpdatableServers();
		File file;
		for (String url : servers) {
			file = getFile(url, localUpdateXML);
			if (file != null) {
				return url;
			}
			int delay = IUpdateReference.TaskDelayTime; // 大于零的值,测试时自己设,用于延时查看细节。
			if (delay > 0)
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	/**
	 * 获取服务器上的最新的版本配置文件(destUrl+fileName)，直接用fileName作为保存在本地的文件路径
	 * 
	 * @param destUrl
	 * @return
	 */
	public File getFile(String destUrl, File file) {
		try {
			FileOutputStream fos = null;
			BufferedInputStream bis = null;
			int size = 0;
			URL url = new URL(new URL(destUrl), iu.getRelativePath());
			HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
			hurl.setConnectTimeout(IUpdateReference.HttpURLConnectionTimeout);
			hurl.setReadTimeout(IUpdateReference.HttpURLConnectionTimeout);
			hurl.connect();
			bis = new BufferedInputStream(hurl.getInputStream());
			fos = new FileOutputStream(file);
			while ((size = bis.read(downloadBuffer)) != -1) {
				fos.write(downloadBuffer, 0, size);
			}
			fos.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return file;
	}

	public static final String String_bundle = "bundle";
	public static final String String_productType = "productType";
	public static final String String_os = "os";
	public static final String String_typeVersion = "typeVersion";
	public static final String String_eclipseproduct = "eclipseproduct";
	public static final String String_typeVersion3_3 = "3.3";
	public static final String String_productVersionStart = "productVersionStart";
	public static final String String_productVersionEnd = "productVersionEnd";
	public static final String String_urlbranch = "urlbranch";

	public static final String String_dir = "dir";
	public static final String String_localName = "localName";
	public static final String String_remoteName = "remoteName";
	public static final String String_updateType = "updateType";

	public static final String String_file = "file";
	public static final String String_plugins = "plugins";
	public static final String String_remote = "remote";
	public static final String String_id = "id";
	public static final String String_version = "version";
	public static final String String_delFile = "delFile";

	public static final String String_zip = "zip";
	public static final String String_format = "format";
	public static final String String_replace = "replace";

	public static final String String_execName = "execName";

	private List<DownloadFile> urls;

	private String execName = EmptyString;

	public String getExecName() {
		return execName;
	}

	public List<DownloadFile> getDownloadFileURLs() {
		return urls;
	}

	/**
	 * 遍历其中的元素，找出较前的版本保存在配置中等待删除，去掉无用的元素
	 * 
	 * @return 是否能够执行更新任务
	 */
	public boolean isUpdate() {
		if (urls == null) {
			urls = new LinkedList<DownloadFile>();
		} else {
			urls.clear();
		}

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource( // XML 实体的单一输入源。
					new FileInputStream(localUpdateXML));
			is.setEncoding(UTF8EncodingString);
			Document doc = builder.parse(is);

			Element root = doc.getDocumentElement();

			/* 找到本次升级所对应的bundle，匹配productType,typeVersion,os,productVersionStart,productVersionEnd，并找到urlbranch */
			Element bundle = null;
			String urlbranch = EmptyString;
			for (Node rootChild = root.getFirstChild(); rootChild != null; rootChild = rootChild
					.getNextSibling()) {
				if (rootChild.getNodeType() != Node.ELEMENT_NODE
						|| !rootChild.getNodeName().equals(String_bundle)) {
					continue;
				}
				Element e = (Element) rootChild;
				String productType = e.getAttribute(String_productType);
				String os = e.getAttribute(String_os);
				String typeVersion = e.getAttribute(String_typeVersion);
				String productVersionStart = e
						.getAttribute(String_productVersionStart);
				String productVersionEnd = e
						.getAttribute(String_productVersionEnd);
				urlbranch = e.getAttribute(String_urlbranch);

				int compStart = compareVersion(productVersionStart,
						iu.getProductVersion());

				int compEnd = compareVersion(productVersionEnd,
						iu.getProductVersion());

				/** 判断是否可用，之所以起始都用闭区间，是因为上限可以用来判断当前版本是否是最新的 */
				boolean isBundle = productType.equals(String_eclipseproduct)
						&& typeVersion.equals(String_typeVersion3_3)
						// TODO &&
						// os.equals(SystemPropertiesUtil.getPlatfromType())
						&& (compStart <= 0 && compEnd >= 0);

				if (isBundle) {
					/** 判断当前版本为最新则返回 */
					if (compEnd == 0)
						return true;
					bundle = e;
					break;
				}

			}

			if (bundle == null)
				return false;

			/* 找到该bundle下所有可用的dir */
			Element dirElement, fileElement, delElement = null;
			/* 对每一个dir进行处理 */
			for (Node bundleChild = bundle.getFirstChild(); bundleChild != null; bundleChild = bundleChild
					.getNextSibling()) {
				if (bundleChild.getNodeType() != Node.ELEMENT_NODE
						|| !bundleChild.getNodeName().equals(String_dir))
					continue;

				dirElement = (Element) bundleChild;
				String remoteName = dirElement.getAttribute(String_remoteName);
				String localName = dirElement.getAttribute(String_localName);

				/* 在remoteName上加上可能的urlbranch */
				if (urlbranch.length() != 0) {
					remoteName = urlbranch + Slash + remoteName;
				}

				if (dirElement.getAttribute(String_format).equals(String_zip)
						&& dirElement.getAttribute(String_updateType).equals(
								String_replace)) {

					/*
					 * 打包时，这一个压缩包中应为一个exec文件和唯一一个文件夹，文件夹中再包含需要的内容，
					 * 因为这是要解压到当前目录下的，应尽量简单避免文件名冲突
					 */
					String eN = dirElement.getAttribute(String_execName);
					/* 本地文件夹目录为空，指示要解压到当前文件夹，这时才使用execName */
					if (localName.length() == 0) {
						execName = eN;
					}

					DownloadFile df = new DownloadFile(localName, remoteName);
					urls.add(df);
					df.setZip(true);

					continue;
				}

				localDir = new File(localName);
				/* 创建子目录 */
				localDir.mkdirs();
				localFileNames = localDir.list();

				/* 对dir下的每一个element进行处理 */
				for (Node dirChild = bundleChild.getFirstChild(); dirChild != null; dirChild = dirChild
						.getNextSibling()) {

					if (dirChild.getNodeType() != Node.ELEMENT_NODE)
						continue;

					String nodeName = dirChild.getNodeName();
					/* 过滤2.0.2.0中每个dir中的旧元素file，剩下的是dir和jar */
					if (nodeName.equals(String_file))
						continue;

					fileElement = (Element) dirChild;
					String fileId = fileElement.getAttribute(String_id);
					String fileVersion = fileElement
							.getAttribute(String_version);
					String localFileName = getLocalVer(fileId, fileVersion);

					/* 尝试在循环集合的过程中删除或添加等结构性更改操作将导致索引丢失的错误 */
					if (localFileName != null) {
						/* 说明新版本中的该文件需要下载更新 */
						String fileRemote = fileElement.getTextContent().trim();
						boolean isZip = nodeName.equals(String_dir)
								&& fileElement.getAttribute(String_format)
										.equals(String_zip);
						DownloadFile df = new DownloadFile(localName
								+ Slash
								+ (isZip ? fileRemote.substring(0,
										fileRemote.lastIndexOf(DotString))
										: fileRemote), remoteName + Slash
								+ fileRemote);
						urls.add(df);

						df.setZip(isZip);
						if (localFileName.length() != 0) {
							/* 说明存在该文件的旧版本 */
							delElement = doc.createElement(String_delFile);
							fileElement.appendChild(delElement);
							delElement.setTextContent(localFileName);
						}
					}

				}

			}

			if (delElement != null)
				transFormerDoc(doc);
			return true;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public int transFormerDoc(Node node) {
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			// 设置输出的encoding为改变UTF-8
			transformer.setOutputProperty(OutputKeys.ENCODING,
					UTF8EncodingString);
			DOMSource source = new DOMSource(node);
			StreamResult result = new StreamResult(localUpdateXML);
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return 3;// 写文件错误
		} catch (TransformerException e) {
			e.printStackTrace();
			return 3;
		}
		return 0;// 替换成功
	}

	/* 这两个变量同步更新内容 */
	private File localDir;
	/* 该值是针对当前路径的 */
	private String[] localFileNames;

	private String getLocalVer(String id, String version) {
		for (String path : localFileNames) {
			int index = path.indexOf(id);
			if (index < 0)
				continue;

			int lastI = path.lastIndexOf(".jar");
			if (lastI < 0) {
				/* 对不带后缀的情况按文件夹的方式对待 */
				lastI = path.length();
			}
			String currentVersion = path.substring(id.length() + 1, lastI);

			if (compareVersion(version, currentVersion) > 0) {
				// 存在更新，需要下载
				return localDir.getName() + File.separator + path;
			} else {
				// 不存在更新，不需要下载
				return null;
			}
		}
		// 不存在本地，需要下载
		return EmptyString;
	}

	/**
	 * 判断两个文本所表示的版本号的大小，前提二者非空
	 * 
	 * @param v1
	 * @param v2
	 * @return 判断结果，即前者-后者，0相等,正值前者大，负值前者小
	 */
	public static int compareVersion(String v1, String v2) {
		StringTokenizer t1 = new StringTokenizer(v1, DotString);
		StringTokenizer t2 = new StringTokenizer(v2, DotString);
		while (t1.hasMoreTokens()) {
			if (!t2.hasMoreTokens())
				return 1;
			int n1 = Integer.parseInt(t1.nextToken());
			int n2 = Integer.parseInt(t2.nextToken());
			int d = n1 - n2;
			if (d != 0)
				return d;
		}
		return t2.hasMoreTokens() ? -1 : 0;
	}

	/**
	 * 
	 */
	public static boolean isVersionUpdatable(String v, String v1, String v2) {
		return compareVersion(v2, v) >= 0 && compareVersion(v, v1) >= 0;
	}

	public static void main(String[] args) {
		System.err.println(isVersionUpdatable("1.0.0", "1.0.0", "1.0.2"));
		// System.out.println("sdfs.fds".lastIndexOf(".jar"));
	}

}
