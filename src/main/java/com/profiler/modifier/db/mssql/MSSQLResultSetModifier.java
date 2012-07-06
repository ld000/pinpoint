package com.profiler.modifier.db.mssql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class MSSQLResultSetModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(MSSQLResultSetModifier.class);

	public static byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("MSSQLResultSetModifier modifing. %s", javassistClassName);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private static byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateNextMethod(classPool, cc);
			updateCloseMethod(classPool, cc);
			byte[] newClassfileBuffer = cc.toBytecode();
			// cc.writeFile();
			printClassConvertComplete(javassistClassName);
			return newClassfileBuffer;
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private static void updateNextMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod1 = cc.getDeclaredMethod("next", null);

		logger.info("Changing next() method");

		serviceMethod1.insertBefore(getNextMethodBeforeInsertCode());
	}

	private static String getNextMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"PreparedStatement.setInternal(int,String) method is called\");");
			sb.append("System.out.println(\"-----Position=\"+$1+\" Value=\"+$2);");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".updateFetchCount();");
		sb.append("}");

		return sb.toString();
	}

	private static void updateCloseMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod1 = cc.getDeclaredMethod("close", null);

		logger.info("Changing close() method");

		serviceMethod1.insertBefore(getCloseMethodBeforeInsertCode());
	}

	private static String getCloseMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"PreparedStatement.setInternal(int,String) method is called\");");
			sb.append("System.out.println(\"-----Position=\"+$1+\" Value=\"+$2);");
			sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_RESULTSET_CLOSE + ");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".addResultSetData();");
		sb.append("}");
		return sb.toString();
	}
}