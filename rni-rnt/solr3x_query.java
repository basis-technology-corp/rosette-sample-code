// Define an RNI query.
Name qName = NameBuilder.data("Will Shakespear")
                        .language(LanguageCode.ENGLISH)
                        .script(ISO15924.Latn)
                        .entityType(NEConstants.NE_TYPE_PERSON)
                        .build();
NameIndexQuery rniQuery = new NameIndexQuery(qName);
