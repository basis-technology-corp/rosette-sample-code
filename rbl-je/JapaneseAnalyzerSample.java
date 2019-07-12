/******************************************************************************
 ** This data and information is proprietary to, and a valuable trade secret
 ** of, Basis Technology Corp.  It is given in confidence by Basis Technology
 ** and may only be used as permitted under the license agreement under which
 ** it has been distributed, and in no other way.
 **
 ** Copyright (c) 2015 Basis Technology Corporation All rights reserved.
 **
 ** The technical data and information provided herein are provided with
 ** `limited rights', and the computer software provided herein is provided
 ** with `restricted rights' as those terms are defined in DAR and ASPR
 ** 7-104.9(a).
 ******************************************************************************/

package com.basistech.rosette.samples;

import com.basistech.rosette.lucene.BaseLinguisticsAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
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
 * Example program that does Japanese analysis with a Japanese base linguistics
 * analyzer. This does not set up and run a Lucene index;
 * it just shows the construction of the analysis chain.
 */
public final class JapaneseAnalyzerSample {
    private String rootDirectory;
    private String inputPathname;
    private String outputPathname;
    private Analyzer rblAnalyzer;

    private JapaneseAnalyzerSample() {
        //
    }

    private void initialize() {
        File rootPath = new File(rootDirectory);
        String licensePath = new File(
                rootPath, "licenses/rlp-license.xml").getAbsolutePath();

        Map<String, String> options = new HashMap<>();
        options.put("language", "jpn");
        options.put("rootDirectory", rootDirectory);
        options.put("licensePath", licensePath);
        options.put("partOfSpeech", "true");
        options.put("addReadings", "true");
        rblAnalyzer = new BaseLinguisticsAnalyzer(options);
    }

    private void run() throws IOException {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(inputPathname),
                    StandardCharsets.UTF_8))) {
            input.mark(1);
            int bomPerhaps = input.read();
            if (bomPerhaps != 0xfeff) {
                input.reset();
            }
            TokenStream tokens = rblAnalyzer.tokenStream("dummy", input);
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

        JapaneseAnalyzerSample that = new JapaneseAnalyzerSample();
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
