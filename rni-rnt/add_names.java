// Add a set of names to the index in a single transaction.
void addNames(String indexPathname, List<Name> names) throws NameIndexException,
  NameIndexStoreException, RNTException {
    INameIndex index = StandardNameIndex.open(indexPathname);
    INameIndexSession session = index.openSession();
    try {
        for (Name name: names) {
            session.addName(name);
        }
        // Commit the transaction.
        session.commit();
    } catch (NameIndexException e) {
        // If the transaction failed, do a rollback.
        session.rollback();
        throw e;
    } finally {
        session.close();
    }
    // After making updates to the index, optimize it for efficiency.
    index.optimize();
    // When you are done using an index, be sure to close it.
    index.close();
}
