package cike.xmlhandler;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**处理XML的解析器工具类
 * @author Qixuan
 *
 */
public class XMLparser {
	
	
	/**
	 * XML解析器，false表示默认解析器
	 */
	private SAXBuilder builder = new SAXBuilder(false);
	/**
	 * document(XML文档对象)
	 */
	public static Document doc;
	/**
	 * 根节点
	 */
	public Element root;
	
	public XMLparser(String xmlpath) {
		try {
			doc = builder.build(xmlpath);
			root = doc.getRootElement();
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
