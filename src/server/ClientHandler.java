package server;

import exceptions.AuthFailException;
import exceptions.RegFailExeption;
import exceptions.WrongDataExeption;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

/**
 * Created by NamaK on 27.02.17.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private DataOutputStream out;
    private DataInputStream in;
    private static int clientsCount = 0;
    private String clientName;
    private String message = null;
    private boolean ready = false;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            clientsCount++;
            clientName = "ClientChat #" + clientsCount;
            System.out.println("Новый пользователь " + clientName + " подключен.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        waitAuth();
        if (ready) waitMessage();
    }

    private void waitAuth() {
        while (true) {
            try {
                message = in.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message.equals("end___session___client")) {
                System.out.println(clientName + ": вышел из чата.");
                server.removeClient(this);
                break;
            } else if(message.contains("reg___")){
                String[] parsedMessage = message.split("___");
                if (parsedMessage.length == 4 && parsedMessage[0].equals("reg")) {
                    try {
                        processReg(parsedMessage);
                    } catch (RegFailExeption e) {
                        server.systemMessageToClient(this, "reg___login___error");
                    }
                }
            } else {
                try {
                    if (isAuthOK(message)) {
                        out.writeUTF("signin___successfull");
                        out.flush();
                        ready = true;
                        server.newMessageFromClient(" вошёл в чат.", clientName);
                        break;
                    } else {
                        out.writeUTF("signin___fail");
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isAuthOK(String message) {
        System.out.println(clientName + "[NO AUTH]: " + message);
        if (message != null) {
            String[] parsedMessage = message.split("___");
            if (parsedMessage.length == 3 && parsedMessage[0].equals("auth")) {
                try {
                    processAuth(parsedMessage);
                    return true;
                } catch (AuthFailException e) {
                    return false;
                }
            }
        }
        return false;
    }

    private void processAuth(String[] parsedMessage) throws AuthFailException {
        System.out.println("Запрос авторизации от  " + clientName);
        String login = parsedMessage[1];
        String password = parsedMessage[2];
        String nick = null;
        try {
            nick = SQLHandler.getNick(login, password);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new WrongDataExeption();
        }
        if (nick != null) {
            server.addClient(this, nick);
            return;
        }
        throw new WrongDataExeption();
    }

    private void waitMessage() {
        while (true) {
            try {
                message = in.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (message.equals("end___session___client")) {
                System.out.println(clientName + ": вышел из чата.");
                server.newMessageFromClient(": вышел из чата.", clientName);
                server.removeClient(this);
                break;
            } else if (message.contains("reg___")){
                String[] parsedMessage = message.split("___");
                if (parsedMessage.length == 4 && parsedMessage[0].equals("reg")) {
                    try {
                        processReg(parsedMessage);
                    } catch (RegFailExeption e) {
                        System.out.println("Неудачная регистрация.");
                        server.systemMessageToClient(this, "reg___login___error");
                    }
                }
            } else {
                System.out.println(clientName + ": " + message);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        server.newMessageFromClient(message, clientName);
                    }
                }).start();
            }
        }
    }

    private void processReg(String[] parsedMessage) throws RegFailExeption{
        System.out.println("Регистрация от " + clientName);
        String login = parsedMessage[1];
        String password = parsedMessage[2];
        String nick = parsedMessage[3];
        try {
            SQLHandler.makeRegistration(login, password, nick);
        } catch (SQLException e) {
        }
    }

    public DataOutputStream getOut() {
        return out;
    }

    public String getClientName() {
        return clientName;
    }

    public void setNick(String nick) {
        this.clientName = nick;
    }
}
