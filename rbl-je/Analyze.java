package com.basistech.rosette.samples;

import com.basistech.rosette.bl.Analysis;
import com.basistech.rosette.bl.Analyzer;
import com.basistech.rosette.bl.AnalyzerFactory;
import com.basistech.rosette.bl.AnalyzerOption;
import com.basistech.rosette.bl.Token;
import com.basistech.util.LanguageCode;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command-line sample that reads text from a file, one token per line,
 * produces output, one line per token. This will produce disambiguated
 * results for languages that support disambiguation; see
 * {@link AnalyzerOption#query} and {@link AnalyzerOption#disambiguate} to
 * control the use of disambiguation.
 */
public final class Analyze {
    static final Logger LOG = LoggerFactory.getLogger(Analyze.class);
    private static final int DEFAULT_TOKEN_STRING_WIDTH = 25;
    private static final String DEFAULT_TOKEN_STRING_WIDTH_FORMAT
            = "%-" + DEFAULT_TOKEN_STRING_WIDTH + "s";
    private static final int DEFAULT_POS_STRING_WIDTH = 10;
    private static final String DEFAULT_POS_STRING_WIDTH_FORMAT
            = "%-" + DEFAULT_POS_STRING_WIDTH + "s";
    private static final String EMPTY_STRING = "";

    @Option(name = "-a", aliases = {"-alternativeTokenization"}, usage = "use alternative tokenization")
    private boolean alternativeTokenization;

    @Option(name = "-i", aliases = {"-in", "-inFile"}, usage = "The input file.", required = true)
    private String inputFilePathname;

    @Option(name = "-l", aliases = {"-lang", "-language"}, usage = "language code", required = true)
    private String languageCode;

    @Option(name = "-o", aliases = {"-out", "-outFile"}, usage = "The output file.", required = true)
    private String outputFilePathname;

    @Option(name = "-r", aliases = {"-root", "-rootDirectory"}, usage = "RBL-JE's root directory", required = true)
    private String rootDirectory;

    private Analyze() {
    }


    public static void main(String[] args) throws Exception {
        System.exit(new Analyze().run(args));
    }

    private static void parseArgs(String[] args, CmdLineParser parser)
            throws CmdLineException, IOException {
        try {
            parser.parseArgument(args);
        } catch (CmdLineException | RuntimeException e) {
            showUsage(parser);
            throw new CmdLineException(parser, e);
        }
    }

    @SuppressWarnings("deprecation")
    public int run(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);
        parseArgs(args, parser);

        LOG.info("Language:    " + languageCode);
        LOG.info("Input file:  " + inputFilePathname);
        LOG.info("Output file: " + outputFilePathname);

        LanguageCode language;
        try {
            language = LanguageCode.lookupByISO639(languageCode);
        } catch (IllegalArgumentException iae) {
            System.err.printf("Invalid language code %s%n", languageCode);
            return 1;
        }

        boolean doingAlternativeTokenization =
                alternativeTokenization && ("zho".equals(languageCode) || "jpn".equals(languageCode));
        AnalyzerFactory factory = new AnalyzerFactory();
        factory.setOption(AnalyzerOption.rootDirectory, rootDirectory);
        File rootPath = new File(rootDirectory);
        factory.setOption(AnalyzerOption.licensePath, new File(
                rootPath, "licenses/rlp-license.xml").getAbsolutePath());
        if (doingAlternativeTokenization) {
            factory.setOption(AnalyzerOption.alternativeTokenization, "true");
            LOG.info("alternativeTokenization: true");
        }

        try (BufferedReader inputData = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFilePathname), StandardCharsets.UTF_8))) {
            try (PrintWriter outputData =
                         new PrintWriter(new File(outputFilePathname), StandardCharsets.UTF_8.name())) {
                Analyzer analyzer;
                try {
                    analyzer = factory.create(language);
                } catch (IOException e) {
                    System.err.printf(
                            "Failed to create analyzer for %s%n", language.name());
                    e.printStackTrace(System.err);
                    return 1;
                }

                try {
                    if (language == LanguageCode.HEBREW) {
                        List<Token> sentenceTokens = new ArrayList<>();
                        String line;
                        writeHeadings(outputData, analyzer);
                        boolean endOfSentence = false;
                        while ((line = inputData.readLine()) != null) {
                            if (!line.isEmpty()) {
                                endOfSentence = false;
                                Token token = new Token();
                                String[] properties = line.split("\t");
                                String surfaceForm = properties[0].trim();
                                token.setSurfaceChars(surfaceForm.toCharArray());
                                token.setSurfaceEnd(surfaceForm.length());
                                List<Analysis> analyses = new ArrayList<>();
                                if (properties.length == 4) {
                                    do {
                                        String lemma = properties[2];
                                        String pos = properties[3];
                                        Analysis analysis = new Analysis();
                                        analysis.setLemma(lemma);
                                        analysis.setPartOfSpeech(pos);
                                        analyses.add(analysis);
                                    } while ((line = inputData.readLine()) != null && !line.isEmpty());
                                }
                                token.setAnalyses(analyses.toArray(new Analysis[0]));
                                sentenceTokens.add(token);
                            } else if (!endOfSentence) {
                                endOfSentence = true;
                            } else {
                                //sentence end
                                analyzer.analyze(sentenceTokens);
                                for (Token token : sentenceTokens) {
                                    writeDisambiguated(outputData, token);
                                }
                                outputData.append('\n');
                                sentenceTokens.clear();
                                endOfSentence = false;
                            }
                        }
                    } else {
                        List<Token> sentenceTokens = new ArrayList<>();
                        String line;
                        writeHeadings(outputData, analyzer);
                        while ((line = inputData.readLine()) != null) {
                            if (line.isEmpty()) {
                                // end of sentence.
                                analyzer.analyze(sentenceTokens);
                                for (Token token : sentenceTokens) {
                                    if (analyzer.supportsDisambiguation()) {
                                        writeDisambiguated(outputData, token);
                                    } else {
                                        writeNotDisambiguated(outputData, token);
                                    }
                                }
                                outputData.append('\n');
                                sentenceTokens.clear();
                            } else {
                                // Assumes output from the Tokenize sample which tab
                                // delimits the token from any following text, but will also
                                // work for a list of tokens only.
                                String surfaceForm = line.split("\t")[0].trim();
                                Token token = new Token();
                                token.setSurfaceChars(surfaceForm.toCharArray());
                                token.setSurfaceEnd(surfaceForm.length());
                                sentenceTokens.add(token);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.printf("IO Exception reading the data.%n");
                    return 1;
                }
            }
        }
        return 0;
    }

    private static void writeNotDisambiguated(PrintWriter output, Token token)
            throws IOException {
        Analysis[] analyses = token.getAnalyses();
        writeAnalyses(output, token, analyses);
    }

    private static void writeDisambiguated(PrintWriter output, Token token)
            throws IOException {
        Analysis[] analysis = new Analysis[] {token.getSelectedAnalysis()};
        writeAnalyses(output, token, analysis);
    }

    private static void writeAnalyses(
            PrintWriter output, Token token, Analysis [] analyses)
            throws IOException {
        String item = new String(
                token.getSurfaceChars(),
                0,
                token.getSurfaceEnd());
        output.format(getDesiredTokenOutputFormat(item.length()), item);
        if (analyses == null) {
            output.append("No analyses.\n");
            return;
        }
        int count = 0;
        for (Analysis analysis : analyses) {
            count++;
            if (analysis == null) {
                output.append("No (i.e., null) analysis.\n");
                continue;
            }
            if (count > 1) {
                output.format(getDesiredTokenOutputFormat(
                        EMPTY_STRING.length()), EMPTY_STRING);
            }
            item = analysis.getLemma() == null
                    ? EMPTY_STRING
                    : analysis.getLemma();
            output.format(getDesiredTokenOutputFormat(item.length()), item);
            item = analysis.getPartOfSpeech() == null
                    ? EMPTY_STRING
                    : analysis.getPartOfSpeech();
            output.format(getDesiredPOSOutputFormat(item.length()), item);
            if (analysis.getCompoundComponents() != null
                    && analysis.getCompoundComponents().length != 0) {
                output.append(Arrays.toString(
                        analysis.getCompoundComponents()));
            }
            output.append('\n');
        }
    }


    private static void writeHeadings(
            PrintWriter outputData, Analyzer analyzer) {
        String fmt = DEFAULT_TOKEN_STRING_WIDTH_FORMAT
                + DEFAULT_TOKEN_STRING_WIDTH_FORMAT
                + DEFAULT_POS_STRING_WIDTH_FORMAT
                + DEFAULT_TOKEN_STRING_WIDTH_FORMAT + "%n";
        if (analyzer.supportsDisambiguation()) {
            outputData.format(fmt, "TOKEN", "LEMMA", "POS", "COMPOUNDS");
            outputData.format(fmt, "-----", "-----", "---", "---------");
        } else {
            outputData.format(fmt, "TOKEN", "LEMMAS", "POS", "COMPOUNDS");
            outputData.format(fmt, "-----", "------", "---", "---------");
        }
    }

    private static String getDesiredTokenOutputFormat(int tokenLen) {
        if (tokenLen < DEFAULT_TOKEN_STRING_WIDTH) {
            return DEFAULT_TOKEN_STRING_WIDTH_FORMAT;
        } else {
            return "%-" + (tokenLen + 2) + "s";
        }
    }

    private static String getDesiredPOSOutputFormat(int posLen) {
        if (posLen < DEFAULT_POS_STRING_WIDTH) {
            return DEFAULT_POS_STRING_WIDTH_FORMAT;
        } else {
            return "%-" + (posLen + 2) + "s";
        }
    }

    private static void showUsage(CmdLineParser parser) throws IOException {
        PrintWriter out =
                new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)));
        parser.printUsage(out, null);
    }
}
