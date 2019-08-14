// Add a name to the index.
void addName(INameIndex index, Integer id) throws NameIndexException {
    String nameData = "John Smith";
    // Language of use in which the name appears.
    LanguageCode langCode = LanguageCode.ENGLISH;
    // Must be the native script for the Language. You can leave out this setting
    // in the NameBuilder and let RNI determine the appropriate script.
    ISO15924 script = ISO15924.Latn;
    // Give the name a unique identifier. Must be a string.
    String uid = Integer.toString(id);
    // NameBuilder provides a static data method to instantiate the NameBuilder,
    // methods for adding other name fields, and a build method that returns the Name.
    Name name = NameBuilder.data(nameData)
                           .language(langCode)
                           .script(script)
                           .uid(uid)
                           .build();
    index.addName(name);
    index.close();
}
