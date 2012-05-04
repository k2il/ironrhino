package org.ironrhino.core.util;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class CodecUtils {

	public static final String DEFAULT_ENCODING = "UTF-8";

	private static ThreadLocal<SoftReference<MessageDigest>> MD5 = new ThreadLocal<SoftReference<MessageDigest>>() {

		@Override
		protected SoftReference<MessageDigest> initialValue() {
			try {
				return new SoftReference<MessageDigest>(
						MessageDigest.getInstance("MD5"));
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("md5 algorythm found");
			}
		}
	};

	private static ThreadLocal<SoftReference<MessageDigest>> SHA = new ThreadLocal<SoftReference<MessageDigest>>() {

		@Override
		protected SoftReference<MessageDigest> initialValue() {
			try {
				return new SoftReference<MessageDigest>(
						MessageDigest.getInstance("SHA"));
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("sha algorythm found");
			}
		}
	};

	public static byte[] md5(byte[] input) {
		SoftReference<MessageDigest> instanceRef = MD5.get();
		if (instanceRef == null) {
			try {
				instanceRef = new SoftReference<MessageDigest>(
						MessageDigest.getInstance("MD5"));
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("md5 algorythm found");
			}
			MD5.set(instanceRef);
		}
		MessageDigest md5 = instanceRef.get();
		md5.reset();
		md5.update(input);
		return md5.digest();
	}

	public static byte[] md5(String input) {
		return md5(input, DEFAULT_ENCODING);
	}

	public static byte[] md5(String input, String encoding) {
		try {
			return md5(input.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public static String md5Hex(byte[] input) {
		return Hex.encodeHexString(md5(input));
	}

	public static String md5Hex(String input) {
		return md5Hex(input, DEFAULT_ENCODING);
	}

	public static String md5Hex(String input, String encoding) {
		try {
			return md5Hex(input.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public static byte[] sha(byte[] input) {
		SoftReference<MessageDigest> instanceRef = SHA.get();
		if (instanceRef == null) {
			try {
				instanceRef = new SoftReference<MessageDigest>(
						MessageDigest.getInstance("SHA"));
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("sha algorythm found");
			}
			SHA.set(instanceRef);
		}
		MessageDigest sha = instanceRef.get();
		sha.reset();
		sha.update(input);
		return sha.digest();
	}

	public static byte[] sha(String input) {
		return sha(input, DEFAULT_ENCODING);
	}

	public static byte[] sha(String input, String encoding) {
		try {
			return sha(input.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public static String shaHex(byte[] input) {
		return Hex.encodeHexString(sha(input));
	}

	public static String shaHex(String input) {
		return shaHex(input, DEFAULT_ENCODING);
	}

	public static String shaHex(String input, String encoding) {
		try {
			return shaHex(input.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public static String fuzzify(String input) {
		try {
			byte[] bytes = Base64
					.encodeBase64(input.getBytes(DEFAULT_ENCODING));
			swap(bytes);
			return new String(bytes);
		} catch (UnsupportedEncodingException e) {
			return input;
		}
	}

	public static String defuzzify(String input) {
		try {
			byte[] bytes = input.getBytes();
			swap(bytes);
			return new String(Base64.decodeBase64(bytes), DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			return input;
		}
	}

	private static void swap(byte[] bytes) {
		int half = bytes.length / 2;
		for (int i = 0; i < half; i++) {
			byte temp = bytes[i];
			bytes[i] = bytes[half + i];
			bytes[half + i] = temp;
		}
	}

	public static String swap(String str) {
		char[] chars = str.toCharArray();
		int half = chars.length / 2;
		for (int i = 0; i < half; i++) {
			char temp = chars[i];
			chars[i] = chars[half + i];
			chars[half + i] = temp;
		}
		return new String(chars);
	}

	public static String digest(String input) {
		return md5Hex(shaHex(input, 3));
	}

	public static String md5Hex(String input, int times) {
		for (int i = 0; i < times; i++)
			input = md5Hex(input);
		return input;
	}

	public static String shaHex(String input, int times) {
		for (int i = 0; i < times; i++)
			input = shaHex(input);
		return input;
	}

	private static String string = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static Random random = new Random();

	public static String randomString(int digits) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digits; i++)
			sb.append(string.charAt(random.nextInt(26)));
		return sb.toString();
	}

	public static String randomDigitalString(int digits) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digits; i++)
			sb.append(random.nextInt(10));
		return sb.toString();
	}

	public static int randomInt(int digits) {
		if (digits <= 1)
			return random.nextInt(10);
		int n = 10;
		for (int i = 1; i < digits - 1; i++)
			n *= 10;
		return n + random.nextInt(n * 9);
	}

	public static String nextId() {
		String id = UUID.randomUUID().toString().replace("-", "");
		id = NumberUtils.xToY(16, 62, id);
		return id;
	}

	public static String nextId(String salt) {
		String id = md5Hex(salt + UUID.randomUUID().toString());
		id = NumberUtils.xToY(16, 62, id);
		return id;
	}

}
