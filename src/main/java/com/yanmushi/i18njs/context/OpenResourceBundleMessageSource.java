/*
 * Copyright (c) 2014-2015 YanMushi 
 * All rights reserved.
 */
package com.yanmushi.i18njs.context;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 
 * @author YanMushi
 * @since
 */
public class OpenResourceBundleMessageSource extends ResourceBundleMessageSource
		implements ResourceBundleRegistry {

	private String folder; // 资源文件根目录
	
	public ResourceBundle loadResourceBundle(String basename, Locale locale) {
		String realName = handleBasename(basename);
		return super.getResourceBundle(folder + realName, locale);
	}

	/**
	 * 将读取文件的名字转换，默认返回当前名字
	 * @param basename
	 * @return
	 */
	protected String handleBasename(String basename) {
		return basename;
	}

	/**
	 * @return Returns the folder.
	 */
	public String getFolder() {
		return folder;
	}

	/**
	 * @param folder The folder to set.
	 */
	public void setFolder(String folder) {
		this.folder = folder.endsWith("/") ? folder : (folder + "/");
		URL url = getClass().getClassLoader().getResource(folder);

		String filepath = url.getPath();
		
		try {
			filepath = URLDecoder.decode(filepath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		}
		
		File f = new File(filepath);
		if (f.exists() && f.isDirectory()) {
			Set<String> fileSet = new HashSet<String>();
			
			fillFiles(f, fileSet);
			
			setBasenames(fileSet.toArray(new String[0]));
		} else {
			throw new RuntimeException("invalid folder path[" + filepath + 
					"]! folder status: " + f.isDirectory());
		}
	}

	/**
	 * @param f
	 * @param fileSet
	 */
	protected void fillFiles(File f, Set<String> fileSet) {
		String parentPath = this.folder;
		File[] files = f.listFiles();
		for (File resource : files) {
			if (resource.isFile()) {
				fileSet.add(parentPath + getResourceName(resource.getName()));
			}
		}
	}

	protected String getResourceName(String name) {
		String n = name.substring(0, name.length() - ".properties".length());
		int local = n.indexOf('_');
		return local > -1 ? n.substring(0, local) : n;
	}
	
}
