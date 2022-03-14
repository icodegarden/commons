package io.github.icodegarden.commons.lang.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * @author Fangfang.Xu
 *
 */
public final class ExceptionUtils {
	private ExceptionUtils() {
	}

	public static String toString(Throwable e) {
		UnsafeStringWriter w = new UnsafeStringWriter();
		PrintWriter p = new PrintWriter(w);
		p.println();
		try {
			e.printStackTrace(p);
			return e.getMessage() != null ? e.getMessage() + w.toString() : w.toString();
		} finally {
			p.close();
		}
	}

	private static class UnsafeStringWriter extends Writer {
		private StringBuilder mBuffer;

		public UnsafeStringWriter() {
			lock = mBuffer = new StringBuilder();
		}

//		public UnsafeStringWriter(int size) {
//			if (size < 0) {
//				throw new IllegalArgumentException("Negative buffer size");
//			}
//
//			lock = mBuffer = new StringBuilder();
//		}

		@Override
		public void write(int c) {
			mBuffer.append((char) c);
		}

		@Override
		public void write(char[] cs) throws IOException {
			mBuffer.append(cs, 0, cs.length);
		}

		@Override
		public void write(char[] cs, int off, int len) throws IOException {
			if ((off < 0) || (off > cs.length) || (len < 0) || ((off + len) > cs.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			}

			if (len > 0) {
				mBuffer.append(cs, off, len);
			}
		}

		@Override
		public void write(String str) {
			mBuffer.append(str);
		}

		@Override
		public void write(String str, int off, int len) {
			mBuffer.append(str, off, off + len);
		}

		@Override
		public Writer append(CharSequence csq) {
			if (csq == null) {
				write("null");
			} else {
				write(csq.toString());
			}
			return this;
		}

		@Override
		public Writer append(CharSequence csq, int start, int end) {
			CharSequence cs = (csq == null ? "null" : csq);
			write(cs.subSequence(start, end).toString());
			return this;
		}

		@Override
		public Writer append(char c) {
			mBuffer.append(c);
			return this;
		}

		@Override
		public void close() {
		}

		@Override
		public void flush() {
		}

		@Override
		public String toString() {
			return mBuffer.toString();
		}
	}
}