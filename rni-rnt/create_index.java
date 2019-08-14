// Create an RNI index.
// indexPathname specifies the directory where the index will be created.
INameIndex createIndex(String indexPathname) throws NameIndexStoreException,
  RNTException {
    IndexStoreDataModelFlags dataModelFlags = new IndexStoreDataModelFlags();
    // With no flags set, the list is configured to include names, not entities,
    // and no transliterations.
    INameIndex index = StandardNameIndex.create(indexPathname, dataModelFlags);
    return index;
}
