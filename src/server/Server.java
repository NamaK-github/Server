package server;


import exceptions.AlreadyExistExeption;
import filters.ChatFilter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by NamaK on 27.02.17.
 */
public class Server {
    private List<ClientHandler> clients;
    private List<ChatFilter> filters;
    private ServerSocket serverSocket = null;
    private Socket socket = null;

    public Server() {
        System.out.println("Инициализация сервера...");
        clients = new LinkedList<>();
        filters = new ArrayList<>();
        int serverPort = 9999;
        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Инициализация серверного сокета завершена.");
            SQLHandler.connect();
            System.out.println("Подключение к базе данных завершено.");
            System.out.println("Сервер готов.");
        } catch (IOException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                waitClients();
            }
        }).start();
    }

    public synchronized void addClient(ClientHandler clientHandler, String nick) throws AlreadyExistExeption {
        for (ClientHandler client : clients) {
            if (client.getClientName().equals(nick)) {
                System.out.println("Клиент с ником  " + nick + " уже подключен!");
                throw new AlreadyExistExeption();
            }
        }
        clientHandler.setNick(nick);
        clients.add(clientHandler);
        System.out.println(clientHandler.getClientName() + " вошёл в чат.");
    }

    public synchronized void removeClient(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    private void waitClients() {
        System.out.println("Ожидание поключения пользователей...");
        try {
            while (true) {
                socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket, this);
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void newMessageFromClient(String message, String clientName) {
        for (ChatFilter filter : filters) {
            message = filter.filter(message);
        }
        for (ClientHandler client : clients) {
            try {
                client.getOut().writeUTF(clientName + ": " + message);
                client.getOut().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void serverClose() {
        try {
            if (serverSocket != null) serverSocket.close();
            System.out.println("Сервер отключён.");
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void systemMessageToClient(ClientHandler client, String message) {
        try {
            client.getOut().writeUTF(message);
            client.getOut().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
