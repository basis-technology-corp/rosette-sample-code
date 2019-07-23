// Clean up for next run.
server.deleteByQuery("*:*");
server.commit();
coreContainer.shutdown();
