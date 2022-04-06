package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message){
        for (Connection connection:connectionMap.values()) {
            try {
                connection.send(message);
            }catch (IOException e){
                ConsoleHelper.writeMessage("Мы не смогли доставить сообщение " + connection.getRemoteSocketAddress());
            }
        }
    }


    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите порт сервера:");
        int serverPort = ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Произошла ошибка подключения!!!");

        }


    }

    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            while (true){
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                if(message.getType() == MessageType.USER_NAME &&
                                !message.getData().isEmpty() && !connectionMap.containsKey(message.getData())){
                    connectionMap.put(message.getData(), connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    return message.getData();
                }

            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException{
            for (String str:connectionMap.keySet()) {
                if (str != userName){
                    connection.send(new Message(MessageType.USER_ADDED,str));
                }

            }

        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if (message.getType() != MessageType.TEXT){
                    ConsoleHelper.writeMessage("Ошибка!!! Принятое сообщение не является текстом!");
                    continue;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(userName).append(":").append(" ").append(message.getData());
                sendBroadcastMessage(new Message(MessageType.TEXT, sb.toString()));
            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом " + socket.getRemoteSocketAddress());
            try {
                Connection connection = new Connection(socket);
                String str = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, str));
                notifyUsers(connection,str);
                serverMainLoop(connection,str);
                connectionMap.remove(str);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, str));
                ConsoleHelper.writeMessage("Cоединение с удаленным адресом закрыто");

            } catch (IOException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом!!!");
            } catch (ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом!!!");
            }
        }
    }

}
