package co.codingnomads.bot.arbitrage.exchange.simulation;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Balance;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SimulatedWallet {

    private Map<Exchange, Map<Currency, Balance>> balances = new ConcurrentHashMap<>();

    public SimulatedWallet() {}

    public SimulatedWallet(Map<Exchange, Map<Currency, Balance>> balances) {
        this.balances = balances;
    }

    public Map<Exchange, Map<Currency, Balance>> getAllBalances() { return balances; }

    public void putBalance(Exchange exchange, Currency currency, Balance balance) {
        balances.computeIfAbsent(exchange, $ -> new HashMap<>());
        balances.get(exchange).put(currency, balance);
    }

    public Balance getBalance(Exchange exchange, Currency currency) {
        balances.computeIfAbsent(exchange, $ -> new HashMap<>());
        return balances.get(exchange).get(currency);
    }

    public Collection<Balance> getBalances(Exchange exchange) {
        return balances.get(exchange).values();
    }

    public void add(Exchange exchange, Currency currency, BigDecimal amount) {
        balances.computeIfAbsent(exchange, $ -> new HashMap<>());
        Map<Currency, Balance> currencyBalanceMap = balances.get(exchange);
        Balance balance = currencyBalanceMap.get(currency);
        if (balance == null) {
            currencyBalanceMap.put(currency, new Balance(currency, amount));
        } else {
            currencyBalanceMap.put(currency, new Balance(currency, balance.getTotal().add(amount)));
        }
    }

    public void sub(Exchange exchange, Currency currency, BigDecimal amount) {
        balances.computeIfAbsent(exchange, $ -> new HashMap<>());
        Map<Currency, Balance> currencyBalanceMap = balances.get(exchange);
        Balance balance = currencyBalanceMap.get(currency);
        if (balance != null) {
            BigDecimal difference = balance.getTotal().subtract(amount);
            if (difference.compareTo(BigDecimal.ZERO) < 0) { difference = BigDecimal.ZERO; }
            currencyBalanceMap.put(currency, new Balance(currency, difference));
        }
    }

}
