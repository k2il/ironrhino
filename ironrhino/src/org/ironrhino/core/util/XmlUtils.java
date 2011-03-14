package org.ironrhino.core.util;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlUtils {

	private static XPath _xpath = XPathFactory.newInstance().newXPath();

	public static String eval(String xpath, Reader reader) {
		try {
			XPathExpression xpe = _xpath.compile(xpath);
			return xpe.evaluate(new InputSource(reader));
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String eval(String xpath, String source) {
		return eval(xpath, new StringReader(source));
	}

	public static NodeList evalNodeList(String xpath, Reader reader) {
		try {
			XPathExpression xpe = _xpath.compile(xpath);
			return (NodeList) xpe.evaluate(new InputSource(reader),
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static NodeList evalNodeList(String xpath, String source) {
		return evalNodeList(xpath, new StringReader(source));
	}

}
