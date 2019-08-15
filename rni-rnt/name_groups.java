// Retrieving groups of names.
void nameGroups(String indexPathname, List<Name> names) throws NameIndexException,
                                                               NameIndexStoreException {
    //1. Create the index with the indexingNameExtra flag set to true.
    IndexStoreDataModelFlags dataModelFlags = new IndexStoreDataModelFlags();
    dataModelFlags.setIndexingNameExtra(true);
    INameIndex index = StandardNameIndex.create(indexPathname, dataModelFlags);
    //2. For each name you want to place in a group, set the Extra field to include
    //   a descriptive token. It may contain additional text as well, but the token
    //   is the key element for later retrieval.
    for (Name name: names) {
        name.setExtra("DEBUG");
        index.addName(name);
    }
    //3. Define a query as follows:
    //   * The query tests the Extra field, not name data.
    //   * The query can return an unlimited number of results.
    //   * The query Name object includes the descriptive token in the Extra field.
    //     It should only contain a single search token (a string with no spaces).
    //     The Data field may be an empty string. The language and script may be any
    //     values that RNI can process.
    Name queryName = NameBuilder.data("").language(LanguageCode.UNKNOWN).extra("DEBUG").build();
    NameIndexQuery query = new NameIndexQuery(queryName);
    query.setTestNameExtra(true);
    query.setTestNameData(false);
    query.setMaximumNamesToConsider(NameIndexQuery.UNLIMITED_RESULTS);
    query.setMaximumNamesToCheck(NameIndexQuery.UNLIMITED_RESULTS);
    query.setMaximumResultsToReturn(NameIndexQuery.UNLIMITED_RESULTS);
    //4. Run the query.
    for (NameIndexQueryResult result : index.query(query)) {
        // Examine the name objects.
        System.out.println("Group results include " + result.getName().getData());
    }
    index.close();
}
