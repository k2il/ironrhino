package initdata;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ironrhino.common.model.Coordinate;
import org.ironrhino.common.model.Region;
import org.ironrhino.common.util.RegionParser;
import org.ironrhino.common.util.RegionUtils;
import org.ironrhino.core.service.EntityManager;
import org.ironrhino.core.util.XmlUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class InitRegion {

	static EntityManager entityManager;

	static Map<String, String> regionAreacodeMap = regionAreacodeMap();
	static Map<String, String> regionCoordinateMap = regionCoordinateMap();

	public static void main(String... strings) throws Exception {
		System.setProperty("app.name", "ironrhino");
		System.setProperty("ironrhino.home", System.getProperty("user.home")
				+ "/" + System.getProperty("app.name"));
		System.setProperty("ironrhino.context", System.getProperty("user.home")
				+ "/" + System.getProperty("app.name"));
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { "initdata/applicationContext-initdata.xml",
						"resources/spring/applicationContext-ds.xml",
						"resources/spring/applicationContext-hibernate.xml" });
		entityManager = (EntityManager) ctx.getBean("entityManager");
		List<Region> regions = RegionParser.parse();
		for (Region region : regions) {
			save(region);
		}
		ctx.close();
	}

	private static void save(Region region) {
		String shortName = RegionUtils.shortenName(region.getName());
		region.setAreacode(regionAreacodeMap.get(region.getName()));
		String coordinate = regionCoordinateMap.get(region.getName());
		if (coordinate == null) {
			coordinate = regionCoordinateMap.get(shortName);
		}
		if (coordinate != null) {
			String[] arr = coordinate.split(",");
			Coordinate c = new Coordinate();
			c.setLatitude(Double.valueOf(arr[1]));
			c.setLongitude(Double.valueOf(arr[0]));
			region.setCoordinate(c);
		}
		if (rank1cities.contains(shortName)) {
			region.setRank(1);
		} else if (rank2cities.contains(shortName)) {
			region.setRank(2);
		} else if (!region.isRoot() && !region.isLeaf()) {
			if (region.getDisplayOrder() == 0)
				region.setRank(3);
			else
				region.setRank(4);
		}
		entityManager.save(region);
		List<Region> list = new ArrayList<Region>();
		for (Region child : region.getChildren())
			list.add(child);
		Collections.sort(list);
		for (Region child : list)
			save(child);
	}

	private static Map<String, String> regionAreacodeMap() {
		List<String> lines = Collections.EMPTY_LIST;
		try {
			lines = IOUtils.readLines(
					Thread.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"resources/data/region_code.txt"), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, String> map = new HashMap<String, String>(lines.size());
		for (String line : lines) {
			if (StringUtils.isBlank(line))
				continue;
			String arr[] = line.split("\\s+", 2);
			map.put(arr[1], arr[0]);
		}
		return map;
	}

	private static Map<String, String> regionCoordinateMap() {
		// http://www.williamlong.info/google/archives/27.html
		NodeList nodeList = null;
		NamespaceContext nsContext = new NamespaceContext() {

			@Override
			public String getNamespaceURI(String prefix) {
				String uri;
				if (prefix.equals("kml")) {
					uri = "http://earth.google.com/kml/2.0";
				} else {
					uri = null;
				}
				return uri;
			}

			@Override
			public String getPrefix(String namespaceURI) {
				String prefix;
				if (namespaceURI.equals("http://earth.google.com/kml/2.0")) {
					prefix = "kml";
				} else {
					prefix = null;
				}
				return prefix;
			}

			@Override
			public Iterator getPrefixes(String namespaceURI) {
				List prefix = new ArrayList();
				prefix.add("kml");
				return prefix.iterator();
			}
		};
		try {
			nodeList = XmlUtils.evalNodeList(
					"//kml:Placemark",
					new InputStreamReader(Thread.currentThread()
							.getContextClassLoader()
							.getResourceAsStream("resources/data/region.kml"),
							"utf-8"), nsContext);
		} catch (UnsupportedEncodingException e) {
			return Collections.EMPTY_MAP;
		}
		Map<String, String> map = new HashMap<String, String>(
				nodeList.getLength());
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element) nodeList.item(i);
			NodeList nl = element.getChildNodes();
			String name = null;
			String coordinate = null;
			for (int j = 0; j < nl.getLength(); j++) {
				Node node = nl.item(j);
				if (node.getNodeType() == Document.ELEMENT_NODE) {
					Element ele = (Element) node;
					if (ele.getTagName().equals("name")) {
						name = ele.getTextContent();
					} else if (ele.getTagName().equals("Point")) {
						NodeList nl2 = ele.getChildNodes();
						for (int k = 0; k < nl2.getLength(); k++) {
							Node node2 = nl2.item(k);
							if (node2.getNodeType() == Document.ELEMENT_NODE) {
								Element ele2 = (Element) node2;
								if (ele2.getTagName().equals("coordinates")) {
									coordinate = ele2.getTextContent();
								}
							}
						}
					}
				}
			}
			if (name != null)
				map.put(name, coordinate);
		}
		return map;
	}

	private static List<String> rank1cities = Arrays
			.asList("北京 上海 广州 深圳 香港 澳门 台北".split("\\s"));
	private static List<String> rank2cities = Arrays
			.asList("天津 重庆 杭州 南京 成都 武汉 西安 沈阳 大连 青岛 厦门".split("\\s"));

}
