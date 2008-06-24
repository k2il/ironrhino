package org.ironrhino.core.performance;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class BufferableResponseWrapper extends HttpServletResponseWrapper {

	private final RoutablePrintWriter routablePrintWriter;

	private final RoutableServletOutputStream routableServletOutputStream;

	private Buffer buffer;

	private boolean aborted = false;

	private boolean parseablePage = false;

	public BufferableResponseWrapper(final HttpServletResponse response) {
		super(response);

		routablePrintWriter = new RoutablePrintWriter(
				new RoutablePrintWriter.DestinationFactory() {
					public PrintWriter activateDestination() throws IOException {
						return response.getWriter();
					}
				});
		routableServletOutputStream = new RoutableServletOutputStream(
				new RoutableServletOutputStream.DestinationFactory() {
					public ServletOutputStream create() throws IOException {
						return response.getOutputStream();
					}
				});

	}

	public void setContentType(String type) {
		super.setContentType(type);

		if (type != null) {
			HttpContentType httpContentType = new HttpContentType(type);
			activate(httpContentType.getType(), httpContentType.getEncoding());

		}

	}

	public void activate(String contentType, String encoding) {
		if (parseablePage) {
			return; // already activated
		}
		buffer = new Buffer(encoding);
		routablePrintWriter
				.updateDestination(new RoutablePrintWriter.DestinationFactory() {
					public PrintWriter activateDestination() {
						return buffer.getWriter();
					}
				});
		routableServletOutputStream
				.updateDestination(new RoutableServletOutputStream.DestinationFactory() {
					public ServletOutputStream create() {
						return buffer.getOutputStream();
					}
				});
		parseablePage = true;
	}

	private void deactivate() {
		parseablePage = false;
		buffer = null;
		routablePrintWriter
				.updateDestination(new RoutablePrintWriter.DestinationFactory() {
					public PrintWriter activateDestination() throws IOException {
						return getResponse().getWriter();
					}
				});
		routableServletOutputStream
				.updateDestination(new RoutableServletOutputStream.DestinationFactory() {
					public ServletOutputStream create() throws IOException {
						return getResponse().getOutputStream();
					}
				});
	}

	public void setContentLength(int contentLength) {
		if (!parseablePage)
			super.setContentLength(contentLength);
	}

	public void flushBuffer() throws IOException {
		if (!parseablePage)
			super.flushBuffer();
	}

	public void setHeader(String name, String value) {
		if (name.toLowerCase().equals("content-type")) {
			setContentType(value);
		} else if (!parseablePage
				|| !name.toLowerCase().equals("content-length")) {
			super.setHeader(name, value);
		}
	}

	public void addHeader(String name, String value) {
		if (name.toLowerCase().equals("content-type")) {
			setContentType(value);
		} else if (!parseablePage
				|| !name.toLowerCase().equals("content-length")) {
			super.addHeader(name, value);
		}
	}

	public void setStatus(int sc) {
		if (sc == HttpServletResponse.SC_NOT_MODIFIED) {
			aborted = true;
			deactivate();
		}
		super.setStatus(sc);
	}

	public ServletOutputStream getOutputStream() {
		return routableServletOutputStream;
	}

	public PrintWriter getWriter() {
		return routablePrintWriter;
	}

	public void sendError(int sc) throws IOException {
		aborted = true;
		super.sendError(sc);
	}

	public void sendError(int sc, String msg) throws IOException {
		aborted = true;
		super.sendError(sc, msg);
	}

	public void sendRedirect(String location) throws IOException {
		aborted = true;
		super.sendRedirect(location);
	}

	public boolean isUsingStream() {
		return buffer != null && buffer.isUsingStream();
	}

	public byte[] getContents() throws IOException {
		if (aborted || !parseablePage || buffer == null) {
			return null;
		} else {
			return buffer.getBytes();
		}
	}
}