/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package kafka.security;

import java.util.Set;
import java.net.SocketOption;
import java.net.SocketAddress;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * A secure server socket channel implementation enclosing accepted non-secure
 * <code>SocketChannels</code> into <code>SSLSocketChannels</code>. The
 * accepted channels must be provided by an adapted non-secure concrete
 * <code>ServerSocketChannel</code> implementation.
 * 
 * <p>
 * This implementation extends abstract <code>ServerSocketChannel</code> and
 * forwards applicable calls to methods of the adapted concrete implementation.
 * It also implements <code>AdaptableChannel</code> as selectors typically
 * don't accept channel implementations from other vendors, so the selector
 * registration must be done with the adaptee channel.
 * </p>
 * 
 * @author Ilkka Priha
 */
public class SSLServerSocketChannel extends ServerSocketChannel implements
    AdaptableChannel
{
    /**
     * The unsecure server socket channel.
     */
    private final ServerSocketChannel socketChannel;

    /**
     * The SSL context to apply.
     */
    private final SSLContext sslContext;

    /**
     * The want authentication option.
     */
    private boolean wantClientAuth;

    /**
     * The need authentication option.
     */
    private boolean needClientAuth;

    /**
     * Construct a new channel.
     * 
     * @param channel the unsecure socket channel.
     * @param context the SSL context.
     */
    public SSLServerSocketChannel(ServerSocketChannel channel,
        SSLContext context)
    {
        super(channel.provider());
        System.out.println("Called super");
        if (context == null)
        {
            throw new NullPointerException("SSLContext context");
        }

        socketChannel = channel;
        sslContext = context;
    }

    public ServerSocket socket()
    {
        return socketChannel.socket();
    }

    public SocketChannel accept() throws IOException
    {
        SocketChannel channel = socketChannel.accept();
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        if (getWantClientAuth())
        {
            engine.setWantClientAuth(true);
        }
        if (getNeedClientAuth())
        {
            engine.setNeedClientAuth(true);
        }
        return new SSLSocketChannel(channel, engine);
    }

    public Channel getAdapteeChannel()
    {
        return socketChannel;
    }

    public String toString()
    {
        return "SSLServerSocketChannel[" + socket().toString() + "]";
    }

    /**
     * Checks whether client authentication is wanted.
     * 
     * @return true for client authentication, false otherwise.
     */
    public boolean getWantClientAuth()
    {
        return wantClientAuth;
    }

    /**
     * Sets whether client authentication is wanted.
     * 
     * @param flag true for client authentication, false otherwise.
     */
    public void setWantClientAuth(boolean flag)
    {
        wantClientAuth = flag;
    }

    /**
     * Checks whether client authentication is needed.
     * 
     * @return true for client authentication, false otherwise.
     */
    public boolean getNeedClientAuth()
    {
        return needClientAuth;
    }

    /**
     * Sets whether client authentication is needed.
     * 
     * @param flag true for client authentication, false otherwise.
     */
    public void setNeedClientAuth(boolean flag)
    {
        needClientAuth = flag;
    }

    protected void implCloseSelectableChannel() throws IOException
    {
        socketChannel.close();
    }

    protected void implConfigureBlocking(boolean block) throws IOException
    {
        socketChannel.configureBlocking(block);
    }

    public <T> ServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException{
        return socketChannel.setOption(name, value);
    }
    public ServerSocketChannel	bind(SocketAddress local, int backlog) throws IOException{
        return socketChannel.bind(local, backlog);
    }
    public <T> T	getOption(SocketOption<T> name) throws IOException{
        return socketChannel.getOption(name);
    }
    public SocketAddress getLocalAddress() throws IOException{
        return socketChannel.getLocalAddress();
    }
    public Set<SocketOption<?>>	supportedOptions() {
        return socketChannel.supportedOptions();
    }
}
