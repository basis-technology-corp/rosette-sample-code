// Create a Solr document.
SolrInputDocument doc = new SolrInputDocument();
// Create a name and add name fields to the Solr document.
Name iName = NameBuilder.data("William Shakespeare").language(LanguageCode.ENGLISH).build();
solrConnector.addFieldsForName(doc, iName);
// Add the document to the index and commit the update.
solrServer.add(doc);
solrServer.commit();
