package io;

import javafx.application.Platform;
import javafx.scene.control.TextInputControl;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


public class CustomOutputStream extends OutputStream {

    private final TextInputControl textInputControl;
    private final CharsetDecoder decoder;
    private ByteArrayOutputStream buf;


    private Charset charset = Charset.defaultCharset();

    public CustomOutputStream(TextInputControl textInputControl) {
        this.textInputControl = textInputControl;
        this.decoder = charset.newDecoder();
    }

    @Override
    public synchronized void write(int b) throws IOException {
        synchronized (this) {
            if (this.buf == null) {
                this.buf = new ByteArrayOutputStream();
            }
            this.buf.write(b);
        }
    }

    @Override
    public void flush() throws IOException {
        Platform.runLater(() -> {
            try {
                flushImpl();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void flushImpl() throws IOException {
        synchronized (this) {
            if (this.buf == null) {
                return;
            }
            final ByteBuffer byteBuffer = ByteBuffer.wrap(this.buf.toByteArray());
            final CharBuffer charBuffer = this.decoder.decode(byteBuffer);
            try {
                this.textInputControl.appendText(charBuffer.toString());
                this.textInputControl.positionCaret(this.textInputControl.getLength());
            } finally {
                this.buf = null;
            }
        }
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    public void clear() throws IOException {
        this.buf = null;
    }

    public Charset getCharset() {
        return charset;
    }
}
