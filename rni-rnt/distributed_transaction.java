// Perform a distributed transaction.
void distributedTransaction(INameIndex index1, INameIndex index2, Name name)
throws NameIndexException,NameIndexStoreException, RNTException {
    INameIndexSession session1 = index1.openSession();
    INameIndexTransaction transaction1 = session1.startTransaction();
    INameIndexSession session2 = index2.openSession();
    INameIndexTransaction transaction2 = session2.startTransaction();
    // In a single transaction, delete a name from Index1 and add it to Index2.
    try {
        session1.deleteName(name.getUID());
        session2.addName(name);
        // First phase of two-phase commit.
        transaction1.prepare();
        transaction2.prepare();
        // Second phase of two-phase commit.
        transaction1.commit();
        transaction2.commit();
    } catch (NameIndexException e) {
        transaction1.rollback();
        transaction2.rollback();
    } finally {
        session1.close();
        session2.close();
        // We are done with both indexes; must close them.
        index1.close();
        index2.close();
    }
}
