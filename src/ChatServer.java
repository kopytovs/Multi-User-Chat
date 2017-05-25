/**
 * Created by Kopytov on 25.05.17.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatServer {

    private static final int PORT = 7774;

    private static HashSet<String> names = new HashSet<String>();

    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public static void main(String[] args) throws Exception {
        System.out.println("Сервер заработал");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                String history = "";

                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            for (PrintWriter writer : writers) {
                                Date now = new Date();
                                DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                                String time = formatter.format(now);
                                writer.println("MESSAGE " + name + "<" + time + ">" + ": " + "Зашел(а) в чат.");
                            }
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED");
                writers.add(out);

                //if (in.readLine().startsWith("NEEDHISTORY")){
                //    writers
                //}

                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    Date now = new Date();
                    DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    String time = formatter.format(now);
                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + "<" + time + ">" + ": " + input);
                        //history += name + "<" + time + ">" + ": " + input;
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {

                if (name != null) {
                    for (PrintWriter writer : writers) {
                        Date now = new Date();
                        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                        String time = formatter.format(now);
                        writer.println("MESSAGE " + name + "<" + time + ">" + ": " + "Вышел(ла) из чата");
                    }
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
