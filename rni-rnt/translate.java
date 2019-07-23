// Translate a name.
// In this example, the name to translate is Arabic.
List<TranslationResult> translate(String nameToTranslate, ITranslator translator)
    throws RNTException {
    ITranslatable toTranslate = NameBuilder.data(nameToTranslate)
                                           .language(LanguageCode.ARABIC)
                                           .script(ISO15924.Arab)
                                           .build();
    // Perform the translation and get the results.
    List<TranslationResult> results = translator.translate(toTranslate);
    return results;
}
