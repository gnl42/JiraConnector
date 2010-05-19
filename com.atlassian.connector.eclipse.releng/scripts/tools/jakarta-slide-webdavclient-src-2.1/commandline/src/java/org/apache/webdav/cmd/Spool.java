/*
 * $Header: /home/cvs/jakarta-slide/webdavclient/commandline/src/java/org/apache/webdav/cmd/Spool.java,v 1.3 2004/07/28 09:30:33 ib Exp $
 * $Revision: 1.3 $
 * $Date: 2004/07/28 09:30:33 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.webdav.cmd;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class provides wrappers around an InputStream and
 * an OutputStream allowing you to spool both streams to a
 * shared OutputStream.
 *
 */
public class Spool
{
    private InputStream  in   = System.in;
    private OutputStream out  = System.out;
    private OutputStream spool= null;

    private InputStream  wrappedInputStream;
    private OutputStream wrappedOutputStream;

    private boolean echo=false;

    /**
     * Create a spool for these two streams
     */
    public Spool(InputStream in, OutputStream out)
    {
        this();
        this.in=in;
        this.out=out;
    }

    /**
     * Create a spool for System.in and System.out
     */
    public Spool()
    {
        wrappedInputStream = new InputStream()
        {
            public int read(byte b[]) throws IOException {
                int bytesRead = in.read(b);
                if (echo)
                    out.write(b,0,bytesRead);
                if (spool!=null)
                    spool.write(b,0,bytesRead);
                return bytesRead;
            }
            public int read(byte b[], int off, int len) throws IOException {
                int bytesRead = in.read(b,off,len);
                if (echo)
                    out.write(b,off,bytesRead);
                if (spool!=null)
                    spool.write(b,off,bytesRead);
                return bytesRead;
            }
            public int read() throws IOException {
                int nextByte = in.read();
                if ((nextByte!=-1) && (echo))
                    out.write(nextByte);
                if ((nextByte!=-1) && (spool!=null))
                    spool.write(nextByte);
                return nextByte;
            }
            public long skip(long n) throws IOException {
                return in.skip(n);
            }
            public int available() throws IOException {
                return in.available();
            }
            public void close() throws IOException {
                in.close();
            }
            public synchronized void mark(int readlimit) {
                in.mark(readlimit);
            }
            public synchronized void reset() throws IOException {
                in.reset();
            }
            public boolean markSupported() {
                return in.markSupported();
            }
        };
        wrappedOutputStream = new OutputStream()
        {
            public void write(int b) throws IOException {
                out.write(b);
                if (spool!=null)
                    spool.write(b);
            }
            public void write(byte b[]) throws IOException {
                out.write(b);
                if (spool!=null)
                    spool.write(b);
            }
            public void write(byte b[], int off, int len) throws IOException {
                out.write(b,off,len);
                if (spool!=null)
                    spool.write(b,off,len);
            }
            public void flush() throws IOException {
                out.flush();
                if (spool!=null)
                    spool.flush();
            }
            public void close() throws IOException {
                out.close();
            }
        };
    }

    /**
     * Echo the input stream to the output stream.
     */
    public void setEcho(boolean isEnabled)
    {
        this.echo=isEnabled;
    }

    /**
     * Enable spooling and spool to the given filename.
     */
    public void enable(String filename) throws FileNotFoundException
    {
        enable(new FileOutputStream(filename));
    }

    /**
     * Enable Spooling and spool to the given OutputStream.
     */
    public void enable(OutputStream spool)
    {
        if (isEnabled())
            disable();
        this.spool = spool;
    }

    /**
     * Disable spooling
     */
    public void disable()
    {
        try
        {
            if (spool!=null)
                spool.close();
        }
        catch (IOException ex)
        {
        }
        spool=null;
    }

    /**
     * Returns if spooling is enabled.
     */
    public boolean isEnabled()
    {
        return (spool!=null);
    }

    /**
     * Returns an InputStream wrapped around the real InputStream.
     * Use this stream to read from.
     */
    public InputStream getInputStream()
    {
        return wrappedInputStream;
    }

    /**
     * Returns an OutputStream wrapped around the real OutputStream.
     * Use this stream to write to.
     */
    public OutputStream getOutputStream()
    {
        return wrappedOutputStream;
    }
}
