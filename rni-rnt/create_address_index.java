// Create an RAI index.
// indexPathname specifies the directory where the index will be created.
StandardAddressIndex createIndex(String indexPathname) throws NameIndexStoreException,
        RNTException {
    StandardAddressIndex index = StandardAddressIndex.create(indexPathname);
    return index;
}