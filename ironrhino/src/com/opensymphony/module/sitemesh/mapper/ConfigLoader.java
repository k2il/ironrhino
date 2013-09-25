/*
 * Title:        ConfigLoader
 * Description:
 *
 * This software is published under the terms of the OpenSymphony Software
 * License version 1.1, of which a copy has been included with this
 * distribution in the LICENSE.txt file.
 */

package com.opensymphony.module.sitemesh.mapper;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The ConfigLoader reads a configuration XML file that contains Decorator definitions
 * (name, url, init-params) and path-mappings (pattern, name).
 *
 * <p>These can then be accessed by the getDecoratorByName() methods and getMappedName()
 * methods respectively.</p>
 *
 * <p>The DTD for the configuration file in old (deprecated) format is located at
 * <a href="http://www.opensymphony.com/dtds/sitemesh_1_0_decorators.dtd">
 *  http://www.opensymphony.com/dtds/sitemesh_1_0_decorators.dtd
 * </a>.</p>
 *
 * <p>The DTD for the configuration file in new format is located at
 * <a href="http://www.opensymphony.com/dtds/sitemesh_1_5_decorators.dtd">
 *  http://www.opensymphony.com/dtds/sitemesh_1_5_decorators.dtd
 * </a>.</p>
 *
 * <p>Editing the config file will cause it to be auto-reloaded.</p>
 *
 * <p>This class is used by ConfigDecoratorMapper, and uses PathMapper for pattern matching.</p>
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 * @author <a href="mailto:pathos@pandora.be">Mathias Bogaert</a>
 * @version $Revision: 1.8 $
 *
 * @see com.opensymphony.module.sitemesh.mapper.ConfigDecoratorMapper
 * @see com.opensymphony.module.sitemesh.mapper.PathMapper
 */
public class ConfigLoader {

    
    private Map<String,Decorator> decorators = new HashMap<String,Decorator>();
    
    private PathMapper pathMapper = new PathMapper();

    private String configFileName = null;

    private Config config = null;

    /**
     * Create new ConfigLoader using supplied File.
     */
    public ConfigLoader(File configFile) throws ServletException {
        this.configFileName = configFile.getName();
        loadConfig();
    }

    /**
     * Create new ConfigLoader using supplied filename and config.
     */
    public ConfigLoader(String configFileName, Config config) throws ServletException {
        this.config = config;
        this.configFileName = configFileName;
        loadConfig();
    }

    /**
     * Retrieve Decorator based on name specified in configuration file.
     */
    public Decorator getDecoratorByName(String name) throws ServletException {
        return (Decorator) decorators.get(name);
    }

    /** Get name of Decorator mapped to given path. */
    public String getMappedName(String path) throws ServletException {
        return pathMapper.get(path);
    }

