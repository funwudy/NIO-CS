package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class NServer {

	public static final int PORT = 22222;

	private Selector selector = null;

	private Charset charset = Charset.forName("UTF-8");

	public void init() throws IOException {
		selector = Selector.open();
		ServerSocketChannel server = ServerSocketChannel.open();
		InetSocketAddress isa = new InetSocketAddress("127.0.0.1", PORT);
		server.bind(isa);
		server.configureBlocking(false);
		server.register(selector, SelectionKey.OP_ACCEPT);
		while (selector.select() > 0) {
			for (SelectionKey key : selector.selectedKeys()) {
				selector.selectedKeys().remove(key);
				if (key.isAcceptable()) {
					SocketChannel sc = server.accept();
					sc.configureBlocking(false);
					sc.register(selector, SelectionKey.OP_READ);
					System.out.println("A new client(" + sc.getRemoteAddress() + ") has come");
					System.out.println("getLocalSocketAddress: " + sc.socket().getLocalSocketAddress());
					System.out.println("getRemoteSocketAddress: " + sc.socket().getRemoteSocketAddress());
					key.interestOps(SelectionKey.OP_ACCEPT);
				}
				if (key.isReadable()) {
					SocketChannel sc = (SocketChannel) key.channel();
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					StringBuilder sb = new StringBuilder();
					try {
						while (sc.read(buffer) > 0) {
							buffer.flip();
							sb.append(charset.decode(buffer));
						}
						System.out.println("New message from " + sc.getRemoteAddress() 
								+ ": \n" + sb);
						key.interestOps(SelectionKey.OP_READ);
					} catch (IOException e) {
						key.cancel();
						if (key.channel() != null) {
							key.channel().close();
							System.out.println("Close connection to " + sc.getRemoteAddress());
						}
					}
					if (sb.length() > 0) {
						for (SelectionKey sk : selector.keys()) {
							if (sk != key) {
								Channel channel = sk.channel();
								if (channel instanceof SocketChannel) {
									SocketChannel dest = (SocketChannel) channel;
									System.out.println("Send message to " + dest.getRemoteAddress());
									dest.write(charset.encode(sb.toString()));
								}
							}
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		new NServer().init();
	}

}
