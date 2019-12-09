package com.basistech.rosette.bl.samples;

import com.basistech.rosette.bl.Analysis;
import com.basistech.rosette.bl.KoreanAnalysis;
import com.basistech.rosette.lucene.AnalysesAttribute;
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
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example program that does analysis with a base linguistics
 * analyzer and demonstrates usage of the AnalysesAttribute object.
 * This does not set up and run a Lucene index;
 * it just shows the construction of the analysis chain.
 */
public final class AnalysesAttributeSample {
    private String rootDirectory;
    private String language;
    private String inputPathname;
    private String outputPathname;
    private Analyzer rblAnalyzer;

    private AnalysesAttributeSample() {
        //
    }

    private void initialize() {
        File rootPath = new File(rootDirectory);
        String licensePath = new File(
                rootPath, "licenses/rlp-license.xml").getAbsolutePath();

        Map<String, String> options = new HashMap<>();
        options.put("language", language);
        options.put("rootDirectory", rootDirectory);
        options.put("licensePath", licensePath);
        options.put("caseSensitive", "true");
        options.put("nfkcNormalize", "true");
        rblAnalyzer = new BaseLinguisticsAnalyzer(options);
    }

    private void run() throws IOException {
        BufferedReader input;

        try {
            input = new BufferedReader(new InputStreamReader(new FileInputStream(inputPathname),
                    StandardCharsets.UTF_8));
            input.mark(1);
            int bomPerhaps = input.read();
            if (bomPerhaps != 0xfeff) {
                input.reset();
            }
        } catch (IOException ie) {
            System.err.printf("Failed to open input file %s%n", inputPathname);
            System.exit(1);
            return;
        }

        TokenStream tokens = rblAnalyzer.tokenStream("", input);
        tokens.reset();

        CharTermAttribute charTerm = tokens.getAttribute(
                CharTermAttribute.class);
        TypeAttribute type = tokens.getAttribute(TypeAttribute.class);
        AnalysesAttribute analysesAttribute = tokens.getAttribute(
                AnalysesAttribute.class);

        Writer output = new OutputStreamWriter(new FileOutputStream(outputPathname), StandardCharsets.UTF_8);
        PrintWriter pr = new PrintWriter(output);

        while (tokens.incrementToken()) {
            Analysis selectedAnalysis = analysesAttribute.selectedAnalysis();
            Analysis[] analyses = analysesAttribute.analyses();

            // Skip tokens that don't have analyses.
            // These include those with type <LEMMA> and <READING>.
            if (null == selectedAnalysis && null == analyses) {
                continue;
            }

            // Print the surface form.
            // (May have been lowercased by the LowerCaseFilter)
            pr.format("%s\t%s%n", charTerm.toString(), type.type());

            // For languages where disambiguation is not supported
            if (null == selectedAnalysis) {
                for (Analysis analysis : analyses) {
                    if (null != analysis) {
                        printAnalysisComponents(analysis, pr);
                    }
                }
            } else {
                printAnalysisComponents(selectedAnalysis, pr);
            }
        }
        input.close();
        pr.close();
        System.out.println("See " + outputPathname);
    }

    private void printAnalysisComponents(Analysis analysis, PrintWriter pr) throws IOException {

        if (analysis.getPartOfSpeech() != null) {
            pr.format("\t\tpart of speech:\t\t%s%n", analysis.getPartOfSpeech());
        }

        if (analysis.getCompoundComponents() != null
                && analysis.getCompoundComponents().length > 0) {
            pr.format("\t\tcompound components:\t%s%n",
                    Arrays.toString(analysis.getCompoundComponents()));
        }

        // This is one way to return lemmas for every token, even if they match,
        // rather than the default analysis chain behavior of omitting
        // matching lemmas.
        if (analysis.getLemma() != null) {
            pr.format("\t\tlemma:\t\t\t%s%n", analysis.getLemma());
        }

        if (analysis.getNormalizedToken() != null) {
            pr.format("\t\tnormalized token:\t%s%n", analysis.getNormalizedToken());
        }

        if (analysis.getReadings() != null
                && analysis.getReadings().length > 0) {
            pr.format("\t\treadings:\t\t%s%n", Arrays.toString(analysis.getReadings()));
        }

        if (analysis.getSemiticRoot() != null) {
            pr.format("\t\tSemitic root:\t\t%s%n", analysis.getSemiticRoot());
        }

        if (analysis.getStem() != null) {
            pr.format("\t\tstem:\t\t\t%s%n", analysis.getStem());
        }

        if ("kor".equals(language)) {
            assert analysis instanceof KoreanAnalysis;
            KoreanAnalysis koreanAnalysis = (KoreanAnalysis) analysis;
            List<String> morphemes = koreanAnalysis.getMorphemes();
            List<String> tags = koreanAnalysis.getTags();

            if (morphemes != null) {
                pr.format("\t\tmorphemes:\t\t%s%n", Arrays.toString(morphemes.toArray()));
            }

            if (tags != null) {
                pr.format("\t\ttags:\t\t\t%s%n", Arrays.toString(tags.toArray()));
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage:"
                    + " com.basistech.rosette.samples.AnalysesAttributeSample "
                    + "rootDirectory language input output");
            return;
        }

        AnalysesAttributeSample that = new AnalysesAttributeSample();
        that.rootDirectory = args[0];
        that.language = args[1];
        that.inputPathname = args[2];
        that.outputPathname = args[3];
        that.initialize();
        try {
            that.run();
        } catch (IOException e) {
            System.err.println("Exception processing the data.");
            e.printStackTrace();
        }
    }
}
