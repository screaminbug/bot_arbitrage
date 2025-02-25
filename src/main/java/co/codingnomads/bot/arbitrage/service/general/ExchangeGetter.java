package co.codingnomads.bot.arbitrage.service.general;

import co.codingnomads.bot.arbitrage.model.exchange.ActivatedExchange;
import co.codingnomads.bot.arbitrage.exchange.ExchangeSpecs;
import co.codingnomads.bot.arbitrage.service.thread.GetExchangeThread;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * Created by Thomas Leruth on 12/13/17
 *
 * A class with a method to get the exchanges correctly set up
 */
@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ExchangeGetter {

    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private CompletionService<ActivatedExchange> pool = new ExecutorCompletionService<>(executor);

    /**
     * Turn a list of exchange with correct security parameters into a list of exchanges with enabled service
     * @param selectedExchanges a list of exchanges with their needed authentification set
     * @param tradingMode whether or not the action behavior is trading
     * @return list of exchange with all services loaded up
     */
    public List<ActivatedExchange> getAllSelectedExchangeServices(
            List<ExchangeSpecs> selectedExchanges,
            boolean tradingMode) {

        List<ActivatedExchange> list = new ArrayList<>();

        //for each exchange spec in the arrayList sumbit into the executor pool
        for (ExchangeSpecs selected : selectedExchanges) {
            GetExchangeThread temp = new GetExchangeThread(selected);
            pool.submit(temp);
        }
        //for the length of the exchange take it from the pool and if it is not null
        //add it to the array list of activated exchange
        for (int i = 0; i < selectedExchanges.size(); i++) {
            try {
                ActivatedExchange activatedExchange = pool.take().get();
                if (null != activatedExchange.getExchange()) {
                    list.add(activatedExchange);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        //if trading mode set each exchange as an activated exchange
        if (tradingMode) {
            for (ActivatedExchange activatedExchange : list) {
                activatedExchange.setActivated(activatedExchange.isTradingMode());
            }
        }
        return list;
    }
}
