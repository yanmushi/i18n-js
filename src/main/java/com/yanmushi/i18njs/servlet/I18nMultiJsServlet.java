/*
 * Copyright (c) 2014-2015 YanMushi 
 * All rights reserved.
 */
package com.yanmushi.i18njs.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;

/**
 * 允许一次请求获取多种信息。
 * <p>请求地址如下:<code>/filename1-filename2_locale</code>
 * 
 * @author YanMushi
 * @since
 */
public class I18nMultiJsServlet extends I18nJsServlet {

	private static final long serialVersionUID = 5769248881567624760L;
	private ConcurrentHashMap<String, Map<String, String>> msgMap = new ConcurrentHashMap<>();
	
	@Override
	protected String getResource(String name, Locale locale) {
		String rkey = name + "." + locale;
		String r = cache.get(rkey);
		if (r != null) return r;
		
		String[] names = name.split("\\-");
		Map<String, String> jsonMap = new HashMap<String, String>();
		
		for (String one : names) {
			put2Map(one, locale, jsonMap);
		}
		
		String bjson = JSON.toJSONString(jsonMap);
		cache.put(rkey, bjson);
		return bjson;
	}

	// 将不同文件名字下的message存放到map中
	private void put2Map(String name, Locale locale, Map<String, String> jsonMap) {
		String nkey = name + "." + locale;
		Map<String, String> datas = msgMap.get(nkey);

		// 加载新的数据信息
		if (datas == null) {
			datas = new HashMap<>();
			ResourceBundle bundle = sources.loadResourceBundle(name, locale);
			Enumeration<String> keys = bundle.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				String val = bundle.getString(key);
				datas.put(key, val);
			}
		}

		// 写入上下文
		jsonMap.putAll(datas);
		// 写入当前文件的缓存中
		msgMap.put(nkey, datas);
	}
}