    /**
     * Load configuration from file.
     */
    private void loadConfig() throws ServletException {
        // The new state which we build up and atomically replace the old state
        // with atomically to avoid other threads seeing partial modifications.
        try {
            // Build a document from the file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document;
            InputStream is = config.getServletContext().getResourceAsStream(configFileName);
            if (is == null){
                is = getClass().getClassLoader().getResourceAsStream(configFileName);
                }
            if (is == null){
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName);
            }
            document = builder.parse(is);

            // Parse the configuration document
            parseConfig(document);

        }
        catch (ParserConfigurationException e) {
            throw new ServletException("Could not get XML parser", e);
        }
        catch (IOException e) {
            throw new ServletException("Could not read the config file: " + configFileName, e);
        }
        catch (SAXException e) {
            throw new ServletException("Could not parse the config file: " + configFileName, e);
        }
        catch (IllegalArgumentException e) {
            throw new ServletException("Could not find the config file: " + configFileName, e);
        }
    }

    private void parseConfig(Document document) {
        Element root = document.getDocumentElement();

        // get the default directory for the decorators
        String defaultDir = getAttribute(root, "defaultdir");
        if (defaultDir == null) defaultDir = getAttribute(root, "defaultDir");

        // Get decorators
        NodeList decoratorNodes = root.getElementsByTagName("decorator");
        Element decoratorElement;

        for (int i = 0; i < decoratorNodes.getLength(); i++) {
            String name, page, uriPath = null, role = null;

            // get the current decorator element
            decoratorElement = (Element) decoratorNodes.item(i);

            if (getAttribute(decoratorElement, "name") != null) {
                // The new format is used
                name = getAttribute(decoratorElement, "name");
                page = getAttribute(decoratorElement, "page");
                uriPath = getAttribute(decoratorElement, "webapp");
                role = getAttribute(decoratorElement, "role");

                // Append the defaultDir
                if (defaultDir != null && page != null && page.length() > 0 && !page.startsWith("/")) {
                    if (page.charAt(0) == '/') page = defaultDir + page;
                    else page = defaultDir + '/' + page;
                }

                // The uriPath must begin with a slash
                if (uriPath != null && uriPath.length() > 0) {
                    if (uriPath.charAt(0) != '/') uriPath = '/' + uriPath;
                }

                // Get all <pattern>...</pattern> and <url-pattern>...</url-pattern> nodes and add a mapping
                populatePathMapper(decoratorElement.getElementsByTagName("pattern"), role, name);
                populatePathMapper(decoratorElement.getElementsByTagName("url-pattern"), role, name);
            } else {
                // NOTE: Deprecated format
                name = getContainedText(decoratorNodes.item(i), "decorator-name");
                page = getContainedText(decoratorNodes.item(i), "resource");
                // We have this here because the use of jsp-file is deprecated, but we still want
                // it to work.
                if (page == null) page = getContainedText(decoratorNodes.item(i), "jsp-file");
            }

            Map<String,String> params = new HashMap<String,String>();

            NodeList paramNodes = decoratorElement.getElementsByTagName("init-param");
            for (int ii = 0; ii < paramNodes.getLength(); ii++) {
                String paramName = getContainedText(paramNodes.item(ii), "param-name");
                String paramValue = getContainedText(paramNodes.item(ii), "param-value");
                params.put(paramName, paramValue);
            }
            storeDecorator(new DefaultDecorator(name, page, uriPath, role, params));
        }

        // Get (deprecated format) decorator-mappings
        NodeList mappingNodes = root.getElementsByTagName("decorator-mapping");
        for (int i = 0; i < mappingNodes.getLength(); i++) {
            Element n = (Element) mappingNodes.item(i);
            String name = getContainedText(mappingNodes.item(i), "decorator-name");

            // Get all <url-pattern>...</url-pattern> nodes and add a mapping
            populatePathMapper(n.getElementsByTagName("url-pattern"), null, name);
        }
    }

    private void populatePathMapper(NodeList patternNodes, String role, String name) {
        for (int j = 0; j < patternNodes.getLength(); j++) {
            Element p = (Element) patternNodes.item(j);
            Text patternText = (Text) p.getFirstChild();
            if (patternText != null) {
                String pattern = patternText.getData().trim();
                if (pattern != null) {
                    if (role != null) {
                        // concatenate name and role to allow more
                        // than one decorator per role
                        pathMapper.put(name + role, pattern);
                    } else {
                        pathMapper.put(name, pattern);
                    }
                }
            }
        }
    }

    private static String getAttribute(Element element, String name) {
        if (element != null && element.getAttribute(name) != null && !element.getAttribute(name).trim() .equals("")) {
            return element.getAttribute(name).trim();
        } else {
            return null;
        }
    }

    private static String getContainedText(Node parent, String childTagName) {
        try {
            Node tag = ((Element) parent).getElementsByTagName(childTagName).item(0);
            return ((Text) tag.getFirstChild()).getData();
        }
        catch (Exception e) {
            return null;
        }
    }

    private void storeDecorator(Decorator d) {
        if (d.getRole() != null) {
            decorators.put(d.getName() + d.getRole(), d);
        } else {
            decorators.put(d.getName(), d);
        }
    }

}