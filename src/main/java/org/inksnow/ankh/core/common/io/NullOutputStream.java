package org.inksnow.ankh.core.common.io;

import java.io.IOException;
import java.io.OutputStream;

public class NullOutputStream extends OutputStream {
  private volatile boolean closed;

  private void ensureOpen() throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
  }

  @Override
  public void write(int b) throws IOException {
    ensureOpen();
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if ((b.length | off | len) < 0 || len > b.length - off) {
      throw new IllegalArgumentException();
    }
    ensureOpen();
  }

  @Override
  public void close() {
    closed = true;
  }
}
