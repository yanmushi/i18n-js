/*
 * Copyright (c) 2014-2015 YanMushi 
 * All rights reserved.
 */
package com.yanmushi.i18njs.context;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * ResourceBundle资源信息注册接口，可通过此处加载资源文件。
 * 
 * @author YanMushi
 * @since
 */
public interface ResourceBundleRegistry {
	
	/**
	 * 通过文件名称获取文件信息
	 * @param basename 基础文件名称
	 * @param locale 语言信息
	 * @return 当前message
	 */
	ResourceBundle loadResourceBundle(String basename, Locale locale);
}
