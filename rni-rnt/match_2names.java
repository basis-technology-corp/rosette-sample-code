// Use MatchScorer to match two names.
void match2Names(Name name1, Name name2) throws NameMatchingException {
    MatchScorer ms = new MatchScorer();
    double score = ms.score(name1, name2);
    // Handle the score.
    System.out.println("Score: " + score);
    // Release resources used by the match scorer.
    ms.close();
}
