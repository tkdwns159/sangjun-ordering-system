-- restaurant ids: d215b5f8-0249-4dc5-89a3-51fd148cfb45, d215b5f8-0249-4dc5-89a3-51fd148cfb46

-- product ids: d215b5f8-0249-4dc5-89a3-51fd148cfb47, d215b5f8-0249-4dc5-89a3-51fd148cfb48,
--  d215b5f8-0249-4dc5-89a3-51fd148cfb49, d215b5f8-0249-4dc5-89a3-51fd148cfb50

-- restaurant_product ids: d215b5f8-0249-4dc5-89a3-51fd148cfb51, d215b5f8-0249-4dc5-89a3-51fd148cfb52,
--  d215b5f8-0249-4dc5-89a3-51fd148cfb53, d215b5f8-0249-4dc5-89a3-51fd148cfb54

INSERT INTO restaurant.order_restaurant_m_view
(restaurant_id, restaurant_name, restaurant_active, product_id, product_name, product_price, product_available)
VALUES
('d215b5f8-0249-4dc5-89a3-51fd148cfb45', 'restaurant1', true, 'd215b5f8-0249-4dc5-89a3-51fd148cfb47', 'product1', 1900, true);

INSERT INTO restaurant.order_restaurant_m_view
(restaurant_id, restaurant_name, restaurant_active, product_id, product_name, product_price, product_available)
VALUES
('d215b5f8-0249-4dc5-89a3-51fd148cfb45', 'restaurant1', true, 'd215b5f8-0249-4dc5-89a3-51fd148cfb48', 'product2', 2300, true);

INSERT INTO restaurant.order_restaurant_m_view
(restaurant_id, restaurant_name, restaurant_active, product_id, product_name, product_price, product_available)
VALUES
('d215b5f8-0249-4dc5-89a3-51fd148cfb46', 'restaurant2', true, 'd215b5f8-0249-4dc5-89a3-51fd148cfb49', 'product3', 3200, true);

INSERT INTO  restaurant.order_restaurant_m_view
(restaurant_id, restaurant_name, restaurant_active, product_id, product_name, product_price, product_available)
VALUES
('d215b5f8-0249-4dc5-89a3-51fd148cfb46', 'restaurant2', true, 'd215b5f8-0249-4dc5-89a3-51fd148cfb50', 'product4', 4100, true);
