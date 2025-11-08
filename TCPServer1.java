
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class TCPServer1 {
    public static void main(String[] args) {
        int port = 6790;
        int poolSize = 8;
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[SERVER] Listening on " + port);
            ExecutorService pool = Executors.newFixedThreadPool(poolSize);
            while (true) {
                try {
                    Socket s = server.accept();
                    pool.execute(new ClientTask(s));
                } catch (IOException e) {
                    System.out.println("[SERVER] accept error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("[SERVER] FAIL to open port: " + e.getMessage());
        }
    }

    static class ClientTask implements Runnable {
        private final Socket socket;
        ClientTask(Socket s) { this.socket = s; }

        @Override public void run() {
            String who = socket.getRemoteSocketAddress().toString();
            System.out.println("[SERVER] Connected: " + who);
            try (Socket s = socket;
                 BufferedReader in = new BufferedReader(
                        new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.equalsIgnoreCase("QUIT")) { out.println("BYE"); break; }
                    String resp = handle(line);
                    out.println(resp);
                }
            } catch (IOException e) {
                System.out.println("[SERVER] I/O error: " + e.getMessage());
            } finally {
                System.out.println("[SERVER] Disconnected: " + who);
            }
        }

        private String handle(String msg) {
            try {
                String[] t = msg.split("\\s+");
                if (t.length != 4 || !t[0].equalsIgnoreCase("CALC")) return "ERROR code=ARG_NUM";
                String op = t[1].toUpperCase();
                double a = Double.parseDouble(t[2]);
                double b = Double.parseDouble(t[3]);
                double res;
                switch (op) {
                    case "ADD": res = a + b; break;
                    case "SUB": res = a - b; break;
                    case "MUL": res = a * b; break;
                    case "DIV": if (b == 0.0) return "ERROR code=DIV_BY_ZERO"; res = a / b; break;
                    default: return "ERROR code=INVALID_OP";
                }
                if (Double.isNaN(res) || Double.isInfinite(res)) return "ERROR code=NAN_OR_INF";
                return "RESULT value=" + res;
            } catch (NumberFormatException e) {
                return "ERROR code=PARSE_ERROR";
            } catch (Exception e) {
                return "ERROR code=UNKNOWN";
            }
        }
    }
}
