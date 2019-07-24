package com.basistech.rosette.samples;

import com.basistech.rosette.bl.Token;
import com.basistech.rosette.breaks.Tokenizer;
import com.basistech.rosette.breaks.TokenizerFactory;
import com.basistech.rosette.breaks.TokenizerOption;
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

/**
 * Command-line sample that reads text from a file, tokenizes it, and
 * produces output, one line per token. The output includes a blank
 * line at the end of each sentence. The output format is
 * <pre>TOKEN-tab-Token Type</pre>
 */
public final class Tokenize {
    static final Logger LOG = LoggerFactory.getLogger(Tokenize.class);

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

    private Tokenize() {
    }


    public static void main(String[] args) throws Exception {
        new Tokenize().run(args);
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

    public void run(String[] args) throws Exception {
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
            return;
        }
        boolean doingAlternativeTokenization = alternativeTokenization && ("zho".equals(languageCode) || "jpn".equals(languageCode));
        TokenizerFactory factory = new TokenizerFactory();
        factory.setOption(TokenizerOption.rootDirectory, rootDirectory);
        File rootPath = new File(rootDirectory);
        factory.setOption(TokenizerOption.licensePath, new File(
                rootPath, "licenses/rlp-license.xml").getAbsolutePath());
        factory.setOption(TokenizerOption.includeRoots, "true");
        factory.setOption(TokenizerOption.nfkcNormalize, "true");
        if (doingAlternativeTokenization) {
            factory.setOption(TokenizerOption.alternativeTokenization, "true");
            LOG.info("alternativeTokenization: true");
        }

        try (BufferedReader inputData = new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePathname),
                StandardCharsets.UTF_8))) {
            inputData.mark(1);
            int bomPerhaps = inputData.read();
            if (bomPerhaps != 0xfeff) {
                inputData.reset();
            }
            Tokenizer tokenizer = factory.create(inputData, language);
            Token token;
            try (PrintWriter outputData = new PrintWriter(new File(outputFilePathname), StandardCharsets.UTF_8.name())) {
                while ((token = tokenizer.next()) != null) {
                    String tokenString = new String(
                            token.getSurfaceChars(),
                            token.getSurfaceStart(),
                            token.getLength());
                    if (language == LanguageCode.HEBREW) {
                        if (token.getAnalyses().length == 0) {
                            outputData.format("%s\t%s%n", tokenString, token.getType());
                        } else {
                            for (int i = 0; i < token.getAnalyses().length; i++) {
                                if (i == 0) {
                                    outputData.format("%s", tokenString);
                                } else {
                                    outputData.append(new String(new char[tokenString.length()]).replace("\0", " "));
                                }
                                outputData.format("\t%s\t%s\t%s%n", token.getType(), token.getAnalyses()[i].getLemma(), token.getAnalyses()[i].getPartOfSpeech());
                            }
                        }
                    } else if (doingAlternativeTokenization) {
                        // The only tokenization alternatives are JLA and CLA, both of which
                        // provide disambiguated POS tags.
                        outputData.format("%s\t%s\t%s", tokenString, token.getType(), token.getSelectedAnalysis().getPartOfSpeech());
                    } else {
                        outputData.format("%s\t%s", tokenString, token.getType());
                    }
                    outputData.append('\n');
                    if (token.isEndOfSentence()) {
                        outputData.append('\n');
                    }
                }
            } catch (IOException e) {
                System.err.printf("IO Exception reading the data.%n");
                System.exit(1);
            }
        } catch (IOException ie) {
            ie.printStackTrace();
            System.exit(1);
        }
    }

    private static void showUsage(CmdLineParser parser) throws IOException {
        PrintWriter out =
                new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)));
        parser.printUsage(out, null);
    }
}
