package crawler;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class StringFilter {
    private String stopWords_a = "a,able,about,abstract,across,after,all,almost,also,am,among,an,and,any,are,as,at,";
    private String stopWords_b = "be,because,been,being,but,by,";
    private String stopWords_c = "can't,cannot,could,couldn't did didn't,do,does,doing,don't,down,during,";
    private String stopWords_e_f = "each,for,from,further,";
    private String stopWords_h = "had,hadn't,has,hasn't,have,haven't,having,";
    private String stopWords_i = "i,i'm,if,in,into,is,isn't,it,it's,its,itself,";
    private String stopWords_m_n = "me,more,most,must,my,myself,no,nor,not,";
    private String stopWords_o = "of,off,on,once,only,or,other,ought,our,ours,ourselves,out,over,own,";
    private String stopWords_s = "same,she,should,so,some,such,";
    private String stopWords_t = "than,that,that's,the,their,theirs,them,then,there,they,this,those,through,to,too,";
    private String stopWords_u_v = "under,until,up,very,";
    private String stopWords_w = "was,wasn't,we,we'd,we'll,we're,were,what,when,where,which,while,who,whom,why,with,won't,would,";
    private String stopWords_y = "you,you'd,you'll,you're,you've,your,yours,yourself,yourselves";
    private String stopWords = stopWords_a + stopWords_b + stopWords_c + stopWords_e_f + stopWords_h + stopWords_i +
            stopWords_m_n + stopWords_o + stopWords_s + stopWords_t + stopWords_u_v + stopWords_w + stopWords_y;

    //1 - Normalize Detail URL
    public String normalizeUrl(String url) {
        int n = url.indexOf("ref");
        String normalizedUrl = url.substring(0, n - 1);
        return normalizedUrl;
    }

    //2 - Clean and Tokenize Title
    public String cleanTokenize(String input, List<String> tokens) {
        StringBuilder sb = new StringBuilder();

        Reader reader = new StringReader(input);
        Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_40, reader);
        TokenStream  tokenStream = new StandardFilter(Version.LUCENE_40, tokenizer);
        tokenStream = new StopFilter(Version.LUCENE_40, tokenStream, getStopWords(stopWords));
        tokenStream = new KStemFilter(tokenStream);
        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

        try {
            tokenStream.reset();
            while(tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                tokens.add(term);
                sb.append(term + " ");
            }
            tokenStream.end();
            tokenStream.close();
            tokenizer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    //3 - Generate N-gram Query
    public List<String> ngramQuery(String query) {
        List<String> results = new ArrayList<String>();

        Reader reader = new StringReader(query);
        Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_40, reader);
        TokenStream tokenStream = new ShingleFilter(tokenizer, 2, 3);
        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String ngramQuery = charTermAttribute.toString();
                results.add(ngramQuery);
                System.out.print(ngramQuery + ";");

            }
            tokenStream.end();
            tokenStream.close();
            tokenizer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
        return results;
    }

    private static CharArraySet getStopWords(String stopWord) {
        List<String> stopWordsList = new ArrayList<String>();
        for(String stop : stopWordsList) {
            stopWordsList.add(stop);
        }
        return new CharArraySet(Version.LUCENE_40, stopWordsList, true);
    }
}
