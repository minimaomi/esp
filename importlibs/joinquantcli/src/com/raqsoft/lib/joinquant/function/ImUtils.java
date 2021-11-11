package com.raqsoft.lib.joinquant.function;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import net.sf.json.JSONObject;

public class ImUtils {
	public static void writeString(String file, String content) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(content.getBytes(), 0, content.getBytes().length);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String ReadStringOfFile(String file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			String content = "";
			// 自定义缓冲区
			byte[] buffer = new byte[512];
			int flag = 0;
			while ((flag = bis.read(buffer)) != -1) {
				content += new String(buffer, 0, flag);
			}

			// 关闭的时候只需要关闭最外层的流就行了
			bis.close();
			return content;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// 输出修改时间，如2009-08-17
	public static String getModifiedTime(String fname) {
		File f = new File(fname);
		Calendar cal = Calendar.getInstance();
		long time = f.lastModified();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		cal.setTimeInMillis(time);
		return formatter.format(cal.getTime());
	}

	public static String getTempTodateFile(String mod) {
		try {
			// create a temp file
			File temp = File.createTempFile("temp-file-name", ".tmp");

			// Get tempropary file path
			String absolutePath = temp.getAbsolutePath();
			String tempFilePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
			String OS = System.getProperty("os.name").toLowerCase();
			if (OS.indexOf("windows") >= 0) {
				String s = tempFilePath.replace("Temp", "raqsoft");
				File f = new File(s);
				if (!f.exists()) {
					f.mkdir();
				}
				s = String.format("%s%sjq_%s_token.txt", s, File.separator, mod);
				return s;
			} else {
				return String.format("%s%sraqsoft%sjq_%s_token.txt", tempFilePath, File.separator, File.separator, mod);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isJsonFormat(String string) {
		try {
			JSONObject.fromString(string);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
