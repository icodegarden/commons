package io.github.icodegarden.commons.lang.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CompressionUtil {

	public static String gzip(String str) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			try (GZIPOutputStream gzip = new GZIPOutputStream(out);) {
				gzip.write(str.getBytes());
			}

			return new String(Base64.getEncoder().encode(out.toByteArray()), "utf-8");
		} catch (IOException e) {
			throw new IllegalArgumentException("ex on gzip, str:" + str, e);
		}
	}

	public static String unGzip(String str) {
		byte[] bytes = null;
		try {
			bytes = Base64.getDecoder().decode(str.getBytes("utf-8"));
		} catch (Exception e) {
			throw new IllegalArgumentException("ex on unGzip decode str, str:" + str, e);
		}
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);) {
			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);) {
				byte[] buffer = new byte[1024];
				int offset = -1;
				while ((offset = gzipInputStream.read(buffer)) != -1) {
					byteArrayOutputStream.write(buffer, 0, offset);
				}
				String decompressed = byteArrayOutputStream.toString();
				return decompressed;
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("ex on unGzip, str:" + str, e);
		}
	}
}