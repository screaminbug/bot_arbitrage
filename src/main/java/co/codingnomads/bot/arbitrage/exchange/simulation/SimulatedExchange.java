package co.codingnomads.bot.arbitrage.exchange.simulation;

import co.codingnomads.bot.arbitrage.service.simulation.SimulatedAccountService;
import co.codingnomads.bot.arbitrage.service.simulation.SimulatedTradeService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import si.mazi.rescu.SynchronizedValueFactory;

import java.io.IOException;
import java.util.List;

public class SimulatedExchange implements Exchange {

    private Exchange realExchange;
    private SimulatedWallet simulatedWallet;

    public SimulatedExchange(Exchange realExchange, SimulatedWallet simulatedWallet) {
        this.realExchange = realExchange;
        this.simulatedWallet = simulatedWallet;
    }

    @Override
    public ExchangeSpecification getExchangeSpecification() {
        return realExchange.getExchangeSpecification();
    }

    @Override
    public ExchangeMetaData getExchangeMetaData() {
        return realExchange.getExchangeMetaData();
    }

    @Override
    public List<CurrencyPair> getExchangeSymbols() {
        return realExchange.getExchangeSymbols();
    }

    @Override
    public SynchronizedValueFactory<Long> getNonceFactory() {
        return realExchange.getNonceFactory();
    }

    @Override
    public ExchangeSpecification getDefaultExchangeSpecification() {
        return realExchange.getDefaultExchangeSpecification();
    }

    @Override
    public void applySpecification(ExchangeSpecification exchangeSpecification) {}

    @Override
    public MarketDataService getMarketDataService() {
        return null;
    }

    @Override
    public TradeService getTradeService() {
        return new SimulatedTradeService(realExchange, simulatedWallet);
    }

    @Override
    public AccountService getAccountService() {
        return new SimulatedAccountService();
    }

    @Override
    public void remoteInit() throws IOException, ExchangeException {

    }
}
