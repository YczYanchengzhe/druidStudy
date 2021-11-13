package com.asdjl.pool.exception;

/**
 * @author chengzhe yan
 * @description
 * @date 2021/11/13 10:20 下午
 */
public class InitException extends RuntimeException{

	public InitException(String message) {
		super(message);
	}

	public InitException(String message, Throwable cause) {
		super(message, cause);
	}
}
