// Use MatchScorer to match two addresses.
void match2Addresses(AddressSpec addr1, AddressSpec addr2) {
    MatchScorer ms = new MatchScorer();
    double score = ms.score(addr1, addr2);
    // Handle the score.
    System.out.println("Score: " + score);
    // Release resources used by the match scorer.
    ms.close();
}