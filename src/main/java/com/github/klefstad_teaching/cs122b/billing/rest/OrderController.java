package com.github.klefstad_teaching.cs122b.billing.rest;

import com.github.klefstad_teaching.cs122b.billing.model.Data.Item;
import com.github.klefstad_teaching.cs122b.billing.model.Data.Sale;
import com.github.klefstad_teaching.cs122b.billing.model.request.OrderCompleteRequest;
import com.github.klefstad_teaching.cs122b.billing.model.response.ListRetrieveResponse;
import com.github.klefstad_teaching.cs122b.billing.model.response.OrderListResponse;
import com.github.klefstad_teaching.cs122b.billing.model.response.OrderPaymentResponse;
import com.github.klefstad_teaching.cs122b.billing.model.response.ResultResponse;
import com.github.klefstad_teaching.cs122b.billing.repo.BillingRepo;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.nimbusds.jwt.SignedJWT;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;

@RestController
public class OrderController
{
    private final BillingRepo repo;
    private final Validate    validate;

    @Autowired
    public OrderController(BillingRepo repo, Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @GetMapping("/order/payment")
    public ResponseEntity<OrderPaymentResponse> orderPayment(@AuthenticationPrincipal SignedJWT user)
            throws ParseException, StripeException
    {
        List<String> claims = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        List<Item> items = repo.cartRetrieve(claims.contains("PREMIUM"), userId);

        OrderPaymentResponse response;

        if (items.size() == 0) {
            response = new OrderPaymentResponse().setResult(BillingResults.CART_EMPTY);
        } else {
            BigDecimal total = BigDecimal.valueOf(0).setScale(2, RoundingMode.DOWN);
            StringBuilder desc = new StringBuilder();

            for (Item item : items) {
                total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                desc.append(item.getMovieTitle() + ", ");
            }

            Long amountInTotalCents = total.unscaledValue().longValue();
            String description = desc.substring(0, desc.length() - 2);
            String userIdString = Long.toString(userId);

            PaymentIntentCreateParams paymentIntentCreateParams =
                    PaymentIntentCreateParams
                            .builder()
                            .setCurrency("USD")
                            .setDescription(description)
                            .setAmount(amountInTotalCents)
                            .putMetadata("userId", userIdString)
                            .setAutomaticPaymentMethods(
                                    PaymentIntentCreateParams.AutomaticPaymentMethods
                                            .builder()
                                            .setEnabled(true)
                                            .build()
                            )
                            .build();

            PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentCreateParams);

            String paymentIntentId = paymentIntent.getId();
            String clientSecret = paymentIntent.getClientSecret();

            response = new OrderPaymentResponse()
                    .setResult(BillingResults.ORDER_PAYMENT_INTENT_CREATED)
                    .setPaymentIntentId(paymentIntentId)
                    .setClientSecret(clientSecret);
        }

        return ResponseEntity.status(response.getResult().status()).body(response);
    }

    @PostMapping("/order/complete")
    public ResponseEntity<ResultResponse> orderComplete(@AuthenticationPrincipal SignedJWT user,
                                                        @RequestBody OrderCompleteRequest request)
            throws ParseException, StripeException
    {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(request.getPaymentIntentId());
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        List<String> claims = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);

        validate.checkPaymentStatus(paymentIntent);
        validate.checkCorrectUser(paymentIntent, Long.toString(userId));

        List<Item> items = repo.cartRetrieve(claims.contains("PREMIUM"), userId);

        repo.orderComplete(items, userId);

        ResultResponse response = new ResultResponse().setResult(BillingResults.ORDER_COMPLETED);

        return ResponseEntity.status(response.getResult().status()).body(response);
    }

    @GetMapping("/order/list")
    public ResponseEntity<OrderListResponse> orderList(@AuthenticationPrincipal SignedJWT user)
            throws ParseException
    {
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        List<Sale> sales = repo.orderList(userId);

        OrderListResponse response;

        if (sales.size() == 0) {
            response = new OrderListResponse().setResult(BillingResults.ORDER_LIST_NO_SALES_FOUND);
        } else {
            response = new OrderListResponse().setResult(BillingResults.ORDER_LIST_FOUND_SALES).setSales(sales);
        }

        return ResponseEntity.status(response.getResult().status()).body(response);
    }

    @GetMapping("/order/detail/{saleId}")
    public ResponseEntity<ListRetrieveResponse> orderDetail(@AuthenticationPrincipal SignedJWT user,
                                                            @PathVariable Long saleId)
            throws ParseException
    {
        List<String> claims = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        List<Item> items = repo.orderDetail(claims.contains("PREMIUM"), saleId, userId);

        BigDecimal total = BigDecimal.valueOf(0).setScale(2, RoundingMode.DOWN);

        for (Item item : items) {
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        ListRetrieveResponse response;

        if (items.size() == 0) {
            response = new ListRetrieveResponse()
                    .setResult(BillingResults.ORDER_DETAIL_NOT_FOUND);
        } else {
            response = new ListRetrieveResponse()
                    .setResult(BillingResults.ORDER_DETAIL_FOUND)
                    .setItems(items)
                    .setTotal(total);
        }

        return ResponseEntity.status(response.getResult().status()).body(response);
    }
}
