package co.codingnomads.bot.arbitrage.action.arbitrage;

import co.codingnomads.bot.arbitrage.action.arbitrage.selection.ArbitrageActionSelection;
import co.codingnomads.bot.arbitrage.exception.ExchangeDataException;
import co.codingnomads.bot.arbitrage.model.rest.TradeResponse;
import co.codingnomads.bot.arbitrage.model.ticker.TickerData;
import co.codingnomads.bot.arbitrage.model.ticker.TickerDataTrading;
import co.codingnomads.bot.arbitrage.model.trading.OrderIDWrapper;
import co.codingnomads.bot.arbitrage.model.trading.TradingData;
import co.codingnomads.bot.arbitrage.model.trading.WalletWrapper;
import co.codingnomads.bot.arbitrage.service.general.MarginDiffCompare;
import co.codingnomads.bot.arbitrage.service.thread.GetWalletWrapperThread;
import co.codingnomads.bot.arbitrage.service.thread.MakeOrderThread;
import co.codingnomads.bot.arbitrage.service.tradehistory.TradeHistoryService;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.*;

/**
 * Created by Thomas Leruth on 12/17/17
 * <p>
 * class for the information needed to use the trading action as behavior action
 */
@Component("trading")
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ArbitrageTradingAction extends ArbitrageActionSelection {

    @Autowired
    private TradeHistoryService tradeHistoryService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private double tradeValueBase;

    //variable to keep track of how many trades you would like to make
    private int round =0;

    public double getTradeValueBase() {
        return tradeValueBase;
    }

    public void setTradeValueBase(double tradeValueBase) {
        this.tradeValueBase = tradeValueBase;
    }

    /**
     * CanTrade method that determines if there is an arbitrage opportunity and if you are able to execute it.
     * @param lowAsk
     * @param highBid
     * @return true if able to trade
     * @throws ExchangeDataException
     */
    public boolean canTrade(TickerData lowAsk,
                            TickerData highBid) throws ExchangeDataException {

        //if the lowAsk exchange is the same as the highBid exchange print to console, this usually means to you do not have the required funds to trades
        if (highBid.getExchange().getExchangeSpecification().getExchangeName().equals(lowAsk.getExchange().getExchangeSpecification().getExchangeName())) {

            System.out.println("###########################################################");
            System.out.println("low ask exchange is the same as high bid exchange");
            System.out.println("You may want to make sure you have required funds to make the trade");
            System.out.println("###########################################################");

            System.out.println("highBid" + highBid.getExchange().getExchangeSpecification().getExchangeName());
            System.out.println("lowAsk" + lowAsk.getExchange().getExchangeSpecification().getExchangeName());
        }


        MarginDiffCompare marginDiffCompare = new MarginDiffCompare();

        //percentage of returns you will make
        BigDecimal expectedDifference = marginDiffCompare.findDiff(lowAsk,highBid);

        //the difference between the arbitrage margin and percentage of returns
        BigDecimal marginSubDiff = marginDiffCompare.diffWithMargin(lowAsk,highBid, getArbitrageMargin());

        //amount chosen to trade
        BigDecimal tradeAmount = BigDecimal.valueOf(getTradeValueBase());
        System.out.println("trade amount" + tradeAmount);

        //currency pair of the lowest ask
        CurrencyPair tradedPair = lowAsk.getCurrencyPair();
        System.out.println(tradedPair);

        //if the abrbitrage margin - the expected percentage of returns is greater than 0
        if (marginSubDiff.compareTo(BigDecimal.ZERO) > 0) {

                //print the expected trade
                System.out.println("==========================================================");
                System.out.println();
                System.out.println("ARBITRAGE DETECTED!!!"
                        + " buy on " + lowAsk.getExchange().getDefaultExchangeSpecification().getExchangeName()
                        + " for " + lowAsk.getAsk()
                        + " and sell on " + highBid.getExchange().getDefaultExchangeSpecification().getExchangeName()
                        + " for " + highBid.getBid());
                System.out.println("initiating trade of " + tradeAmount + " " + tradedPair.base.toString() + " you should make a return (before fees) of "
                        + expectedDifference + "%");
                System.out.println();
                System.out.println("==========================================================");
                round++;
                System.out.println("round:" + round);
                return true;

            } else {
                //else not a good trade, return false
                System.out.println("==========================================================");
                System.out.println();
                System.out.println("No profitable arbitrage found");
                System.out.println("Please make sure you have the needed funds for the trade");
                System.out.println("==========================================================");
                round++;
                System.out.println("round:" + round);
                simpMessagingTemplate.convertAndSend("/topic/traderesults",
                        new TradeResponse(false, "Nothing yet. Retrying... " + round));
            }
            return false;
        }




    /**
     * Make trade method that executes trades on the low ask exchange and the high bid exchange
     * @param lowAsk
     * @param highBid
     */
    public void makeTrade(TickerData lowAsk,
                          TickerData highBid) {

        MarginDiffCompare marginDiffCompare = new MarginDiffCompare();

        //percentage of returns you will make
        BigDecimal expectedDifference = marginDiffCompare.findDiff(lowAsk, highBid);

        //the difference between the arbitrage margin and percentage of returns
        BigDecimal marginSubDiff = marginDiffCompare.diffWithMargin(lowAsk, highBid, getArbitrageMargin());

        //the volume you wish to trade
        BigDecimal tradeAmount = BigDecimal.valueOf(getTradeValueBase());

        //currency pair of the lowest ask
        CurrencyPair tradedPair = lowAsk.getCurrencyPair();

        //MarketOrder object for the buy and sell
        MarketOrder marketOrderBuy = new MarketOrder(Order.OrderType.BID, tradeAmount, tradedPair);
        MarketOrder marketOrderSell = new MarketOrder(Order.OrderType.ASK, tradeAmount, tradedPair);

        System.out.println();
        System.out.println(marketOrderBuy.toString());
        System.out.println(marketOrderSell.toString());
        System.out.println();


        //set marketOrderBuyId, and marketOrderSellId to failed
        String marketOrderBuyId = "failed";
        String marketOrderSellId = "failed";

        //make a fixed thread pool of 2  to submit the order for the low ask and high bid
        ExecutorService executorMakeOrder = Executors.newFixedThreadPool(2);
        CompletionService<OrderIDWrapper> poolMakeOrder = new ExecutorCompletionService<>(executorMakeOrder);
        poolMakeOrder.submit(new

                MakeOrderThread(marketOrderBuy, lowAsk));

        poolMakeOrder.submit(new

                MakeOrderThread(marketOrderSell, highBid));


        //for two loops insert the thread
        for (
                int i = 0;
                i < 2; i++)

        {
            try {

                OrderIDWrapper temp = poolMakeOrder.take().get();
                if (temp.getExchangeName().equals(lowAsk.getExchange().getDefaultExchangeSpecification().getExchangeName())) {
                    marketOrderBuyId = temp.getOrderID();
                    System.out.println(temp.getOrderID());
                }
                if (temp.getExchangeName().equals(highBid.getExchange().getDefaultExchangeSpecification().getExchangeName())) {
                    marketOrderSellId = temp.getOrderID();
                    System.out.println(temp.getOrderID());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorMakeOrder.shutdown();

        //if either the buy order or the sell order did not fail, continue. If they did fail print which order failed
        if (marketOrderBuyId.equals("failed")) System.out.println("marketOrderBuy failed");
        System.out.println("===============================================================");
        if (marketOrderSellId.equals("failed")) System.out.println("marketOrderSell failed");
        System.out.println("===============================================================");
        if (!marketOrderBuyId.equals("failed") && !marketOrderSellId.equals("failed"))

        {   //print the order id for buyId and sellId
            System.out.println("===============================================================");
            System.out.println("buy order " + marketOrderBuyId);
            System.out.println("===============================================================");
            System.out.println("sell order " + marketOrderSellId);
            System.out.println("===============================================================");
            System.out.println("trades successful");
            System.out.println("===============================================================");
        }

        Wallet walletBuy = null;
        Wallet walletSell = null;
        ExecutorService executorWalletWrapper = Executors.newFixedThreadPool(2);
        CompletionService<WalletWrapper> poolWalletWrapper = new ExecutorCompletionService<>(executorWalletWrapper);

        poolWalletWrapper.submit(new GetWalletWrapperThread(lowAsk));
        poolWalletWrapper.submit(new GetWalletWrapperThread(highBid));

        for (int i = 0; i < 2; i++) {
            try {
                WalletWrapper temp = poolWalletWrapper.take().get();
                if (temp.getExchangeName().equals(lowAsk.getExchange().getDefaultExchangeSpecification().getExchangeName())) {
                    walletBuy = temp.getWallet();
                }
                if (temp.getExchangeName().equals(highBid.getExchange().getDefaultExchangeSpecification().getExchangeName())) {
                    walletSell = temp.getWallet();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorWalletWrapper.shutdown();

        // create the tradingData based on trades
        TradingData tradingData =
                new TradingData(
                        (TickerDataTrading) lowAsk,
                        (TickerDataTrading) highBid,
                        walletBuy,
                        walletSell);


        simpMessagingTemplate.convertAndSend("/topic/traderesults",
                new TradeResponse(false,
                        String.format("Ask: %s %s on %s<br>Bid: %s %s on %s",
                                lowAsk.getAsk().toString(),
                                lowAsk.getCurrencyPair().base,
                                lowAsk.getExchange().getExchangeSpecification().getExchangeName(),
                                highBid.getAsk().toString(),
                                highBid.getCurrencyPair().base,
                                highBid.getExchange().getExchangeSpecification().getExchangeName())
                )
        );

        //insert the tradingData into database
        //tradeHistoryService.insertTrades(tradingData);
//        BigDecimal estimatedFees = expectedDifference.subtract(tradingData.getRealDifferenceFormated());

//        String message = "real bid was " + tradingData.getRealBid()
//                + " and real ask was " + tradingData.getRealAsk()
//                + " for a difference (after fees) of " + tradingData.getRealDifferenceFormated()
//                + "% vs an expected of " + expectedDifference + " %"
//                + "/nEstimated fees = " + estimatedFees + tradingData.getCounterName();

    }
}


