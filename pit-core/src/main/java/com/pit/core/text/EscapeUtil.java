package com.pit.core.text;

import org.apache.commons.text.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * url、xml转义工具
 * 1. 使用 @{link java.net.URLEncoder} 对URL转义
 * 2. 使用 @{link org.apache.commons.text.StringEscapeUtils} 对xml、html转义
 * example：
 * " 转为 &quot;
 * & 转为 &amp;
 *
 * @author gy
 * @date 2020/3/20
 */
public class EscapeUtil {
	/**
	 * URL encode，使用UTF-8字符集，encode后可作为URL的参数
	 *
	 * @param param
	 * @return
	 */
	public static String urlEncode(String param) {
		try {
			return URLEncoder.encode(param, Charsets2.UTF_8_NAME);
		} catch (UnsupportedEncodingException ignored) { // NOSONAR
			// this exception is only for detecting and handling invalid inputs
			return null;
		}
	}

	/**
	 * URL decode, 使用UTF-8字符集， decode后得到URL参数原始字符串
	 *
	 * @param urlParam
	 * @return
	 */
	public static String urlDecode(String urlParam) {
		try {
			return URLDecoder.decode(urlParam, Charsets2.UTF_8_NAME);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * XML escape，将字符串转码为符合XML1.1格式的字符串
	 *
	 * @param xml
	 * @return
	 */
	public static String escapeXml(String xml) {
		return StringEscapeUtils.escapeXml11(xml);
	}

	/**
	 * Xml unescape ，XML格式的字符串解码为普通字符串
	 *
	 * @param xml
	 * @return
	 */
	public static String unescapeXml(String xml) {
		return StringEscapeUtils.unescapeXml(xml);
	}

	/**
	 * 将字符串转码为符合HTML4格式的字符串
	 *
	 * @param html
	 * @return
	 */
	public static String escapeHtml(String html) {
		return StringEscapeUtils.escapeHtml4(html);
	}

	/**
	 * 将HTML4格式的字符串转码解码为普通字符串
	 *
	 * @param html
	 * @return
	 */
	public static String unescapeHtml(String html) {
		return StringEscapeUtils.unescapeHtml4(html);
	}
}