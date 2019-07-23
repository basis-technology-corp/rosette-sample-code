// Create a Name and add it to the index.
Name iName = NameBuilder.data("William Shakespeare")
                        .language(LanguageCode.ENGLISH)
                        .script(ISO15924.Latn)
                        .entityType(NEConstants.NE_TYPE_PERSON)
                        .build();
solr3xNameIndex.addName(iName);
