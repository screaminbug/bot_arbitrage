package co.codingnomads.bot.arbitrage.service.simulation;

import co.codingnomads.bot.arbitrage.exchange.simulation.SimulatedWallet;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collection;
import java.util.Random;

public class SimulatedTradeService implements TradeService {

    private SimulatedWallet simulatedWallet;
    private Exchange exchange;
    private TradeService tradeService;

    public SimulatedTradeService(Exchange exchange, SimulatedWallet simulatedWallet) {

        this.exchange = exchange;
        this.simulatedWallet = simulatedWallet;
    }

    @Override
    public OpenOrders getOpenOrders() throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
        return null;
    }

    @Override
    public OpenOrders getOpenOrders(OpenOrdersParams params) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
        return null;
    }

    @Override
    public String placeMarketOrder(MarketOrder marketOrder) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {

        System.out.println();
        System.out.println("====================================================================================");

        System.out.println("----->> Placed simulated order <<------");

        if (marketOrder.getType() == Order.OrderType.ASK) {
            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("Sold " + marketOrder.getOriginalAmount()
                    + " of " + marketOrder.getCurrencyPair().base
                    + " for " + marketOrder.getAveragePrice() + " " + marketOrder.getCurrencyPair().counter
                    + " on " + exchange.getExchangeSpecification().getExchangeName());
            System.out.println("----------------------------------------------------------------------------------");

            simulatedWallet.sub(exchange, marketOrder.getCurrencyPair().base, marketOrder.getOriginalAmount());
            simulatedWallet.add(exchange, marketOrder.getCurrencyPair().counter, marketOrder.getAveragePrice());
        } else if (marketOrder.getType() == Order.OrderType.BID) {

            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("Bought " + marketOrder.getOriginalAmount()
                    + " of " + marketOrder.getCurrencyPair().base
                    + " for " + marketOrder.getAveragePrice() + " " + marketOrder.getCurrencyPair().counter
                    + " on " + exchange.getExchangeSpecification().getExchangeName());
            System.out.println("----------------------------------------------------------------------------------");

            simulatedWallet.add(exchange, marketOrder.getCurrencyPair().base, marketOrder.getOriginalAmount());
            simulatedWallet.sub(exchange, marketOrder.getCurrencyPair().counter, marketOrder.getAveragePrice());
        } else {
            System.out.println("Unsupported order type: " + marketOrder.getType());
        }
        System.out.println("====================================================================================");


        return new Random().nextLong() + "";
    }

    @Override
    public String placeLimitOrder(LimitOrder limitOrder) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
        return null;
    }

    @Override
    public boolean cancelOrder(String orderId) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
        return false;
    }

    @Override
    public boolean cancelOrder(CancelOrderParams orderParams) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
        return false;
    }

    @Override
    public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
        return null;
    }

    @Override
    public TradeHistoryParams createTradeHistoryParams() {
        return null;
    }

    @Override
    public OpenOrdersParams createOpenOrdersParams() {
        return null;
    }

    @Override
    public void verifyOrder(LimitOrder limitOrder) {

    }

    @Override
    public void verifyOrder(MarketOrder marketOrder) {

    }

    @Override
    public Collection<Order> getOrder(String... orderIds) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
        return null;
    }
}
