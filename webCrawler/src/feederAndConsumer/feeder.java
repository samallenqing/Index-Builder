package feederAndConsumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class feeder {
    private final static String QUEUE_NAME = "q_feeds";

    public static void main(String[] args) throws Exception {
        String rawQueryDataFilePath = "rawQuery.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(rawQueryDataFilePath))) {

            String line;
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("127.0.0.1");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            while ((line = br.readLine()) != null) {
                if (line.isEmpty())
                    continue;
                System.out.println(" [x] Sent '" + line + "'");
                channel.basicPublish("", QUEUE_NAME, null, line.getBytes("UTF-8"));
            }
            channel.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
