// Define a query.
AddressIndexQuery defineQuery(AddressSpec address)
        throws Exception {
    AddressIndexQuery query = new AddressIndexQuery(address);
    query.setAddressDataMinimumMatchScore(.30);
    return query;
}
