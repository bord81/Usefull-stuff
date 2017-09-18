//A simple daemon for client apps testing which listens to HTTP port, 
//returns simple message and logs processing status and the incoming request headers to stdout 

import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class NetTest {

    public static void main(String[] args) throws Throwable {
        ServerSocket ss = new ServerSocket(8088);
        while (true) {
            Socket s = ss.accept();
            System.err.println("Client accepted");
            new Thread(new SocketProcessor(s)).start();
        }
    }

    private static class SocketProcessor implements Runnable {

        private StringBuffer showResp = new StringBuffer();
        private Socket s;
        private InputStream is;
        private OutputStream os;

        private SocketProcessor(Socket s) throws Throwable {
            this.s = s;
            this.is = s.getInputStream();
            this.os = s.getOutputStream();
        }

        public void run() {
            try {
                readInputHeaders();

                writeResponse("<html><body><h1>Hello from TestServer</h1></body></html>");
            } catch (Throwable t) {
            } finally {
                try {
                    s.close();
                } catch (Throwable t) {
                }
            }
            System.err.println("Client processing finished");
        }

        private void writeResponse(String s) throws Throwable {
            String response = "HTTP/1.1 200 OK\r\n"
                    + "Server: NetTestDaemon\r\n"
                    + "Content-Type: text/html\r\n"
                    + "Content-Length: " + s.length() + "\r\n"
                    + "Connection: close\r\n\r\n";
            String result = response + s;
            os.write(result.getBytes());
            os.flush();
        }

        private void readInputHeaders() throws Throwable {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String check = br.readLine();
                if (check == null || check.trim().length() == 0) {
                    System.err.println(showResp);
                    break;
                } else {
                    showResp.append(check);
                }
            }
        }
    }
}
