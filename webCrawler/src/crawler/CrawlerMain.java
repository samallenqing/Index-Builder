package crawler;

import ad.Ad;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeoutException;


public class CrawlerMain {
    private final static String IN_QUEUE_NAME = "q_feeds";
    private final static String OUT_QUEUE_NAME = "q_product";
    private final static String ERR_QUEUE_NAME = "q_error";

    private static AmazonCrawler crawler;
    private static ObjectMapper mapper;
    private static Channel outChannel;
    private static Channel errChannel;
    private static IndexBuilder indexBuilder;

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {

        args = new String[]{"proxylist.txt","logFile.txt"};

        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream("config.properties");
        properties.load(inputStream);
        String mysqlPass = properties.getProperty("mysqlPass");
        String adsDataFilePath = properties.getProperty("adsDataFilePath");
        String proxyFilePath = args[0];
        String logFilePath = args[1];

        AmazonCrawler crawler = new AmazonCrawler(proxyFilePath, logFilePath);
        int[] num = new int[]{crawler.id}; //Setting adId start from 2000

        File file = new File(adsDataFilePath);
        //if file does not exist, create one
        if(!file.exists()) {
            file.createNewFile(); //here requests adding IoException
        }

        mapper = new ObjectMapper();

        String memcachedServer = "127.0.0.1";
        int memcachedPortal = 11211;
        String mysqlDb = "AmazonAd";
        String mysqlUser = "root";
        String mysqlHost = "127.0.0.1:3306";


        inputStream.close();

        indexBuilder = new IndexBuilder(memcachedServer, memcachedPortal, mysqlHost, mysqlDb, mysqlUser, mysqlPass);


        Set<String> querySet = new HashSet<String>();
        StringFilter tool = new StringFilter();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel inChannel = connection.createChannel();
        inChannel.queueDeclare(IN_QUEUE_NAME, true, false, false, null);
        inChannel.basicQos(10); // Per consumer limit
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        outChannel = connection.createChannel();
        outChannel.queueDeclare(OUT_QUEUE_NAME, true, false, false, null);
        errChannel = connection.createChannel();
        errChannel.queueDeclare(ERR_QUEUE_NAME, true, false, false, null);
        Consumer consumer = new DefaultConsumer(inChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                try {
                    FileWriter fw = new FileWriter(file.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                    String[] fields = message.split(",");
                    String query = fields[0].trim();
                    double bidPrice = Double.parseDouble(fields[1].trim());
                    int campaignId = Integer.parseInt(fields[2].trim());
                    int queryGroupId = Integer.parseInt(fields[3].trim());
                    querySet.add(query);
                    List<Ad> ads = crawler.GetAdBasicInfoByQuery(query, bidPrice, campaignId, queryGroupId);
                    for (Ad ad : ads) {
                        ad.adId = num[0]++;
                        String jsonInString = mapper.writeValueAsString(ad);
                        bw.write(jsonInString);
                        bw.newLine();
                        System.out.println("The ad is  " + jsonInString);
                        outChannel.basicPublish("", OUT_QUEUE_NAME, null, jsonInString.getBytes("UTF-8"));
                    }

                    List<String> N_gram_query = tool.ngramQuery(query);
                    for(String str : N_gram_query) {
                        if(!querySet.contains(str)) {
                            querySet.add(str);
                            List<Ad> newAds = crawler.GetAdBasicInfoByQuery(str, bidPrice, campaignId, queryGroupId);
                            for(Ad newAd : newAds) {
                                newAd.adId = num[0]++;
                                String jsonInString = mapper.writeValueAsString(newAd);
                                bw.write(jsonInString);
                                bw.newLine();
                            }
                        }
                    }
                    bw.close();
                    indexBuilder.init(adsDataFilePath);
                    Thread.sleep(2000);

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        inChannel.basicConsume(IN_QUEUE_NAME, true, consumer);

    }
}
