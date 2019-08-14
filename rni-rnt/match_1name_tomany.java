// Use a cached scorer to match the first name in the list against each
// of the other names. Note: Must prepare each name for use by the cached scorer.
void match1NameToMany(List<Name> names) throws NameMatchingException,
  RNTException {
    MatchScorer ms = new MatchScorer();
    //Prepare the query name for the cached scorer.
    Name queryName = ms.prepNameForCachedScorer(names.get(0));
    MatchScorer.CachedScorer cachedScorer = ms.createCachedScorer(queryName);
    for (int i = 1; i < names.size(); i++) {
        //Prepare each reference name for the cached scorer.
        Name refName = ms.prepNameForCachedScorer(names.get(i));
        double score = cachedScorer.score(refName);
        // Handle the score.
        System.out.println("Cached score: " + score);
    }
    // Release resources associated with the cached scorer and match scorer.
    cachedScorer.close();
    ms.close();
}
