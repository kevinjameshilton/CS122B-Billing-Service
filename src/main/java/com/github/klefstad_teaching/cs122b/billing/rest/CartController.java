package com.github.klefstad_teaching.cs122b.billing.rest;

import com.github.klefstad_teaching.cs122b.billing.model.Data.Item;
import com.github.klefstad_teaching.cs122b.billing.model.request.CartInsertUpdateRequest;
import com.github.klefstad_teaching.cs122b.billing.model.response.ResultResponse;
import com.github.klefstad_teaching.cs122b.billing.model.response.ListRetrieveResponse;
import com.github.klefstad_teaching.cs122b.billing.repo.BillingRepo;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;

@RestController
public class CartController
{
    private final BillingRepo repo;
    private final Validate    validate;

    @Autowired
    public CartController(BillingRepo repo, Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @PostMapping("/cart/insert")
    public ResponseEntity<ResultResponse> cartInsert(@AuthenticationPrincipal SignedJWT user,
                                                     @RequestBody CartInsertUpdateRequest request) throws ParseException
    {
        validate.checkQuantity(request.getQuantity());

        // List<String> claims = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        try {
            repo.cartInsert(request, userId);
        } catch (DuplicateKeyException e) {
            throw new ResultError(BillingResults.CART_ITEM_EXISTS);
        }

        ResultResponse response = new ResultResponse().setResult(BillingResults.CART_ITEM_INSERTED);

        return ResponseEntity.status(response.getResult().status()).body(response);
    }

    @PostMapping("/cart/update")
    public ResponseEntity<ResultResponse> cartUpdate(@AuthenticationPrincipal SignedJWT user,
                                                     @RequestBody CartInsertUpdateRequest request) throws ParseException
    {
        validate.checkQuantity(request.getQuantity());

        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        Integer numRowsAffected = repo.cartUpdate(request, userId);

        if (numRowsAffected == 0) {
            throw new ResultError(BillingResults.CART_ITEM_DOES_NOT_EXIST);
        }

        ResultResponse response = new ResultResponse().setResult(BillingResults.CART_ITEM_UPDATED);

        return ResponseEntity.status(response.getResult().status()).body(response);
    }

    @DeleteMapping("/cart/delete/{movieId}")
    public ResponseEntity<ResultResponse> cartDelete(@AuthenticationPrincipal SignedJWT user,
                                                     @PathVariable Long movieId) throws ParseException
    {
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        Integer numRowsAffected = repo.cartDelete(movieId, userId);

        if (numRowsAffected == 0) {
            throw new ResultError(BillingResults.CART_ITEM_DOES_NOT_EXIST);
        }

        ResultResponse response = new ResultResponse().setResult(BillingResults.CART_ITEM_DELETED);

        return ResponseEntity.status(response.getResult().status()).body(response);
    }

    @GetMapping("/cart/retrieve")
    public ResponseEntity<ListRetrieveResponse> cartRetrieve(@AuthenticationPrincipal SignedJWT user)
            throws ParseException
    {
        List<String> claims = user.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);

        List<Item> items = repo.cartRetrieve(claims.contains("PREMIUM"), userId);

        BigDecimal total = BigDecimal.valueOf(0).setScale(2, RoundingMode.DOWN);

        for (Item item : items) {
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        ListRetrieveResponse response;

        if (items.size() == 0) {
            response = new ListRetrieveResponse()
                    .setResult(BillingResults.CART_EMPTY);
        } else {
            response = new ListRetrieveResponse()
                    .setResult(BillingResults.CART_RETRIEVED)
                    .setItems(items)
                    .setTotal(total);
        }

        return ResponseEntity.status(response.getResult().status()).body(response);
    }

    @PostMapping("/cart/clear")
    public ResponseEntity<ResultResponse> cartClear(@AuthenticationPrincipal SignedJWT user)
            throws ParseException
    {
        Long userId = user.getJWTClaimsSet().getLongClaim(JWTManager.CLAIM_ID);
        Integer numRowsAffected = repo.cartClear(userId);

        ResultResponse response;

        if (numRowsAffected == 0) {
            response = new ResultResponse()
                    .setResult(BillingResults.CART_EMPTY);
        } else {
            response = new ResultResponse()
                    .setResult(BillingResults.CART_CLEARED);
        }

        return ResponseEntity.status(response.getResult().status()).body(response);
    }
}
