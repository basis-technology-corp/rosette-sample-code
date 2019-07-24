
import com.basistech.rosette.bl.Token;
import com.basistech.rosette.breaks.Tokenizer;
import com.basistech.rosette.breaks.TokenizerFactory;
import com.basistech.rosette.breaks.TokenizerOption;
import com.basistech.util.LanguageCode;
import com.basistech.rosette.csc.CSCAnalyzerFactory;
import com.basistech.rosette.csc.CSCAnalyzer;
import com.basistech.rosette.csc.CSCAnalyzerOption;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public void translate(String rootDirectory, String tcInput) {
    String licensePath =
        new File(rootDirectory, "licenses/rlp-license.xml").getAbsolutePath();

    TokenizerFactory tf = new TokenizerFactory();
    tf.setOption(TokenizerOption.rootDirectory, rootDirectory);
    tf.setOption(TokenizerOption.licensePath, licensePath);

    try {
        Tokenizer tokenizer = tf.create(new StringReader(tcInput), LanguageCode.CHINESE);
        CSCAnalyzerFactory caf= new CSCAnalyzerFactory();
        caf.setOption(CSCAnalyzerOption.rootDirectory, rootDirectory);
        caf.setOption(CSCAnalyzerOption.licensePath, licensePath);
        caf.setOption(CSCAnalyzerOption.conversionLevel, "orthographic");

        CSCAnalyzer cscAnalyzer =
            caf.create(LanguageCode.TRADITIONAL_CHINESE, LanguageCode.SIMPLIFIED_CHINESE);

        Token token;
        while ((token = tokenizer.next()) != null) {
            String tokenIn = new String(token.getSurfaceChars(),
                    token.getSurfaceStart(),
                    token.getLength());
            System.out.println("Input: " + tokenIn);
            cscAnalyzer.analyze(token);
            System.out.println("SC translation: " + token.getTranslation());
        }
    } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
    }
}
