package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NClient {
	
	public static final int PORT = 22222;
	
	private Selector selector = null;
	
	private Charset charset = Charset.forName("UTF-8");
	
	private SocketChannel sc = null;
	
	public void init() throws IOException {
		selector = Selector.open();
		InetSocketAddress isa = new InetSocketAddress("127.0.0.1", PORT);
		sc = SocketChannel.open(isa);
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ);
		new ClientThread().start();
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			sc.write(charset.encode(line));
		}
	}
	
	private class ClientThread extends Thread {
		public void run() {
			try {
				while (selector.select() > 0) {
					for (SelectionKey key : selector.selectedKeys()) {
						selector.selectedKeys().remove(key);
						if (key.isReadable()) {
							SocketChannel sc = (SocketChannel) key.channel();
							ByteBuffer buffer = ByteBuffer.allocate(1024);
							String content = "";
							while (sc.read(buffer) > 0) {
								buffer.flip();
								content += charset.decode(buffer);
							}
							System.out.println("¡ƒÃÏ–≈œ¢£∫" + content);
							key.interestOps(SelectionKey.OP_READ);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();// TODO: handle exception
			}
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		new NClient().init();
	}

}
