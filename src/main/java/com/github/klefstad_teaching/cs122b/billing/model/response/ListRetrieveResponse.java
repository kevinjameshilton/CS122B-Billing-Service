package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.klefstad_teaching.cs122b.billing.model.Data.Item;
import com.github.klefstad_teaching.cs122b.core.result.Result;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListRetrieveResponse {
    private BigDecimal total;
    private List<Item> items;
    private Result result;

    public BigDecimal getTotal() {
        return total;
    }

    public ListRetrieveResponse setTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public List<Item> getItems() {
        return items;
    }

    public ListRetrieveResponse setItems(List<Item> items) {
        this.items = items;
        return this;
    }

    public Result getResult() {
        return result;
    }

    public ListRetrieveResponse setResult(Result result) {
        this.result = result;
        return this;
    }
}
