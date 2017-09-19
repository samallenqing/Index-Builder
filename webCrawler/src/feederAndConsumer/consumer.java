package feederAndConsumer;


import com.rabbitmq.client.*;

import java.io.IOException;

public class consumer {
    private static String queue = "q_test";


    public static void main(String[] args) throws Exception {
        // write your code here
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel inChannel = connection.createChannel();
        inChannel.basicQos(1); // Per consumer limit
        inChannel.exchangeDeclare("e_topic_demo", "topic", true);
        inChannel.queueBind("q_feeds", "e_topic_demo", "*.error");


        //inChannel.queueDeclare(queue, true, false, false, null);
        Consumer consumer = new DefaultConsumer(inChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + envelope.getRoutingKey() + ":" + message + "'");
            }
        };
        inChannel.basicConsume("q_feeds", true, consumer);
    }
}
