// Create a translator.
ITranslator createTranslator() throws RNTException {
    // Source domain: Arabic language (Arabic script and native transliteration are defaults).
    TextDomain srcDomain = new TextDomain(LanguageCode.ARABIC);
    // Target domain: Latin script, English language, and IC transliteration.
    TextDomain targetDomain = new TextDomain(ISO15924.Latn,
                                             LanguageCode.ENGLISH,
                                             TransliterationScheme.IC);
    ITranslator translator = BasicTranslatorFactory.create(srcDomain, targetDomain);
    return translator;
}
