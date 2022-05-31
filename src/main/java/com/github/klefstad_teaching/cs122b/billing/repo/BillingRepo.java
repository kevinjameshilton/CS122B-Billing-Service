package com.github.klefstad_teaching.cs122b.billing.repo;

import com.github.klefstad_teaching.cs122b.billing.model.Data.Item;
import com.github.klefstad_teaching.cs122b.billing.model.Data.Sale;
import com.github.klefstad_teaching.cs122b.billing.model.request.CartInsertUpdateRequest;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Types;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class BillingRepo
{
    private final NamedParameterJdbcTemplate template;
    private static final Logger LOG = LoggerFactory.getLogger(Validate.class);

    @Autowired
    public BillingRepo(NamedParameterJdbcTemplate template)
    {
        this.template = template;
    }

    //language=sql
    private final static String CART_INSERT =
            "INSERT INTO billing.cart " +
                    "VALUES (:userId, :movieId, :quantity);";

    //language=sql
    private final static String CART_UPDATE =
            "UPDATE billing.cart " +
                    "SET cart.quantity = :quantity " +
                    "WHERE cart.user_id = :userId AND cart.movie_id = :movieId;";

    //language=sql
    private final static String CART_DELETE =
            "DELETE FROM billing.cart " +
                    "WHERE cart.user_id = :userId AND cart.movie_id = :movieId;";

    //language=sql
    private final static String CART_RETRIEVE =
            "SELECT movie_price.unit_price, cart.quantity, cart.movie_id, movie.title, " +
                    "movie.backdrop_path, movie.poster_path, movie_price.premium_discount " +
                "FROM billing.cart " +
                "JOIN billing.movie_price ON cart.movie_id = movie_price.movie_id " +
                "JOIN movies.movie ON cart.movie_id = movie.id " +
                "WHERE cart.user_id = :userId;";

    //language=sql
    private final static String CART_CLEAR =
            "DELETE FROM billing.cart " +
                    "WHERE cart.user_id = :userId;";

    //language=sql
    private final static String SALE_INSERT =
            "INSERT INTO billing.sale (user_id, total, order_date) " +
                    "VALUES (:userId, :total, :orderDate);";

    //language=sql
    private final static String SALE_ITEM_INSERT =
            "INSERT INTO billing.sale_item (sale_id, movie_id, quantity) " +
                    "VALUES (:saleId, :movieId, :quantity);";

    //language=sql
    private final static String ORDER_LIST =
            "SELECT id, total, order_date " +
                    "FROM billing.sale " +
                    "WHERE sale.user_id = :userId " +
                    "ORDER BY order_date DESC " +
                    "LIMIT 5;";

    //language=sql
    private final static String ORDER_DETAIL =
            "SELECT movie_price.unit_price, sale_item.quantity, sale_item.movie_id, movie.title, " +
                    "movie.backdrop_path, movie.poster_path, movie_price.premium_discount " +
                    "FROM billing.sale_item " +
                    "JOIN billing.movie_price ON sale_item.movie_id = movie_price.movie_id " +
                    "JOIN movies.movie ON sale_item.movie_id = movie.id " +
                    "JOIN billing.sale ON sale_item.sale_id = sale.id " +
                    "WHERE sale_item.sale_id = :saleId AND sale.user_id = :userId;";

    public void cartInsert(CartInsertUpdateRequest request, Long userId) throws DuplicateKeyException
    {
        MapSqlParameterSource source = new MapSqlParameterSource();

        source.addValue("userId", userId, Types.BIGINT);
        source.addValue("movieId", request.getMovieId(), Types.BIGINT);
        source.addValue("quantity", request.getQuantity(), Types.INTEGER);

        this.template.update(CART_INSERT, source);
    }

    public Integer cartUpdate(CartInsertUpdateRequest request, Long userId)
    {
        MapSqlParameterSource source = new MapSqlParameterSource();

        source.addValue("userId", userId, Types.BIGINT);
        source.addValue("movieId", request.getMovieId(), Types.BIGINT);
        source.addValue("quantity", request.getQuantity(), Types.INTEGER);

        Integer numRowsAffected = this.template.update(CART_UPDATE, source);

        return numRowsAffected;
    }

    public Integer cartDelete(Long movieId, Long userId)
    {
        MapSqlParameterSource source = new MapSqlParameterSource();

        source.addValue("userId", userId, Types.BIGINT);
        source.addValue("movieId", movieId, Types.BIGINT);

        Integer numRowsAffected = this.template.update(CART_DELETE, source);

        return numRowsAffected;
    }

    public List<Item> cartRetrieve(Boolean isPremium, Long userId)
    {
        // LOG.info("isPremium: " + isPremium.toString());

        List<Item> items = this.template.query(
                CART_RETRIEVE,
                new MapSqlParameterSource()
                        .addValue("userId", userId, Types.BIGINT),
                (rs, rowNum) ->
                        new Item()
                                .setMovieId(rs.getLong("cart.movie_id"))
                                .setBackdropPath(rs.getString("movie.backdrop_path"))
                                .setMovieTitle(rs.getString("movie.title"))
                                .setQuantity(rs.getInt("cart.quantity"))
                                .setPosterPath(rs.getString("movie.poster_path"))
                                .setUnitPrice(
                                        BigDecimal.valueOf(isPremium ? rs.getDouble("movie_price.unit_price")
                                                        * (1 - (rs.getInt("movie_price.premium_discount") / 100.0))
                                                        : rs.getDouble("movie_price.unit_price"))
                                        .setScale(2, RoundingMode.DOWN))
        );

        return items;
    }

    public Integer cartClear(Long userId)
    {
        Integer numRowsAffected = this.template.update(CART_CLEAR, new MapSqlParameterSource().addValue("userId", userId, Types.BIGINT));

        return numRowsAffected;
    }

    public void orderComplete(List<Item> items, Long userId) {
        MapSqlParameterSource source = new MapSqlParameterSource();

        BigDecimal total = BigDecimal.valueOf(0).setScale(4, RoundingMode.UNNECESSARY);

        for (Item item : items) {
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        source.addValue("userId", userId, Types.BIGINT);
        source.addValue("total", total.doubleValue(), Types.DOUBLE);
        source.addValue("orderDate", Date.from(Instant.now()), Types.TIMESTAMP);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.template.update(SALE_INSERT, source, keyHolder);

        for (Item item : items) {
            this.template.update(SALE_ITEM_INSERT,
                    new MapSqlParameterSource()
                            .addValue("saleId", keyHolder.getKey(), Types.INTEGER)
                            .addValue("movieId", item.getMovieId(), Types.BIGINT)
                            .addValue("quantity", item.getQuantity(), Types.INTEGER));
        }

        this.cartClear(userId);
    }

    public List<Sale> orderList(Long userId)
    {
        List<Sale> sales = this.template.query(ORDER_LIST,
                new MapSqlParameterSource()
                        .addValue("userId", userId, Types.BIGINT),
                (rs, rowNum) ->
                        new Sale()
                                .setSaleId(rs.getLong("id"))
                                .setOrderDate(rs.getTimestamp("order_date").toInstant())
                                .setTotal(BigDecimal.valueOf(rs.getDouble("total"))
                                        .setScale(2, RoundingMode.DOWN))
        );

        return sales;
    }

    public List<Item> orderDetail(Boolean isPremium, Long saleId, Long userId)
    {
        // LOG.info("isPremium: " + isPremium.toString());

        List<Item> items = this.template.query(
                ORDER_DETAIL,
                new MapSqlParameterSource()
                        .addValue("saleId", saleId, Types.BIGINT)
                        .addValue("userId", userId, Types.BIGINT),
                (rs, rowNum) ->
                        new Item()
                                .setMovieId(rs.getLong("sale_item.movie_id"))
                                .setBackdropPath(rs.getString("movie.backdrop_path"))
                                .setMovieTitle(rs.getString("movie.title"))
                                .setQuantity(rs.getInt("sale_item.quantity"))
                                .setPosterPath(rs.getString("movie.poster_path"))
                                .setUnitPrice(
                                        BigDecimal.valueOf(isPremium ? rs.getDouble("movie_price.unit_price")
                                                        * (1 - (rs.getInt("movie_price.premium_discount") / 100.0))
                                                        : rs.getDouble("movie_price.unit_price"))
                                                .setScale(2, RoundingMode.DOWN))
        );

        return items;
    }
}
