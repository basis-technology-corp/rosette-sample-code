// Define an RNI query.
Name qName = NameBuilder.data("Will Shakespeare").language(LanguageCode.ENGLISH).build();
NameIndexQuery rniQuery = new NameIndexQuery(qName);
// Derive a Solr query from the RNI query.
SolrQuery solrQuery = solrConnector.deriveQuery(rniQuery);
// Execute the Solr query.
QueryResponse qResponse = solrServer.query(solrQuery);
