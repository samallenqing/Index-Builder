package crawler;

import ad.Ad;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;

public class AmazonCrawler {
    //https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=nikon+SLR&page=2
    private static final String AMAZON_QUERY_URL = "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";
    private final String authUser = "bittiger";
    private final String authPassword = "cs504";

    private List<String> proxyList;
    private List<String> titleList;
    private List<String> priceList;
    private List<String> brandList;
    private List<String> categoryList;
    private HashSet crawledUrl;

    BufferedWriter logBFWriter;

    private int index = 0;
    public int id;

    //Constructor
    public AmazonCrawler(String proxy_file, String log_file) throws IOException {
        id = 2000;

        crawledUrl = new HashSet();

        initProxyList(proxy_file);

        initHtmlSelector();


        initLog(log_file);
    }

    private void initProxyList(String proxy_file) throws IOException {
        proxyList = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(proxy_file));
            //needs to handle FileNotFoundException
            String line;
            while((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String ip = fields[0].trim();
                proxyList.add(ip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );

        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
        System.setProperty("socksProxyPort", "61336"); // set proxy port
    }

    private void initHtmlSelector() {
        titleList = new ArrayList<String>();
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > a");
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a");

        priceList = new ArrayList<String>();
        for(int i = 2 ; i <= 4; i++) {
            priceList.add(" > div:nth-child(" + Integer.toString(i) + ") > div.a-column.a-span7 > div > a > span");
            priceList.add(" > div:nth-child(" + Integer.toString(i) + ") > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span");
            priceList.add(" > div:nth-child(" + Integer.toString(i) + ") > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span");
            priceList.add(" > div:nth-child(" + Integer.toString(i) + ") > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span");
            priceList.add(" > div:nth-child(" + Integer.toString(i) + ") > div.a-column.a-span7 > div:nth-child(2) > a > span.a-color-base.sx-zero-spacing");
            priceList.add(" > div:nth-child(" + Integer.toString(i) + ") > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing");
        }

        brandList = new ArrayList<String>();
        brandList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div > span:nth-child(2)");
        brandList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(2) > span:nth-child(2)");
        brandList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(2) > span:nth-child(2) > a");

        categoryList = new ArrayList<String>();
        categoryList.add("#refinements > div.categoryRefinementsSection > ul.forExpando > li > a > span.boldRefinementLink");
        categoryList.add("#refinements > div.categoryRefinementsSection > ul.forExpando > li:nth-child(1) > a > span.boldRefinementLink");

    }

    private void initLog(String log_path) {
        try {
            File log = new File(log_path);
            //if log_file doesn't exist, create a new one
            if(!log.exists()) {
                log.createNewFile(); //needs to handle IOException
            }
            FileWriter fw = new FileWriter(log.getAbsoluteFile());
            logBFWriter = new BufferedWriter(fw);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    //Get Ad's basic information by query
    public List<Ad> GetAdBasicInfoByQuery(String query, double bidPrice, int campaignId, int queryGroupId) {
        List<Ad> products = new ArrayList<Ad>();
        int item_num = 0;

        try {
            if(false) {
                //testing proxy
                testProxy();
                return products;
            }

          ///  setProxy();
            //HTTP headers setting
            HashMap<String, String> headers = new HashMap<String, String>();
            //   headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            //   headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
            //   headers.put("Accept-Language", "en-US,en;q=0.8");

            for(int page = 1; page <= 3; page++) {
                System.out.println();
                System.out.println("query = " + query);

                String newQuery = query.replace(" ", "+");
                String url = AMAZON_QUERY_URL + newQuery + "&page=" + Integer.toString(page);  //Request URL
                System.out.println("query_url = " + url);

                Document doc = Jsoup.connect(url).headers(headers).userAgent(USER_AGENT).timeout(1000).get();

                //select a list of items - use attribute data-asin
                Elements results = doc.select("li[data-asin]");
                int resultSize = results.size();
                System.out.println("page = " + page + ", num of results = " + resultSize);
                for(int i = 0; i < resultSize; i++) {
                    System.out.println();

                    Ad ad = new Ad();
                    StringFilter tool = new StringFilter();

                    //Ad - query field
                    ad.query = query;

                    //Ad - query_group_id field
                    ad.query_group_id = queryGroupId;

                    //Ad - campaignId field
                    ad.campaignId = campaignId;

                    //Ad - bidPrice field
                    ad.bidPrice = bidPrice;

                    //Ad - title field
                    List<String> tokens = new ArrayList<String>();
                    for(String title : titleList) {//html selectors for title are stored in titleList
                        String title_ele_path = "#result_" + Integer.toString(i + item_num) + title;
                        Element titleEle = doc.select(title_ele_path).first();
                        if(titleEle != null) {
                            //cleanup
                            String rawTitle = titleEle.text();
                            String cleanedTitle = tool.cleanTokenize(rawTitle, tokens);
                            ad.title = cleanedTitle;

                            //Ad - keywords field (tokenize title)
                            ad.keyWords = tokens;
                            //    break;
                        }
                    }
                    System.out.println("title = " + ad.title);
                    if(ad.title == "") {
                        logBFWriter.write("cannot parse title for query: " + query);
                        logBFWriter.newLine();
                        System.out.println("cannot parse title");
                        continue;
                    }

                    //Ad - thumbnail field (img)
                    String imgPath = "#result_" + Integer.toString(i + item_num) + " > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img";
                    Element imgEle = doc.select(imgPath).first();
                    if(imgEle != null) {
                        String imgUrl = imgEle.attr("src");
                        ad.thumbnail = imgUrl;
                    } else {
                        logBFWriter.write("cannot parse thumbnail for query: " + query + ", title: " + ad.title);
                        logBFWriter.newLine();
                        System.out.println("cannot parse img");
                        continue;
                    }
                    System.out.println("Thumbnail link = " + ad.thumbnail);

                    //Ad - brand
                    for(String brand_format : brandList) {
                        String brand_Path = "#result_" + Integer.toString(i + item_num) +  brand_format;
                        Element brandEle = doc.select(brand_Path).first();
                        if(brandEle != null) {
                            ad.brand = brandEle.text();
                            break;
                        }
                    }
                    System.out.println("brand = " + ad.brand);
                    if(ad.brand == "") {
                        logBFWriter.write("cannot parse brand for query: " + query + ", title: " + ad.title);
                        logBFWriter.newLine();
                        continue;
                    }

                    //Ad - detail_url field
                    String detailUrlPath = "#result_" + Integer.toString(i + item_num) + " > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a";
                    Element detailUrlEle = doc.select(detailUrlPath).first();
                    if(detailUrlEle != null) {
                        String detailUrl = detailUrlEle.attr("href");
                        String normalizedUrl = tool.normalizeUrl(detailUrl);//Normalization of the detail_url
                        System.out.println("detail_url = " + detailUrl);

                        if(crawledUrl.contains(normalizedUrl)) { //Check the crawledUrl set
                            logBFWriter.write("crawled url: " + normalizedUrl);//record the crawled url in the log file
                            logBFWriter.newLine();
                            System.out.println("Have been crawled");
                            continue;
                        }
                        crawledUrl.add(normalizedUrl);
                        System.out.println("normalized_url = " + normalizedUrl);
                        ad.detail_url = normalizedUrl;
                    } else {
                        logBFWriter.write("cannot parse detail url for query: " + query + ", title: " + ad.title);
                        logBFWriter.newLine();
                        System.out.println("cannot parse detail url");
                        continue;
                    }

                    //Ad - category field
                    for(String categoryPath : categoryList) {
                        Element categoryEle = doc.select(categoryPath).first();
                        if(categoryEle != null) {
                            ad.category = categoryEle.text();
                            break;
                        }
                    }
                    System.out.println("category = " + ad.category);
                    if(ad.category == "") {
                        logBFWriter.write("cannot parse category for query: " + query + ", title: " + ad.title);
                        logBFWriter.newLine();
                        continue;
                    }

                    //Ad - price field
                    for(String price_format : priceList) {
                        String pricePath = "#result_" + Integer.toString(i + item_num) + " > div > div > div > div.a-fixed-left-grid-col.a-col-right" + price_format;
                        Element priceEle = doc.select(pricePath).first();
                        if(priceEle != null) {
                            String priceStr = priceEle.attr("aria-label");
                            //remove "$" in price representation
                            if(priceStr.contains("$")) {
                                priceStr = priceStr.replaceAll("\\$", "");
                            }
                            //remove "," in price representation
                            if(priceStr.contains(",")) {
                                priceStr = priceStr.replaceAll(",", "");
                            }

                            try {
                                ad.price = Double.parseDouble(priceStr);
                            } catch (NumberFormatException ne) {
                                ne.printStackTrace();
                                logBFWriter.write("cannot parse price for query: " + query + ", title: " + ad.title);
                                logBFWriter.newLine();
                                System.out.println("cannot parse price");
                                continue;
                            }
                            break;
                        }
                    }
                    System.out.println("price = " + ad.price );
                    if(ad.price == null) {
                        logBFWriter.write("cannot obtain price for query: " + query);
                        logBFWriter.newLine();
                        System.out.println("cannot obtain price from query");
                        continue;
                    }
                    products.add(ad);
                }
                item_num = item_num + resultSize;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    private void testProxy() {
        System.setProperty("socksProxyHost", "199.101.97.149"); // set proxy server
        System.setProperty("socksProxyPort", "61336"); // set proxy port
        String test_url = "http://www.toolsvoid.com/what-is-my-ip-address";
        try {
            Document doc = Jsoup.connect(test_url).userAgent(USER_AGENT).timeout(10000).get();
            String iP = doc.select("body > section.articles-section > div > div > div > div.col-md-8.display-flex > div > div.table-responsive > table > tbody > tr:nth-child(1) > td:nth-child(2) > strong").first().text(); //get used IP.
            System.out.println("IP-Address: " + iP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setProxy() {
        //rotate
        if (index == proxyList.size()) {
            index = 0;
        }
        String proxy = proxyList.get(index);
        System.setProperty("socksProxyHost", proxy); // set proxy server
        index++;
    }

    //Part3 - Clean Up Function
    public void cleanup() {
        if (logBFWriter != null) {
            try {
                logBFWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}