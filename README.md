# CS122B Backend 3 - The Billing Service

#### [Config](#config)

#### [Database](#database)

#### [Endpoints](#endpoints)
1. [POST: Cart Insert](#cart-insert)
2. [POST: Cart Update](#cart-update)
3. [DELETE: Cart Delete](#cart-delete)
4. [GET: Cart Retrieve](#cart-retrieve)
5. [POST: Cart Clear](#cart-clear)
6. [GET: Order Payment](#order-payment)
7. [POST: Order Complete](#order-complete)
8. [GET: Order List](#order-list)
9. [GET: Order Detail](#order-detail)

#### [Notes](#notes-1)

## Config
##### `application.yml`

```yml
spring:
  application:
    name: BillingService
  datasource:
    url: jdbc:mysql://localhost:3306
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

server:
  address: 0.0.0.0
  port: 8083
  error: # These settings are for debugging
    include-exception: true
    include-message: always

logging:
  file:
    name: ./BillingService.log

billing:
  stripe-api-key: sk_test_...
``` 

## Database

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">💾 billing.cart</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="175">Column Name</th>
      <th align="left" width="175">Type</th>
      <th align="left">Attributes</th>
    </tr>
    <tr>
      <td>user_id</td>
      <td><code>INT</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td>movie_id</td>
      <td><code>INT</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td>quantity</td>
      <td><code>INT</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr>
      <th colspan="3" align="left">Constraints</th>
    </tr>
    <tr>
      <td colspan="3"><code>PRIMARY KEY</code> <code>(user_id, movie_id)</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td colspan="3"><code>FOREIGN KEY</code> <code>(user_id)</code> <code>REFERENCES</code> <code>idm.user (id)</code> <code>ON UPDATE CASCADE</code> <code>ON DELETE CASCADE</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td colspan="3"><code>FOREIGN KEY</code> <code>(movie_id)</code> <code>REFERENCES</code> <code>movies.movie (id)</code> <code>ON UPDATE CASCADE</code> <code>ON DELETE CASCADE</code></td>
    </tr>
  </tbody>
</table>

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">💾 billing.sale</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="175">Column Name</th>
      <th align="left" width="175">Type</th>
      <th align="left">Attributes</th>
    </tr>
    <tr>
      <td>id</td>
      <td><code>INT</code></td>
      <td><code>NOT NULL</code> <code>PRIMARY KEY</code> <code>AUTO_INCREMENT</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td>user_id</td>
      <td><code>INT</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td>total</td>
      <td><code>DECMIAL(19,4)</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td>order_date</td>
      <td><code>TIMESTAMP</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr>
      <th colspan="3" align="left">Constraints</th>
    </tr>
    <tr>
      <td colspan="3"><code>FOREIGN KEY</code> <code>(user_id)</code> <code>REFERENCES</code> <code>idm.user (id)</code> <code>ON UPDATE CASCADE</code> <code>ON DELETE CASCADE</code></td>
    </tr>
  </tbody>
</table>

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">💾 billing.sale_item</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="175">Column Name</th>
      <th align="left" width="175">Type</th>
      <th align="left">Attributes</th>
    </tr>
    <tr>
      <td>sale_id</td>
      <td><code>INT</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td>movie_id</td>
      <td><code>INT</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td>quantity</td>
      <td><code>INT</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr>
      <th colspan="3" align="left">Constraints</th>
    </tr>
    <tr>
      <td colspan="3"><code>PRIMARY KEY</code> <code>(sale_id, movie_id)</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td colspan="3"><code>FOREIGN KEY</code> <code>(sale_id)</code> <code>REFERENCES</code> <code>billing.sale (id)</code> <code>ON UPDATE CASCADE</code> <code>ON DELETE CASCADE</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td colspan="3"><code>FOREIGN KEY</code> <code>(movie_id)</code> <code>REFERENCES</code> <code>movies.movie (id)</code> <code>ON UPDATE CASCADE</code> <code>ON DELETE CASCADE</code></td>
    </tr>
  </tbody>
</table>

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">💾 billing.movie_price</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="175">Column Name</th>
      <th align="left" width="175">Type</th>
      <th align="left">Attributes</th>
    </tr>
    <tr>
      <td>movie_id</td>
      <td><code>INT</code></td>
      <td><code>NOT NULL</code> <code>PRIMARY KEY</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td>unit_price</td>
      <td><code>DECIMAL(19,4)</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td>premium_discount</td>
      <td><code>INT</code></td>
      <td><code>NOT NULL</code></td>
    </tr>
    <tr>
      <th colspan="3" align="left">Constraints</th>
    </tr>
    <tr>
      <td colspan="3"><code>CHECK</code> <code>(premium_discount BETWEEN 0 AND 25)</code></td>
    </tr>
    <tr></tr>
    <tr>
      <td colspan="3"><code>FOREIGN KEY</code> <code>(movie_id)</code> <code>REFERENCES</code> <code>movies.movie (id)</code> <code>ON UPDATE CASCADE</code> <code>ON DELETE CASCADE</code></td>
    </tr>
  </tbody>
</table>

# Endpoints

### Order of Validation
All <code>❗ 400: Bad Request</code> Results must be checked first, and returned before any other action is made. \
The order of the checks within <code>❗ 400: Bad Request</code> is not tested as each Result is tested individually.

### JsonInclude
In the case of non-successful results, where values are expected, the values should not be included, for example.
```json
{
   "result": {
      "code": 32,
      "message": "Data contains invalid integers"
   },
   "value": null 
}
```
the <code>value</code> key should not be included: 
```json
{
   "result": {
      "code": 32,
      "message": "Data contains invalid integers"
   }
}
```
This is done by insuring that all <code>null</code> values are dropped by either:
- Having your Model extend <code>ResponseModel<Model></code>, or
- Putting the <code>@JsonInclude(JsonInclude.Include.NON_NULL)</code> on your Model class
  
### Result
All <code>Result</code> objects are avaible as static constants inside of the <code>com.github.klefstad_teaching.cs122b.core.result.BillingResults</code> class.
These can be used rather than creating your own.

## Cart Insert
Insert a given <code>movieId</code> into a user's cart with the given <code>quantity</code>

### Path
```http 
POST /cart/insert
```

### API

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">📨&nbsp;&nbsp;Headers</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Header</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>Authorization</td>
      <td><code>String</code></td>
      <td>User's bearer token</td>
    </tr>
    <tr></tr>
    <tr>
      <td>Transaction-ID</td>
      <td><code>String</code></td>
      <td>Request's transaction id from the gateway service</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📥 Request</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
movieId: Long
quantity: Integer</pre></td>
      <td align="left"><pre lang="json">
{
     "movieId": 4154796,
     "quantity": 2
}
</pre></td>
    <tr>
      <th align="left">Key</th>
      <th align="left">Required</th>
      <th align="left">Description </th>
    </tr>
    <tr>
      <td><code>movieId</code></td><td><code>Yes</code></td><td>Movie Id of the movie to insert</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>quantity</code></td><td><code>Yes</code></td><td>Quantity of the movie to add, must be between <code>[1-10]</code> (inclusive)</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📤 Response</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
result: Result
    code: Integer
    message: String</pre></td>
      <td align="left"><pre lang="json">
{
    "result": {
        "code": 3010,
        "message": "Item inserted into cart"
    }
}</pre></td>
    </tr>    
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📦 Results</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="200">Status</th>
      <th align="left">Code</th>
      <th align="left">Message</th>
    </tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3010</td>
      <td>Item inserted into cart</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>❗ 400: Bad Request</code></td>
      <td>3000</td>
      <td>Quantity cannot be zero or less</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>❗ 400: Bad Request</code></td>
      <td>3001</td>
      <td>Quantity exceeded max limit</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>❗ 409: Conflict</code></td>
      <td>3002</td>
      <td>Item already in cart</td>
    </tr>
  </tbody>
</table>

## Cart Update
Updates the given <code>movieId</code> <code>quantity</code> in the user's cart.

### Path
```http 
POST /cart/update
```

### API

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">📨&nbsp;&nbsp;Headers</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Header</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>Authorization</td>
      <td><code>String</code></td>
      <td>User's bearer token</td>
    </tr>
    <tr></tr>
    <tr>
      <td>Transaction-ID</td>
      <td><code>String</code></td>
      <td>Request's transaction id from the gateway service</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📥 Request</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
movieId: Long
quantity: Integer</pre></td>
      <td align="left"><pre lang="json">
{
     "movieId": 4154796,
     "quantity": 2
}
</pre></td>
    <tr>
      <th align="left">Key</th>
      <th align="left">Required</th>
      <th align="left">Description </th>
    </tr>
    <tr>
      <td><code>email</code></td><td><code>Yes</code></td><td>User's email</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>movieId</code></td><td><code>Yes</code></td><td>Movie Id of the movie to update</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>quantity</code></td><td><code>Yes</code></td><td>Quantity of the movie to update, must be between <code>[1-10]</code> (inclusive)</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📤 Response</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
result: Result
    code: Integer
    message: String</pre></td>
      <td align="left"><pre lang="json">
{
    "result": {
        "code": 3020,
        "message": "Item in cart updated"
    }
}</pre></td>
    </tr>    
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📦 Results</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="200">Status</th>
      <th align="left">Code</th>
      <th align="left">Message</th>
    </tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3020</td>
      <td>Item in cart updated</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>❗ 400: Bad Request</code></td>
      <td>3000</td>
      <td>Quantity cannot be zero or less</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>❗ 400: Bad Request</code></td>
      <td>3001</td>
      <td>Quantity exceeded max limit</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>❗ 409: Conflict</code></td>
      <td>3003</td>
      <td>Item not in cart</td>
    </tr>
  </tbody>
</table>

## Cart Delete
Deletes the given <code>movieId</code> from the user's cart

### Path
```http 
DELETE /cart/delete/{movieId}
```

### API

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">📨&nbsp;&nbsp;Headers</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Header</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>Authorization</td>
      <td><code>String</code></td>
      <td>User's bearer token</td>
    </tr>
    <tr></tr>
    <tr>
      <td>Transaction-ID</td>
      <td><code>String</code></td>
      <td>Request's transaction id from the gateway service</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">🔖&nbsp;&nbsp;Path</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Parameter</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>movieId</td>
      <td><code>Long</code></td>
      <td>Movie id of the movie to delete from cart</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📤 Response</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
result: Result
    code: Integer
    message: String</pre></td>
      <td align="left"><pre lang="json">
{
    "result": {
        "code": 3030,
        "message": "Item deleted from cart"
    }
}</pre></td>
    </tr>    
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📦 Results</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="200">Status</th>
      <th align="left">Code</th>
      <th align="left">Message</th>
    </tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3030</td>
      <td>Item deleted from cart</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>❗ 409: Conflict</code></td>
      <td>3003</td>
      <td>Item not in cart</td>
    </tr>
  </tbody>
</table>

## Cart Retrieve
Retrieve's all items from the user's cart with some movie details

### Path
```http 
GET /cart/retrieve
```

### API

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">📨&nbsp;&nbsp;Headers</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Header</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>Authorization</td>
      <td><code>String</code></td>
      <td>User's bearer token</td>
    </tr>
    <tr></tr>
    <tr>
      <td>Transaction-ID</td>
      <td><code>String</code></td>
      <td>Request's transaction id from the gateway service</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📤 Response</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
result: Result
    code: Integer
    message: String
total: BigDecimal
items: Item[]
    unitPrice: BigDecimal
    quantity: Integer
    movieId: Long
    movieTitle: String
    backdropPath: String
    posterPath: String</pre></td>
      <td align="left"><pre lang="json">
{
    "result": {
        "code": 3040,
        "message": "Cart items retrieved"
    },
    "total": 10.25,
    "items": [
        {
            "unitPrice": 10.25,
            "quantity": 2,
            "movieId": 4154796,
            "movieTitle": "Avengers: Endgame",
            "backdropPath": "/7RyHsO4yDXtBv1zUU3mTpHeQ0d5.jpg",
            "posterPath": "/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
        }
    ]
}</pre></td>
    </tr>    
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📦 Results</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="200">Status</th>
      <th align="left">Code</th>
      <th align="left">Message</th>
    </tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3040</td>
      <td>Cart items retrieved</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3004</td>
      <td>Cart is empty</td>
    </tr>
  </tbody>
</table>

## Cart Clear
Clears all movies from the user's cart

### Path
```http 
POST /cart/clear
```

### API

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">📨&nbsp;&nbsp;Headers</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Header</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>Authorization</td>
      <td><code>String</code></td>
      <td>User's bearer token</td>
    </tr>
    <tr></tr>
    <tr>
      <td>Transaction-ID</td>
      <td><code>String</code></td>
      <td>Request's transaction id from the gateway service</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📤 Response</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
result: Result
    code: Integer
    message: String</pre></td>
      <td align="left"><pre lang="json">
{
    "result": {
        "code": 3050,
        "message": "Items have been cleared from cart"
    }
}</pre></td>
    </tr>    
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📦 Results</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="200">Status</th>
      <th align="left">Code</th>
      <th align="left">Message</th>
    </tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3050</td>
      <td>Items have been cleared from cart</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3004</td>
      <td>Cart is empty</td>
    </tr>
  </tbody>
</table>

## Order Payment
Creates a <code>PaymentIntent</code> with <code>Stripe</code>

### PaymentIntent
The PaymentIntent should be created with these three properties
1. **Amount:** The total amount of the carts contents. When applying discount we apply the discount to the <code>unitPrice</code> first with <code>RoundingMode.DOWN</code> and a scale of <code>2</code>. Refer to [Notes](#notes) on how to deal with <code>BigDecimal</code>'s especially the last part with dealing with scale and rounding.
2. **Description:** The description of the movie's titles in list format (<title>, <title>, ... , <title>). 
3. **Metadata:** The key-value pair of "userId": <userId stored in the user's JWT>

Returning the newly created <code>PaymentIntent</code>'s <code>id</code> and <code>clientSecret</code>

### Path
```http 
POST /order/payment
```

### API

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">📨&nbsp;&nbsp;Headers</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Header</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>Authorization</td>
      <td><code>String</code></td>
      <td>User's bearer token</td>
    </tr>
    <tr></tr>
    <tr>
      <td>Transaction-ID</td>
      <td><code>String</code></td>
      <td>Request's transaction id from the gateway service</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📤 Response</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
result: Result
    code: Integer
    message: String
paymentIntentId: String
clientSecret: String</pre></td>
      <td align="left"><pre lang="json">
{
    "result": {
        "code": 3060,
        "message": "Order Payment Intent Created"
    },
    "paymentIntentId": "pi_..."
    "clientSecret": "pi_..."
}</pre></td>
    </tr>    
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📦 Results</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="300">Status</th>
      <th align="left">Code</th>
      <th align="left">Message</th>
    </tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3060</td>
      <td>Order Payment Intent Created</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3004</td>
      <td>Cart is empty</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>❗ 500: Internal Server Error</code></td>
      <td>3005</td>
      <td>Stripe encountered and error</td>
    </tr>
  </tbody>
</table>

## Order Complete
Once the order payment is accepted on the frontend we must retrieve the <code>PaymentIntent</code> from <code>Stripe</code> by calling <code>PaymentIntent.retrieve("paymentIntentId")</code> and verifying it.

### Verification
PaymentIntent is verified by checking these two things:
1. **Payment Status:** We verify the status by ensuring that <code>paymentIntent.getStatus()</code> is <code>"succeeded"</code>. 
2. **Correct User:** We verify that the user's <code>userId</code> calling this endpoint has the same id as the one stored in this <code>PaymentIntent</code>'s meta data by calling <code>paymentIntent.getMetadata().get("userId")</code>

Once we verify that the payment has succeeded then we create a new <code>billing.sale</code> record and then populate the <code>billing.sale_item</code> with the contents of the user's <code>billing.cart</code>. Once that is done the current users cart must be cleared.

### Path
```http 
GET /order/complete
```

### API

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">📨&nbsp;&nbsp;Headers</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Header</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>Authorization</td>
      <td><code>String</code></td>
      <td>User's bearer token</td>
    </tr>
    <tr></tr>
    <tr>
      <td>Transaction-ID</td>
      <td><code>String</code></td>
      <td>Request's transaction id from the gateway service</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📤 Response</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
result: Result
    code: Integer
    message: String
paymentIntentId: String</pre></td>
      <td align="left"><pre lang="json">
{
    "result": {
        "code": 3070,
        "message": "Order Completed"
    },
    "paymentIntentId": "pi_..."
}</pre></td>
    </tr>    
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📦 Results</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="200">Status</th>
      <th align="left">Code</th>
      <th align="left">Message</th>
    </tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3070</td>
      <td>Order Completed</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>❗ 400: Bad Request</code></td>
      <td>3071</td>
      <td>The order has not yet been paid</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>❗ 400: Bad Request</code></td>
      <td>3072</td>
      <td>The order is not meant for this user</td>
    </tr>
  </tbody>
</table>

## Order List
Return a list of sales found for the given user. To keep this endpoint simple return only the last <code>five</code> sales ordered by the most recent sales first.


### Path
```http 
GET /order/list
```

### API

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">📨&nbsp;&nbsp;Headers</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Header</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>Authorization</td>
      <td><code>String</code></td>
      <td>User's bearer token</td>
    </tr>
    <tr></tr>
    <tr>
      <td>Transaction-ID</td>
      <td><code>String</code></td>
      <td>Request's transaction id from the gateway service</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📤 Response</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
result: Result
    code: Integer
    message: String
sales: Sale[]
    saleId: Long
    total: BigDecimal
    orderDate: Instant</pre></td>
      <td align="left"><pre lang="json">
{
    "result": {
        "code": 3080,
        "message": "Sales found for user"
    },
    "sales": [
        {
          "saleId": 2,
          "total": 299.30,
          "orderDate": "2022-01-02T20:00:00Z"
        },
        {
          "saleId": 1,
          "total": 234.45,
          "orderDate": "2022-01-01T20:00:00Z"
        }
    ]
}</pre></td>
    </tr>    
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📦 Results</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="200">Status</th>
      <th align="left">Code</th>
      <th align="left">Message</th>
    </tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3080</td>
      <td>Sales found for user</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3081</td>
      <td>No sales found for user</td>
    </tr>
  </tbody>
</table>


## Order Detail
Return a detailed view of a given sale by its saleId


### Path
```http 
GET /order/detail/{saleId}
```

### API

<table>
  <tbody>
    <tr>
      <th colspan="3" align="left" width="1100">📨&nbsp;&nbsp;Headers</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Header</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>Authorization</td>
      <td><code>String</code></td>
      <td>User's bearer token</td>
    </tr>
    <tr></tr>
    <tr>
      <td>Transaction-ID</td>
      <td><code>String</code></td>
      <td>Request's transaction id from the gateway service</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">🔖&nbsp;&nbsp;Path</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left">Parameter</th>
      <th align="left">Type</th>
      <th align="left">Description</th>
    </tr>
    <tr>
      <td>saleId</td>
      <td><code>Long</code></td>
      <td>Sale Id of the sale to retrieve</td>
    </tr>
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📤 Response</th>
    </tr>
    <tr></tr>
    <tr>
      <th colspan="2" align="left">Model </th>
      <th align="left">Example </th>
    </tr>
    <tr>
      <td colspan="2" align="left"><pre lang="yml">
result: Result
    code: Integer
    message: String
total: BigDecimal
items: Item[]
    unitPrice: BigDecimal
    quantity: Integer
    movieId: Long
    movieTitle: String
    backdropPath: String
    posterPath: String</pre></td>
      <td align="left"><pre lang="json">
{
    "result": {
        "code": 3090,
        "message": "Sale details found for saleId"
    },
    "total": 10.25,
    "items": [
        {
            "unitPrice": 10.25,
            "quantity": 2,
            "movieId": 4154796,
            "movieTitle": "Avengers: Endgame",
            "backdropPath": "/7RyHsO4yDXtBv1zUU3mTpHeQ0d5.jpg",
            "posterPath": "/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
        }
    ]
}</pre></td>
    </tr>    
    <tr><td colspan="3" ></td></tr>
    <tr></tr>
    <tr>
      <th colspan="3" align="left">📦 Results</th>
    </tr>
    <tr></tr>
    <tr>
      <th align="left" width="200">Status</th>
      <th align="left">Code</th>
      <th align="left">Message</th>
    </tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3090</td>
      <td>Sale details found for saleId</td>
    </tr>
    <tr></tr>
    <tr>
      <td><code>✅ 200: Ok</code></td>
      <td>3091</td>
      <td>No sale details found for saleId</td>
    </tr>
  </tbody>
</table>

# Notes

## BigDecimal

When dealing with currency we want to ensure that we are working with the same scale and we are rounding in a consistent manner when applying discounts.

For this we will be using Java's <code>BigDecimal</code> class. This allows us the control we need and also provides us with the numbers string format.

### Important
When calling methods to calculate a <code>BigDecimal</code> a **NEW** <code>BigDecimal</code> is created and returned. The <code>BigDecimal</code> whos method was called **IS NOT MODIFIED**. This is because all instances of <code>BigDecimal</code> are **IMMUTABLE**.

```java
BigDecimal first = BigDecimal.valueOf(1);
BigDecimal second = BigDecimal.valueOf(2);

first.add(second); // Because the return value is not saved this does nothing

System.out.println(first); // prints 1 as it is Immutable

BigDecimal sum = first.add(second); 

System.out.println(sum); // prints 3 because the new instance of BigDecimal when calling .add() is saved to sum
```

### Common Pattern

When working with <code>BigDecimal</code> it is usually common to keep *overriding* the initial value with the new value.

```java
BigDecimal total = BigDecimal.ZERO;

total = total.add(BigDecimal.valueOf(1.5));  // Overwrites total with the new value
total = total.add(BigDecimal.valueOf(2.5));  // Overwrites total with the new value
total = total.add(BigDecimal.valueOf(0.01)); // Overwrites total with the new value

System.out.println(total); // prints 4.01

```

### Scale and RoundingMode

One of the primary uses of <code>BigDecimal</code> is the ability to set the <code>scale</code> or in other terms, the amount of places after the decimal that we care about.

```java
BigDecimal hundreds = BigDecimal.valueOf(1).setScale(2);

System.out.println(hundreds); // prints 1.00

BigDecimal thousands = BigDecimal.valueOf(1).setScale(3);

System.out.println(thousands); // prints 1.000
```

This also helps when we need to round the value:

```java
BigDecimal num = BigDecimal.valueOf(1.555);

// Setting the scale to 1
System.out.println(num.setScale(1, RoundingMode.DOWN));    // prints 1.5

System.out.println(num.setScale(1, RoundingMode.UP));      // prints 1.6

System.out.println(num.setScale(1, RoundingMode.HALF_UP)); // prints 1.6


// Setting the scale to 2
System.out.println(num.setScale(2, RoundingMode.DOWN));    // prints 1.55

System.out.println(num.setScale(2, RoundingMode.UP));      // prints 1.56

System.out.println(num.setScale(2, RoundingMode.HALF_UP)); // prints 1.56


// THROWS ERROR Because there is a remainder left after dividing because
// RoundingMode.UNNECESSARY is the same as saying: There should be no remainder
System.out.println(num.setScale(1, RoundingMode.UNNECESSARY)); // Throws Error
```

We can also just do all the math we need then apply the <code>RoundingMode</code> when we set the <code>scale</code>

```java
BigDecimal discounted = BigDecimal.valueOf(19.99).multiply(BigDecimal.valueOf(0.85)); 

discounted = discounted.setScale(2, RoundingMode.DOWN);
```
  
