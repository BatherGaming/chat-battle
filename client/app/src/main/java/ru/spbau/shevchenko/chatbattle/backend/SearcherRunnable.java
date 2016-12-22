package ru.spbau.shevchenko.chatbattle.backend;


public class SearcherRunnable implements Runnable {
    private SearchHandler searchHandler;
    static private final int SLEEPING_TIME = 200; // mills

    public SearcherRunnable(SearchHandler searchHandler) {
        this.searchHandler = searchHandler;
    }

    @Override
    public void run() {
        while (true) {
            if (!searchHandler.isWaitingCallback()) {
                RequestMaker.checkIfFound(ProfileManager.getPlayer().getId(),
                        searchHandler.getCheckIfFoundCallback());
                searchHandler.setWaitingCallback(true);
            }
            try {
                Thread.sleep(SLEEPING_TIME);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
