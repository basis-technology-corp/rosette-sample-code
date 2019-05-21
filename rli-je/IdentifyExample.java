/******************************************************************************
 ** This data and information is proprietary to, and a valuable trade secret
 ** of, Basis Technology Corp.  It is given in confidence by Basis Technology
 ** and may only be used as permitted under the license agreement under which
 ** it has been distributed, and in no other way.
 **
 ** Copyright (c) 2018 Basis Technology Corporation All rights reserved.
 **
 ** The technical data and information provided herein are provided with
 ** `limited rights', and the computer software provided herein is provided
 ** with `restricted rights' as those terms are defined in DAR and ASPR
 ** 7-104.9(a).
 ******************************************************************************/

package sample;

import com.basistech.rosette.dm.AnnotatedText;
import com.basistech.rosette.dm.Annotator;
import com.basistech.rosette.dm.LanguageDetection;
import com.basistech.rosette.dm.RawData;
import com.basistech.rosette.languageidentifier.LanguageIdentificationAnnotator;
import com.basistech.rosette.languageidentifier.LanguageIdentifierBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

/**
 * Creates a LanguageIdentifier instance,  prints the results
 * of language detection on the bytes from example_text, and the results
 * of language region detection on the bytes from language-region-sample.txt.
 */
public final class IdentifyExample {

    private IdentifyExample() {
    }

    private static byte[] getBytes(String fileName) throws IOException {
        File inFile = new File(fileName);
        byte[] bytes;
        try (InputStream in = new FileInputStream(inFile)) {
            // Get the size of the file
            long length = inFile.length();

            // byte array cannot be made from long
            if (length > Integer.MAX_VALUE) {
                // the file is too large for reading in one go
                throw new RuntimeException("File too large for single read");
            } else {
                bytes = new byte[(int) length];
            }

            // read in the bytes
            in.read(bytes);
        }
        return bytes;
    }


    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: IdentifyExample RLI-ROOT-DIRECTORY");
            System.exit(1);
            return;
        }
        IdentifyExample id = new IdentifyExample();
        id.run(args[0]);
    }

    public void run(String rootDirectory) throws Exception {
        // 1. Process example.txt as RawData with standard analysis: shortStringThreshold = 0.
        //    (shortStringThreshold = 0)
        // get the bytes from the example.txt file
        byte[] bytes = getBytes("example.txt");

        LanguageIdentificationAnnotator annotator = new LanguageIdentifierBuilder(new File(rootDirectory))
                .buildSingleLanguageAnnotator();
        // Process the monolingual text file (example.txt) as Raw Data;  shortStringThreshold is equal to 0,
        // so shortString language detection is inactive.
        try {
            RawData input = new RawData(ByteBuffer.wrap(bytes), new HashMap<>());
            AnnotatedText results = annotator.annotate(input);

            // print the language detection results.
            System.out.println("1. RawData, shortStringThreshold = 0: Language Detection for example.txt.");
            System.out.println("------------------------------------------------------------------------");
            System.out.format("%15s %15s %15s %s%n",
                    "Language",
                    "Encoding",
                    "Script",
                    "Confidence");
            for (LanguageDetection.DetectionResult result
                    : results.getWholeTextLanguageDetection().getDetectionResults()) {
                System.out.format("%15s %15s %15s %g%n",
                        result.getLanguage().languageName(),
                        result.getEncoding(),
                        result.getScript(),
                        result.getConfidence());
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        System.out.println();


        // 2. Run short string algorithm with RawData.
        annotator = new LanguageIdentifierBuilder(new File(rootDirectory)).shortStringThreshold(1)
                .buildSingleLanguageAnnotator();
        // Process the monolingual text file (example.txt) as RawData, with shortStringThreshold > 0,
        // which throws UnsupportedOperationException, because shortString detection does
        // not support RawData input (length of string content is unknown).
        try {
            // print the language detection results.
            System.out.println("2. RawData, shortStringThreshold > 0: Language Detection for example.txt.");
            System.out.println("------------------------------------------------------------------------");
            RawData input2 = new RawData(ByteBuffer.wrap(bytes), new HashMap<>());
            AnnotatedText results2 = annotator.annotate(input2);

            for (LanguageDetection.DetectionResult result
                    : results2.getWholeTextLanguageDetection().getDetectionResults()) {
                System.out.format("%15s %15s %15s %g%n",
                        result.getLanguage().languageName(),
                        result.getEncoding(),
                        result.getScript(),
                        result.getConfidence());
            }
        } catch (RuntimeException e) {
            System.out.println(e.getLocalizedMessage());
        }
        System.out.println();

        // 3. Process the short-string text file (short.txt) as string (Annotated Text)
        //    and report the best result. Also, illustrate the use of LanguageIdentifierBuilder.license(String)
        //    when one wants to pass in a license via the API.
        bytes = getBytes("short.txt");
        try {
            // Get the license in the root directory for illustration purposes.
            String xmlLicense =
                    new String(readAllBytes(get(rootDirectory, "licenses", "rlp-license.xml")), StandardCharsets.UTF_8);
            String text = new String(bytes, StandardCharsets.UTF_8);
            int threshold = text.length() + 1;
            annotator =
                    new LanguageIdentifierBuilder(new File(rootDirectory))
                            .license(xmlLicense)
                            .shortStringThreshold(threshold)
                            .buildSingleLanguageAnnotator();

            // Alternatively, use the models bundled in the JAR.
            /*
            annotator =
                    new LanguageIdentifierBuilder(xmlLicense)
                            .useModelsInJar(true)
                            .shortStringThreshold(threshold)
                            .buildSingleLanguageAnnotator();
            */

            System.out.format("%s%d%s%n",

                    "3. String from file, shortStringThreshold = ",
                    threshold,
                    ": the best result with short-string analysis of short.txt.");
            System.out.println("------------------------------------------------------------------------");
            System.out.format("%15s %15s %15s %s%n",
                    "Language",
                    "Encoding",
                    "Script",
                    "Confidence");
            AnnotatedText shortStringText = annotator.annotate(text);
            // Get the best result.
            LanguageDetection.DetectionResult result =
                shortStringText.getWholeTextLanguageDetection().getDetectionResults().get(0);
            System.out.format("%15s %15s %15s %g%n",
                        result.getLanguage().languageName(),
                        result.getEncoding(),
                        result.getScript(),
                        result.getConfidence());

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        System.out.println();

        // 4 Process a multilingual text file (language-region-sample.txt) and reports
        //   the best result for each region with regions offsets.
        try {
        /* now language regions */
            System.out.println("4. String Language Regions from language-region-sample.txt.");
            System.out.println("------------------------------------------------------------------------");
            Annotator regionAnnotator = new LanguageIdentifierBuilder(new File(rootDirectory))
                    .buildLanguageRegionAnnotator();
            byte[] lrData = getBytes("language-region-sample.txt");
            String lrString = new String(lrData, StandardCharsets.UTF_8);
            AnnotatedText lrResults = regionAnnotator.annotate(lrString);
            // print the best language detection result for each language region.
            for (LanguageDetection languageDetection
                    : lrResults.getLanguageDetectionRegions()) {
                System.out.format("Region from %3d to %3d%n",
                        languageDetection.getStartOffset(),
                        languageDetection.getEndOffset());
                LanguageDetection.DetectionResult result =
                        languageDetection.getDetectionResults().get(0);
                System.out.format("%15s %15s %g%n",
                        result.getLanguage().languageName(),
                        result.getScript(),
                        result.getConfidence());
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
