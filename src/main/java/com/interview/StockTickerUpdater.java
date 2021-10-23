package com.interview;

import java.util.concurrent.*;

/**
 * The idea is to maintain a queue which keeps track of whether a ticker is currently being updated for a given company
 * I think we should avoid sending consecutive tickers for same Company to speed up processing
 */
public class StockTickerUpdater {

    // ajay.mat80@gmail.com
    private CompanyTicker[] tickers;
    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private ExecutorService executors = Executors.newFixedThreadPool(8);

    public StockTickerUpdater(CompanyTicker[] tickers) {
        this.tickers = tickers;
    }

    private void updateStockTickers() {
        if(tickers == null || tickers.length == 0) {
            throw new IllegalArgumentException();
        }
        try {
            if (tickers.length > 0) {
                for (int i = 0; i < tickers.length; i++) {

                    CompanyTicker ticker = tickers[i];
                    // Uncomment to get the latest ticker from the same company which are streaming in consecutively
                    // This reduce the redundant updates that it needs to make for the same company
//                    int j = i+1;
//                    if(j < tickers.length) {
//                        while (ticker.getCompanyName().equals(tickers[j].getCompanyName())) {
//                            ticker = tickers[j];
//                            i = j;
//                            j++;
//                        }
//                    }

                    CompanyTicker finalTicker = ticker;
                    if(!queue.contains(finalTicker.getCompanyName())) {
                        updateCompanyData(finalTicker);
                    } else {
                        while (!queue.isEmpty() && queue.contains(finalTicker.getCompanyName())) {
                            Thread.sleep(100);
                        }
                        updateCompanyData(finalTicker);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executors.shutdown();
            queue.clear();
        }
    }



    // I imagine this is some long IO operation such as writing to a database
    private void updateCompanyData(CompanyTicker companyTicker) throws InterruptedException {
        if(companyTicker == null) {
            throw new IllegalArgumentException();
        }
        queue.add(companyTicker.getCompanyName());
        executors.execute(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("company = " + companyTicker);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                queue.remove(companyTicker.getCompanyName());
            }
        });

    }

    public static void main(String[] args) {
        CompanyTicker[] tickers = new CompanyTicker[11];
        tickers[0] = new CompanyTicker("AAPL", 100);
        tickers[1] = new CompanyTicker("AAPL", 103);
        tickers[2] = new CompanyTicker("AAPL", 101);
        tickers[3] = new CompanyTicker("GOOG", 300);
        tickers[4] = new CompanyTicker("NFLX", 250);
        tickers[5] = new CompanyTicker("GOOG", 301);
        tickers[6] = new CompanyTicker("AAPL", 102);
        tickers[7] = new CompanyTicker("TSLA", 700);
        tickers[8] = new CompanyTicker("FB", 500);
        tickers[9] = new CompanyTicker("FB", 520);
        tickers[10] = new CompanyTicker("TSLA", 750);
        StockTickerUpdater updater = new StockTickerUpdater(tickers);
        updater.updateStockTickers();

    }
}

