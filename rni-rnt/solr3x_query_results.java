// Perform the query and return the high-precision results.
Iterable<NameIndexQueryResult> results = solr3xNameIndex.query(rniQuery);
for (NameIndexQueryResult result : results) {
    System.out.println("Precision Result: " + result.getName().getData()
                      + "\tScore: " + result.getMatchScore());
}
