
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import com.basistech.rosette.lucene.BaseLinguisticsTokenizerFactory;
import com.basistech.rosette.lucene.BaseLinguisticsTokenizer;
import com.basistech.rosette.lucene.BaseLinguisticsCSCTokenFilterFactory;
import com.basistech.rosette.lucene.BaseLinguisticsCSCTokenFilter;
import com.basistech.util.LanguageCode;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

public static void translate(String rootDirectory, String tcInput) {
    String licensePath =
        new File(rootDirectory, "licenses/rlp-license.xml").getAbsolutePath();
    Map<String, String> options = new HashMap<String, String>();
    options.put("rootDirectory", rootDirectory);
    options.put("licensePath", licensePath);
    BaseLinguisticsTokenizerFactory blTokenizerFactory =
        new BaseLinguisticsTokenizerFactory(options);

    Tokenizer tokenizer;
    try {
        tokenizer =
            blTokenizerFactory.create(new StringReader(tcInput), LanguageCode.CHINESE);

        // more options for CSC: orthographic TC to SC conversion
        options.put("language", "zht");
        options.put("targetLanguage", "zhs");
        options.put("conversionLevel", "orthographic");

        BaseLinguisticsCSCTokenFilterFactory cscTokenFilterFactory =
            new BaseLinguisticsCSCTokenFilterFactory(options);
        TokenStream tokens = cscTokenFilterFactory.create(tokenizer);

        tokens.reset();
        CharTermAttribute charTerm
            = tokens.getAttribute(CharTermAttribute.class);

        while (tokens.incrementToken()) {
            System.out.println("SC translation: " + charTerm.toString());
        }
        tokens.close();
    } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
    }
}
