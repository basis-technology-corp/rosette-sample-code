// Add an address to the index.
void addAddress(StandardAddressIndex index, Integer id) throws NameIndexException, IOException {
    // Give the address a unique identifier. Must be a string.
    String uid = Integer.toString(id);
    // AddressSpecBuilder provides methods for adding address fields, 
    // and a build method that returns the AddressSpec.
    AddressSpec addr = new AddressSpecBuilder()
            .house("101")
            .road("Stuart Street")
            .city("Boston")
            .state("MA")
            .countryRegion("New England")
            .uid(uid)
            .build();

    index.addAddress(addr);
    index.close();
}