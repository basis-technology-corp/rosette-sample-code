// Assemble list of RNI names derived from the Solr documents returned
// by the high-recall Solr query.
ArrayList<Name> highRecallResults = new ArrayList<Name>();
for (SolrDocument solrDocument : qResponse.getResults()) {
    highRecallResults.add(solrConnector.deriveNameFromDoc(solrDocument));
}
// Use RNI linguistic analysis to extract high-precision results from the
// high-recall results.
StandardNameIndexFilter filter;
try {
    filter = new StandardNameIndexFilter();
} catch (RNTException e) {
    throw new NameIndexException("Could not instantiate StandardNameIndexFilter", e);
}
List<NameIndexQueryResult> precisionResults =
    filter.filterCollection(rniQuery, highRecallResults);
filter.close();
for (NameIndexQueryResult precisionResult : precisionResults) {
    System.out.println("Precision Result: " + precisionResult.getName().getData() +
                       "\tScore: " + precisionResult.getMatchScore());
}
