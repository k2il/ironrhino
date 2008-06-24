/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003-2005 Frederico Caldeira Knabben
 * 
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 * 
 * For further information visit:
 * 		http://www.fckeditor.net/
 * 
 * File Name: SimpleUploaderServlet.java
 * 	Java File Uploader class.
 * 
 * Version:  2.3
 * Modified: 2005-08-11 16:29:00
 * 
 * File Authors:
 * 		Simone Chiaretta (simo@users.sourceforge.net)
 */

package org.ironrhino.core.ext.fckeditor;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

public class UploadAction extends ActionSupport {

	protected String baseDir = "/uploadfiles/";

	protected String type;

	protected String newFileFileName;

	protected String newFileContentType;

	protected File newFile;

	public File getNewFile() {
		return newFile;
	}

	public void setNewFile(File newFile) {
		this.newFile = newFile;
	}

	public String getNewFileContentType() {
		return newFileContentType;
	}

	public void setNewFileContentType(String newFileContentType) {
		this.newFileContentType = newFileContentType;
	}

	public String getNewFileFileName() {
		return newFileFileName;
	}

	public void setNewFileFileName(String newFileFileName) {
		this.newFileFileName = newFileFileName;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String execute() throws Exception {
		if (newFile==null||newFileFileName.endsWith(".jsp"))
			return NONE;
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();

		String currentPath = baseDir + type;
		String currentDirPath = ServletActionContext.getServletContext()
				.getRealPath(currentPath);
		currentPath = request.getContextPath() + currentPath;
		String retVal = "0";
		String newName = "";
		String fileUrl = "";
		String errorMessage = "";

		String nameWithoutExt = newFileFileName.substring(0, newFileFileName
				.lastIndexOf('.'));
		String ext = newFileFileName
				.substring(newFileFileName.lastIndexOf('.') + 1);
		File pathToSave = new File(currentDirPath, newFileFileName);
		int counter = 1;
		while (pathToSave.exists()) {
			newName = nameWithoutExt + "(" + counter + ")" + "." + ext;
			retVal = "201";
			pathToSave = new File(currentDirPath, newName);
			counter++;
		}
		newFile.renameTo(pathToSave);
		out.println("<script type=\"text/javascript\">");
		out.println("window.parent.OnUploadCompleted(" + retVal + ",'"
				+ fileUrl + "','" + newName + "','" + errorMessage + "');");
		out.println("</script>");
		out.flush();
		out.close();

		return NONE;

	}

}
