// Inspect the translation results.
void inspectResults(List<TranslationResult> results) throws RNTException {
    for (TranslationResult result : results) {
        String trans = result.getTranslation();
        Double transConfidence = result.getConfidence();
        System.out.println("Translation: " + trans + "\tConfidence: " + transConfidence);
        // Get available additional information. Note: For Chinese, use
        // getAdditionalInformation(SegmentationResult.class) to get the segmentation.
        OrthographyCompletionResult ocr =
            result.getAdditionalInformation(OrthographyCompletionResult.class);
        if (ocr != null) {
            // The orthographic completion (diacritization for Arabic) and confidence.
            String complResult = ocr.getCompletedResult();
            Double orthoCompleteConf = ocr.getConfidence();
            System.out.println("Orthographic Completion: " + complResult + "\t Confidence: "
                               + orthoCompleteConf);
        }
    }
}
