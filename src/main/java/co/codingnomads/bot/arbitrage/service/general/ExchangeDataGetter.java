package co.codingnomads.bot.arbitrage.service.general;

import co.codingnomads.bot.arbitrage.model.ActivatedExchange;
import co.codingnomads.bot.arbitrage.model.TickerData;
import co.codingnomads.bot.arbitrage.service.general.thread.GetTickerDataThread;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Created by Thomas Leruth on 12/13/17
 *
 * A class to get data from exchanges and format it correctly
 */
@Service
public class ExchangeDataGetter {

    // protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final static int TIMEOUT = 30;

    /**
     *
     * Get All the TickerData from the selected exchanged
     * @param activatedExchanges list of currently acrivated exchanges
     * @param currencyPair the pair the TickerData is seeked for
     * @param tradeValueBase the value of the trade if using the trading action as behavior
     * @return A list of TickerData for all the exchanges
     */
    public ArrayList<TickerData> getAllTickerData(ArrayList<ActivatedExchange> activatedExchanges,

                                              CurrencyPair currencyPair,
                                              double tradeValueBase) {

        ArrayList<TickerData> list = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<TickerData> pool = new ExecutorCompletionService<>(executor);

        for (ActivatedExchange activatedExchange : activatedExchanges) {
            if (activatedExchange.isActivated()) {
                GetTickerDataThread temp = new GetTickerDataThread(activatedExchange, currencyPair, tradeValueBase);
                pool.submit(temp);
            }
        }
        for (ActivatedExchange activatedExchange : activatedExchanges) {
            if (activatedExchange.isActivated()) {
                try {
                    TickerData tickerData = pool.take().get();
                    if (null != tickerData) {
                        list.add(tickerData);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        executor.shutdown();

        return list;
    }


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



