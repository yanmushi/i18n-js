/*
 * Copyright (c) 2014-2015 YanMushi 
 * All rights reserved.
 */
package com.yanmushi.i18njs.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.alibaba.fastjson.JSON;
import com.yanmushi.i18njs.context.ResourceBundleRegistry;

/**
 * 主要用于ResourceBundle资源转换为js脚本。
 * 
 * <p>
 * 从客户端请求：<code>/filename_locale</code>
 * </p>
 * 通过解析后，获取到资源文件，并将资源转换为json数据信息。
 * 并将调用callback方法，将json存入到客户端的上下文中。
 * 
 * @author YanMushi
 * @since
 */
public class I18nJsServlet extends HttpServlet {

	private static final long serialVersionUID = -6601090275298017439L;
	private static final String DEFAULT_CALLBACK = "register";
	
	protected ResourceBundleRegistry sources;
	protected Map<String, String> cache = new HashMap<String, String>();

	private boolean init;
	private String callback = DEFAULT_CALLBACK;

	private String encoding = "UTF-8";
	private long cacheExpires = 10 * 60 * 1000;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String cbk = config.getInitParameter("callback");
		if (StringUtils.isNotEmpty(cbk)) {
			callback = cbk;
		}
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		if (isNotModify(request)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		
		if (!init) doInitContext();
		
		setHeader(response);

		String outjs = null;
		String[] path = analysePath(request);
		String name = path[0];
		Locale locale = path[1] == null ? null : new Locale(path[1]);
		outjs = getResource(name, locale);
		
		String cbk = request.getParameter("cbk");
		
		if (StringUtils.isEmpty(cbk)) {
			cbk = callback;
		}

		response.getWriter().println(cbk + "('" + name + "'," + outjs + ")");
	}

	/**
	 * @param request
	 * @return
	 */
	protected String[] analysePath(HttpServletRequest request) {
		String path = request.getPathInfo();
		int local = path.indexOf('_');
		String locale = null;
		if (local > -1) {
			locale = path.substring(local + 1);
		}
		return new String[]{path.substring(1, local), locale};
	}

	/**
	 * @param request
	 * @return
	 */
	private boolean isNotModify(HttpServletRequest request) {
		String cache = request.getHeader("Cache-Control");
		return StringUtils.isNotEmpty(cache) && cache.contains("max-age");
	}

	// 设置响应头信息
	private void setHeader(HttpServletResponse response) {
		response.setContentType("text/javascript");
		response.setCharacterEncoding(encoding);
		// 设置缓存信息
		response.setHeader("Cache-Control", "max-age=0");
		response.setDateHeader("Last-Modified", System.currentTimeMillis());
		response.setDateHeader("Expires", System.currentTimeMillis() + cacheExpires);
		
	}

	/**
	 * @param name
	 * @param locale
	 * @return
	 */
	protected String getResource(String name, Locale locale) {
		String rkey = name + "." + locale;
		String r = cache.get(rkey);
		if (r != null) return r;
		ResourceBundle bundle = sources.loadResourceBundle(name, locale);
		Enumeration<String> keys = bundle.getKeys();
		Map<String, String> jsonMap = new HashMap<String, String>();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String val = bundle.getString(key);
			jsonMap.put(key, val);
		}
		
		String bjson = JSON.toJSONString(jsonMap);
		cache.put(rkey, bjson);
		return bjson;
	}

	private void doInitContext() throws ServletException {
		ServletContext context = getServletContext();
		WebApplicationContext rootContext =
				WebApplicationContextUtils.getWebApplicationContext(context);
		sources = rootContext.getBean(ResourceBundleRegistry.class);
	}
	
}
