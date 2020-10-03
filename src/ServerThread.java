import java.io.*;
import java.net.*;
import java.util.*;

public class ServerThread extends Thread {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<BookEntry> bookEntries;

    public ServerThread(String name, Socket socket, ArrayList<BookEntry> bookEntries) {
        super(name);
        this.socket = socket;
        this.bookEntries = bookEntries;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            listen();
            disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String processData(String[] data) {
        final String request = data[0].trim();
        switch (request) {
            case "SUBMIT":
                return handleSubmit(data);
            case "GET":
                return handleGet(data);
            case "UPDATE":
                return handleUpdate(data);
            case "REMOVE":
                return handleRemove(data);
            default:
                return "ERROR: Invalid request please do one of [SUBMIT, GET, UPDATE, REMOVE]";
        }
    }


    private String handleSubmit(String[] data) {
        String message = "";
        BookEntry bookEntry = new BookEntry();
        for (String line : data) {
            line = line.trim();
            String[] words = line.split(" ");
            String value;
            switch (words[0]) {
                case "ISBN":
                    bookEntry.setISBN(words[1]);
                    break;
                case "TITLE":
                    value = line.substring(words[0].length()).trim();
                    bookEntry.setTITLE(value);
                    break;
                case "AUTHOR":
                    value = line.substring(words[0].length()).trim();
                    bookEntry.setAUTHOR(value);
                    break;
                case "PUBLISHER":
                    value = line.substring(words[0].length()).trim();
                    bookEntry.setPUBLISHER(value);
                    break;
                case "YEAR":
                    bookEntry.setYEAR(Integer.parseInt(words[1]));
                    break;
                default:
                    break;
            }
        }
        message = "-----Successfully added-----\n" + bookEntry.toString();
        bookEntries.add(bookEntry);
        return message;
    }

    private String handleGet(String[] data) {
        String message = "";
        return message;
    }

    private String handleUpdate(String[] data) {
        String message;
        ArrayList<BookEntry> foundBooks;
        BookEntry foundBook = null;
        outer:
        for (String line : data) {
            line = line.trim();
            String[] words = line.split(" ");
            String value = line.substring(words[0].length()).trim();
            switch (words[0]) {
                case "ISBN":
                    foundBooks = Util.findByAttribute(bookEntries, "ISBN", value);
                    if (foundBooks.size() == 1) {
                        foundBook = foundBooks.get(0);
                    } else {
                        break outer;
                    }
                    break;
                case "TITLE":
                    if (foundBook != null)
                        foundBook.setTITLE(value);
                    break;
                case "AUTHOR":
                    if (foundBook != null)
                        foundBook.setAUTHOR(value);

                    break;
                case "PUBLISHER":
                    if (foundBook != null)
                        foundBook.setPUBLISHER(value);
                    break;
                case "YEAR":
                    if (foundBook != null)
                        foundBook.setYEAR(Integer.parseInt(value));
                    break;
            }
        }
        if (foundBook != null)
            message = "-----Successfully updated-----\n" + foundBook.toString();
        else
            message = "The book does not exist";
        return message;
    }

    private String handleRemove(String[] data) {
        String message = "";
        return message;
    }


    private void listen() {
        String line, inMessage, outMessage;
        try {
            line = in.readLine();
            while (line != null) {
                inMessage = "";
                if (line.equals("ping")) {
                    outMessage = "pong";
                } else {
                    /* READ DATA START */
                    while (!line.contains("\\EOF")) {
                        inMessage = inMessage.concat(line + "\r\n");
                        line = in.readLine();
                    }
                    /* READ DATA END */

                    /* PROCESS DATA START*/
                    outMessage = processData(inMessage.split("\n")).trim() + "\r\n\\EOF";
                }
                out.println(outMessage);
                line = in.readLine();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void disconnect() throws IOException {
        out.close();
        in.close();
        socket.close();
        System.out.println("Server thread [" + getName() + "] disconnected.");
        System.out.println("Active connections: " + (Thread.activeCount() - 2));
        this.interrupt();
    }
}
