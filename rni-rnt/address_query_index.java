// Run the query and investigate the results.
void runQuery(String indexPathname, AddressIndexQuery query)
        throws Exception {
    StandardAddressIndex index = StandardAddressIndex.open(indexPathname);
    List<AddressIndexQueryResult> results = index.query(query);
    for (AddressIndexQueryResult result : results) {
        AddressSpec foundAddress = result.getAddress();
        // Handle the address.
        System.out.println("Query returned " + foundAddress.toString());
        // The index may also have been set up to include extra data, such as
        // category e.g. “restaurant”.
        String info = foundName.getExtra();
        System.out.println(entityType + " " + info);
    }
    index.close();
}
