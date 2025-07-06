package org.codewithzea.restaurantservice.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

public class GZipServletResponseWrapper extends HttpServletResponseWrapper {
    private final GZIPOutputStream gzipOutputStream;
    private PrintWriter writer;

    public GZipServletResponseWrapper(HttpServletResponse response, GZIPOutputStream gzipOutputStream) {
        super(response);
        this.gzipOutputStream = gzipOutputStream;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new GZipServletOutputStream(gzipOutputStream);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(gzipOutputStream), getCharacterEncoding()));
        }
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        gzipOutputStream.flush();
    }

    public void finish() throws IOException {
        if (writer != null) {
            writer.close();
        }
        gzipOutputStream.finish();
    }

    private static class GZipServletOutputStream extends ServletOutputStream {
        private final GZIPOutputStream gzipOutputStream;

        public GZipServletOutputStream(GZIPOutputStream gzipOutputStream) {
            this.gzipOutputStream = gzipOutputStream;
        }

        @Override
        public void write(int b) throws IOException {
            gzipOutputStream.write(b);
        }

        @Override
        public void write(byte @NotNull [] b) throws IOException {
            gzipOutputStream.write(b);
        }

        @Override
        public void write(byte @NotNull []  b, int off, int len) throws IOException {
            gzipOutputStream.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            gzipOutputStream.flush();
        }

        @Override
        public void close() throws IOException {
            gzipOutputStream.close();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            throw new UnsupportedOperationException();
        }
    }
}
