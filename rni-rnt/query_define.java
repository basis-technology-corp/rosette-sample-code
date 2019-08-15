// Define a query.
NameIndexQuery defineQuery(Name queryName)
  throws NameIndexException, NameIndexStoreException, RNTException {
    NameIndexQuery query = new NameIndexQuery(queryName);
    query.setNameDataMinimumMatchScore(.30);
    return query;
}
