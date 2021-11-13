package com.asdjl.pool.utils;

/**
 * @author chengzhe yan
 * @description
 * @date 2021/11/13 11:30 下午
 */
public class StringUtil {

	public static boolean isEmpty(CharSequence value) {
		return value == null || value.length() == 0;
	}

	public static boolean isNotEmpty(CharSequence value) {
		return !isEmpty(value);
	}
}
