
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TCPClient1 {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 6790;
        try (Socket sock = new Socket(host, port);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader stdin = new BufferedReader(
                     new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            System.out.println("[CLIENT] Connected to " + host + ":" + port);
            System.out.println("Usage: CALC <ADD|SUB|MUL|DIV> <A> <B>");
            System.out.println("Type QUIT to exit.");
            System.out.print("Input> ");
            String line;
            while ((line = stdin.readLine()) != null) {
                line = line.trim();
                out.println(line);
                String resp = in.readLine();
                if (resp == null) {
                    System.out.println("[CLIENT] Server closed.");
                    break;
                }
                System.out.println(resp);
                if (line.equalsIgnoreCase("QUIT")) break;
                System.out.print("Input> ");
            }
        } catch (UnknownHostException e) {
            System.out.println("[CLIENT] Unknown host: " + host);
        } catch (ConnectException e) {
            System.out.println("[CLIENT] Connection refused (server off?)");
        } catch (IOException e) {
            System.out.println("[CLIENT] I/O error: " + e.getMessage());
        }
    }
}
