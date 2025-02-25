package co.codingnomads.bot.arbitrage.service.general;

import co.codingnomads.bot.arbitrage.exchange.simulation.SimulatedWallet;
import co.codingnomads.bot.arbitrage.model.exchange.ActivatedExchange;
import co.codingnomads.bot.arbitrage.model.ticker.TickerData;
import co.codingnomads.bot.arbitrage.service.thread.GetTickerDataThread;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Thomas Leruth on 12/13/17
 *
 * A class to get data from exchanges and format it correctly
 */
@Slf4j
public class ExchangeDataGetter {

    private final static int TIMEOUT = 30;

    /**
     *
     * Get All the TickerData from the selected exchanged
     * @param activatedExchanges list of currently acrivated exchanges
     * @param currencyPair the pair the TickerData is seeked for
     * @param tradeValueBase the value of the trade if using the trading action as behavior
     * @return A list of TickerData for all the exchanges
     */
    public List<TickerData> getAllTickerData(List<ActivatedExchange> activatedExchanges,
                                             CurrencyPair currencyPair,
                                             BigDecimal tradeValueBase,
                                             SimulatedWallet simulatedWallet) {

        List<TickerData> list = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<TickerData> pool = new ExecutorCompletionService<>(executor);

        //for each activated exchange submit into the executor pool
        for (ActivatedExchange activatedExchange : activatedExchanges) {
            if (activatedExchange.isActivated() || activatedExchange.isSimulatedMode()) {
                GetTickerDataThread temp =
                        new GetTickerDataThread(
                                activatedExchange,
                                currencyPair,
                                tradeValueBase,
                                new SimulatedWallet(simulatedWallet.getAllBalances()));
                pool.submit(temp);
            }
        }
        //for each activated exchange if it is activated take from the executor pool and return those activated exchanges
        for (ActivatedExchange activatedExchange : activatedExchanges) {
            if (activatedExchange.isActivated() || activatedExchange.isSimulatedMode()) {
                try {
                    TickerData tickerData = pool.take().get();
                    if (null != tickerData) {
                        list.add(tickerData);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.info("Couldn't get ticker data because of: {}", e.getMessage());
                }
            }
        }

        executor.shutdown();

        return list;
    }

    /**
     * Takes an exchange and currency pairs throws them in a thread and calls the corresponding api to the exchange and
     * returns the bid and ask price for each currency pair. If the api call is longer than the timeout, the thread is terminated.
     * @param exchange
     * @param currencyPair
     * @return TickerData object
     * @throws TimeoutException if it takes longer than 30 seconds for the api to be called
     */
    public static TickerData getTickerData(Exchange exchange, CurrencyPair currencyPair) throws TimeoutException {

        final ExecutorService service = Executors.newSingleThreadExecutor();

        try {
            final Future<TickerData> f = service.submit(() -> {
                Ticker ticker = exchange.getMarketDataService().getTicker(currencyPair);
                return new TickerData(currencyPair, exchange, ticker.getBid(), ticker.getAsk());
            });

            return f.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            service.shutdown();
        }
    }
}



