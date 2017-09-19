package ad;

import java.io.Serializable;
import java.util.List;

public class Ad implements Serializable {
    private static final long serialVersionUID = 1L;
    public long adId;
    public long campaignId;
    public String category;
    public int query_group_id;
    public String query;
    public String title;
    public List<String> keyWords;
    public Double price;
    public String thumbnail;
    public String description;
    public String brand;
    public String detail_url;
    public double relevanceScore;
    public double pClick;
    public double bidPrice;
    public double rankScore;
    public double qualityScore;
    public double costPerClick;
    public int position;
}
