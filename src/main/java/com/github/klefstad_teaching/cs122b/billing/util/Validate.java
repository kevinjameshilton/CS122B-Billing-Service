package com.github.klefstad_teaching.cs122b.billing.util;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Component;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;

@Component
public final class Validate
{
    public static void checkQuantity(Integer quantity) throws ResultError
    {
        if (quantity < 1) {
            throw new ResultError(BillingResults.INVALID_QUANTITY);
        } else if (quantity > 10) {
            throw new ResultError(BillingResults.MAX_QUANTITY);
        }
    }

    public static void checkPaymentStatus(PaymentIntent paymentIntent) throws ResultError
    {
        if (!paymentIntent.getStatus().equals("succeeded")) {
            throw new ResultError(BillingResults.ORDER_CANNOT_COMPLETE_NOT_SUCCEEDED);
        }
    }

    public static void checkCorrectUser(PaymentIntent paymentIntent, String userId) throws ResultError
    {
        if (!paymentIntent.getMetadata().get("userId").equals(userId)) {
            throw new ResultError(BillingResults.ORDER_CANNOT_COMPLETE_WRONG_USER);
        }
    }

}
