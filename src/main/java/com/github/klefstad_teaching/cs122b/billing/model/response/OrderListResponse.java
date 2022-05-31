package com.github.klefstad_teaching.cs122b.billing.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.klefstad_teaching.cs122b.billing.model.Data.Sale;
import com.github.klefstad_teaching.cs122b.core.result.Result;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderListResponse {
    private Result result;
    private List<Sale> sales;

    public Result getResult() {
        return result;
    }

    public OrderListResponse setResult(Result result) {
        this.result = result;
        return this;
    }

    public List<Sale> getSales() {
        return sales;
    }

    public OrderListResponse setSales(List<Sale> sales) {
        this.sales = sales;
        return this;
    }
}
