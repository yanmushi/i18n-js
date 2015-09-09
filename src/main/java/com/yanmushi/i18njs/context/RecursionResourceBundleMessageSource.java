/*
 * Copyright (c) 2014-2015 YanMushi 
 * All rights reserved.
 */
package com.yanmushi.i18njs.context;

import java.io.File;
import java.util.Set;

/**
 * 递归获取文件路径下的文件信息
 * @author YanMushi
 * @since
 */
public class RecursionResourceBundleMessageSource extends OpenResourceBundleMessageSource {

	/*
	 * 将父亲路径下的文件递归加载到文件集中
	 */
	@Override
	protected void fillFiles(File f, Set<String> fileSet) {
		String parentPath = this.getFolder();
		File[] files = f.listFiles();
		for (File resource : files) {
			if (resource.isDirectory()) {
				fillFiles(resource, fileSet);
			} else {
				fileSet.add(parentPath + getResourceName(resource.getName()));
			}
		}
	}
}
