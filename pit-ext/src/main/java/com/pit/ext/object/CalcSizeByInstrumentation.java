package com.pit.ext.object;

import java.lang.instrument.Instrumentation;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/12.
 */
public class CalcSizeByInstrumentation {
	/**
	 * 假设main方法所在的jar包为：A.jar，premain方法所在的jar包为B.jar。
	 * 注意为main所在的代码打包时，和其它工具类打包一样，需要声明一个MANIFEST.MF清单文件，如下所求：
	 *
	 * Manifest-Version: 1.0
	 * Main-Class: x.y.Main
	 * Premain-Class: com.pit.ext.object.CalcSizeByInstrumentation
	 *
	 *
	 * 然后执行java命令执行jar文件：
	 * java -javaagent:B.jar -jar A.jar
	 */
	private static Instrumentation instrumentation;

	public static void premain(String args, Instrumentation inst) {
		instrumentation = inst;
	}

	public static long getObjectSize(Object o) {
		return instrumentation.getObjectSize(o);
	}
}
