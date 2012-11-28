package org.ironrhino.core.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeUtils {

	public static byte[] encode(String content, String encoding, String format,
			int width, int height) throws WriterException, IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream(256);
		encode(content, encoding, format, width, height, stream);
		byte[] bytes = stream.toByteArray();
		return bytes;
	}

	public static void encode(String content, String encoding, String format,
			int width, int height, OutputStream stream) throws WriterException,
			IOException {
		BitMatrix matrix = new QRCodeWriter().encode(
				new String(content.getBytes(encoding), "ISO-8859-1"),
				BarcodeFormat.QR_CODE, width, height);
		MatrixToImageWriter.writeToStream(matrix, format, stream);
		stream.close();
	}

	public static String decode(byte[] bytes, String encoding)
			throws IOException, NotFoundException, ChecksumException,
			FormatException {
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		return decode(stream, encoding);
	}

	public static String decode(InputStream stream, String encoding)
			throws IOException, NotFoundException, ChecksumException,
			FormatException {
		BufferedImage image = ImageIO.read(stream);
		LuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Map<DecodeHintType, String> hints = new HashMap<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, encoding);
		Result result = new QRCodeReader().decode(bitmap, hints);
		return result.getText();
	}

	public static String decode(String url, String encoding)
			throws IOException, NotFoundException, ChecksumException,
			FormatException {
		URLConnection connection = new URL(url).openConnection();
		return decode(connection.getInputStream(), encoding);
	}

}
