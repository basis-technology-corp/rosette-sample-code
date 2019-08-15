// Run the query and investigate the results.
void runQuery(String indexPathname, NameIndexQuery query)
  throws NameIndexException, NameIndexStoreException, RNTException {
    INameIndex index = StandardNameIndex.open(indexPathname);
    Iterable<NameIndexQueryResult> resultIter = index.query(query);
    for (NameIndexQueryResult result : resultIter) {
        Name foundName = result.getName();
        // Handle the name.
        System.out.println("Query returned " + foundName.getData());
        // The index may contain an entity type for the name, such as "PERSON" or "LOCATION".
        String entityType = com.basistech.util.NEConstants.toString(foundName.getEntityType());
        // The index may also have been set up to include extra data with each entry, such as
        // a brief description of a person or the geocoordinates of a location.
       String info = foundName.getExtra();
       System.out.println(entityType + " " + info);
    }
    index.close();
}
