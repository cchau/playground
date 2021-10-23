package com.interview;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.tuple.*;

/**
 * The idea is to maintain a queue which keeps track of whether a ticker is currently being updated for a given company
 * I think we should avoid sending consecutive tickers for same Company to speed up processing
 */
public class StockTickerUpdater {

    // ajay.mat80@gmail.com
    final Lock lock = new ReentrantLock();
    final Condition latestTickerFound = lock.newCondition();
    private CompanyTicker[] tickers;
    private BlockingQueue<ImmutablePair<Integer, CompanyTicker>> queue = new LinkedBlockingQueue<>();
    private ExecutorService executors = Executors.newFixedThreadPool(8);

    public StockTickerUpdater(CompanyTicker[] tickers) {
        this.tickers = tickers;
    }

    private void updateStockTickers() {
        if (tickers == null || tickers.length == 0) {
            throw new IllegalArgumentException("Empty Tickers");
        }
        try {

            for (int i = 0; i < tickers.length; i++) {
                updateCompanyData(i, tickers[i]);
            }

            executors.shutdown();
            executors.awaitTermination(5, TimeUnit.MINUTES);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            queue.clear();
        }

    }


    // I imagine this is some long IO operation such as writing to a database
    private void updateCompanyData(int index, CompanyTicker companyTicker) throws InterruptedException {
        if (companyTicker == null) {
            throw new IllegalArgumentException();
        }
        queue.add(new ImmutablePair<>(index, companyTicker));
        executors.execute(() -> {
            try {
                Thread.sleep((long) (Math.random() + 0.5) * 1000);
                lock.lock();
                while (true) {
                    ImmutablePair<Integer, CompanyTicker> tickerTuple = queue.peek();
                    int currentIndex = tickerTuple.left;

                    if (index != currentIndex) {
                        latestTickerFound.await();
                    } else {
                        break;
                    }
                }

                latestTickerFound.signalAll();
                queue.take();
                System.out.println("Taken!");
                System.out.println("company = " + companyTicker);


                lock.unlock();
            } catch (InterruptedException e) {
                e.printStackTrace();
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

