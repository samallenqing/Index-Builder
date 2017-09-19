package feederAndConsumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.*;
import java.util.Properties;

public class feeder {
    private final static String QUEUE_NAME = "q_feeds";

    public static void main(String[] args) throws Exception {

        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream("config.properties");
        properties.load(inputStream);
        String rawQueryDataFilePath = properties.getProperty("rawQueryDataFilePath");
        inputStream.close();
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
