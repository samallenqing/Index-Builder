package crawler;

import ad.Ad;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class IndexBuilder {
    private int EXP = 0; //0: never expire
    private String mMemcachedServer;
    private int mMemcachedPortal;
    private String mysql_host;
    private String mysql_db;
    private String mysql_user;
    private String mysql_pass;
    private MySQLAccess mysql;
    private MemcachedClient cache;


    public void Close() {
        if (mysql != null) {
            try {
                mysql.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public IndexBuilder(String memcachedServer, int memcachedPortal, String mysqlHost, String mysqlDb, String user, String pass) {
        mMemcachedServer = memcachedServer;
        mMemcachedPortal = memcachedPortal;
        mysql_host = mysqlHost;
        mysql_db = mysqlDb;
        mysql_user = user;
        mysql_pass = pass;
        try {
            mysql = new MySQLAccess(mysql_host, mysql_db, mysql_user, mysql_pass);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String address = mMemcachedServer + ":" + mMemcachedPortal;
        try {
            cache = new MemcachedClient(new ConnectionFactoryBuilder().setDaemon(true).setFailureMode(FailureMode.Retry).build(), AddrUtil.getAddresses(address));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean init(String adsDataFilePath) {
        try {
            System.out.println("Started building Ads index from file: " + adsDataFilePath);
            BufferedReader reader = new BufferedReader(new FileReader(adsDataFilePath));
            String line = "";
            while ((line = reader.readLine()) != null) {
                Ad ad = new Ad();
                JSONObject adJson = new JSONObject(line);
                if (adJson.isNull("adId") || adJson.isNull("campaignId")) {
                    continue;
                }
                ad.adId = adJson.getLong("adId");
                ad.campaignId = adJson.getLong("campaignId");
                ad.brand = adJson.isNull("brand") ? "" : adJson.getString("brand");
                ad.price = adJson.isNull("price") ? 100.0 : adJson.getDouble("price");
                ad.thumbnail = adJson.isNull("thumbnail") ? "" : adJson.getString("thumbnail");
                ad.title = adJson.isNull("title") ? "" : adJson.getString("title");
                ad.detail_url = adJson.isNull("detail_url") ? "" : adJson.getString("detail_url");
                ad.bidPrice = adJson.isNull("bidPrice") ? 1.0 : adJson.getDouble("bidPrice");
                ad.pClick = adJson.isNull("pClick") ? 0.0 : adJson.getDouble("pClick");
                ad.category = adJson.isNull("category") ? "" : adJson.getString("category");
                ad.description = adJson.isNull("description") ? "" : adJson.getString("description");
                ad.keyWords = new ArrayList<String>();
                JSONArray keyWords = adJson.isNull("keyWords") ? null : adJson.getJSONArray("keyWords");
                for (int j = 0; j < keyWords.length(); j++) {
                    ad.keyWords.add(keyWords.getString(j));
                }
                buildAdsInvertIndex(ad);
                buildAdsForwardIndex(ad);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.Close();
        return true;
    }

    public Boolean buildAdsInvertIndex(Ad ad) throws Exception {

        String keyWords = Utility.strJoin(ad.keyWords, ",");
        List<String> tokens = Utility.cleanedTokenize(keyWords);
		for(String key : ad.keyWords) {
			if(cache.get(key) instanceof Set) {
				@SuppressWarnings("unchecked")
				Set<Long> adIdList = (Set<Long>)cache.get(key);
				adIdList.add(ad.adId);
				cache.set(key, EXP, adIdList);
			} else {
				Set<Long> adIdList = new HashSet<Long>();
				adIdList.add(ad.adId);
				cache.set(key, EXP, adIdList);
			}
		}
		return true;
    }

    public Boolean buildAdsForwardIndex(Ad ad) {
        try {
            mysql.getConnection();
            mysql.addAdData(ad);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}