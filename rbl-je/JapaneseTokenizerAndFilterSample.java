package com.basistech.rosette.samples;

import com.basistech.rosette.breaks.TokenizerFactory;
import com.basistech.rosette.breaks.TokenizerOption;
import com.basistech.rosette.lucene.BaseLinguisticsTokenFilterFactory;
import com.basistech.rosette.lucene.BaseLinguisticsTokenizer;
import com.basistech.util.LanguageCode;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cjk.CJKWidthFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Example program that does uses a Japanese tokenizer and Japanese TokenFilter.
 * This does not set up and run a Lucene index;
 * it just shows the construction of the analysis chain.
 */
public final class JapaneseTokenizerAndFilterSample {
    private TokenizerFactory tokenizerFactory;
    private BaseLinguisticsTokenFilterFactory tokenFilterFactory;
    private String rootDirectory;
    private String inputPathname;
    private String outputPathname;

    private JapaneseTokenizerAndFilterSample() {
        //
    }

    private void initialize() {
        File rootPath = new File(rootDirectory);
        String licensePath = new File(
                rootPath, "licenses/rlp-license.xml").getAbsolutePath();
        tokenizerFactory = new TokenizerFactory();
        tokenizerFactory.setOption(
                TokenizerOption.rootDirectory, rootDirectory);
        tokenizerFactory.setOption(TokenizerOption.licensePath, licensePath);
        tokenizerFactory.setOption(TokenizerOption.partOfSpeech, "true");
        Map<String, String> options = new HashMap<>();
        options.put("language", "jpn");
        options.put("rootDirectory", rootDirectory);
        options.put("addReadings", "true");
        tokenFilterFactory = new BaseLinguisticsTokenFilterFactory(options);
    }

    private void run() throws IOException {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(inputPathname),
                StandardCharsets.UTF_8))) {
            input.mark(1);
            int bomPerhaps = input.read();
            if (bomPerhaps != 0xfeff) {
                input.reset();
            }
            Tokenizer tokenizer = new BaseLinguisticsTokenizer(tokenizerFactory.create(null, LanguageCode.JAPANESE));
            tokenizer.setReader(input);

            TokenStream tokens = tokenFilterFactory.create(tokenizer);
            // To replicate behavior of JavaAnalyzerSample, include LowerCaseFilter
            // (a Japanese document may contain Roman script) and CJKWidthFilter
            // (to normalize fullwidth Roman script letters and digits to halfwidth,
            // and halfwidth Katakana variants into the equivalent Kana.
            tokens = new LowerCaseFilter(tokens);
            tokens = new CJKWidthFilter(tokens);
            tokens.reset();

            CharTermAttribute charTerm
                    = tokens.getAttribute(CharTermAttribute.class);
            TypeAttribute type = tokens.getAttribute(TypeAttribute.class);

            try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputPathname),
                    StandardCharsets.UTF_8))) {
                while (tokens.incrementToken()) {
                    printWriter.format("%s\t%s%n", charTerm.toString(), type.type());
                }
            }
        } catch (IOException ie) {
            System.err.printf("Failed to open input file %s%n", inputPathname);
            System.exit(1);
            return;
        }
        System.out.println("See " + outputPathname);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: rootDirectory input output");
            return;
        }

        JapaneseTokenizerAndFilterSample that
                = new JapaneseTokenizerAndFilterSample();
        that.rootDirectory = args[0];
        that.inputPathname = args[1];
        that.outputPathname = args[2];
        that.initialize();
        try {
            that.run();
        } catch (IOException e) {
            System.err.println("Exception processing the data.");
            e.printStackTrace();
        }
    }
}
