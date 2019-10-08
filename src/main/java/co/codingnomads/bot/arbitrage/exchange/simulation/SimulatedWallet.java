package co.codingnomads.bot.arbitrage.exchange.simulation;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Balance;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimulatedWallet {

    private Map<Exchange, Map<Currency, Balance>> balances = new HashMap<>();

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
