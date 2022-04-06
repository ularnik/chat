package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client{
    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int)(Math.random()*100);
    }

    public class BotSocketThread extends Client.SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            String[] array = message.split(": ");
            if (array.length!=2){
                return;
            }
            String name = array[0];
            String text = array[1];
            String format = null;

            switch (text){
                case "дата":
                    format = "d.MM.YYYY";
                    break;

                case "день":
                    format = "d";
                    break;


                case "месяц":
                    format = "MMMM";
                    break;

                case "год":
                    format = "YYYY";
                    break;

                case "время":
                    format = "H:mm:ss";
                    break;

                case "час":
                    format = "H";
                    break;

                case "минуты":
                    format = "m";
                    break;

                case "секунды":
                    format = "s";
                    break;
            }

            if (format!=null){
                String data = new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
                BotClient.this.sendTextMessage("Информация для " + name + ": " + data);
            }


        }
    }
}
